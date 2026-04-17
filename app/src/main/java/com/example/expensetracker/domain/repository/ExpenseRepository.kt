package com.example.expensetracker.domain.repository

import com.example.expensetracker.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    // Чтение данных
    fun getAllExpenses(): Flow<List<Expense>>
    fun getBudgetLimit(): Flow<Double>

    // Запись данных
    suspend fun addExpense(expense: Expense)
    suspend fun setBudgetLimit(limit: Double)
}