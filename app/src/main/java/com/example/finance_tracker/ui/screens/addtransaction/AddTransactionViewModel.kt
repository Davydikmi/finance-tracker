package com.example.finance_tracker.ui.screens.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.domain.model.Category
import com.example.finance_tracker.domain.model.Transaction
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.domain.repository.CategoryRepository
import com.example.finance_tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

data class AddTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountInput: String = "",
    val selectedCategory: Category? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val description: String = "",
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false
) {
    val isSaveEnabled: Boolean
        get() = !isSaving &&
            (amountInput.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0 &&
            selectedCategory != null
}

sealed interface AddTransactionEvent {
    data object Saved : AddTransactionEvent
    data class ShowError(val message: String) : AddTransactionEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _events = Channel<AddTransactionEvent>(Channel.BUFFERED)
    val events: Flow<AddTransactionEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            _uiState.map { it.type }
                .distinctUntilChanged()
                .flatMapLatest { type -> categoryRepository.getCategoriesByType(type) }
                .collect { categories ->
                    _uiState.update { it.copy(categories = categories) }
                }
        }
    }

    fun setType(type: TransactionType) {
        if (type == _uiState.value.type) return
        // Категории расхода и дохода разные — сбрасываем выбор.
        _uiState.update { it.copy(type = type, selectedCategory = null) }
    }

    fun onAmountChange(input: String) {
        var separatorSeen = false
        val cleaned = input
            .filter { it.isDigit() || it == ',' || it == '.' }
            .filter { c ->
                if (c == ',' || c == '.') {
                    if (separatorSeen) false else {
                        separatorSeen = true
                        true
                    }
                } else {
                    true
                }
            }
            .take(MAX_AMOUNT_LENGTH)
        _uiState.update { it.copy(amountInput = cleaned) }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onDescriptionChange(text: String) {
        _uiState.update { it.copy(description = text.take(MAX_DESCRIPTION_LENGTH)) }
    }

    fun save() {
        val state = _uiState.value
        val category = state.selectedCategory
        val amount = state.amountInput.replace(',', '.').toDoubleOrNull()

        when {
            amount == null || amount <= 0.0 ->
                sendEvent(AddTransactionEvent.ShowError("Введите сумму больше нуля"))

            category == null ->
                sendEvent(AddTransactionEvent.ShowError("Выберите категорию"))

            else -> viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }
                val dateMillis = state.selectedDate.atTime(LocalTime.now())
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                transactionRepository.addTransaction(
                    Transaction(
                        type = state.type,
                        amount = amount,
                        categoryId = category.id,
                        date = dateMillis,
                        description = state.description.trim().takeIf { it.isNotEmpty() }
                    )
                )
                _events.send(AddTransactionEvent.Saved)
            }
        }
    }

    private fun sendEvent(event: AddTransactionEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private companion object {
        const val MAX_AMOUNT_LENGTH = 10
        const val MAX_DESCRIPTION_LENGTH = 200
    }
}
