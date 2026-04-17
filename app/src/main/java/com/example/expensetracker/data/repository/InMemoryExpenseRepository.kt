package com.example.expensetracker.data.repository

import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryExpenseRepository : ExpenseRepository {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    private val _budgetLimit = MutableStateFlow(0.0)

    // Выдаем данные наружу только для чтения
    override fun getAllExpenses(): Flow<List<Expense>> = _expenses.asStateFlow()

    override fun getBudgetLimit(): Flow<Double> = _budgetLimit.asStateFlow()

    override suspend fun addExpense(expense: Expense) {
        // Добавляем новый расход в наш список
        _expenses.update { currentList ->
            currentList + expense
        }
    }

    override suspend fun setBudgetLimit(limit: Double) {
        _budgetLimit.value = limit
    }
}