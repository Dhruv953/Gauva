package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.StatusResponse
import com.example.data.api.SystemFolder
import com.example.data.api.SystemChild
import com.example.ui.GuavaViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SoftYellow
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ErrorRed

class MainActivity : ComponentActivity() {
    private val viewModel: GuavaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BlackBackground
                ) { innerPadding ->
                    PenguinControlApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PenguinControlApp(
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
        // --- 1. PREMIUM HEADER BAR WITH MINIMAL PENGUIN AVATAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Minimalist cute penguin drawing
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(SurfaceCard, CircleShape)
                        .border(1.dp, SoftYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E2433), CircleShape)
                    ) {
                        // Drawing using nested blocks (cute white belly, glowing yellow beak)
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .align(Alignment.BottomCenter)
                                .background(Color.White, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(3.dp).background(MintGreen, CircleShape))
                            Box(modifier = Modifier.size(3.dp).background(MintGreen, CircleShape))
                        }
                        // Beak
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .align(Alignment.Center)
                                .offset(y = 2.dp)
                                .background(SoftYellow, RoundedCornerShape(2.dp))
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "Penguin Control",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Pi AI Assistant Host Manager",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            // Connection Status Glowing Badge
            Box(
                modifier = Modifier
                    .background(
                        color = when {
                            isSimulated -> MintGreenVariant
                            connectionStatus.contains("Online") || connectionStatus.contains("Active") -> MintGreenVariant
                            connectionStatus.contains("Pinging") || connectionStatus.contains("Connecting") -> Color(0x26FFE082)
                            else -> Color(0x26F87171)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        BorderStroke(
                            width = 1.dp,
                            color = when {
                                isSimulated -> MintGreen.copy(alpha = 0.5f)
                                connectionStatus.contains("Online") || connectionStatus.contains("Active") -> MintGreen.copy(alpha = 0.5f)
                                connectionStatus.contains("Pinging") || connectionStatus.contains("Connecting") -> SoftYellow.copy(alpha = 0.5f)
                                else -> ErrorRed.copy(alpha = 0.5f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.fetchStatus() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = when {
                                    isSimulated -> MintGreen
                                    connectionStatus.contains("Online") || connectionStatus.contains("Active") -> MintGreen
                                    connectionStatus.contains("Pinging") || connectionStatus.contains("Connecting") -> SoftYellow
                                    else -> ErrorRed
                                },
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (isSimulated) "SANDBOX" else connectionStatus.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Sim Warning Alert Bar
        if (isSimulated) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .background(MintGreenVariant, RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, MintGreen.copy(alpha = 0.2f)), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Sandbox Info",
                        tint = MintGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Demo Sandbox Enabled - Simulated responses active.",
                        fontSize = 11.sp,
                        color = TextPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Network Activity Line Loop
        if (isLoading) {
            LinearProgressIndicator(
                color = SoftYellow,
                trackColor = Color(0xFF161E2E),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }

        // --- 2. BENTO MAIN NAVIGATION (CAPSULE TABS) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(SurfaceDark, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    Triple(0, "HOME", Icons.Default.Home),
                    Triple(1, "MODES", Icons.Default.Menu),
                    Triple(2, "SYSTEM", Icons.Default.List),
                    Triple(3, "SETTINGS", Icons.Default.Settings)
                )

                tabs.forEach { (id, label, icon) ->
                    val active = activeScreenId == id
                    val activeColor = if (id == 0 || id == 3) SoftYellow else MintGreen

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .background(
                                color = if (active) activeColor else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setScreen(id) }
                            .testTag("nav_tab_$id"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(16.dp),
                                tint = if (active) Color(0xFF0C0E12) else TextSecondary
                            )
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (active) Color(0xFF0C0E12) else TextPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. ACTIVE VIEW CONTAINER (SCROLLABLE BENTO CONTENT) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            AnimatedContent(
                targetState = activeScreenId,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "penguin_view"
            ) { targetScreenId ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (targetScreenId) {
                        0 -> PenguinHomeScreen(viewModel, deviceStatus)
                        1 -> PenguinModesScreen(viewModel, deviceStatus)
                        2 -> PenguinSystemScreen(viewModel)
                        3 -> PenguinSettingsScreen(viewModel, deviceStatus)
                    }
                }
            }
        }
    }
}

// Helper extension to make sizing consistent
private fun Modifier.size(size: Int) = this.size(size.dp)

// ==========================================
// SCREEN 0: PENGUIN HOME SCREEN
// ==========================================
@Composable
fun PenguinHomeScreen(
    viewModel: GuavaViewModel,
    status: StatusResponse
) {
    val isSimulated by viewModel.isSimulated.collectAsState()
    val lastError by viewModel.lastError.collectAsState()

    // Title Section
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "OVERVIEW",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SoftYellow,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Always-on Assistant Status",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    // Diagnostics if API error occurred and not in simulation
    if (lastError != null && !isSimulated) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x26F87171)),
            border = BorderStroke(1.dp, ErrorRed)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Pi Connection Error",
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$lastError. Ensure your phone can access http://${viewModel.getActiveBaseUrl()} or toggle simulated Sandbox in settings.",
                        color = TextPrimary.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }

    // Live Metrics Bento Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // IP Address & SSID Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, Color(0x19FFFFFF))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "WiFi",
                    tint = SoftYellow,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "HARDWARE PATH",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (status.deviceIp == "unknown") "Offline" else status.deviceIp,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "SSID: ${status.ssid.ifEmpty { "Disconnected" }}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Active Voice Model Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, Color(0x19FFFFFF))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Voice",
                    tint = MintGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "VOICE ENGINE",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = status.voiceName.ifEmpty { "Ryan low" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Engine: ${status.voiceEngine.uppercase()}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Detailed Hardware Integration Checklists Bento Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0x19FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "COGNITIVE DIALS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )

            // Voice Engine
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(SoftYellow, CircleShape))
                    Text(text = "Assistant Broadcast Name", fontSize = 12.sp, color = TextPrimary)
                }
                Text(
                    text = status.assistantName,
                    fontSize = 12.sp,
                    color = SoftYellow,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Voice enabled
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(MintGreen, CircleShape))
                    Text(text = "Voice Synthesis Audio", fontSize = 12.sp, color = TextPrimary)
                }
                Text(
                    text = if (status.voiceEnabled) "Vocal replies: ON" else "OFF",
                    fontSize = 12.sp,
                    color = if (status.voiceEnabled) MintGreen else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Always listening while processing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(MintGreen, CircleShape))
                    Text(text = "Listen While Processing", fontSize = 12.sp, color = TextPrimary)
                }
                Text(
                    text = if (status.listenWhileProcessing) "ACTIVE" else "DISABLED",
                    fontSize = 12.sp,
                    color = if (status.listenWhileProcessing) MintGreen else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Diary notes compilation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(SoftYellow, CircleShape))
                    Text(text = "Diaries & Action Digests", fontSize = 12.sp, color = TextPrimary)
                }
                Text(
                    text = if (status.notesEnabled) "Job summaries: ON" else "OFF",
                    fontSize = 12.sp,
                    color = if (status.notesEnabled) SoftYellow else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }

    // --- QUICK MODE SELECT SWITCHES ---
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "QUICK MODE INTERFLOW",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = SoftYellow,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Tap to switch the active AI persona instantly.",
            fontSize = 12.sp,
            color = TextSecondary
        )
    }

    val modes = listOf(
        Triple("penguin", "Penguin", "Personal buddy mode. Chat, casual discussion, voice reply, and system memories."),
        Triple("halo", "Halo", "Work buddy mode. Meeting minutes, task summaries, digests, and action lists."),
        Triple("guava", "Guava", "Router/tool mode. Routes commands, links tools (Spotify, calendar, alarms).")
    )

    modes.forEach { (modeId, modeName, modeDesc) ->
        val isActive = status.mode.lowercase() == modeId
        val borderFlash = if (isActive) BorderStroke(1.5.dp, SoftYellow) else BorderStroke(1.dp, Color(0x0FFFFFFF))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setMode(modeId) }
                .testTag("quick_mode_$modeId"),
            colors = CardDefaults.cardColors(containerColor = if (isActive) Color(0xFF1E2433) else SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = borderFlash
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (isActive) SoftYellow else SurfaceDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (modeId) {
                            "penguin" -> Icons.Default.Face
                            "halo" -> Icons.Default.CheckCircle
                            else -> Icons.Default.Build
                        },
                        contentDescription = modeName,
                        tint = if (isActive) Color(0xFF0C0E12) else SoftYellow,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = modeName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .background(MintGreenVariant, RoundedCornerShape(6.dp))
                                    .border(BorderStroke(0.5.dp, MintGreen), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MintGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                    Text(
                        text = modeDesc,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }

    // Refresh Live Status Button at bottom of screen
    Button(
        onClick = { viewModel.fetchStatus() },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("home_refresh_btn"),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceDark,
            contentColor = SoftYellow
        ),
        border = BorderStroke(1.dp, SoftYellow.copy(alpha = 0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Sync",
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "SYNCHRONIZE STATUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ==========================================
// SCREEN 1: PENGUIN MODES SCREEN
// ==========================================
@Composable
fun PenguinModesScreen(
    viewModel: GuavaViewModel,
    status: StatusResponse
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "PERSONALS & BEHAVIORS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MintGreen,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Active AI Mode Protocols",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    val activeMode = status.mode.lowercase()

    // 1. PENGUIN CARD
    ModeVisualCard(
        title = "Penguin Protocol",
        badge = "Personal Buddy",
        description = "Optimized for companionable discussions, vocal replies, voice chat and cognitive contextual retrieval.",
        characteristics = listOf(
            "Natural spoken conversation flow",
            "Personalized user memories & interests tracking",
            "Sleek and friendly companion replies",
            "Automatic linguistic matching (HI/GU/EN)"
        ),
        isActive = activeMode == "penguin",
        colorAccent = SoftYellow,
        icon = Icons.Default.Face,
        onClick = { viewModel.setMode("penguin") },
        testTag = "mode_protocol_penguin"
    )

    // 2. HALO CARD
    ModeVisualCard(
        title = "Halo Protocol",
        badge = "Work Partner",
        description = "Optimized for corporate office tracking, fast transcript summarizations, meeting note logs, and task parsing.",
        characteristics = listOf(
            "Automated meeting summary generation",
            "Digests structured task items recursively",
            "Clean workspace task tracker creation",
            "Highly professional bulleted notes style"
        ),
        isActive = activeMode == "halo",
        colorAccent = MintGreen,
        icon = Icons.Default.CheckCircle,
        onClick = { viewModel.setMode("halo") },
        testTag = "mode_protocol_halo"
    )

    // 3. GUAVA CARD
    ModeVisualCard(
        title = "Guava Protocol",
        badge = "System Router",
        description = "Optimized as a tool gateway and system router, handling command filtration and future home automation script loops.",
        characteristics = listOf(
            "Flexible smart home integrations router",
            "Triggers media streams on Spotify or local Pi",
            "Forwards requests directly to Penguin or Halo",
            "Handles background cron automated triggers"
        ),
        isActive = activeMode == "guava",
        colorAccent = TextSecondary,
        icon = Icons.Default.Build,
        onClick = { viewModel.setMode("guava") },
        testTag = "mode_protocol_guava"
    )
}

@Composable
fun ModeVisualCard(
    title: String,
    badge: String,
    description: String,
    characteristics: List<String>,
    isActive: Boolean,
    colorAccent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val outlineBorder = if (isActive) BorderStroke(2.dp, colorAccent) else BorderStroke(1.dp, Color(0x15FFFFFF))
    val bg = if (isActive) SurfaceDark else SurfaceCard

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = outlineBorder
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(colorAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colorAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            color = colorAccent,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .background(colorAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .border(BorderStroke(0.5.dp, colorAccent), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE PRIMARY",
                            fontSize = 8.sp,
                            color = colorAccent,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Text(
                text = description,
                fontSize = 12.sp,
                color = TextPrimary.copy(alpha = 0.9f),
                lineHeight = 16.sp
            )

            // Characteristics List Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlackBackground.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                characteristics.forEach { trait ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "checked",
                            tint = MintGreen,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = trait,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: PENGUIN SYSTEM SCREEN (Visual file explorer)
// ==========================================
@Composable
fun PenguinSystemScreen(
    viewModel: GuavaViewModel
) {
    val systemOverview by viewModel.systemOverview.collectAsState()
    val systemError by viewModel.systemOverviewError.collectAsState()
    val isSimulated by viewModel.isSimulated.collectAsState()

    // Title Section
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "PI SYSTEM MAP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MintGreen,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Directory structure",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            IconButton(
                onClick = { viewModel.fetchSystemOverview() },
                modifier = Modifier.testTag("system_refresh_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh live folder overview",
                    tint = MintGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // State expanded Map track
    val expandedFoldersMap = remember { mutableStateMapOf<String, Boolean>() }
    var selectedPathByTap by remember { mutableStateOf<String?>(null) }

    if (systemOverview == null) {
        // Fallback or Loader or Retry
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, Color(0x19FFFFFF))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Offline Filesystem Map",
                    tint = SoftYellow,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "No System Connection",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (systemError != null) "Reason: $systemError" else "The system endpoint is currently unreachable. Configure correct Pi IP link in Settings or use Simulated Sandbox.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.fetchSystemOverview() },
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = Color(0xFF0C0E12)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "RETRY LINK", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    OutlinedButton(
                        onClick = { viewModel.setSimulated(true) },
                        border = BorderStroke(1.dp, SoftYellow),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftYellow),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "LOAD SANDBOX", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    } else {
        val overview = systemOverview!!

        // High visual detail indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, MintGreen.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ACTIVE SERVER FILESYSTEM CONTAINER",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MintGreen
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Host Dir Root: ${overview.root}", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .background(MintGreenVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "JSON SECURE", color = MintGreen, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Expanded tapped path footer/bar
        if (selectedPathByTap != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, SoftYellow.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "SELECTED CLOUD PATH", fontSize = 8.sp, color = SoftYellow, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(
                            text = selectedPathByTap!!,
                            fontSize = 11.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { selectedPathByTap = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "dismiss", tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Tree Viewer Block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(20.dp))
                .border(BorderStroke(1.dp, Color(0x19FFFFFF)), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            overview.folders.forEach { folder ->
                val isExpanded = expandedFoldersMap[folder.name] ?: false
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Folder Node
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedFoldersMap[folder.name] = !isExpanded
                                selectedPathByTap = folder.path
                            }
                            .padding(vertical = 6.dp)
                            .testTag("system_folder_${folder.name}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "dropdown toggle",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Share, // Folder icon placeholder
                                contentDescription = "folder",
                                tint = SoftYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = folder.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Customize match badges
                        val hasBadge = when (folder.name) {
                            "memory" -> "Penguin Memory"
                            "prompts" -> "AI Prompts"
                            "agents" -> "Logical Agents"
                            else -> null
                        }

                        if (hasBadge != null) {
                            Box(
                                modifier = Modifier
                                    .background(MintGreenVariant, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = hasBadge.uppercase(),
                                    color = MintGreen,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Children or Description
                    if (isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, bottom = 6.dp)
                        ) {
                            // Render Description Block
                            Text(
                                text = "Desc: ${folder.description}",
                                style = LocalTextStyle.current.copy(
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                modifier = Modifier.padding(bottom = 6.dp, start = 12.dp)
                            )

                            // Render file children nodes
                            if (folder.children.isEmpty()) {
                                Row(
                                    modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(4.dp).background(TextSecondary, CircleShape))
                                    Text(text = "(empty/system hidden)", fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                                }
                            } else {
                                folder.children.forEach { child ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPathByTap = child.path }
                                            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check, // File indicator
                                                contentDescription = "file",
                                                tint = MintGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = child.name,
                                                fontSize = 12.sp,
                                                color = TextPrimary,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        // Path tag logic for specific mode children
                                        val subBadge = when {
                                            child.name.contains("penguin") || child.path.contains("penguin") -> "Penguin"
                                            child.name.contains("halo") || child.path.contains("halo") -> "Halo"
                                            child.name.contains("router") || child.name.contains("guava") -> "Guava"
                                            else -> null
                                        }

                                        if (subBadge != null) {
                                            Box(
                                                modifier = Modifier
                                                    .background(if (subBadge == "Penguin") Color(0x33FFE082) else MintGreenVariant, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = subBadge,
                                                    fontSize = 7.sp,
                                                    color = if (subBadge == "Penguin") SoftYellow else MintGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Divider segment
                    HorizontalDivider(color = Color(0x0FFFFFFF), modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: PENGUIN SETTINGS SCREEN
// ==========================================
@Composable
fun PenguinSettingsScreen(
    viewModel: GuavaViewModel,
    status: StatusResponse
) {
    val assistantName by viewModel.assistantName.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val voiceEnabled by viewModel.voiceEnabled.collectAsState()
    val notesEnabled by viewModel.notesEnabled.collectAsState()
    val ipAddress by viewModel.ipAddress.collectAsState()
    val isSetupMode by viewModel.isSetupMode.collectAsState()
    val isSimulated by viewModel.isSimulated.collectAsState()

    // Screen Title
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "HARDWARE PORTS & FLAGS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SoftYellow,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "System Ports Settings",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    // 1. HARDWARE LINK CONFIG (PREVENTS STATIC HARDCODING)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SoftYellow.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "RASPBERRY PI BASE URL IP",
                fontSize = 10.sp,
                color = SoftYellow,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            OutlinedTextField(
                value = ipAddress,
                onValueChange = { viewModel.updateIpAddress(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ip_host_input"),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = SoftYellow,
                    unfocusedBorderColor = Color(0x19FFFFFF),
                    cursorColor = SoftYellow
                ),
                placeholder = { Text("e.g. 192.168.1.16", color = TextSecondary, fontSize = 13.sp) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.fetchStatus() },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftYellow, contentColor = Color(0xFF0C0E12)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(40.dp).testTag("save_host_ip_btn")
                ) {
                    Text(text = "SAVE & SYNC", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                OutlinedButton(
                    onClick = { viewModel.fetchStatus() },
                    border = BorderStroke(1.dp, SoftYellow),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftYellow),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f).height(40.dp).testTag("refresh_host_btn")
                ) {
                    Text(text = "REFRESH STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }

    // 2. ASSISTANT BROADCAST SETTINGS
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0x15FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Field 1: Name Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ASSISTANT NAME",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                OutlinedTextField(
                    value = assistantName,
                    onValueChange = { viewModel.assistantName.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_assistant_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = MintGreen,
                        unfocusedBorderColor = Color(0x19FFFFFF)
                    )
                )
            }

            // Field 2: Language Selector Row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "LAVAL VOICE CODE LANGUAGE",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                val langsList = listOf("auto", "english", "hindi", "gujarati")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    langsList.forEach { lang ->
                        val isSelected = selectedLanguage.lowercase() == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .background(
                                    color = if (isSelected) MintGreen else SurfaceDark,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (isSelected) Color.Transparent else Color(0x19FFFFFF), RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectedLanguage.value = lang }
                                .testTag("lang_toggle_$lang"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lang.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF0C0E12) else Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Field 3: Voice Output Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "VOICE ENGINE SOUNDS", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = "Enable synthetic speech output replies on Pi speakers.", fontSize = 10.sp, color = TextSecondary)
                }
                Switch(
                    checked = voiceEnabled,
                    onCheckedChange = { viewModel.voiceEnabled.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0C0E12),
                        checkedTrackColor = MintGreen
                    ),
                    modifier = Modifier.testTag("voice_sound_toggle")
                )
            }

            // Field 4: Diary/Notes Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "NOTES & DIARY SAVING", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = "Log action items and summary files dynamically.", fontSize = 10.sp, color = TextSecondary)
                }
                Switch(
                    checked = notesEnabled,
                    onCheckedChange = { viewModel.notesEnabled.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0C0E12),
                        checkedTrackColor = MintGreen
                    ),
                    modifier = Modifier.testTag("notes_save_toggle")
                )
            }

            // Apply settings button capsule
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("apply_settings_btn"),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = Color(0xFF0C0E12))
            ) {
                Text(text = "APPLY Assistant SETTINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }

    // 3. SECURE RESTART BLOCK WITH DIALOG CONFIRMATION
    var showRebootDialog by remember { mutableStateOf(false) }

    if (showRebootDialog) {
        AlertDialog(
            onDismissRequest = { showRebootDialog = false },
            title = { Text(text = "Power Cycle Raspberry Pi", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text(text = "Are you sure you want to trigger a host reboot cycle onto the active AI assistant environment? Connection will restream post-startup.", color = TextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restartDevice()
                        showRebootDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text(text = "CONFIRM FORCE REBOOT", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRebootDialog = false }) {
                    Text(text = "CANCEL")
                }
            },
            containerColor = SurfaceDark,
            textContentColor = TextPrimary,
            titleContentColor = Color.White
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x19F87171)),
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "FORCE POWER REBOOT CYCLE",
                fontSize = 10.sp,
                color = ErrorRed,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Initiates immediate graceful warm reboot. Restarts voice processes, memory database locks, and audio queues.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )

            Button(
                onClick = { showRebootDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("reboot_device_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text(text = "REBUILD & REBOOT DEVICE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }

    // 4. SANDBOX HARDWARE TOGGLE
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color(0x15FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "SANDBOX SIMULATOR DETECTOR",
                    fontSize = 10.sp,
                    color = SoftYellow,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Simulates online responsive board when the physical Pi assistant is offline.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
            Switch(
                checked = isSimulated,
                onCheckedChange = { viewModel.setSimulated(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF0C0E12),
                    checkedTrackColor = SoftYellow
                ),
                modifier = Modifier.testTag("simulation_toggle_detector")
            )
        }
    }

    // Modern aesthetic visual wave monitor
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, Color(0x0FFFFFFF))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "AI SPECTRUM PULSE MONITOR",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MintGreen,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val waveBeats = listOf(0.3f, 0.45f, 0.6f, 0.2f, 0.8f, 1.0f, 0.4f, 0.5f, 0.3f, 0.7f, 0.9f, 0.5f, 0.3f, 0.6f, 0.75f, 0.2f, 0.5f)
                waveBeats.forEach { wave ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(wave)
                            .background(
                                color = MintGreen.copy(alpha = wave.coerceIn(0.2f, 0.9f)),
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                            )
                    )
                }
            }
        }
    }
}
