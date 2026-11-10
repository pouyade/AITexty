package com.dpouya.messages.features.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dpouya.messages.data.local.ConversationEntity
import com.dpouya.messages.data.local.MessageEntity
import com.dpouya.messages.data.repository.SmsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val conversationId: Long,
    private val smsRepository: SmsRepository,
) : ViewModel() {
    val conversation: StateFlow<ConversationEntity?> =
        smsRepository.observeConversation(conversationId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val messages: StateFlow<List<MessageEntity>> =
        smsRepository.observeMessages(conversationId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft

    init {
        viewModelScope.launch { smsRepository.markRead(conversationId) }
    }

    fun onDraftChange(value: String) {
        _draft.value = value
    }

    fun send() {
        val text = _draft.value
        viewModelScope.launch {
            smsRepository.sendMessage(conversationId, text).onSuccess {
                _draft.value = ""
            }
        }
    }

    class Factory(
        private val id: Long,
        private val repo: SmsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ConversationViewModel(id, repo) as T
    }
}
