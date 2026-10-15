package com.example.finance_tracker.ui.screens.converter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finance_tracker.core.CurrencyConfig
import com.example.finance_tracker.ui.components.Formatters
import com.example.finance_tracker.ui.theme.CardBg
import com.example.finance_tracker.ui.theme.CardSolid
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary
import com.example.finance_tracker.ui.theme.ScreenBg
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Конвертер валют: два поля (сумма + валюта), кнопка обмена, пересчёт
 * в реальном времени по кэшированным курсам НБ РБ.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    onBack: () -> Unit,
    viewModel: ConverterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Конвертер",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = InkPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBg)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            CurrencyBlock(
                label = "Меняю",
                amount = state.amountInput,
                currency = state.fromCurrency,
                onAmountChange = viewModel::onAmountChange,
                onCurrencySelected = viewModel::setFromCurrency
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                IconButton(onClick = viewModel::swap) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "Поменять валюты",
                        tint = InkPrimary
                    )
                }
            }

            CurrencyBlock(
                label = "Получаю",
                amount = state.result?.let { Formatters.money(it) }.orEmpty(),
                currency = state.toCurrency,
                onAmountChange = {},
                onCurrencySelected = viewModel::setToCurrency,
                readOnly = true
            )

            state.lastUpdateTime?.let { updatedAt ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Курс на " + Instant.ofEpochMilli(updatedAt)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                        " (НБ РБ)",
                    fontSize = 12.sp,
                    color = InkSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyBlock(
    label: String,
    amount: String,
    currency: String,
    onAmountChange: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    readOnly: Boolean = false
) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, fontSize = 12.sp, color = InkSecondary)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    readOnly = readOnly,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = InkPrimary,
                        unfocusedTextColor = InkPrimary,
                        cursorColor = InkPrimary,
                        focusedBorderColor = InkPrimary,
                        unfocusedBorderColor = CardSolid
                    )
                )
                Spacer(Modifier.width(10.dp))
                Surface(
                    onClick = { menuOpen = true },
                    shape = RoundedCornerShape(14.dp),
                    color = CardSolid
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currency,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkPrimary
                        )
                        Icon(
                            imageVector = Icons.Outlined.ArrowDropDown,
                            contentDescription = "Выбрать валюту",
                            tint = InkPrimary
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        CurrencyConfig.supported.forEach { code ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${CurrencyConfig.symbolFor(code)} $code",
                                        color = if (code == currency) InkPrimary else InkPrimary
                                    )
                                },
                                onClick = {
                                    onCurrencySelected(code)
                                    menuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
