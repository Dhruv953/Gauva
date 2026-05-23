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
import com.example.data.api.SystemOverviewResponse
import com.example.data.api.SystemFolder
import com.example.data.api.SystemChild
import com.example.data.api.SystemModeDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class GuavaViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("guava_prefs", Context.MODE_PRIVATE)

    // Saved IP configuration
    private val _ipAddress = MutableStateFlow(sharedPrefs.getString("ip_address", "192.168.1.16") ?: "192.168.1.16")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _isSetupMode = MutableStateFlow(sharedPrefs.getBoolean("is_setup_mode", false))
    val isSetupMode: StateFlow<Boolean> = _isSetupMode.asStateFlow()

    // Simulation/Sandbox mode to test all flows fully if the real Pi isn't reachable
    private val _isSimulated = MutableStateFlow(sharedPrefs.getBoolean("is_simulated", true))
    val isSimulated: StateFlow<Boolean> = _isSimulated.asStateFlow()

    // Active screen selection (0: Home, 1: Modes, 2: System, 3: Settings)
    private val _activeScreenId = MutableStateFlow(0)
    val activeScreenId: StateFlow<Int> = _activeScreenId.asStateFlow()

    // Loading & Network status
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Sandbox Running")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // Real device state Cache (synced with GET /api/status)
    private val _deviceStatus = MutableStateFlow(StatusResponse())
    val deviceStatus: StateFlow<StatusResponse> = _deviceStatus.asStateFlow()

    // Advanced modular system overview cache
    private val _systemOverview = MutableStateFlow<SystemOverviewResponse?>(null)
    val systemOverview: StateFlow<SystemOverviewResponse?> = _systemOverview.asStateFlow()

    private val _systemOverviewError = MutableStateFlow<String?>(null)
    val systemOverviewError: StateFlow<String?> = _systemOverviewError.asStateFlow()

    // Local state for UI Form fields
    val wifiSsid = MutableStateFlow(sharedPrefs.getString("wifi_ssid", "Airtel_nikh_5001") ?: "Airtel_nikh_5001")
    val wifiPassword = MutableStateFlow(sharedPrefs.getString("wifi_password", "") ?: "")
    val assistantName = MutableStateFlow(sharedPrefs.getString("assistant_name", "Penguin") ?: "Penguin")
    val selectedLanguage = MutableStateFlow(sharedPrefs.getString("settings_language", "auto") ?: "auto")
    val voiceEnabled = MutableStateFlow(sharedPrefs.getBoolean("settings_voice", true))
    val notesEnabled = MutableStateFlow(sharedPrefs.getBoolean("settings_notes", true))

    init {
        // Initialize state
        if (_isSimulated.value) {
            setSimulated(true)
        } else {
            fetchStatus()
            fetchSystemOverview()
        }
    }

    fun getActiveBaseUrl(): String {
        return if (_isSetupMode.value) "10.42.0.1" else _ipAddress.value
    }

    fun setSetupMode(enable: Boolean) {
        _isSetupMode.value = enable
        sharedPrefs.edit().putBoolean("is_setup_mode", enable).apply()
        fetchStatus()
        fetchSystemOverview()
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
                deviceIp = "192.168.1.16",
                ssid = wifiSsid.value.ifEmpty { "Airtel_nikh_5001" },
                mode = _deviceStatus.value.mode, // retain current mode selection in sim
                language = selectedLanguage.value.ifEmpty { "auto" },
                voiceEnabled = voiceEnabled.value,
                notesEnabled = notesEnabled.value,
                assistantName = assistantName.value.ifEmpty { "Penguin" },
                voiceEngine = "piper",
                listenWhileProcessing = true,
                queueLimit = 20,
                voiceName = "Ryan low",
                voiceGender = "male",
                piperModel = "/home/dhruvpatel/guava_voices/piper/en_US-ryan-low.onnx",
                silenceSeconds = 0.85,
                minUtteranceSeconds = 0.35,
                maxUtteranceSeconds = 18.0
            )
            _systemOverview.value = getMockSystemOverview()
            _systemOverviewError.value = null
        } else {
            fetchStatus()
            fetchSystemOverview()
        }
    }

    fun setScreen(id: Int) {
        _activeScreenId.value = id
        if (id == 2) { // System tab: fetch visual structural live overview
            fetchSystemOverview()
        }
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

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _lastError.value = null
            _connectionStatus.value = "Pinging http://${getActiveBaseUrl()}..."
            try {
                val api = GuavaApiClient.createService(getActiveBaseUrl())
                val res = api.getStatus()
                _deviceStatus.value = res
                _connectionStatus.value = "Pi Online"
                // Match fields to incoming status
                wifiSsid.value = res.ssid
                assistantName.value = res.assistantName
                selectedLanguage.value = res.language
                voiceEnabled.value = res.voiceEnabled
                notesEnabled.value = res.notesEnabled
            } catch (e: Throwable) {
                _connectionStatus.value = "Offline"
                _lastError.value = e.localizedMessage ?: e.toString()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchSystemOverview() {
        if (_isSimulated.value) {
            _systemOverview.value = getMockSystemOverview()
            _systemOverviewError.value = null
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _systemOverviewError.value = null
            try {
                val api = GuavaApiClient.createService(getActiveBaseUrl())
                val res = api.getSystemOverview()
                _systemOverview.value = res
            } catch (e: Throwable) {
                _systemOverview.value = null
                _systemOverviewError.value = e.localizedMessage ?: e.toString()
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
            _connectionStatus.value = "WiFi Applied (Sim)"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _lastError.value = null
            try {
                val api = GuavaApiClient.createService(getActiveBaseUrl())
                val res = api.saveWifi(WifiRequest(ssid = ssid, password = pwd))
                _deviceStatus.value = res
                _connectionStatus.value = "WiFi Applied"
            } catch (e: Throwable) {
                _lastError.value = e.localizedMessage ?: e.toString()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setMode(modeName: String) {
        if (_isSimulated.value) {
            _deviceStatus.value = _deviceStatus.value.copy(mode = modeName)
            _connectionStatus.value = "Active Mode: $modeName"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _lastError.value = null
            try {
                val api = GuavaApiClient.createService(getActiveBaseUrl())
                val res = api.setMode(ModeRequest(mode = modeName))
                _deviceStatus.value = res
                _connectionStatus.value = "Active Mode: $modeName"
            } catch (e: Throwable) {
                _lastError.value = e.localizedMessage ?: e.toString()
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
            _connectionStatus.value = "Settings Saved (Sim)"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
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
                _connectionStatus.value = "Settings Saved"
            } catch (e: Throwable) {
                _lastError.value = e.localizedMessage ?: e.toString()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restartDevice() {
        if (_isSimulated.value) {
            _connectionStatus.value = "Rebooting Pi (Simulated)"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _lastError.value = null
            try {
                val api = GuavaApiClient.createService(getActiveBaseUrl())
                api.restartDevice()
                _connectionStatus.value = "Pi Rebooting"
            } catch (e: Throwable) {
                // Connection drops during restart
                _connectionStatus.value = "Pi Rebooting..."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMockSystemOverview(): SystemOverviewResponse {
        return SystemOverviewResponse(
            root = "/ai-system",
            folders = listOf(
                SystemFolder(
                    name = "projects",
                    path = "/ai-system/projects",
                    description = "User projects and generated apps",
                    children = emptyList()
                ),
                SystemFolder(
                    name = "agents",
                    path = "/ai-system/agents",
                    description = "Planner, coding, executor, and safety agents",
                    children = listOf(
                        SystemChild(name = "router", path = "/ai-system/agents/router"),
                        SystemChild(name = "safety", path = "/ai-system/agents/safety")
                    )
                ),
                SystemFolder(
                    name = "memory",
                    path = "/ai-system/memory",
                    description = "Persistent memory and conversations",
                    children = listOf(
                        SystemChild(name = "conversations", path = "/ai-system/memory/conversations"),
                        SystemChild(name = "preferences", path = "/ai-system/memory/preferences")
                    )
                ),
                SystemFolder(
                    name = "prompts",
                    path = "/ai-system/prompts",
                    description = "Mode prompts for Penguin, Halo, and Guava",
                    children = emptyList()
                ),
                SystemFolder(
                    name = "configs",
                    path = "/ai-system/configs",
                    description = "Runtime and system configs",
                    children = emptyList()
                ),
                SystemFolder(
                    name = "deployments",
                    path = "/ai-system/deployments",
                    description = "Deployment notes and runtime patches",
                    children = emptyList()
                ),
                SystemFolder(
                    name = "logs",
                    path = "/ai-system/logs",
                    description = "System and assistant logs",
                    children = emptyList()
                )
            ),
            modes = mapOf(
                "penguin" to SystemModeDetails(
                    role = "Personal buddy",
                    memory = "/ai-system/memory/conversations/penguin.jsonl",
                    prompt = "/ai-system/prompts/penguin.md"
                ),
                "halo" to SystemModeDetails(
                    role = "Work buddy",
                    memory = "/ai-system/memory/conversations/halo.jsonl",
                    prompt = "/ai-system/prompts/halo.md"
                ),
                "guava" to SystemModeDetails(
                    role = "Router/tool layer",
                    memory = "/ai-system/memory/conversations/guava.jsonl",
                    prompt = "/ai-system/prompts/router.md"
                )
            )
        )
    }
}
