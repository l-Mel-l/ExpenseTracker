package com.example.expensetracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class ReportPeriod(val title: String) {
    DAY("Сегодня"),
    WEEK("Неделя"),
    MONTH("Месяц"),
    ALL("Все время")
}

class MainViewModel(private val repository: ExpenseRepository) : ViewModel() {

    // Состояние текущего фильтра
    private val _currentPeriod = MutableStateFlow(ReportPeriod.MONTH)
    val currentPeriod: StateFlow<ReportPeriod> = _currentPeriod.asStateFlow()

    val budgetLimit: StateFlow<Double> = repository.getBudgetLimit()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expenses: StateFlow<List<Expense>> = combine(
        repository.getAllExpenses(),
        _currentPeriod
    ) { allExpenses, period ->
        val now = LocalDate.now()
        allExpenses.filter { expense ->
            when (period) {
                ReportPeriod.DAY -> expense.date == now
                ReportPeriod.WEEK -> expense.date.isAfter(now.minusDays(7)) || expense.date == now.minusDays(7)
                ReportPeriod.MONTH -> expense.date.month == now.month && expense.date.year == now.year
                ReportPeriod.ALL -> true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Действия
    fun addExpense(expense: Expense) = viewModelScope.launch { repository.addExpense(expense) }

    fun setBudget(limit: Double) {
        if (limit >= 0) {
            viewModelScope.launch { repository.setBudgetLimit(limit) }
        }
    }

    fun setPeriod(period: ReportPeriod) { _currentPeriod.value = period }
}