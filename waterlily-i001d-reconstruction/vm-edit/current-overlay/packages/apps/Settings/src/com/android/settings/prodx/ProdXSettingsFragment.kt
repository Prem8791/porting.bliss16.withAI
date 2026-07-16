package com.android.settings.prodx

import android.app.prodx.ProdXManager
import android.app.prodx.ProdXMode
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.android.settings.R
import java.security.SecureRandom

class ProdXSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prodx_settings, rootKey)
        val manager = ProdXManager(requireContext())
        val modeController = ProdXModePreferenceController(manager)
        val healthController = ProdXHealthPreferenceController(manager)
        val adminController = ProdXGrantListPreferenceController(manager, UserHandle.myUserId())
        val auditController = ProdXAuditLogPreferenceController(manager, UserHandle.myUserId())

        findPreference<ListPreference>(KEY_MODE)?.apply {
            isVisible = Build.IS_DEBUGGABLE
            value = manager.settingsMode.value.toString()
            summary = modeController.getCurrentModeLabel()
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                if (!Build.IS_DEBUGGABLE) return@OnPreferenceChangeListener false
                val mode = ProdXMode.fromValue(newValue.toString().toIntOrNull() ?: 0)
                if (!modeController.hasAuthentication()) {
                    modeController.requestAuthentication(newAdminChallenge())
                    return@OnPreferenceChangeListener false
                }
                val changed = modeController.setMode(mode)
                if (changed) summary = mode.name.lowercase()
                changed
            }
        }
        findPreference<Preference>(KEY_HEALTH)?.summary = healthController.getHealthStatus()
        findPreference<Preference>(KEY_GENERATION)?.summary =
            healthController.getRegistryGeneration().toString()
        findPreference<Preference>(KEY_PROVIDERS)?.summary = adminController.getProviderCount().toString()
        findPreference<Preference>(KEY_GRANTS)?.summary = adminController.getGrantCount().toString()
        findPreference<Preference>(KEY_QUARANTINE)?.summary =
            adminController.getQuarantinedProviderCount().toString()
        findPreference<Preference>(KEY_HISTORY)?.summary = auditController.getSummary()
        findPreference<Preference>(KEY_EMERGENCY_DISABLE)?.setOnPreferenceClickListener {
            if (!modeController.hasAuthentication()) {
                modeController.requestAuthentication(newAdminChallenge())
            } else {
                modeController.emergencyDisable()
            }
        }
    }

    companion object {
        private const val KEY_MODE = "prodx_mode"
        private const val KEY_HEALTH = "prodx_health"
        private const val KEY_GENERATION = "prodx_registry_generation"
        private const val KEY_HISTORY = "prodx_audit_history"
        private const val KEY_PROVIDERS = "prodx_providers"
        private const val KEY_GRANTS = "prodx_grants"
        private const val KEY_QUARANTINE = "prodx_quarantine"
        private const val KEY_EMERGENCY_DISABLE = "prodx_emergency_disable"

        private fun newAdminChallenge(): ByteArray = ByteArray(32).also(SecureRandom()::nextBytes)
    }
}
