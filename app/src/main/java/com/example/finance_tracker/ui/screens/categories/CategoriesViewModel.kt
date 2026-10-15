package com.example.finance_tracker.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.domain.model.Category
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CategoriesFilter(val label: String) {
    ALL("Все"),
    EXPENSES("Расходы"),
    INCOMES("Доходы")
}

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val filter: CategoriesFilter = CategoriesFilter.ALL,
    val isLoading: Boolean = true
)

sealed interface CategoriesEvent {
    data class ShowError(val message: String) : CategoriesEvent
}

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val filter = MutableStateFlow(CategoriesFilter.ALL)

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.getAllCategories(),
        filter
    ) { categories, selectedFilter ->
        val visible = when (selectedFilter) {
            CategoriesFilter.ALL -> categories
            CategoriesFilter.EXPENSES -> categories.filter { it.type == TransactionType.EXPENSE }
            CategoriesFilter.INCOMES -> categories.filter { it.type == TransactionType.INCOME }
        }
        CategoriesUiState(
            categories = visible,
            filter = selectedFilter,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState()
    )

    private val _events = Channel<CategoriesEvent>(Channel.BUFFERED)
    val events: Flow<CategoriesEvent> = _events.receiveAsFlow()

    fun setFilter(newFilter: CategoriesFilter) {
        filter.update { newFilter }
    }

    /**
     * Создание (existing == null) или обновление категории.
     * id у новой категории 0 — Room сгенерирует свой.
     */
    fun saveCategory(
        existing: Category?,
        name: String,
        iconName: String,
        color: Long,
        type: TransactionType
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            sendEvent(CategoriesEvent.ShowError("Введите название категории"))
            return
        }
        viewModelScope.launch {
            try {
                val category = Category(
                    id = existing?.id ?: 0L,
                    name = trimmed,
                    icon = iconName,
                    color = color,
                    type = type,
                    isDefault = existing?.isDefault ?: false,
                    sortOrder = existing?.sortOrder ?: 0
                )
                if (existing == null) {
                    categoryRepository.insertCategory(category)
                } else {
                    categoryRepository.updateCategory(category)
                }
            } catch (e: Exception) {
                sendEvent(CategoriesEvent.ShowError(e.message ?: "Не удалось сохранить категорию"))
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(category)
            } catch (e: Exception) {
                sendEvent(CategoriesEvent.ShowError(e.message ?: "Не удалось удалить категорию"))
            }
        }
    }

    /** Число транзакций категории — для предупреждения перед удалением. */
    suspend fun countTransactions(categoryId: Long): Int =
        categoryRepository.countTransactionsInCategory(categoryId)

    private fun sendEvent(event: CategoriesEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
