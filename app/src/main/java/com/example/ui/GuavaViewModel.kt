package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GuavaApiClient
import com.example.data.api.ModeRequest
import com.example.data.api.SettingsRequest
import com.example.data.api.StatusResponse
import com.example.data.api.WifiRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GuavaViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("guava_prefs", Context.MODE_PRIVATE)

    // Saved IP configuration
    private val _ipAddress = MutableStateFlow(sharedPrefs.getString("ip_address", "10.42.0.1") ?: "10.42.0.1")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _isSetupMode = MutableStateFlow(sharedPrefs.getBoolean("is_setup_mode", true))
    val isSetupMode: StateFlow<Boolean> = _isSetupMode.asStateFlow()

    // Simulation/Sandbox mode to test all flows fully if the real Pi isn't reachable
    private val _isSimulated = MutableStateFlow(sharedPrefs.getBoolean("is_simulated", false))
    val isSimulated: StateFlow<Boolean> = _isSimulated.asStateFlow()

    // Active screen selection (0: Connection, 1: Wi-Fi, 2: Mode, 3: Settings)
    private val _activeScreenId = MutableStateFlow(0)
    val activeScreenId: StateFlow<Int> = _activeScreenId.asStateFlow()

    // Loading & Network status
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // Real device state Cache (synced with GET /api/status)
    private val _deviceStatus = MutableStateFlow(StatusResponse())
    val deviceStatus: StateFlow<StatusResponse> = _deviceStatus.asStateFlow()

    // Local state for UI Form fields
    val wifiSsid = MutableStateFlow(sharedPrefs.getString("wifi_ssid", "Home_WiFi") ?: "Home_WiFi")
    val wifiPassword = MutableStateFlow(sharedPrefs.getString("wifi_password", "") ?: "")
    val assistantName = MutableStateFlow(sharedPrefs.getString("assistant_name", "Guava") ?: "Guava")
    val selectedLanguage = MutableStateFlow(sharedPrefs.getString("settings_language", "english") ?: "english")
    val voiceEnabled = MutableStateFlow(sharedPrefs.getBoolean("settings_voice", true))
    val notesEnabled = MutableStateFlow(sharedPrefs.getBoolean("settings_notes", true))

    init {
        // Load initial device state on launch
        fetchStatus()
    }

    fun getActiveBaseUrl(): String {
        return if (_isSetupMode.value) "10.42.0.1" else _ipAddress.value
    }

    fun setSetupMode(enable: Boolean) {
        _isSetupMode.value = enable
        sharedPrefs.edit().putBoolean("is_setup_mode", enable).apply()
        fetchStatus()
    }

    fun updateIpAddress(newIp: String) {
        _ipAddress.value = newIp
        sharedPrefs.edit().putString("ip_address", newIp).apply()
    }

    fun setSimulated(enable: Boolean) {
        _isSimulated.value = enable
        sharedPrefs.edit().putBoolean("is_simulated", enable).apply()
        if (enable) {
            _connectionStatus.value = "Sandbox Running"
            _deviceStatus.value = StatusResponse(
                wifiStatus = "connected",
                deviceIp = "192.168.1.135",
                ssid = wifiSsid.value.ifEmpty { "Guava_Guest" },
                mode = "guava",
                language = selectedLanguage.value,
                voiceEnabled = voiceEnabled.value,
                notesEnabled = notesEnabled.value,
                assistantName = assistantName.value
            )
        } else {
            fetchStatus()
        }
    }

    fun setScreen(id: Int) {
        _activeScreenId.value = id
    }

    fun fetchStatus() {
        if (_isSimulated.value) {
            _connectionStatus.value = "Sandbox Running"
            _deviceStatus.value = _deviceStatus.value.copy(
                assistantName = assistantName.value,
                language = selectedLanguage.value,
                voiceEnabled = voiceEnabled.value,
                notesEnabled = notesEnabled.value,
                ssid = wifiSsid.value
            )
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _lastError.value = null
            _connectionStatus.value = "Pinging http://${getActiveBaseUrl()}..."
            try {
                val api = GuavaApiClient.createService(getActiveBaseUrl())
                val res = api.getStatus()
                _deviceStatus.value = res
                _connectionStatus.value = "Guava Active"
                // Match fields to incoming status
                wifiSsid.value = res.ssid
                assistantName.value = res.assistantName
                selectedLanguage.value = res.language
                voiceEnabled.value = res.voiceEnabled
                notesEnabled.value = res.notesEnabled
            } catch (e: Exception) {
                _connectionStatus.value = "Connection Failed"
                _lastError.value = e.localizedMessage ?: "unreachable"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveWifi() {
        val ssid = wifiSsid.value
        val pwd = wifiPassword.value
        
        sharedPrefs.edit().putString("wifi_ssid", ssid).putString("wifi_password", pwd).apply()

        if (_isSimulated.value) {
            _deviceStatus.value = _deviceStatus.value.copy(
                ssid = ssid,
                wifiStatus = "connected"
            )
            _connectionStatus.value = "WiFi configured (Sim)"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _lastError.value = null
            try {
                val api = GuavaApiClient.createService(getActiveBaseUrl())
                val res = api.saveWifi(WifiRequest(ssid = ssid, password = pwd))
                _deviceStatus.value = res
                _connectionStatus.value = "WiFi Applied"
            } catch (e: Exception) {
                _lastError.value = e.localizedMessage ?: "Failed to save WiFi"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun testWifiConnection() {
        fetchStatus()
    }

    fun setMode(modeName: String) {
        if (_isSimulated.value) {
            _deviceStatus.value = _deviceStatus.value.copy(mode = modeName)
            _connectionStatus.value = "Mode changed: $modeName"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _lastError.value = null
            try {
                val api = GuavaApiClient.createService(getActiveBaseUrl())
                val res = api.setMode(ModeRequest(mode = modeName))
                _deviceStatus.value = res
                _connectionStatus.value = "Guava active mode is $modeName"
            } catch (e: Exception) {
                _lastError.value = e.localizedMessage ?: "Failed to set Mode"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSettings() {
        val name = assistantName.value
        val lang = selectedLanguage.value
        val voice = voiceEnabled.value
        val notes = notesEnabled.value

        sharedPrefs.edit()
            .putString("assistant_name", name)
            .putString("settings_language", lang)
            .putBoolean("settings_voice", voice)
            .putBoolean("settings_notes", notes)
            .apply()

        if (_isSimulated.value) {
            _deviceStatus.value = _deviceStatus.value.copy(
                assistantName = name,
                language = lang,
                voiceEnabled = voice,
                notesEnabled = notes
            )
            _connectionStatus.value = "Settings configured (Sim)"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _lastError.value = null
            try {
                val api = GuavaApiClient.createService(getActiveBaseUrl())
                val res = api.saveSettings(SettingsRequest(
                    language = lang,
                    voiceEnabled = voice,
                    notesEnabled = notes,
                    assistantName = name
                ))
                _deviceStatus.value = res
                _connectionStatus.value = "Settings applied successfully"
            } catch (e: Exception) {
                _lastError.value = e.localizedMessage ?: "Failed to save settings"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restartDevice() {
        if (_isSimulated.value) {
            _connectionStatus.value = "Rebooting (Simulated)..."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _lastError.value = null
            try {
                val api = GuavaApiClient.createService(getActiveBaseUrl())
                api.restartDevice()
                _connectionStatus.value = "Restart request sent"
            } catch (e: Exception) {
                // Connection drops during restart
                _connectionStatus.value = "Restart command fired successfully"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
