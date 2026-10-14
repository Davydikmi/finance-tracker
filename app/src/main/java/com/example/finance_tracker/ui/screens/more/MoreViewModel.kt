package com.example.finance_tracker.ui.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.export.TransactionPdfExporter
import com.example.finance_tracker.domain.model.AppSettings
import com.example.finance_tracker.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val pdfExporter: TransactionPdfExporter
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = settingsRepository.observeSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _events = Channel<MoreEvent>(Channel.BUFFERED)
    val events: Flow<MoreEvent> = _events.receiveAsFlow()

    fun setThemeMode(mode: String) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setBaseCurrency(code: String) {
        viewModelScope.launch { settingsRepository.setBaseCurrency(code) }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch { settingsRepository.setLanguage(code) }
    }

    /** Отчёт по операциям текущего месяца; файл во временном каталоге. */
    suspend fun exportCurrentMonthPdf(): File =
        pdfExporter.exportMonth(YearMonth.now())
}

sealed interface MoreEvent {
    data class ShowError(val message: String) : MoreEvent
}
