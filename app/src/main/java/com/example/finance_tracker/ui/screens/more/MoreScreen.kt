package com.example.finance_tracker.ui.screens.more

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finance_tracker.core.CurrencyConfig
import com.example.finance_tracker.domain.model.AppSettings
import com.example.finance_tracker.ui.components.Formatters
import com.example.finance_tracker.ui.theme.CardBg
import com.example.finance_tracker.ui.theme.CardSolid
import com.example.finance_tracker.ui.theme.DividerColor
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary
import com.example.finance_tracker.ui.theme.ScreenBg
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * «Ещё»: категории, экспорт PDF, тема, валюта и язык приложения.
 */
@Composable
fun MoreScreen(
    onOpenCategories: () -> Unit,
    viewModel: MoreViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val monthBudget by viewModel.monthBudget.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MoreEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Ещё",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
            Spacer(Modifier.height(16.dp))

            SettingsCard(title = "Управление") {
                MenuRow(
                    icon = Icons.Outlined.Category,
                    title = "Категории",
                    subtitle = "Расходы и доходы, создание и редактирование",
                    onClick = onOpenCategories
                )
                HorizontalDivider(color = DividerColor)
                MenuRow(
                    icon = Icons.Outlined.Savings,
                    title = "Бюджет месяца",
                    subtitle = monthBudget?.let {
                        "Задан: ${Formatters.money(it.totalLimit)}"
                    } ?: "Не задан — установите лимит трат",
                    onClick = { showBudgetDialog = true }
                )
            }

            Spacer(Modifier.height(16.dp))
            SettingsCard(title = "Данные") {
                MenuRow(
                    icon = Icons.Outlined.PictureAsPdf,
                    title = "Экспорт в PDF",
                    subtitle = "Отчёт по операциям за текущий месяц",
                    onClick = {
                        if (!isExporting) {
                            isExporting = true
                            scope.launch {
                                try {
                                    val file = viewModel.exportCurrentMonthPdf()
                                    sharePdf(context, file)
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Не удалось создать PDF")
                                } finally {
                                    isExporting = false
                                }
                            }
                        }
                    },
                    trailing = {
                        if (isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            SettingsCard(title = "Тема") {
                ThemeRow("Светлая", settings?.themeMode == AppSettings.THEME_LIGHT) {
                    viewModel.setThemeMode(AppSettings.THEME_LIGHT)
                }
                HorizontalDivider(color = DividerColor)
                ThemeRow("Тёмная", settings?.themeMode == AppSettings.THEME_DARK) {
                    viewModel.setThemeMode(AppSettings.THEME_DARK)
                }
                HorizontalDivider(color = DividerColor)
                ThemeRow("Системная", settings?.themeMode == AppSettings.THEME_SYSTEM) {
                    viewModel.setThemeMode(AppSettings.THEME_SYSTEM)
                }
            }

            Spacer(Modifier.height(16.dp))
            SettingsCard(title = "Приложение") {
                MenuRow(
                    icon = Icons.Outlined.Payments,
                    title = "Валюта",
                    subtitle = settings?.let { "${it.baseCurrency} · ${CurrencyConfig.symbolFor(it.baseCurrency)}" },
                    onClick = { showCurrencyDialog = true }
                )
                HorizontalDivider(color = DividerColor)
                MenuRow(
                    icon = Icons.Outlined.Language,
                    title = "Язык",
                    subtitle = when (settings?.language) {
                        AppSettings.LANG_EN -> "English"
                        else -> "Русский"
                    },
                    onClick = { showLanguageDialog = true }
                )
                HorizontalDivider(color = DividerColor)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Уведомления",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkPrimary
                        )
                        Text(
                            text = "Предупреждения при 80% и 100% бюджета",
                            fontSize = 12.sp,
                            color = InkSecondary
                        )
                    }
                    Switch(
                        checked = settings?.notificationsEnabled ?: true,
                        onCheckedChange = viewModel::setNotificationsEnabled,
                        colors = SwitchDefaults.colors(checkedTrackColor = InkPrimary)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SettingsCard(title = "О приложении") {
                Text(
                    text = "Финансовый трекер — локальный учёт доходов и расходов " +
                        "с копилками, бюджетами и курсами валют НБ РБ. " +
                        "Все данные хранятся только на вашем устройстве.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = InkSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Финансовый трекер · версия 1.0",
                fontSize = 12.sp,
                color = InkSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showBudgetDialog) {
        BudgetDialog(
            currentAmount = monthBudget?.totalLimit,
            onDismiss = { showBudgetDialog = false },
            onSave = { input ->
                viewModel.setMonthBudget(input)
                showBudgetDialog = false
            }
        )
    }

    if (showCurrencyDialog) {
        SelectionDialog(
            title = "Валюта приложения",
            options = CurrencyConfig.supported,
            selected = settings?.baseCurrency,
            label = { code -> "$code · ${CurrencyConfig.displayName(code)}" },
            onSelect = {
                viewModel.setBaseCurrency(it)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showLanguageDialog) {
        SelectionDialog(
            title = "Язык интерфейса",
            options = listOf(AppSettings.LANG_RU, AppSettings.LANG_EN),
            selected = settings?.language,
            label = { code -> if (code == AppSettings.LANG_EN) "English" else "Русский" },
            onSelect = {
                viewModel.setLanguage(it)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = InkSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = CardBg
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = InkPrimary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = InkPrimary)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = InkSecondary)
            }
        }
        trailing()
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = InkSecondary
        )
    }
}

@Composable
private fun ThemeRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = InkPrimary,
            modifier = Modifier.weight(1f)
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = InkPrimary)
        )
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selected: String?,
    label: (String) -> String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSolid,
        title = { Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label(option),
                            fontSize = 15.sp,
                            color = InkPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = InkPrimary)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Отмена") }
        }
    )
}

@Composable
private fun BudgetDialog(
    currentAmount: Double?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var input by remember {
        mutableStateOf(
            currentAmount?.let {
                DecimalFormat("#,##0.##", DecimalFormatSymbols(java.util.Locale("ru"))).format(it)
            }.orEmpty()
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSolid,
        title = {
            Text(
                text = "Бюджет на ${Formatters.currentMonthTitle()}",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(12) },
                    singleLine = true,
                    label = { Text("Лимит трат, ${CurrencyConfig.symbol}") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = InkPrimary,
                        unfocusedTextColor = InkPrimary,
                        cursorColor = InkPrimary,
                        focusedBorderColor = InkPrimary,
                        unfocusedBorderColor = CardSolid
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "При 80% и 100% придёт уведомление",
                    fontSize = 12.sp,
                    color = InkSecondary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(input) }) {
                Text(text = "Сохранить", color = InkPrimary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Отмена", color = InkSecondary) }
        }
    )
}

private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Отправить отчёт"))
}
