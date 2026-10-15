package com.example.finance_tracker.ui.screens.addtransaction

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.ReceiptScan
import com.example.finance_tracker.core.ReceiptScannerHelper
import com.example.finance_tracker.domain.model.Category
import com.example.finance_tracker.domain.model.Transaction
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.domain.repository.CategoryRepository
import com.example.finance_tracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
import java.time.Instant
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
    val isSaving: Boolean = false,
    /** Для редактирования: createdAt исходной записи сохраняем при апдейте. */
    val createdAt: Long = System.currentTimeMillis()
) {
    val isSaveEnabled: Boolean
        get() = !isSaving &&
            (amountInput.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0 &&
            selectedCategory != null
}

sealed interface AddTransactionEvent {
    data object Saved : AddTransactionEvent
    data object Deleted : AddTransactionEvent
    data class ShowError(val message: String) : AddTransactionEvent
}

/**
 * Экран добавления И редактирования операции: если в навигации передан
 * transactionId — загружаем существующую запись, save() делает update,
 * доступно удаление.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val receiptScanner: ReceiptScannerHelper
) : ViewModel() {

    private val editingId: Long = savedStateHandle.get<Long>("transactionId") ?: -1L
    val isEditing: Boolean = editingId > 0

    /** Предзаполнение из банковского уведомления (share-intent). */
    private val prefillAmount: Double? =
        savedStateHandle.get<String>("prefillAmount")?.toDoubleOrNull()
    private val prefillDate: Long? =
        savedStateHandle.get<Long>("prefillDate")?.takeIf { it > 0 }
    private val prefillKeyword: String? = savedStateHandle.get<String>("prefillKeyword")

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
                    _uiState.update { state ->
                        // Подсказка категории из банковского уведомления: выбираем
                        // первую подходящую, пользователь может поменять.
                        val hint = if (!isEditing && state.selectedCategory == null) {
                            prefillKeyword?.lowercase()?.let { keyword ->
                                categories.firstOrNull {
                                    it.name.lowercase().contains(keyword)
                                }
                            }
                        } else {
                            null
                        }
                        state.copy(categories = categories, selectedCategory = hint ?: state.selectedCategory)
                    }
                }
        }
        if (isEditing) {
            viewModelScope.launch { loadOriginal() }
        } else if (prefillAmount != null || prefillDate != null) {
            _uiState.update { state ->
                state.copy(
                    amountInput = prefillAmount?.let { formatAmount(it) } ?: state.amountInput,
                    selectedDate = prefillDate?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    } ?: state.selectedDate
                )
            }
        }
    }

    private suspend fun loadOriginal() {
        val original = transactionRepository.getTransactionById(editingId)
        if (original == null) {
            sendEvent(AddTransactionEvent.ShowError("Операция не найдена"))
            return
        }
        _uiState.update {
            it.copy(
                type = original.type,
                amountInput = formatAmount(original.amount),
                // Категория могла быть удалена (categoryId = 0) — пользователь выберет заново.
                selectedCategory = categoryRepository.getCategoryById(original.categoryId),
                selectedDate = Instant.ofEpochMilli(original.date)
                    .atZone(ZoneId.systemDefault()).toLocalDate(),
                description = original.description.orEmpty(),
                createdAt = original.createdAt
            )
        }
    }

    fun setType(type: TransactionType) {
        if (type == _uiState.value.type) return
        // Категории расхода и дохода разные — сбрасываем выбор.
        _uiState.update { it.copy(type = type, selectedCategory = null) }
    }

    fun onAmountChange(input: String) {
        _uiState.update { it.copy(amountInput = sanitizeAmount(input)) }
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
                val transaction = Transaction(
                    id = if (isEditing) editingId else 0L,
                    type = state.type,
                    amount = amount,
                    categoryId = category.id,
                    date = dateMillis,
                    description = state.description.trim().takeIf { it.isNotEmpty() },
                    createdAt = state.createdAt
                )
                try {
                    if (isEditing) {
                        transactionRepository.updateTransaction(transaction)
                    } else {
                        transactionRepository.addTransaction(transaction)
                    }
                    _events.send(AddTransactionEvent.Saved)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.update { it.copy(isSaving = false) }
                    sendEvent(AddTransactionEvent.ShowError(e.message ?: "Не удалось сохранить"))
                }
            }
        }
    }

    fun delete() {
        if (!isEditing) return
        viewModelScope.launch {
            val original = transactionRepository.getTransactionById(editingId)
            if (original != null) {
                transactionRepository.deleteTransaction(original)
            }
            // Если записи уже нет (удалена из другого места) — просто закрываем экран.
            _events.send(AddTransactionEvent.Deleted)
        }
    }

    private fun sendEvent(event: AddTransactionEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    /** Распознаёт чек на картинке (ML Kit, офлайн). */
    suspend fun scanReceipt(imageUri: Uri): ReceiptScan =
        receiptScanner.scan(imageUri)

    /** Подставляет найденные на чеке сумму и дату в форму. */
    fun applyReceiptScan(scan: ReceiptScan) {
        _uiState.update { state ->
            state.copy(
                amountInput = scan.amount?.let { formatAmount(it) } ?: state.amountInput,
                selectedDate = scan.date ?: state.selectedDate
            )
        }
    }

    private companion object {
        const val MAX_DESCRIPTION_LENGTH = 200

        /** Только цифры и один разделитель, до 10 знаков. */
        fun sanitizeAmount(input: String): String {
            var separatorSeen = false
            return input
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
        }

        /** 150.0 → "150", 150.5 → "150.5" — для предзаполнения при редактировании. */
        fun formatAmount(amount: Double): String =
            if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString()

        const val MAX_AMOUNT_LENGTH = 10
    }
}
