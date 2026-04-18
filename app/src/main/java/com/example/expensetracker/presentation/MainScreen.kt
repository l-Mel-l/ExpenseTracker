package com.example.expensetracker.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.model.ExpenseCategory

val BgColor = Color(0xFFF4F7FB)
val CardColor = Color.White
val PrimaryBlue = Color(0xFF2C5282)
val TextDark = Color(0xFF1A202C)
val TextGray = Color(0xFF718096)

val CategoryColors = mapOf(
    ExpenseCategory.FOOD to Color(0xFFE53E3E),
    ExpenseCategory.TRANSPORT to Color(0xFF3182CE),
    ExpenseCategory.ENTERTAINMENT to Color(0xFFD69E2E),
    ExpenseCategory.BILLS to Color(0xFF38A169),
    ExpenseCategory.OTHER to Color(0xFFA0AEC0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val budgetLimit by viewModel.budgetLimit.collectAsState()
    val currentPeriod by viewModel.currentPeriod.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    val totalSpent = expenses.sumOf { it.amount }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = { Text("Трекер расходов", fontWeight = FontWeight.Bold, color = TextDark) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ФИЛЬТРЫ (ОТЧЕТЫ)
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ReportPeriod.values()) { period ->
                        FilterChip(
                            selected = period == currentPeriod,
                            onClick = { viewModel.setPeriod(period) },
                            label = { Text(period.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // КАРТОЧКА БЮДЖЕТА
            item {
                SummaryCard(
                    totalSpent = totalSpent,
                    budgetLimit = budgetLimit,
                    onEditBudget = { showBudgetDialog = true }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (expenses.isNotEmpty()) {
                item {
                    Text("Структура расходов", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    DonutChartCard(expenses = expenses)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // СПИСОК
            item {
                Text("Операции", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (expenses.isEmpty()) {
                item { Text("Нет данных за этот период", color = TextGray) }
            } else {
                items(expenses.reversed()) { expense ->
                    ExpenseItem(expense)
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { amount, category, desc ->
                viewModel.addExpense(Expense(amount = amount, category = category, description = desc))
                showAddDialog = false
            }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            currentBudget = budgetLimit,
            onDismiss = { showBudgetDialog = false },
            onSave = { limit ->
                viewModel.setBudget(limit)
                showBudgetDialog = false
            }
        )
    }
}


@Composable
fun SummaryCard(totalSpent: Double, budgetLimit: Double, onEditBudget: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Потрачено", fontSize = 14.sp, color = TextGray)
                // Кнопка редактирования бюджета
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Изменить бюджет",
                    tint = TextGray,
                    modifier = Modifier.size(18.dp).clickable { onEditBudget() }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("$totalSpent ₽", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)

            // Логика бюджета
            if (budgetLimit > 0) {
                val percent = (totalSpent / budgetLimit).coerceAtMost(1.0).toFloat()
                val isExceeded = totalSpent > budgetLimit

                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { percent },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (isExceeded) Color.Red else PrimaryBlue,
                    trackColor = BgColor,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Лимит: $budgetLimit ₽", fontSize = 12.sp, color = TextGray)
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Нажмите на карандаш, чтобы задать бюджет", fontSize = 12.sp, color = TextGray)
            }
        }
    }
}

@Composable
fun DonutChartCard(expenses: List<Expense>) {
    val grouped = expenses.groupBy { it.category }.mapValues { it.value.sumOf { exp -> exp.amount } }
    val total = grouped.values.sum()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(100.dp)) {
                var startAngle = -90f
                val strokeWidth = 35f

                grouped.forEach { (category, amount) ->
                    val sweepAngle = (amount / total).toFloat() * 360f
                    drawArc(
                        color = CategoryColors[category] ?: Color.Gray,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Легенда с ПРОЦЕНТАМИ
            Column {
                grouped.forEach { (category, amount) ->
                    val percentage = ((amount / total) * 100).toInt()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(CategoryColors[category] ?: Color.Gray, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category.displayName, fontSize = 14.sp, color = TextDark)
                        }
                        Text("$percentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    }
                }
            }
        }
    }
}

// Новый диалог для установки бюджета
@Composable
fun BudgetDialog(currentBudget: Double, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amount by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Лимит бюджета") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Сумма (₽)") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onSave(amount.toDoubleOrNull() ?: 0.0) }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        containerColor = CardColor
    )
}

@Composable
fun ExpenseItem(expense: Expense) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(CategoryColors[expense.category] ?: Color.Gray, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description.ifEmpty { expense.category.displayName },
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 16.sp
                )
                Text(expense.date.toString(), fontSize = 12.sp, color = TextGray)
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BgColor,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = expense.category.displayName,
                    fontSize = 12.sp,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Text("${expense.amount} ₽", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}


@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onSave: (Double, ExpenseCategory, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый расход", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Сумма (₽)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (например: Кофе)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Категория", color = TextGray, fontSize = 14.sp)

                // Простой выбор категорий
                LazyColumn(modifier = Modifier.height(150.dp)) {
                    items(ExpenseCategory.values()) { category ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = category == selectedCategory,
                                onClick = { selectedCategory = category }
                            )
                            Text(category.displayName)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sum = amount.toDoubleOrNull()
                    if (sum != null && sum > 0) onSave(sum, selectedCategory, description)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = TextGray) } },
        containerColor = CardColor
    )
}