package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.StatusResponse
import com.example.ui.GuavaViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarmGreen
import com.example.ui.theme.WarmGreenVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.BlackBackground

class MainActivity : ComponentActivity() {
    private val viewModel: GuavaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black
                ) { innerPadding ->
                    GuavaSetupApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GuavaSetupApp(
    viewModel: GuavaViewModel,
    modifier: Modifier = Modifier
) {
    val activeScreenId by viewModel.activeScreenId.collectAsState()
    val isSimulated by viewModel.isSimulated.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lastError by viewModel.lastError.collectAsState()
    val deviceStatus by viewModel.deviceStatus.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        // --- 1. APP HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Guava",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    color = WarmGreen,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "Device Configuration • v1.0.4",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    color = TextSecondary,
                    letterSpacing = 0.2.sp
                )
            }

            // Connection Status Badge with Glowing Accent
            Box(
                modifier = Modifier
                    .background(
                        color = when {
                            isSimulated -> Color(0x33C1FF72)
                            connectionStatus.contains("Active") || connectionStatus.contains("Connected") -> Color(0x33C1FF72)
                            connectionStatus.contains("Pinging") || connectionStatus.contains("Connecting") -> Color(0x33FFB020)
                            else -> Color(0x33FF453A)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        BorderStroke(
                            width = 1.dp,
                            color = when {
                                isSimulated -> WarmGreen.copy(alpha = 0.5f)
                                connectionStatus.contains("Active") || connectionStatus.contains("Connected") -> WarmGreen.copy(alpha = 0.5f)
                                connectionStatus.contains("Pinging") || connectionStatus.contains("Connecting") -> Color(0xFFFFB020).copy(alpha = 0.5f)
                                else -> ErrorRed.copy(alpha = 0.5f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when {
                                    isSimulated -> WarmGreen
                                    connectionStatus.contains("Active") || connectionStatus.contains("Connected") -> WarmGreen
                                    connectionStatus.contains("Pinging") || connectionStatus.contains("Connecting") -> Color(0xFFFFB020)
                                    else -> ErrorRed
                                },
                                shape = RoundedCornerShape(50)
                            )
                    )
                    Text(
                        text = if (isSimulated) "SANDBOX" else connectionStatus.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Sandbox Warning Banner (As a sleek minimalist warning slip)
        if (isSimulated) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .background(Color(0x1AC1FF72), RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, WarmGreen.copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Sandbox Info",
                        tint = WarmGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Sandbox Mode Active • Devices locally simulated.",
                        fontSize = 12.sp,
                        color = TextPrimary.copy(alpha = 0.8f),
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- 2. STEP/TAB SWITCHER (Pill-Styled Bento Controls) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .background(SurfaceCard, RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, Color(0x0FFFFFFF)), RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    Pair(0, "IP LINK"),
                    Pair(1, "WI-FI"),
                    Pair(2, "MODE"),
                    Pair(3, "SETTINGS")
                )

                tabs.forEach { (id, label) ->
                    val active = activeScreenId == id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(
                                color = if (active) WarmGreen else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.setScreen(id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = if (active) Color(0xFF0D0D0D) else TextPrimary.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Loading stream indicator lines
        if (isLoading) {
            LinearProgressIndicator(
                color = WarmGreen,
                trackColor = Color(0xFF111111),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // --- 4. MAIN SCROLLABLE CONTAINER ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            AnimatedContent(
                targetState = activeScreenId,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_navigation"
            ) { targetScreenId ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    when (targetScreenId) {
                        0 -> ConnectionScreen(viewModel, isSimulated, lastError)
                        1 -> WifiSetupScreen(viewModel, deviceStatus)
                        2 -> ModeScreen(viewModel, deviceStatus)
                        3 -> SettingsScreen(viewModel, deviceStatus)
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: CONNECTION SCREEN
// ==========================================
@Composable
fun ConnectionScreen(
    viewModel: GuavaViewModel,
    isSimulated: Boolean,
    lastError: String?
) {
    val isSetupMode by viewModel.isSetupMode.collectAsState()
    val ipAddress by viewModel.ipAddress.collectAsState()

    // Title Bento Item
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "1. DEFINE TARGET IP",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = WarmGreen,
            letterSpacing = 1.sp
        )
        Text(
            text = "Select connection state to begin configure commands. If Guava is fresh out of box, standard setup mode uses direct IP.",
            fontSize = 13.sp,
            color = TextPrimary.copy(alpha = 0.6f),
            lineHeight = 18.sp
        )
    }

    // Tiled Bento Options
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Setup Mode (10.42.0.1) Bento block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setSetupMode(true) },
            colors = CardDefaults.cardColors(
                containerColor = SurfaceCard
            ),
            border = BorderStroke(
                width = if (isSetupMode) 2.dp else 1.dp,
                color = if (isSetupMode) WarmGreen.copy(alpha = 0.5f) else Color(0x0FFFFFFF)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RadioButton(
                    selected = isSetupMode,
                    onClick = { viewModel.setSetupMode(true) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = WarmGreen,
                        unselectedColor = TextSecondary
                    )
                )
                Column {
                    Text(
                        text = "Direct Setup Mode",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Connect to default fallback: http://10.42.0.1",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Manual IP Mode Bento Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setSetupMode(false) },
            colors = CardDefaults.cardColors(
                containerColor = SurfaceCard
            ),
            border = BorderStroke(
                width = if (!isSetupMode) 2.dp else 1.dp,
                color = if (!isSetupMode) WarmGreen.copy(alpha = 0.5f) else Color(0x0FFFFFFF)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RadioButton(
                    selected = !isSetupMode,
                    onClick = { viewModel.setSetupMode(false) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = WarmGreen,
                        unselectedColor = TextSecondary
                    )
                )
                Column {
                    Text(
                        text = "Manual Network Mode",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Enter direct IP of your Guava on local WiFi",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }

    // Manual Custom IP input drawer
    if (!isSetupMode) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0x0FFFFFFF))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "GUAVA DEVICE IP ADDRESS",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { viewModel.updateIpAddress(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("device_ip_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = WarmGreen,
                        unfocusedBorderColor = Color(0x19FFFFFF),
                        cursorColor = WarmGreen
                    )
                )
            }
        }
    }

    // Main action button as pill capsule
    Button(
        onClick = { viewModel.fetchStatus() },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("find_guava_btn"),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = WarmGreen,
            contentColor = Color(0xFF0D0D0D)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon",
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "FIND GUAVA",
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }

    // Diagnostics / Error Bento Banner
    if (lastError != null && !isSimulated) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, ErrorRed),
            colors = CardDefaults.cardColors(containerColor = Color(0x1AFF453A))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(ErrorRed, RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = "DIAGNOSTIC ERROR",
                        color = ErrorRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Could not ping hardware: $lastError\n\nEnsure your smartphone is connected directly to Guava WiFi Access Point before pairing, or verify the IP address.",
                    color = TextPrimary.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }

    // Simulation Config Controller Bento Cell
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SANDBOX SIMULATION",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = WarmGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Simulate active server responses to run the app fully without physical hardware.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isSimulated,
                onCheckedChange = { viewModel.setSimulated(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF0D0D0D),
                    checkedTrackColor = WarmGreen,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }

    // System activity wave monitor at the base
    SystemActivityVisualizer()
}

// ==========================================
// SCREEN 2: WIFI SETUP SCREEN
// ==========================================
@Composable
fun WifiSetupScreen(
    viewModel: GuavaViewModel,
    deviceStatus: StatusResponse
) {
    val wifiSsid by viewModel.wifiSsid.collectAsState()
    val wifiPassword by viewModel.wifiPassword.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "2. WI-FI CONFIGURATION",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = WarmGreen,
            letterSpacing = 1.sp
        )
        Text(
            text = "Submit the SSID and passphrase. The device will test local linkage, reconnect, and establish communication.",
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }

    // Current WiFi Status Bento card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "ACTIVE WIFI LINKAGE STATUS",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "WiFi status icon",
                        tint = if (deviceStatus.wifiStatus == "connected") WarmGreen else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (deviceStatus.ssid.isNotEmpty()) deviceStatus.ssid else "No wifi configured",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (deviceStatus.wifiStatus == "connected") Color(0x1AC1FF72) else Color(0x1AFF453A),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = deviceStatus.wifiStatus.uppercase(),
                        color = if (deviceStatus.wifiStatus == "connected") WarmGreen else ErrorRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    // Unified Bento Input Block
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SSID Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "WIFI SSID",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                OutlinedTextField(
                    value = wifiSsid,
                    onValueChange = { viewModel.wifiSsid.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wifi_ssid_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = WarmGreen,
                        unfocusedBorderColor = Color(0x19FFFFFF),
                        cursorColor = WarmGreen
                    ),
                    placeholder = { Text("Search or input network name", color = Color.DarkGray, fontSize = 14.sp) }
                )
            }

            // Password Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "WIFI PASSWORD",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                OutlinedTextField(
                    value = wifiPassword,
                    onValueChange = { viewModel.wifiPassword.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wifi_password_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp
                    ),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = WarmGreen,
                        unfocusedBorderColor = Color(0x19FFFFFF),
                        cursorColor = WarmGreen
                    ),
                    placeholder = { Text("Leave empty to keep current password", color = Color.DarkGray, fontSize = 14.sp) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Info else Icons.Default.Lock,
                                contentDescription = "Toggle password visibility",
                                tint = TextSecondary
                            )
                        }
                    }
                )
            }
        }
    }

    // Setup action buttons row (High-comfort capsules)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { viewModel.saveWifi() },
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .testTag("save_wifi_btn"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WarmGreen,
                contentColor = Color(0xFF0D0D0D)
            )
        ) {
            Text(
                text = "SAVE WIFI",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        OutlinedButton(
            onClick = { viewModel.testWifiConnection() },
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .testTag("test_wifi_btn"),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, WarmGreen),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = WarmGreen
            )
        ) {
            Text(
                text = "TEST LINK",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Restart process card in deep slate error background
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0x33FF453A)),
        colors = CardDefaults.cardColors(containerColor = Color(0x16FF453A))
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "RELOAD POWER CYCLE",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ErrorRed,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Trigger command to hot-reboot Guava hardware system directly.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { viewModel.restartDevice() },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("restart_guava_btn")
            ) {
                Text(
                    text = "RESTART",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ==========================================
// SCREEN 3: ACTIVE MODE SCREEN (Real Bento Columns)
// ==========================================
@Composable
fun ModeScreen(
    viewModel: GuavaViewModel,
    deviceStatus: StatusResponse
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "3. SELECT ACTIVE MODE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = WarmGreen,
            letterSpacing = 1.sp
        )
        Text(
            text = "Choose the operational persona for your local Pi assistant. Activating transitions the device system logic instantly.",
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }

    val currentModeSelection = deviceStatus.mode.lowercase()

    // 2-Column Bento Grid Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            ModeCard(
                title = "Guava",
                description = "Personal chat buddy. Speaks/listens in HI / GU / EN.",
                isActive = currentModeSelection == "guava",
                onClick = { viewModel.setMode("guava") },
                testTag = "mode_guava_card"
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            ModeCard(
                title = "Halo",
                description = "Work buddy for office notes, tasks, and productivity.",
                isActive = currentModeSelection == "halo",
                onClick = { viewModel.setMode("halo") },
                testTag = "mode_halo_card"
            )
        }
    }

    // Third Settings mode item span across full width
    ModeCard(
        title = "Settings",
        description = "Device and assistant configuration settings setup tunnel.",
        isActive = currentModeSelection == "settings",
        onClick = { viewModel.setScreen(3) }, // Switches view to Settings
        testTag = "mode_settings_card"
    )

    // Current State Readout Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Active status info",
                tint = WarmGreen,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Active system logic: ${deviceStatus.mode.uppercase()}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    description: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceCard
        ),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) WarmGreen.copy(alpha = 0.5f) else Color(0x0FFFFFFF)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isActive) WarmGreen else Color.White
                )

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(WarmGreen, RoundedCornerShape(4.dp))
                    )
                }
            }

            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = if (isActive) TextPrimary else TextSecondary
            )
        }
    }
}

