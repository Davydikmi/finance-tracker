package com.example.finance_tracker.ui.screens.converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.CurrencyConfig
import com.example.finance_tracker.domain.repository.ExchangeRateRepository
import com.example.finance_tracker.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConverterUiState(
    val amountInput: String = "1",
    val fromCurrency: String = CurrencyConfig.AppDefaultCurrency,
    val toCurrency: String = "USD",
    val result: Double? = null,
    val lastUpdateTime: Long? = null
)

/**
 * Конвертер валют через BYN: сумма × курс from (BYN за единицу) ÷ курс to.
 * Курсы берутся из локального кэша НБ РБ.
 */
@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val base = settingsRepository.currentBaseCurrency()
            _uiState.update { it.copy(fromCurrency = base) }
            exchangeRateRepository.getLastUpdateTime()?.let { last ->
                _uiState.update { it.copy(lastUpdateTime = last) }
            }
            recompute()
        }
    }

    fun onAmountChange(input: String) {
        _uiState.update { it.copy(amountInput = input.take(12)) }
        recompute()
    }

    fun setFromCurrency(code: String) {
        if (code == _uiState.value.toCurrency) swap()
        else {
            _uiState.update { it.copy(fromCurrency = code) }
            recompute()
        }
    }

    fun setToCurrency(code: String) {
        if (code == _uiState.value.fromCurrency) swap()
        else {
            _uiState.update { it.copy(toCurrency = code) }
            recompute()
        }
    }

    fun swap() {
        _uiState.update {
            it.copy(fromCurrency = it.toCurrency, toCurrency = it.fromCurrency)
        }
        recompute()
    }

    private fun recompute() {
        val state = _uiState.value
        val amount = state.amountInput.replace(',', '.').toDoubleOrNull() ?: run {
            _uiState.update { it.copy(result = null) }
            return
        }
        viewModelScope.launch {
            val rateFrom = exchangeRateRepository.getRatePerUnit(state.fromCurrency) ?: return@launch
            val rateTo = exchangeRateRepository.getRatePerUnit(state.toCurrency) ?: return@launch
            // amount в единицах from → BYN → единицы to.
            val result = amount * rateFrom / rateTo
            _uiState.update { it.copy(result = result) }
        }
    }
}
