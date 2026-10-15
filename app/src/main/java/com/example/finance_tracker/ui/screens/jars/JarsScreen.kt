package com.example.finance_tracker.ui.screens.jars

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.finance_tracker.core.CurrencyConfig
import com.example.finance_tracker.domain.model.SavingsJar
import com.example.finance_tracker.ui.components.Formatters
import com.example.finance_tracker.ui.theme.AccentBlue
import com.example.finance_tracker.ui.theme.CardBg
import com.example.finance_tracker.ui.theme.CardSolid
import com.example.finance_tracker.ui.theme.ExpenseRed
import com.example.finance_tracker.ui.theme.FabDark
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary
import com.example.finance_tracker.ui.theme.OnAccent
import com.example.finance_tracker.ui.theme.ScreenBg

/**
 * Копилки: общий итог, карточки с прогрессом к цели, «Отложить»/«Снять»,
 * создание копилки (название, цель по желанию, цвет).
 */
@Composable
fun JarsScreen(viewModel: JarsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is JarsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    // Копилка, для которой открыт диалог ввода суммы; isDeposit — какая операция.
    var amountDialogJar by remember { mutableStateOf<SavingsJar?>(null) }
    var amountDialogIsDeposit by remember { mutableStateOf(true) }

    androidx.compose.material3.Scaffold(
        containerColor = ScreenBg,
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Копилки",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Всего накоплено: ${Formatters.money(state.totalSaved)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = InkSecondary
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.jars.isEmpty() -> EmptyState(modifier = Modifier.weight(1f))

                else -> JarList(
                    jars = state.jars,
                    modifier = Modifier.weight(1f),
                    onDeposit = { jar ->
                        amountDialogJar = jar
                        amountDialogIsDeposit = true
                    },
                    onWithdraw = { jar ->
                        amountDialogJar = jar
                        amountDialogIsDeposit = false
                    }
                )
            }

            CreateJarButton(onClick = { showCreateDialog = true })
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showCreateDialog) {
        CreateJarDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, target, color ->
                viewModel.createJar(name, target, color)
                showCreateDialog = false
            }
        )
    }

    amountDialogJar?.let { jar ->
        AmountDialog(
            jar = jar,
            isDeposit = amountDialogIsDeposit,
            onDismiss = { amountDialogJar = null },
            onConfirm = { amountInput ->
                if (amountDialogIsDeposit) {
                    viewModel.deposit(jar.id, amountInput)
                } else {
                    viewModel.withdraw(jar.id, amountInput)
                }
                amountDialogJar = null
            }
        )
    }
}

@Composable
private fun JarList(
    jars: List<SavingsJar>,
    modifier: Modifier = Modifier,
    onDeposit: (SavingsJar) -> Unit,
    onWithdraw: (SavingsJar) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 14.dp)
    ) {
        items(jars, key = { it.id }) { jar ->
            JarCard(
                jar = jar,
                onDeposit = { onDeposit(jar) },
                onWithdraw = { onWithdraw(jar) }
            )
        }
    }
}

@Composable
private fun JarCard(
    jar: SavingsJar,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit
) {
    val jarColor = Color(jar.color)
    val target = jar.targetAmount
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(jarColor, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = jar.name,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Formatters.money(jar.savedAmount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
            }

            Spacer(Modifier.height(10.dp))
            if (target != null && target > 0.0) {
                val percent = ((jar.savedAmount / target) * 100).toInt().coerceIn(0, 100)
                LinearProgressIndicator(
                    progress = { (jar.savedAmount / target).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AccentBlue,
                    trackColor = CardSolid
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "цель: ${Formatters.money(target)} · $percent%",
                    fontSize = 12.sp,
                    color = InkSecondary
                )
            } else {
                Text(text = "без цели", fontSize = 12.sp, color = InkSecondary)
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onDeposit,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FabDark,
                        contentColor = OnAccent
                    )
                ) {
                    Text(text = "Отложить", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = onWithdraw,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InkPrimary)
                ) {
                    Text(text = "Снять", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun CreateJarButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardSolid,
        border = androidx.compose.foundation.BorderStroke(1.dp, InkPrimary)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = InkPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Новая копилка",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Копилок пока нет",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = InkPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Создайте первую и откладывайте на цель",
                fontSize = 13.sp,
                color = InkSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private val JAR_COLORS = listOf(
    0xFF2F80ED, 0xFFE5484D, 0xFF30A46C, 0xFFAB47BC,
    0xFFFFB300, 0xFF26C6DA, 0xFF8D6E63, 0xFF9E9E9E
)

/** Формат ввода суммы: пробелы-разделители тысяч, запятая. */
private fun formatAmountInput(raw: String): String {
    var separatorSeen = false
    val cleaned = raw
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
        .take(10)
    return cleaned
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateJarDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, targetAmount: Double?, color: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(JAR_COLORS.first()) }
    val target = targetInput.replace(',', '.').toDoubleOrNull()

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
                text = "Новая копилка",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(40) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = InkPrimary,
                    unfocusedBorderColor = ScreenBg,
                    focusedTextColor = InkPrimary,
                    unfocusedTextColor = InkPrimary,
                    cursorColor = InkPrimary
                )
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = targetInput,
                onValueChange = { targetInput = formatAmountInput(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Цель, ${CurrencyConfig.symbol} (необязательно)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = InkPrimary,
                    unfocusedBorderColor = ScreenBg,
                    focusedTextColor = InkPrimary,
                    unfocusedTextColor = InkPrimary,
                    cursorColor = InkPrimary
                )
            )
            Spacer(Modifier.height(16.dp))
            Text(text = "Цвет", fontSize = 13.sp, color = InkSecondary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                JAR_COLORS.forEach { color ->
                    ColorPickDot(
                        color = Color(color),
                        selected = color == selectedColor,
                        onClick = { selectedColor = color }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onCreate(name, target?.takeIf { it > 0.0 }, selectedColor) },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FabDark,
                    contentColor = OnAccent,
                    disabledContainerColor = CardBg,
                    disabledContentColor = InkSecondary
                )
            ) {
                Text(text = "Создать", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ColorPickDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = InkPrimary,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountDialog(
    jar: SavingsJar,
    isDeposit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    val amount = amountInput.replace(',', '.').toDoubleOrNull()

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
                text = if (isDeposit) "Отложить в «${jar.name}»" else "Снять с «${jar.name}»",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Сейчас в копилке: ${Formatters.money(jar.savedAmount)}",
                fontSize = 13.sp,
                color = InkSecondary
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = formatAmountInput(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Сумма, ${CurrencyConfig.symbol}") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = InkPrimary,
                    unfocusedBorderColor = ScreenBg,
                    focusedTextColor = InkPrimary,
                    unfocusedTextColor = InkPrimary,
                    cursorColor = InkPrimary
                )
            )
            if (!isDeposit && amount != null && amount > jar.savedAmount) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "В копилке недостаточно средств",
                    fontSize = 12.sp,
                    color = ExpenseRed
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onConfirm(amountInput) },
                    enabled = amount != null && amount > 0.0 &&
                        (isDeposit || amount <= jar.savedAmount),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FabDark,
                        contentColor = OnAccent,
                        disabledContainerColor = CardBg,
                        disabledContentColor = InkSecondary
                    )
                ) {
                    Text(
                        text = if (isDeposit) "Отложить" else "Снять",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InkPrimary)
                ) {
                    Text(text = "Отмена", fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
