package com.dpouya.messages.data.repository

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.dpouya.messages.data.local.ConversationEntity
import com.dpouya.messages.data.local.MessageEntity
import com.dpouya.messages.data.local.MessagesDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SmsRepository(
    private val context: Context,
    private val db: MessagesDatabase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val conversations = db.conversationDao()
    private val messages = db.messageDao()

    fun observeConversations(): Flow<List<ConversationEntity>> = conversations.observeAll()
    fun searchConversations(q: String): Flow<List<ConversationEntity>> =
        if (q.isBlank()) conversations.observeAll() else conversations.search(q)
    fun observeConversation(id: Long) = conversations.observeById(id)
    fun observeMessages(conversationId: Long) = messages.observeForConversation(conversationId)

    suspend fun syncFromProvider() = withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.READ,
            ),
            null,
            null,
            "${Telephony.Sms.DATE} DESC",
        ) ?: return@withContext

        val contactRepo = ContactRepository(context)
        val latestByAddress = linkedMapOf<String, SmsPreview>()
        cursor.use {
            while (it.moveToNext()) {
                val address = it.getString(0) ?: continue
                val key = normalize(address)
                if (key in latestByAddress) continue
                latestByAddress[key] = SmsPreview(
                    body = it.getString(1) ?: "",
                    date = it.getLong(2),
                    type = it.getInt(3),
                    read = it.getInt(4) == 1,
                    rawAddress = address,
                )
            }
        }

        for ((key, preview) in latestByAddress) {
            val name = contactRepo.resolveDisplayName(preview.rawAddress)
            val conv = conversations.getByAddress(key)
            if (conv == null) {
                val id = conversations.insert(
                    ConversationEntity(
                        address = key,
                        displayName = name,
                        snippet = preview.body,
                        timestamp = preview.date,
                        unreadCount = if (!preview.read && preview.type == Telephony.Sms.MESSAGE_TYPE_INBOX) 1 else 0,
                    ),
                )
                importThread(key, id)
            } else if (preview.date > conv.timestamp) {
                conversations.update(
                    conv.copy(
                        displayName = name,
                        snippet = preview.body,
                        timestamp = preview.date,
                    ),
                )
                importThread(key, conv.id)
            }
        }
    }

    private suspend fun importThread(addressKey: String, conversationId: Long) {
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
            ),
            null,
            null,
            "${Telephony.Sms.DATE} ASC",
        ) ?: return
        cursor.use {
            while (it.moveToNext()) {
                val address = it.getString(0) ?: continue
                if (normalize(address) != addressKey) continue
                val body = it.getString(1) ?: ""
                val date = it.getLong(2)
                val type = it.getInt(3)
                messages.insert(
                    MessageEntity(
                        conversationId = conversationId,
                        address = addressKey,
                        body = body,
                        timestamp = date,
                        isOutgoing = type != Telephony.Sms.MESSAGE_TYPE_INBOX,
                        status = MessageEntity.Status.DELIVERED,
                    ),
                )
            }
        }
    }

    private data class SmsPreview(
        val body: String,
        val date: Long,
        val type: Int,
        val read: Boolean,
        val rawAddress: String,
    )

    suspend fun getOrCreateConversation(address: String, displayName: String? = null): ConversationEntity {
        val key = normalize(address)
        conversations.getByAddress(key)?.let { return it }
        val name = displayName ?: ContactRepository(context).resolveDisplayName(address)
        val id = conversations.insert(
            ConversationEntity(address = key, displayName = name, timestamp = System.currentTimeMillis()),
        )
        return conversations.getById(id)!!
    }

    suspend fun markRead(conversationId: Long) {
        conversations.clearUnread(conversationId)
    }

    suspend fun sendMessage(conversationId: Long, body: String): Result<Unit> = withContext(Dispatchers.IO) {
        val conv = conversations.getById(conversationId) ?: return@withContext Result.failure(IllegalArgumentException("Missing conversation"))
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return@withContext Result.failure(IllegalArgumentException("Empty"))

        val msgId = messages.insert(
            MessageEntity(
                conversationId = conversationId,
                address = conv.address,
                body = trimmed,
                timestamp = System.currentTimeMillis(),
                isOutgoing = true,
                status = MessageEntity.Status.SENT,
            ),
        )
        conversations.update(
            conv.copy(snippet = trimmed, timestamp = System.currentTimeMillis()),
        )

        try {
            val sms = context.getSystemService(SmsManager::class.java)
            val sentAction = "com.dpouya.messages.SMS_SENT_$msgId"
            val sentPi = PendingIntent.getBroadcast(
                context,
                msgId.toInt(),
                Intent(sentAction),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    scope.launch {
                        val status = if (resultCode == android.app.Activity.RESULT_OK) {
                            MessageEntity.Status.DELIVERED
                        } else {
                            MessageEntity.Status.FAILED
                        }
                        messages.updateStatus(msgId, status)
                    }
                    try {
                        context.unregisterReceiver(this)
                    } catch (_: Exception) {
                    }
                }
            }
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(sentAction),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            val parts = sms.divideMessage(trimmed)
            if (parts.size == 1) {
                sms.sendTextMessage(conv.address, null, trimmed, sentPi, null)
            } else {
                val sentIntents = ArrayList<PendingIntent>(parts.size)
                repeat(parts.size) { sentIntents.add(sentPi) }
                sms.sendMultipartTextMessage(conv.address, null, parts, sentIntents, null)
            }
            Result.success(Unit)
        } catch (t: Throwable) {
            messages.updateStatus(msgId, MessageEntity.Status.FAILED)
            Result.failure(t)
        }
    }

    fun onIncomingSms(address: String, body: String, timestamp: Long) {
        scope.launch {
            val key = normalize(address)
            val name = ContactRepository(context).resolveDisplayName(address)
            var conv = conversations.getByAddress(key)
            if (conv == null) {
                val id = conversations.insert(
                    ConversationEntity(
                        address = key,
                        displayName = name,
                        snippet = body,
                        timestamp = timestamp,
                        unreadCount = 1,
                    ),
                )
                conv = conversations.getById(id)
            } else {
                conversations.update(
                    conv.copy(
                        snippet = body,
                        timestamp = timestamp,
                        unreadCount = conv.unreadCount + 1,
                        displayName = name,
                    ),
                )
                conv = conversations.getByAddress(key)
            }
            val c = conv ?: return@launch
            messages.insert(
                MessageEntity(
                    conversationId = c.id,
                    address = key,
                    body = body,
                    timestamp = timestamp,
                    isOutgoing = false,
                    status = MessageEntity.Status.DELIVERED,
                ),
            )
        }
    }

    private fun normalize(address: String): String =
        address.filter { it.isDigit() || it == '+' }.ifBlank { address.trim() }
}
