package com.example.finance_tracker.ui.screens.categories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.finance_tracker.domain.model.Category
import com.example.finance_tracker.domain.model.TransactionType
import com.example.finance_tracker.ui.components.AppIcons
import com.example.finance_tracker.ui.components.CategoryDot
import com.example.finance_tracker.ui.theme.CardBg
import com.example.finance_tracker.ui.theme.CardSolid
import com.example.finance_tracker.ui.theme.DividerColor
import com.example.finance_tracker.ui.theme.FabDark
import com.example.finance_tracker.ui.theme.InkPrimary
import com.example.finance_tracker.ui.theme.InkSecondary
import com.example.finance_tracker.ui.theme.OnAccent
import com.example.finance_tracker.ui.theme.ScreenBg
import kotlinx.coroutines.launch

/** Диалог редактирования: null — закрыт, Category — редактирование. */
private object NewCategoryMarker

/**
 * Справочник категорий: фильтр по типу, создание/редактирование
 * (название, иконка, цвет, тип), удаление с предупреждением.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // null — закрыт; NewCategoryMarker — создание; Category — редактирование
    var editorTarget by remember { mutableStateOf<Any?>(null) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }
    var deleteUsageCount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        TopAppBar(
            title = {
                Text(
                    text = "Категории",
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

        FilterChips(selected = state.filter, onSelect = viewModel::setFilter)
        Spacer(Modifier.height(12.dp))

        if (state.isLoading) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.categories, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        onEdit = { editorTarget = category },
                        onDelete = {
                            scope.launch {
                                deleteUsageCount = viewModel.countTransactions(category.id)
                                deleteTarget = category
                            }
                        }
                    )
                }
                if (state.categories.isEmpty()) {
                    item {
                        Text(
                            text = "Категорий пока нет — создайте первую",
                            fontSize = 13.sp,
                            color = InkSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        )
                    }
                }
            }
        }

        NewCategoryButton(onClick = { editorTarget = NewCategoryMarker })
        Spacer(Modifier.height(16.dp))
    }

    editorTarget?.let { target ->
        CategoryEditorDialog(
            existing = target as? Category,
            onDismiss = { editorTarget = null },
            onSave = { name, icon, color, type ->
                viewModel.saveCategory(target as? Category, name, icon, color, type)
                editorTarget = null
            }
        )
    }

    deleteTarget?.let { category ->
        AlertDialog(
        onDismissRequest = { deleteTarget = null },
        containerColor = CardSolid,
            title = { Text(text = "Удалить «${category.name}»?", fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    text = if (deleteUsageCount > 0) {
                        "У категории $deleteUsageCount операций — они сохранятся и перейдут в «Без категории»."
                    } else {
                        "Категория будет удалена. Связанных операций нет."
                    },
                    fontSize = 14.sp,
                    color = InkSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(category)
                    deleteTarget = null
                }) { Text(text = "Удалить", color = Color(0xFFE5484D), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(text = "Отмена") }
            }
        )
    }
}

@Composable
private fun FilterChips(selected: CategoriesFilter, onSelect: (CategoriesFilter) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CategoriesFilter.entries.forEach { filter ->
            val active = filter == selected
            Surface(
                onClick = { onSelect(filter) },
                shape = RoundedCornerShape(14.dp),
                color = if (active) InkPrimary else CardBg
            ) {
                Text(
                    text = filter.label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (active) OnAccent else InkPrimary
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryDot(color = category.color)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = InkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (category.type == TransactionType.EXPENSE) "расход" else "доход",
                    fontSize = 12.sp,
                    color = InkSecondary
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Редактировать",
                    tint = InkSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (!category.isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Удалить",
                        tint = InkSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NewCategoryButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardSolid,
        border = BorderStroke(1.dp, InkPrimary)
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
                text = "Новая категория",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkPrimary
            )
        }
    }
}

private val CATEGORY_COLORS = listOf(
    0xFFEF5350, 0xFF42A5F5, 0xFFAB47BC, 0xFF30A46C,
    0xFFFFB300, 0xFF26C6DA, 0xFF8D6E63, 0xFF9E9E9E
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditorDialog(
    existing: Category?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: Long, type: TransactionType) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var iconName by remember { mutableStateOf(existing?.icon ?: "category") }
    var color by remember { mutableStateOf(existing?.color ?: CATEGORY_COLORS.first()) }
    var type by remember { mutableStateOf(existing?.type ?: TransactionType.EXPENSE) }

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
                text = if (existing == null) "Новая категория" else "Редактирование",
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
            Spacer(Modifier.height(16.dp))

            Text(text = "Тип", fontSize = 13.sp, color = InkSecondary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypePill("Расход", type == TransactionType.EXPENSE, Modifier.weight(1f)) {
                    type = TransactionType.EXPENSE
                }
                TypePill("Доход", type == TransactionType.INCOME, Modifier.weight(1f)) {
                    type = TransactionType.INCOME
                }
            }
            Spacer(Modifier.height(16.dp))

            Text(text = "Иконка", fontSize = 13.sp, color = InkSecondary)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppIcons.selectableNames.forEach { candidate ->
                    IconOption(
                        iconName = candidate,
                        tint = Color(color),
                        selected = candidate == iconName,
                        onClick = { iconName = candidate }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Text(text = "Цвет", fontSize = 13.sp, color = InkSecondary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CATEGORY_COLORS.forEach { candidate ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(candidate))
                            .border(
                                width = if (candidate == color) 3.dp else 0.dp,
                                color = InkPrimary,
                                shape = CircleShape
                            )
                            .clickable { color = candidate }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { onSave(name, iconName, color, type) },
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
                Text(text = "Сохранить", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TypePill(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) InkPrimary else CardBg
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) OnAccent else InkSecondary
        )
    }
}

@Composable
private fun IconOption(
    iconName: String,
    tint: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) tint.copy(alpha = 0.16f) else CardBg,
        border = if (selected) BorderStroke(1.dp, tint) else null
    ) {
        Icon(
            imageVector = AppIcons.vectorFor(iconName),
            contentDescription = iconName,
            tint = tint,
            modifier = Modifier
                .padding(10.dp)
                .size(22.dp)
        )
    }
}
