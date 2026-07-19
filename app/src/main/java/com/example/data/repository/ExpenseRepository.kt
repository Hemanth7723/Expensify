package com.example.data.repository

import com.example.data.database.ExpenseDao
import com.example.data.model.Expense
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun insert(expense: Expense) = expenseDao.insertExpense(expense)

    suspend fun update(expense: Expense) = expenseDao.updateExpense(expense)

    suspend fun delete(expense: Expense) = expenseDao.deleteExpense(expense)

    suspend fun deleteById(id: Int) = expenseDao.deleteExpenseById(id)

    suspend fun clearAll() = expenseDao.clearAllExpenses()

    /**
     * Genrate spreadsheet data in Excel-ready CSV format
     */
    fun generateCsvData(expenses: List<Expense>, currencySymbol: String): String {
        val sb = java.lang.StringBuilder()
        // CSV Header
        sb.append("ID,Date,Category,Title,Amount,Currency,Notes\n")
        
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        
        for (expense in expenses) {
            val dateStr = df.format(Date(expense.timestamp))
            // Escape values to prevent disruption if they contain commas
            val escapedTitle = escapeCsv(expense.title)
            val escapedCategory = escapeCsv(expense.category)
            val escapedNote = escapeCsv(expense.note)
            
            sb.append("${expense.id},")
              .append("$dateStr,")
              .append("$escapedCategory,")
              .append("$escapedTitle,")
              .append(String.format(Locale.US, "%.2f", expense.amount)).append(",")
              .append("$currencySymbol,")
              .append("$escapedNote\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        var clean = value.replace("\"", "\"\"")
        if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            clean = "\"$clean\""
        }
        return clean
    }
}
