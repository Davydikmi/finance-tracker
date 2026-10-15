package com.example.finance_tracker.ui.screens.addtransaction

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finance_tracker.core.CurrencyConfig
import com.example.finance_tracker.core.ReceiptScan
import com.example.finance_tracker.domain.model.Category
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.ui.components.Formatters
import com.example.finance_tracker.ui.theme.AccentBlue
import com.example.finance_tracker.ui.theme.CardBg
import com.example.finance_tracker.ui.theme.CardSolid
import com.example.finance_tracker.ui.theme.DividerColor
import com.example.finance_tracker.ui.theme.ExpenseRed
import com.example.finance_tracker.ui.theme.FabDark
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary
import com.example.finance_tracker.ui.theme.OnAccent
import com.example.finance_tracker.ui.theme.ScreenBg
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Экран добавления и редактирования расхода/дохода: переключатель типа,
 * крупная сумма, категории-чипы из БД, дата, комментарий.
 * При переходе с transactionId в стеке — режим редактирования с удалением.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var receiptScan by remember { mutableStateOf<ReceiptScan?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun scanImage(uri: Uri) {
        isScanning = true
        scope.launch {
            try {
                receiptScan = viewModel.scanReceipt(uri)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Не удалось распознать чек")
            } finally {
                isScanning = false
            }
        }
    }

    // Фото чека: во временный файл кэша, отдаём через FileProvider.
    val cameraUri = remember {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File.createTempFile("receipt_", ".jpg", dir)
        )
    }
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) scanImage(cameraUri) }
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { scanImage(it) } }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddTransactionEvent.Saved -> onSaved()
                AddTransactionEvent.Deleted -> onDeleted()
                is AddTransactionEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.isEditing) "Редактирование" else "Новая операция",
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // Сначала сжимаем вьюпорт под клавиатуру, потом скролл —
                // иначе фокусное поле остаётся под клавиатурой.
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            TypeToggle(type = state.type, onSelect = viewModel::setType)

            if (!viewModel.isEditing) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScanButton(
                        label = "Сканировать чек",
                        icon = Icons.Outlined.DocumentScanner,
                        loading = isScanning,
                        enabled = !isScanning,
                        modifier = Modifier.weight(1f),
                        onClick = { takePictureLauncher.launch(cameraUri) }
                    )
                    ScanButton(
                        label = "Галерея",
                        icon = Icons.Outlined.PhotoLibrary,
                        loading = false,
                        enabled = !isScanning,
                        modifier = Modifier.weight(1f),
                        onClick = { pickImageLauncher.launch("image/*") }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            AmountField(value = state.amountInput, onValueChange = viewModel::onAmountChange)

            Spacer(Modifier.height(26.dp))
            SectionLabel("Категория")
            Spacer(Modifier.height(10.dp))
            CategoryChips(
                categories = state.categories,
                selected = state.selectedCategory,
                onSelect = viewModel::selectCategory
            )

            Spacer(Modifier.height(26.dp))
            SectionLabel("Дата")
            Spacer(Modifier.height(10.dp))
            DateSelection(
                selectedDate = state.selectedDate,
                today = today,
                onDateSelected = viewModel::setDate,
                onPickDate = { showDatePicker = true }
            )

            Spacer(Modifier.height(26.dp))
            SectionLabel("Комментарий")
            Spacer(Modifier.height(10.dp))
            DescriptionField(value = state.description, onValueChange = viewModel::onDescriptionChange)

            Spacer(Modifier.height(30.dp))
            Button(
                onClick = viewModel::save,
                enabled = state.isSaveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = InkPrimary,
                    contentColor = OnAccent,
                    disabledContainerColor = CardBg,
                    disabledContentColor = InkSecondary
                )
            ) {
                Text(
                    text = if (state.isSaving) "Сохранение…" else "Сохранить",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (viewModel.isEditing) {
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Удалить операцию",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    receiptScan?.let { scan ->
        ReceiptSheet(
            scan = scan,
            onDismiss = { receiptScan = null },
            onApply = {
                viewModel.applyReceiptScan(scan)
                receiptScan = null
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialogBox(
            selectedDate = state.selectedDate,
            onDismiss = { showDatePicker = false },
            onDatePicked = { date ->
                viewModel.setDate(date)
                showDatePicker = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = CardSolid,
            title = {
                Text(
                    text = "Удалить операцию?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary
                )
            },
            text = {
                Text(
                    text = "Операция будет удалена безвозвратно, балансы и статистика пересчитаются автоматически.",
                    fontSize = 14.sp,
                    color = InkSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete()
                }) {
                    Text(
                        text = "Удалить",
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = "Отмена", color = InkPrimary)
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = InkSecondary)
}

@Composable
private fun ScanButton(
    label: String,
    icon: ImageVector,
    loading: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = CardSolid,
        border = BorderStroke(1.dp, InkPrimary)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = InkPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptSheet(
    scan: ReceiptScan,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ScreenBg
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Чек распознан",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = listOfNotNull(
                    scan.amount?.let { "Сумма: ${Formatters.money(it)}" },
                    scan.date?.let { "Дата: ${Formatters.fullDate(it)}" }
                ).joinToString(" · ").ifEmpty {
                    "Сумму и дату определить не удалось — заполните вручную"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = InkSecondary
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardBg
            ) {
                Text(
                    text = scan.text.ifBlank { "Текст не распознан" },
                    modifier = Modifier
                        .padding(12.dp)
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState()),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = InkPrimary
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FabDark,
                    contentColor = OnAccent
                )
            ) {
                Text(
                    text = "Заполнить сумму и дату",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TypeToggle(type: TransactionType, onSelect: (TransactionType) -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = CardBg) {
        Row(modifier = Modifier.padding(4.dp)) {
            TypeOption(
                label = "Расход",
                selected = type == TransactionType.EXPENSE,
                modifier = Modifier.weight(1f)
            ) { onSelect(TransactionType.EXPENSE) }
            TypeOption(
                label = "Доход",
                selected = type == TransactionType.INCOME,
                modifier = Modifier.weight(1f)
            ) { onSelect(TransactionType.INCOME) }
        }
    }
}

@Composable
private fun RowScope.TypeOption(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) InkPrimary else Color.Transparent
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) OnAccent else InkSecondary
        )
    }
}

@Composable
private fun AmountField(value: String, onValueChange: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = SolidColor(AccentBlue),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) {
                        Text(
                            text = "0",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkSecondary.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text(text = "Сумма, ${CurrencyConfig.symbol}", fontSize = 12.sp, color = InkSecondary)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    categories: List<Category>,
    selected: Category?,
    onSelect: (Category) -> Unit
) {
    if (categories.isEmpty()) {
        Text(
            text = "Нет категорий этого типа — создайте их в разделе категорий",
            fontSize = 13.sp,
            color = InkSecondary
        )
        return
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            CategoryChip(
                category = category,
                selected = selected?.id == category.id,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = Color(category.color)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) categoryColor.copy(alpha = 0.16f) else CardBg,
        border = if (selected) BorderStroke(1.dp, categoryColor) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(categoryColor, CircleShape)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = category.name,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = InkPrimary
            )
        }
    }
}

@Composable
private fun DateSelection(
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPickDate: () -> Unit
) {
    val yesterday = remember(today) { today.minusDays(1) }
    val isToday = selectedDate == today
    val isYesterday = selectedDate == yesterday
    val isCustom = !isToday && !isYesterday
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DateOptionPill(
            text = "Сегодня",
            selected = isToday,
            modifier = Modifier.weight(1f)
        ) { onDateSelected(today) }
        DateOptionPill(
            text = "Вчера",
            selected = isYesterday,
            modifier = Modifier.weight(1f)
        ) { onDateSelected(yesterday) }
        DateOptionPill(
            text = if (isCustom) Formatters.fullDate(selectedDate) else "Другая дата",
            selected = isCustom,
            modifier = Modifier.weight(1f),
            onClick = onPickDate
        )
    }
}

@Composable
private fun DateOptionPill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) InkPrimary else CardBg
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) OnAccent else InkPrimary
        )
    }
}

@Composable
private fun DescriptionField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = "Например: продукты на неделю", color = InkSecondary) },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = InkPrimary,
            unfocusedTextColor = InkPrimary,
            cursorColor = InkPrimary,
            unfocusedBorderColor = DividerColor,
            focusedBorderColor = InkPrimary,
            unfocusedContainerColor = CardSolid,
            focusedContainerColor = CardSolid
        ),
        minLines = 1,
        maxLines = 2
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogBox(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onDatePicked: (LocalDate) -> Unit
) {
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    onDatePicked(
                        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    )
                } ?: onDismiss()
            }) { Text(text = "Выбрать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Отмена") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