// ==========================================
// SCREEN 4: SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(
    viewModel: GuavaViewModel,
    deviceStatus: StatusResponse
) {
    val assistantName by viewModel.assistantName.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val voiceEnabled by viewModel.voiceEnabled.collectAsState()
    val notesEnabled by viewModel.notesEnabled.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "4. SYSTEM & PERSONAL SETTINGS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = WarmGreen,
            letterSpacing = 1.sp
        )
        Text(
            text = "Tune the physical assistant properties, audio defaults, feature toggles, and metadata flags directly.",
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }

    // Language Grid Selection Inside Bento Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SPEECH LANGUAGE SELECT",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            val languages = listOf("english", "hindi", "gujarati", "auto")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                languages.forEach { lang ->
                    val selected = selectedLanguage.lowercase() == lang
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(
                                color = if (selected) WarmGreen else SurfaceDark,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (selected) Color.Transparent else Color(0x19FFFFFF),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.selectedLanguage.value = lang }
                            .testTag("lang_${lang}_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lang.uppercase(),
                            color = if (selected) Color(0xFF0D0D0D) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }

    // Unified Toggle Switches Bento Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Row 1: Voice Enabled
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "VOICE ENGINE OUTPUT",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Enable speaker speech rendering",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = voiceEnabled,
                    onCheckedChange = { viewModel.voiceEnabled.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0D0D0D),
                        checkedTrackColor = WarmGreen
                    ),
                    modifier = Modifier.testTag("voice_enabled_switch")
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color(0x0FFFFFFF)
            )

            // Row 2: Diary Notes Enabled
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "PRODUCTIVITY DIARY NOTES",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Record compiled work activity digests",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = notesEnabled,
                    onCheckedChange = { viewModel.notesEnabled.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0D0D0D),
                        checkedTrackColor = WarmGreen
                    ),
                    modifier = Modifier.testTag("notes_enabled_switch")
                )
            }
        }
    }

    // Broadcast Name Input inside Bento Box
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ASSISTANT BROADCAST NAME",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            OutlinedTextField(
                value = assistantName,
                onValueChange = { viewModel.assistantName.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assistant_name_input"),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = WarmGreen,
                    unfocusedBorderColor = Color(0x19FFFFFF),
                    cursorColor = WarmGreen
                )
            )
        }
    }

    // Action Capsule Button
    Button(
        onClick = { viewModel.saveSettings() },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("save_settings_btn"),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = WarmGreen,
            contentColor = Color(0xFF0D0D0D)
        )
    ) {
        Text(
            text = "SAVE DEVICE SETTINGS",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    }

    // Diagnostics System profile list Bento Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "HARDWARE DEVICE PROFILE STATS",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "WiFi Network Integration", fontSize = 12.sp, color = TextSecondary)
                Text(
                    text = if (deviceStatus.ssid.isNotEmpty()) deviceStatus.ssid else "disconnected",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }

            HorizontalDivider(color = Color(0x0FFFFFFF))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Primary Host URL/IP", fontSize = 12.sp, color = TextSecondary)
                Text(
                    text = "http://${viewModel.getActiveBaseUrl()}",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }

            HorizontalDivider(color = Color(0x0FFFFFFF))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Active Process Persona", fontSize = 12.sp, color = TextSecondary)
                Text(
                    text = deviceStatus.mode.uppercase(),
                    fontSize = 12.sp,
                    color = WarmGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Reboot process action capsule inside settings
    OutlinedButton(
        onClick = { viewModel.restartDevice() },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("settings_restart_btn"),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, ErrorRed),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = ErrorRed
        )
    ) {
        Text(
            text = "REBOOT PROCESS ENGINE",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// Minimalist premium mock system spectrum analyzer chart
@Composable
fun SystemActivityVisualizer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x08C1FF72)),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "SYSTEM ACTIVITY SENSOR",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = WarmGreen,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val heights = listOf(0.4f, 0.6f, 0.3f, 1.0f, 0.5f, 0.8f, 0.4f, 0.7f, 0.5f, 0.9f, 0.3f, 0.6f)
                heights.forEach { heightWeight ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(heightWeight)
                            .background(
                                color = WarmGreen.copy(alpha = heightWeight.coerceIn(0.2f, 1.0f)),
                                shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                            )
                    )
                }
            }
        }
    }
}

