package com.example.expensetracker.presentation

import com.example.expensetracker.data.repository.InMemoryExpenseRepository
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.model.ExpenseCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var repository: InMemoryExpenseRepository
    private lateinit var viewModel: MainViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = InMemoryExpenseRepository()
        viewModel = MainViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should add new expense to the list (Scenario 1)`() = runTest {
        // Эмуляция подписки UI для активации ленивого StateFlow
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.expenses.collect {}
        }

        // Arrange (Подготовка данных)
        val testExpense = Expense(
            amount = 500.0,
            category = ExpenseCategory.FOOD,
            description = "Обед"
        )

        // Act (Действие)
        viewModel.addExpense(testExpense)
        advanceUntilIdle() // Ожидание завершения всех асинхронных операций

        // Assert (Проверка)
        val currentExpenses = viewModel.expenses.value
        assertEquals(1, currentExpenses.size)
        assertEquals(500.0, currentExpenses[0].amount, 0.0)
    }

    @Test
    fun `should ignore negative budget limits (Scenario 2)`() = runTest {
        // Симуляция подписки UI
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.budgetLimit.collect {}
        }

        // Arrange
        val initialBudget = viewModel.budgetLimit.value
        assertEquals(0.0, initialBudget, 0.0)

        // Act
        viewModel.setBudget(-1000.0)
        advanceUntilIdle()

        // Assert
        val updatedBudget = viewModel.budgetLimit.value
        assertEquals(0.0, updatedBudget, 0.0)
    }

    @Test
    fun `should filter expenses by TODAY correctly (Scenario 3)`() = runTest {
        // Симуляция подписки UI
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.expenses.collect {}
        }

        // Arrange
        val expenseToday = Expense(amount = 100.0, category = ExpenseCategory.FOOD, date = LocalDate.now())
        val expensePast = Expense(amount = 200.0, category = ExpenseCategory.TRANSPORT, date = LocalDate.now().minusDays(5))

        viewModel.addExpense(expenseToday)
        viewModel.addExpense(expensePast)
        advanceUntilIdle()

        // Act
        viewModel.setPeriod(ReportPeriod.DAY)
        advanceUntilIdle()

        // Assert
        val filteredExpenses = viewModel.expenses.value
        assertEquals(1, filteredExpenses.size)
        assertEquals(100.0, filteredExpenses[0].amount, 0.0)
    }
}