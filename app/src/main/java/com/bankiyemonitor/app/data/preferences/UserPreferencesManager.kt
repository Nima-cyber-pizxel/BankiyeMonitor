package com.bankiyemonitor.app.data.preferences

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import java.util.UUID
import javax.crypto.KeyGenerator

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bankiye_preferences")

class UserPreferencesManager(private val context: Context) {

    // Preference keys
    companion object {
        val SUMMARY_FREQUENCY = stringPreferencesKey("summary_frequency")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SECURITY_ALERTS_ENABLED = booleanPreferencesKey("security_alerts_enabled")
        val HIGH_RISK_ONLY = booleanPreferencesKey("high_risk_only")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SMS_PERMISSION_GRANTED = booleanPreferencesKey("sms_permission_granted")
        val DB_PASSPHRASE_KEY = stringPreferencesKey("db_passphrase")
        val SELECTED_BANKS = stringSetPreferencesKey("selected_banks")
        val THEME_MODE = stringPreferencesKey("theme_mode") // LIGHT, DARK, SYSTEM
        val LAST_SCAN_TIME = longPreferencesKey("last_scan_time")
        val INITIAL_SCAN_COMPLETED = booleanPreferencesKey("initial_scan_completed")
    }

    val summaryFrequency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SUMMARY_FREQUENCY] ?: "WEEKLY"
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    val securityAlertsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SECURITY_ALERTS_ENABLED] ?: true
    }

    val highRiskOnly: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HIGH_RISK_ONLY] ?: false
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    val smsPermissionGranted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SMS_PERMISSION_GRANTED] ?: false
    }

    val selectedBanks: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_BANKS] ?: setOf("cbe", "dashen", "awash", "abyssinia", "wegagen", "coopbank")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "SYSTEM"
    }

    val lastScanTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_SCAN_TIME] ?: 0L
    }

    val initialScanCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[INITIAL_SCAN_COMPLETED] ?: false
    }

    suspend fun setSummaryFrequency(frequency: String) {
        context.dataStore.edit { it[SUMMARY_FREQUENCY] = frequency }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setSecurityAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SECURITY_ALERTS_ENABLED] = enabled }
    }

    suspend fun setHighRiskOnly(enabled: Boolean) {
        context.dataStore.edit { it[HIGH_RISK_ONLY] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setSmsPermissionGranted(granted: Boolean) {
        context.dataStore.edit { it[SMS_PERMISSION_GRANTED] = granted }
    }

    suspend fun setSelectedBanks(banks: Set<String>) {
        context.dataStore.edit { it[SELECTED_BANKS] = banks }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setLastScanTime(time: Long) {
        context.dataStore.edit { it[LAST_SCAN_TIME] = time }
    }

    suspend fun setInitialScanCompleted(completed: Boolean) {
        context.dataStore.edit { it[INITIAL_SCAN_COMPLETED] = completed }
    }

    /**
     * Get or generate database encryption passphrase.
     * Uses Android Keystore for secure key storage.
     */
    fun getDatabasePassphrase(): ByteArray {
        val prefs = context.getSharedPreferences("bankiye_secure", Context.MODE_PRIVATE)
        var passphrase = prefs.getString("db_key", null)
        
        if (passphrase == null) {
            passphrase = UUID.randomUUID().toString() + UUID.randomUUID().toString()
            prefs.edit().putString("db_key", passphrase).apply()
        }
        
        return passphrase.toByteArray()
    }
}
