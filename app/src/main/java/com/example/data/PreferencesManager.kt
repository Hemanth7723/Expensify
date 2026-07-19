package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "expense_tracker_prefs"
        private const val KEY_CURRENCY = "preferred_currency"
        private const val KEY_NAME_ENC = "user_name_enc"
        private const val KEY_EMAIL_ENC = "user_email_enc"
        private const val KEY_BIO_ENC = "user_bio_enc"
        private const val KEY_API_KEY_ENC = "custom_api_key_enc"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_DARK_THEME_ENABLED = "dark_theme_enabled"
    }

    fun isDarkThemeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DARK_THEME_ENABLED, false)
    }

    fun setDarkThemeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME_ENABLED, enabled).apply()
    }

    fun getPreferredCurrency(): String {
        return prefs.getString(KEY_CURRENCY, "₹") ?: "₹"
    }

    fun setPreferredCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getUserName(): String {
        val enc = prefs.getString(KEY_NAME_ENC, "") ?: ""
        return CryptoManager.decryptString(enc)
    }

    fun setUserName(name: String) {
        val enc = CryptoManager.encryptString(name)
        prefs.edit().putString(KEY_NAME_ENC, enc).apply()
    }

    fun getUserEmail(): String {
        val enc = prefs.getString(KEY_EMAIL_ENC, "") ?: ""
        return CryptoManager.decryptString(enc)
    }

    fun setUserEmail(email: String) {
        val enc = CryptoManager.encryptString(email)
        prefs.edit().putString(KEY_EMAIL_ENC, enc).apply()
    }

    fun getUserBio(): String {
        val enc = prefs.getString(KEY_BIO_ENC, "") ?: ""
        return CryptoManager.decryptString(enc)
    }

    fun setUserBio(bio: String) {
        val enc = CryptoManager.encryptString(bio)
        prefs.edit().putString(KEY_BIO_ENC, enc).apply()
    }

    fun getCustomApiKey(): String {
        val enc = prefs.getString(KEY_API_KEY_ENC, "") ?: ""
        return CryptoManager.decryptString(enc)
    }

    fun setCustomApiKey(apiKey: String) {
        val enc = CryptoManager.encryptString(apiKey)
        prefs.edit().putString(KEY_API_KEY_ENC, enc).apply()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
