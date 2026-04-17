package com.example.expensetracker.domain.model

import java.time.LocalDate
import java.util.UUID

// Перечисление доступных категорий
enum class ExpenseCategory(val displayName: String) {
    FOOD("Еда"),
    TRANSPORT("Транспорт"),
    ENTERTAINMENT("Развлечения"),
    BILLS("Счета"),
    OTHER("Другое")
}

data class Expense(
    val id: String = UUID.randomUUID().toString(), // Генерируем уникальный ID
    val amount: Double,
    val category: ExpenseCategory,
    val date: LocalDate = LocalDate.now(),
    val description: String = ""
)