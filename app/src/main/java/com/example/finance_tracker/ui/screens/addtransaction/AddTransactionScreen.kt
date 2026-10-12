package com.example.finance_tracker.ui.screens.addtransaction

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finance_tracker.domain.model.Category
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.ui.components.Formatters
import com.example.finance_tracker.ui.theme.AccentBlue
import com.example.finance_tracker.ui.theme.CardBg
import com.example.finance_tracker.ui.theme.DividerColor
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary
import com.example.finance_tracker.ui.theme.ScreenBg
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Экран добавления расхода/дохода: переключатель типа, крупная сумма,
 * категории-чипы из БД, дата (Сегодня/Вчера/календарь), комментарий.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val today = remember { LocalDate.now() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddTransactionEvent.Saved -> onSaved()
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
                        text = "Новая операция",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            TypeToggle(type = state.type, onSelect = viewModel::setType)

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
                    contentColor = Color.White,
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

            Spacer(Modifier.height(24.dp))
        }
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
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = InkSecondary)
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
            color = if (selected) Color.White else InkSecondary
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
        Text(text = "Сумма, ₽", fontSize = 12.sp, color = InkSecondary)
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
            color = if (selected) Color.White else InkPrimary
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
            unfocusedBorderColor = DividerColor,
            focusedBorderColor = InkPrimary,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
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
