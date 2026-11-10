package com.dpouya.messages.features.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dpouya.messages.data.local.ConversationEntity
import com.dpouya.messages.data.repository.SmsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModel(
    private val smsRepository: SmsRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val conversations: StateFlow<List<ConversationEntity>> = query
        .flatMapLatest { q -> smsRepository.searchConversations(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val searchQuery: StateFlow<String> = query

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun sync() {
        viewModelScope.launch { smsRepository.syncFromProvider() }
    }

    class Factory(private val repo: SmsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = InboxViewModel(repo) as T
    }
}
