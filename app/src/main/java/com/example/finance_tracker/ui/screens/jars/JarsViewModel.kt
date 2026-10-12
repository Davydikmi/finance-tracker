package com.example.finance_tracker.ui.screens.jars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.domain.model.SavingsJar
import com.example.finance_tracker.domain.repository.SavingsJarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JarsUiState(
    val jars: List<SavingsJar> = emptyList(),
    val isLoading: Boolean = true
) {
    val totalSaved: Double
        get() = jars.sumOf { it.savedAmount }
}

sealed interface JarsEvent {
    data class ShowError(val message: String) : JarsEvent
}

@HiltViewModel
class JarsViewModel @Inject constructor(
    private val savingsJarRepository: SavingsJarRepository
) : ViewModel() {

    val uiState: StateFlow<JarsUiState> =
        savingsJarRepository.getActiveJars()
            .map { jars -> JarsUiState(jars = jars, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = JarsUiState()
            )

    private val _events = Channel<JarsEvent>(Channel.BUFFERED)
    val events: Flow<JarsEvent> = _events.receiveAsFlow()

    fun createJar(name: String, targetAmount: Double?, color: Long) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            sendEvent(JarsEvent.ShowError("Введите название копилки"))
            return
        }
        viewModelScope.launch {
            try {
                savingsJarRepository.createJar(
                    SavingsJar(
                        name = trimmed,
                        targetAmount = targetAmount,
                        icon = JAR_DEFAULT_ICON,
                        color = color
                    )
                )
            } catch (e: Exception) {
                sendEvent(JarsEvent.ShowError(e.message ?: "Не удалось создать копилку"))
            }
        }
    }

    fun deposit(jarId: Long, amountInput: String) {
        transact(jarId, amountInput, isDeposit = true)
    }

    fun withdraw(jarId: Long, amountInput: String) {
        transact(jarId, amountInput, isDeposit = false)
    }

    private fun transact(jarId: Long, amountInput: String, isDeposit: Boolean) {
        val amount = amountInput.replace(',', '.').toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            sendEvent(JarsEvent.ShowError("Введите сумму больше нуля"))
            return
        }
        viewModelScope.launch {
            try {
                if (isDeposit) {
                    savingsJarRepository.addFunds(jarId, amount)
                } else {
                    savingsJarRepository.withdrawFunds(jarId, amount)
                }
            } catch (e: IllegalArgumentException) {
                // Ошибки бизнес-правил от репозитория (сумма, баланс, копилка не найдена).
                sendEvent(JarsEvent.ShowError(e.message ?: "Операция невозможна"))
            } catch (e: Exception) {
                sendEvent(JarsEvent.ShowError(e.message ?: "Операция не выполнена"))
            }
        }
    }

    private fun sendEvent(event: JarsEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private companion object {
        const val JAR_DEFAULT_ICON = "savings"
    }
}
