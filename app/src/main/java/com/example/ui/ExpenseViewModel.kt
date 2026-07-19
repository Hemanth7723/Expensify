package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.PreferencesManager
import com.example.data.VoiceRecognizerHelper
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.ExpenseDatabase
import com.example.data.model.Expense
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ExpenseDatabase.getDatabase(application)
    private val repository = ExpenseRepository(db.expenseDao())
    val preferencesManager = PreferencesManager(application)
    val voiceHelper = VoiceRecognizerHelper(application)

    // Current app state flows
    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currency = MutableStateFlow(preferencesManager.getPreferredCurrency())
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _sortOrder = MutableStateFlow("Newest") // Newest, Oldest, Amount High, Amount Low
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(preferencesManager.isDarkThemeEnabled())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Sorted and filtered list
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        allExpenses, _currency, _sortOrder, _selectedCategoryFilter
    ) { expenses, _, sOrder, catFilter ->
        var list = expenses
        if (catFilter != "All") {
            list = list.filter { it.category.equals(catFilter, ignoreCase = true) }
        }
        when (sOrder) {
            "Oldest" -> list.sortedBy { it.timestamp }
            "Amount High" -> list.sortedByDescending { it.amount }
            "Amount Low" -> list.sortedBy { it.amount }
            else -> list.sortedByDescending { it.timestamp } // Newest
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profile Details
    var userName by mutableStateOf(preferencesManager.getUserName())
        private set
    var userEmail by mutableStateOf(preferencesManager.getUserEmail())
        private set
    var userBio by mutableStateOf(preferencesManager.getUserBio())
        private set
    var customApiKey by mutableStateOf(preferencesManager.getCustomApiKey())
        private set

    // UI state states
    var aiInsights by mutableStateOf<String?>(null)
        private set
    var isGeneratingInsights by mutableStateOf(false)
        private set
    var aiInsightError by mutableStateOf<String?>(null)
        private set

    var voiceParsingError by mutableStateOf<String?>(null)
        private set
    var isAiParsingVoice by mutableStateOf(false)
        private set

    init {
        // Fallback checks / migrations
        if (preferencesManager.getPreferredCurrency().isEmpty()) {
            preferencesManager.setPreferredCurrency("₹")
        }
    }

    fun changeCurrency(newCurrency: String) {
        preferencesManager.setPreferredCurrency(newCurrency)
        _currency.value = newCurrency
    }

    fun toggleDarkTheme(enabled: Boolean) {
        preferencesManager.setDarkThemeEnabled(enabled)
        _isDarkTheme.value = enabled
    }

    fun changeSortOrder(order: String) {
        _sortOrder.value = order
    }

    fun changeCategoryFilter(cat: String) {
        _selectedCategoryFilter.value = cat
    }

    // Save profile details
    fun saveProfile(name: String, email: String, bio: String, apiKey: String) {
        userName = name
        userEmail = email
        userBio = bio
        customApiKey = apiKey

        preferencesManager.setUserName(name)
        preferencesManager.setUserEmail(email)
        preferencesManager.setUserBio(bio)
        preferencesManager.setCustomApiKey(apiKey)
        preferencesManager.setOnboardingCompleted(true)
    }

    // Insert, update, delete
    fun addExpense(title: String, amount: Double, category: String, note: String, date: Long) {
        viewModelScope.launch {
            repository.insert(
                Expense(
                    title = title,
                    amount = amount,
                    category = category,
                    timestamp = date,
                    note = note
                )
            )
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.update(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    private fun getActiveApiKey(): String {
        val custom = preferencesManager.getCustomApiKey()
        if (custom.isNotEmpty() && custom.isNotBlank()) {
            return custom
        }
        // Fallback to BuildConfig injected from metadata/secrets
        return BuildConfig.GEMINI_API_KEY
    }

    /**
     * Parse verbal transcript with Gemini to populate expense data
     */
    fun parseTranscriptToExpense(transcript: String, onParsed: (title: String, amount: Double, category: String, note: String) -> Unit) {
        if (transcript.isBlank()) return
        isAiParsingVoice = true
        voiceParsingError = null

        viewModelScope.launch {
            val apiKey = getActiveApiKey()
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                isAiParsingVoice = false
                voiceParsingError = "API Key not configured. Please add an API Key in the Profile (top right corner)."
                return@launch
            }

            val systemInstruction = """
                You are a smart expense formatting assistant. Parse the text describing an expense transaction and extract details.
                Return ONLY a raw JSON object containing these exact fields:
                - "title": String (short name of the purchase, e.g. Starbucks Coffee, Taxi Ride, Utility Bill, Grocery Shopping)
                - "amount": Double (the cost value, e.g. 15.50. Extract the number. If not specified, default to 0.0)
                - "category": String (must be EXACTLY one of these pre-defined categories based on what fits best: Food, Transport, Entertainment, Shopping, Health, Utilities, Miscellaneous)
                - "note": String (any details or context)

                Rules:
                - Do not output any Markdown code blocks, no ```json formatting, or any preamble or wrap-up text. Just raw, pure JSON code starting with { and ending with }.
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = transcript)))),
                systemInstruction = Content(parts = listOf(Part(text = systemInstruction))),
                generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.1f)
            )

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(apiKey, request)
                }
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                
                // Securely parse the returned JSON text string
                val cleanJson = extractJson(rawText)
                if (cleanJson.isNotEmpty()) {
                    val json = JSONObject(cleanJson)
                    val title = json.optString("title", "Voice Expense")
                    val amount = json.optDouble("amount", 0.0)
                    val category = json.optString("category", "Miscellaneous")
                    val note = json.optString("note", "")
                    
                    // Direct callback
                    onParsed(title, amount, category, note)
                } else {
                    voiceParsingError = "Unable to process the response. Raw returned: $rawText"
                }
            } catch (e: Exception) {
                voiceParsingError = "AI error: ${e.localizedMessage ?: "Failed connection"}"
            } finally {
                isAiParsingVoice = false
            }
        }
    }

    /**
     * Retrieve behavioral AI spending analytics
     */
    fun genAiInsights() {
        val expenses = allExpenses.value
        val curCurrency = currency.value
        
        if (expenses.isEmpty()) {
            aiInsights = "No transaction recorded yet. Log some expenses using the Voice system or Manual form to let AI generate spending culture analysis."
            return
        }

        isGeneratingInsights = true
        aiInsightError = null

        viewModelScope.launch {
            val apiKey = getActiveApiKey()
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                isGeneratingInsights = false
                aiInsightError = "Gemini API Key missing. Please provide a key in the Profile (top right) or system secrets."
                return@launch
            }

            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sb = java.lang.StringBuilder()
            expenses.take(30).forEach {
                sb.append("- Date: ${df.format(Date(it.timestamp))}, Category: ${it.category}, Amount: $curCurrency${it.amount}, Title: ${it.title}, Note: ${it.note}\n")
            }

            val sysInstruction = """
                You are a witty, friendly personal financial manager and visual data analyst.
                Review the user's spending ledger and output a highly insightful budget analysis reflecting their "spending culture".
                Present your critique in 3 sections:
                1. 🎭 Financial Persona Profile (What does this layout say about their habits?)
                2. 🔍 Expenditure Critique (Highs, lows, anomalies, or waste areas)
                3. ⚡ Smart Behavioral Upgrades (Custom actionable recommendations)

                Rules:
                - Do not refer to standard budget limits or threshold configurations because the threshold feature has been deactivated.
                - Use bullet points, bold markers, and friendly visual icons/emojis in markdown. Keep it engaging, motivating, and readable.
            """.trimIndent()

            val mainPrompt = "Here is my recent money expense list in currency $curCurrency:\n$sb"

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = mainPrompt)))),
                systemInstruction = Content(parts = listOf(Part(text = sysInstruction))),
                generationConfig = GenerationConfig(temperature = 0.7f)
            )

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(apiKey, request)
                }
                aiInsights = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "No insights returned from AI."
            } catch (e: Exception) {
                aiInsightError = "Failed generating insights: ${e.localizedMessage ?: "Network error"}"
            } finally {
                isGeneratingInsights = false
            }
        }
    }

    /**
     * Defensive JSON Extractor to find first { and last } boundaries
     */
    private fun extractJson(rawText: String): String {
        var start = rawText.indexOf('{')
        var end = rawText.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return rawText.substring(start, end + 1)
        }
        return rawText.trim()
    }

    /**
     * Helper list of categories
     */
    val categories = listOf("Food", "Transport", "Entertainment", "Shopping", "Health", "Utilities", "Miscellaneous")

    fun getCategoryIconAndColor(category: String): Pair<String, androidx.compose.ui.graphics.Color> {
        return when (category.lowercase(Locale.ROOT)) {
            "food" -> "🍔" to androidx.compose.ui.graphics.Color(0xFFF59E0B)
            "transport" -> "🚗" to androidx.compose.ui.graphics.Color(0xFF3B82F6)
            "entertainment" -> "🎬" to androidx.compose.ui.graphics.Color(0xFFEC4899)
            "shopping" -> "🛍️" to androidx.compose.ui.graphics.Color(0xFF8B5CF6)
            "health" -> "🩺" to androidx.compose.ui.graphics.Color(0xFFEF4444)
            "utilities" -> "💡" to androidx.compose.ui.graphics.Color(0xFF06B6D4)
            else -> "📝" to androidx.compose.ui.graphics.Color(0xFF64748B)
        }
    }

    /**
     * Helper getters for metrics (INSTANT recalculations)
     */
    fun calculateTotalSpend(list: List<Expense>): Double {
        return list.sumOf { it.amount }
    }

    fun groupByCategory(list: List<Expense>): Map<String, Double> {
        return list.groupBy { it.category }.mapValues { it.value.sumOf { it.amount } }
    }

    fun groupByDay(list: List<Expense>): Map<String, Double> {
        val df = SimpleDateFormat("M/d", Locale.US)
        val grouped = list.groupBy {
            df.format(Date(it.timestamp))
        }
        val sorted = grouped.entries.sortedBy { entry ->
            entry.value.minOf { it.timestamp }
        }
        val result = LinkedHashMap<String, Double>()
        for (entry in sorted) {
            result[entry.key] = entry.value.sumOf { it.amount }
        }
        return result
    }

    fun groupByWeek(list: List<Expense>): Map<String, Double> {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("M/d", Locale.US)
        
        val grouped = list.groupBy {
            calendar.timeInMillis = it.timestamp
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val startDate = sdf.format(calendar.time)
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val endDate = sdf.format(calendar.time)
            "$startDate-$endDate"
        }
        
        val sorted = grouped.entries.sortedBy { entry ->
            entry.value.minOf { it.timestamp }
        }
        
        val result = LinkedHashMap<String, Double>()
        for (entry in sorted) {
            result[entry.key] = entry.value.sumOf { it.amount }
        }
        return result
    }

    fun groupByMonth(list: List<Expense>): Map<String, Double> {
        val df = SimpleDateFormat("MMM", Locale.US)
        val grouped = list.groupBy {
            df.format(Date(it.timestamp))
        }
        val sorted = grouped.entries.sortedBy { entry ->
            entry.value.minOf { it.timestamp }
        }
        val result = LinkedHashMap<String, Double>()
        for (entry in sorted) {
            result[entry.key] = entry.value.sumOf { it.amount }
        }
        return result
    }

    fun groupByYear(list: List<Expense>): Map<String, Double> {
        val df = SimpleDateFormat("yyyy", Locale.US)
        val grouped = list.groupBy {
            df.format(Date(it.timestamp))
        }
        val sorted = grouped.entries.sortedBy { entry ->
            entry.value.minOf { it.timestamp }
        }
        val result = LinkedHashMap<String, Double>()
        for (entry in sorted) {
            result[entry.key] = entry.value.sumOf { it.amount }
        }
        return result
    }

    fun formatCsv(expenses: List<Expense>): String {
        return repository.generateCsvData(expenses, currency.value)
    }

    override fun onCleared() {
        super.onCleared()
        voiceHelper.cancel()
    }
}
