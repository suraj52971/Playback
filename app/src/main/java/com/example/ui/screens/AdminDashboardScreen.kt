package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.AnnouncementEntity
import com.example.data.local.entities.RegistrationEntity
import com.example.data.local.entities.TournamentEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.Flow

@Composable
fun AdminDashboardScreen(
    activeUser: UserEntity? = null,
    tournaments: List<TournamentEntity>,
    announcements: List<AnnouncementEntity>,
    pendingRegistrations: List<RegistrationEntity> = emptyList(),
    allRegistrations: List<RegistrationEntity> = emptyList(),
    allUsers: List<UserEntity> = emptyList(),
    adminUpiId: String = "suraj52971-2@okaxis",
    onCreateTournament: (String, String, String, String, Long, Double, Double, Double, Int, String) -> Unit,
    onUpdateStatus: (Int, String) -> Unit,
    onUpdateRoom: (Int, String, String) -> Unit,
    onRecordResult: (RegistrationEntity, Int, Int, Double, Double) -> Unit,
    onCancelTournament: (Int) -> Unit,
    onPostAnnouncement: (String, String, Boolean) -> Unit,
    onDeleteAnnouncement: (Int) -> Unit,
    getRegistrationsForTournament: (Int) -> Flow<List<RegistrationEntity>>,
    onUpdateLiveStats: ((RegistrationEntity, Int, Int, Int, Boolean, Int) -> Unit)? = null,
    onToggleAdminRole: ((Boolean) -> Unit)? = null,
    onApproveUpiRegistration: (Int) -> Unit = {},
    onSyncFirestore: () -> Unit = {},
    onUpdateAdminUpiId: (String) -> Unit = {},
    onUpdateTournamentMaxPlayers: (Int, Int) -> Unit = { _, _ -> },
    onRemoveParticipant: (Int) -> Unit = {}
) {
    var isPasskeyUnlocked by remember { mutableStateOf(false) }
    val isUserAuthorized = (activeUser?.isAdmin == true) || isPasskeyUnlocked

    if (!isUserAuthorized) {
        AdminAccessGateScreen(
            activeUser = activeUser,
            onUnlock = { isPasskeyUnlocked = true },
            onEnableAdminRole = { onToggleAdminRole?.invoke(true) }
        ) {
            isPasskeyUnlocked = true
        }
        return
    }

    var activeAdminTab by remember { mutableStateOf("OVERVIEW") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Restricted Admin Header
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAmber.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(GoldAmber.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = GoldAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RESTRICTED ADMIN PORTAL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(EmeraldGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Firestore Realtime Sync Active",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSyncFirestore,
                        modifier = Modifier.testTag("admin_sync_firestore_button")
                    ) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = "Sync to Firestore",
                            tint = NeonCyan
                        )
                    }
                    Surface(
                        color = CrimsonRed.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "AUTHORIZED",
                            color = GoldAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Navigation Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("OVERVIEW", "MANAGE", "VERIFY_UPI", "CREATE", "ANNOUNCE").forEach { tab ->
                val isSelected = activeAdminTab == tab
                Button(
                    onClick = { activeAdminTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) GoldAmber else DarkSurface,
                        contentColor = if (isSelected) Color.Black else TextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("admin_tab_$tab"),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when (tab) {
                            "OVERVIEW" -> "USERS & UPI"
                            "MANAGE" -> "MATCHES"
                            "VERIFY_UPI" -> if (pendingRegistrations.isNotEmpty()) "UPI (${pendingRegistrations.size})" else "VERIFY UPI"
                            "CREATE" -> "+ NEW"
                            else -> "ANNOUNCE"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (activeAdminTab) {
            "OVERVIEW" -> OverviewAndUsersSection(
                allUsers = allUsers,
                tournaments = tournaments,
                allRegistrations = allRegistrations,
                adminUpiId = adminUpiId,
                onUpdateAdminUpiId = onUpdateAdminUpiId
            )
            "MANAGE" -> ManageTournamentsSection(
                tournaments = tournaments,
                onUpdateStatus = onUpdateStatus,
                onUpdateRoom = onUpdateRoom,
                onRecordResult = onRecordResult,
                onCancelTournament = onCancelTournament,
                getRegistrationsForTournament = getRegistrationsForTournament,
                onUpdateLiveStats = onUpdateLiveStats,
                onSyncFirestore = onSyncFirestore,
                onUpdateTournamentMaxPlayers = onUpdateTournamentMaxPlayers,
                onRemoveParticipant = onRemoveParticipant
            )
            "VERIFY_UPI" -> PendingUpiVerificationSection(
                pendingRegistrations = pendingRegistrations,
                tournaments = tournaments,
                onApprove = onApproveUpiRegistration
            )
            "CREATE" -> CreateTournamentForm(onCreateTournament)
            "ANNOUNCE" -> AnnouncementsSection(
                announcements = announcements,
                onPostAnnouncement = onPostAnnouncement,
                onDeleteAnnouncement = onDeleteAnnouncement
            )
        }
    }
}

@Composable
fun AdminAccessGateScreen(
    activeUser: UserEntity?,
    onUnlock: () -> Unit,
    onEnableAdminRole: () -> Unit,
    onPasskeySuccess: () -> Unit
) {
    var passkeyInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(CrimsonRed.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Restricted Access",
                        tint = CrimsonRed,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RESTRICTED ADMIN AREA",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Authorization required to manage tournament entries and sync match statuses in Firestore.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Surface(
                    color = DarkBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Enter Admin Passkey / PIN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAmber
                        )

                        OutlinedTextField(
                            value = passkeyInput,
                            onValueChange = {
                                passkeyInput = it
                                errorText = null
                            },
                            label = { Text("Passkey (Default: 1234 or admin123)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_passkey_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAmber)
                        )

                        errorText?.let { err ->
                            Text(err, color = CrimsonRed, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (passkeyInput == "1234" || passkeyInput == "admin123" || passkeyInput == "admin") {
                                    onUnlock()
                                    onPasskeySuccess()
                                } else {
                                    errorText = "Invalid Admin Passkey. Use '1234' for quick testing."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("admin_unlock_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAmber),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AUTHORIZE ADMIN SESSION", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }

                if (activeUser != null && !activeUser.isAdmin) {
                    HorizontalDivider(color = DarkSurfaceVariant)
                    OutlinedButton(
                        onClick = onEnableAdminRole,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ELEVATE ACCOUNT TO ADMIN ROLE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTournamentForm(
    onCreateTournament: (String, String, String, String, Long, Double, Double, Double, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var game by remember { mutableStateOf("Free Fire") }
    var map by remember { mutableStateOf("Bermuda") }
    var mode by remember { mutableStateOf("Solo") }
    var entryFeeText by remember { mutableStateOf("30") }
    var prizePoolText by remember { mutableStateOf("1000") }
    var perKillText by remember { mutableStateOf("15") }
    var maxPlayersText by remember { mutableStateOf("48") }
    var rules by remember { mutableStateOf("1. Mobile only.\n2. No cheats/emulators.\n3. Room details revealed 15 min prior.") }

    val gamesList = listOf("Free Fire", "BGMI", "Call of Duty", "Valorant")
    val modesList = listOf("Solo", "Duo", "Squad")

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("CREATE NEW MATCH & PUSH TO FIRESTORE", fontWeight = FontWeight.Bold, color = GoldAmber, fontSize = 15.sp)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tournament Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAmber)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Game", fontSize = 11.sp, color = TextMuted)
                            Row {
                                gamesList.take(2).forEach { g ->
                                    FilterChip(
                                        selected = game == g,
                                        onClick = { game = g },
                                        label = { Text(g, fontSize = 10.sp) },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mode", fontSize = 11.sp, color = TextMuted)
                            Row {
                                modesList.forEach { m ->
                                    FilterChip(
                                        selected = mode == m,
                                        onClick = { mode = m },
                                        label = { Text(m, fontSize = 10.sp) },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = map,
                        onValueChange = { map = it },
                        label = { Text("Map Name (e.g., Bermuda / Erangel)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_map_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAmber)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = entryFeeText,
                            onValueChange = { entryFeeText = it },
                            label = { Text("Entry Fee (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_entry_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = prizePoolText,
                            onValueChange = { prizePoolText = it },
                            label = { Text("Prize Pool (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_prizepool_input"),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = perKillText,
                            onValueChange = { perKillText = it },
                            label = { Text("Per Kill (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_perkill_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = maxPlayersText,
                            onValueChange = { maxPlayersText = it },
                            label = { Text("Max Slots") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_slots_input"),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = rules,
                        onValueChange = { rules = it },
                        label = { Text("Rules & Guidelines") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("admin_rules_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAmber)
                    )

                    Button(
                        onClick = {
                            val entry = entryFeeText.toDoubleOrNull() ?: 0.0
                            val prize = prizePoolText.toDoubleOrNull() ?: 0.0
                            val perKill = perKillText.toDoubleOrNull() ?: 0.0
                            val slots = maxPlayersText.toIntOrNull() ?: 48
                            val matchTime = System.currentTimeMillis() + 3600000L

                            if (title.isNotBlank()) {
                                onCreateTournament(
                                    title, game, map, mode, matchTime, entry, prize, perKill, slots, rules
                                )
                                title = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_publish_tournament_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAmber)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PUBLISH MATCH & PUSH TO FIRESTORE 🔥", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun OverviewAndUsersSection(
    allUsers: List<UserEntity>,
    tournaments: List<TournamentEntity>,
    allRegistrations: List<RegistrationEntity>,
    adminUpiId: String,
    onUpdateAdminUpiId: (String) -> Unit
) {
    var upiInput by remember(adminUpiId) { mutableStateOf(adminUpiId) }
    var userSearchQuery by remember { mutableStateOf("") }

    val totalUsersCount = allUsers.size
    val totalWalletLiquidity = allUsers.sumOf { it.walletBalance }
    val totalRegistrationsCount = allRegistrations.size

    val totalRevenueCollected = remember(tournaments, allRegistrations) {
        allRegistrations.sumOf { reg ->
            tournaments.find { it.id == reg.tournamentId }?.entryFee ?: 0.0
        }
    }

    val totalMaxSlotsAcrossTournaments = tournaments.sumOf { it.maxPlayers }
    val occupancyPercentage = if (totalMaxSlotsAcrossTournaments > 0) {
        ((totalRegistrationsCount.toDouble() / totalMaxSlotsAcrossTournaments.toDouble()) * 100).toInt().coerceAtMost(100)
    } else 0

    val filteredUsers = remember(allUsers, userSearchQuery) {
        if (userSearchQuery.isBlank()) allUsers
        else allUsers.filter {
            it.name.contains(userSearchQuery, ignoreCase = true) ||
            it.email.contains(userSearchQuery, ignoreCase = true) ||
            it.gameUid.contains(userSearchQuery, ignoreCase = true) ||
            it.inGameName.contains(userSearchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // --- 1. REAL-TIME USER ANALYTICS & PLATFORM METRICS CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("USER ANALYTICS & PLATFORM METRICS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
                                Text("Real-Time Player & Match Stats", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        Surface(color = EmeraldGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                            Text("LIVE", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }

                    HorizontalDivider(color = DarkSurfaceVariant)

                    // 2x2 Grid of Key Analytics
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricTile(
                            label = "LOGGED-IN USERS",
                            value = "$totalUsersCount Players",
                            icon = Icons.Default.Group,
                            color = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            label = "MATCH PARTICIPATIONS",
                            value = "$totalRegistrationsCount Joined",
                            icon = Icons.Default.EmojiEvents,
                            color = GoldAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricTile(
                            label = "GROSS ENTRY REVENUE",
                            value = "₹${totalRevenueCollected.toInt()}",
                            icon = Icons.Default.Payments,
                            color = EmeraldGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            label = "SLOT OCCUPANCY",
                            value = "$occupancyPercentage%",
                            icon = Icons.Default.PieChart,
                            color = ElectricViolet,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Total Wallet System Liquidity
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Total User Wallet Balance Pool", fontSize = 11.sp, color = TextSecondary)
                            }
                            Text("₹${totalWalletLiquidity.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldAmber)
                        }
                    }
                }
            }
        }

        // --- 2. ADMIN UPI RECEIVER CONFIG CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAmber.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = GoldAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ADMIN RECEIVER UPI ID CONFIGURATION",
                            fontWeight = FontWeight.Bold,
                            color = GoldAmber,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "This official UPI ID will be used in all user UPI payment intents for direct match entry.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = upiInput,
                        onValueChange = { upiInput = it },
                        label = { Text("Official Admin UPI ID (e.g. name@upi)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_upi_id_config_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAmber)
                    )

                    Button(
                        onClick = { onUpdateAdminUpiId(upiInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_admin_upi_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAmber)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("UPDATE RECEIVER UPI ID", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // --- 3. ALL REGISTERED USERS ROSTER ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "REGISTERED USERS ROSTER (${filteredUsers.size})",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 14.sp
                )

                OutlinedTextField(
                    value = userSearchQuery,
                    onValueChange = { userSearchQuery = it },
                    placeholder = { Text("Search by name, email, or Game UID...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_user_search_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
                )
            }
        }

        if (filteredUsers.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No users found matching query.", color = TextMuted)
                    }
                }
            }
        } else {
            items(filteredUsers, key = { it.id }) { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                if (user.isAdmin) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = GoldAmber, shape = RoundedCornerShape(4.dp)) {
                                        Text("ADMIN", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text("IGN: ${user.inGameName} | UID: ${user.gameUid}", fontSize = 11.sp, color = NeonCyan)
                            Text("Email: ${user.email} | WA: ${user.whatsappNumber}", fontSize = 10.sp, color = TextMuted)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Wallet", fontSize = 10.sp, color = TextMuted)
                            Text("₹${user.walletBalance.toInt()}", fontWeight = FontWeight.Bold, color = GoldAmber, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageTournamentsSection(
    tournaments: List<TournamentEntity>,
    onUpdateStatus: (Int, String) -> Unit,
    onUpdateRoom: (Int, String, String) -> Unit,
    onRecordResult: (RegistrationEntity, Int, Int, Double, Double) -> Unit,
    onCancelTournament: (Int) -> Unit,
    getRegistrationsForTournament: (Int) -> Flow<List<RegistrationEntity>>,
    onUpdateLiveStats: ((RegistrationEntity, Int, Int, Int, Boolean, Int) -> Unit)? = null,
    onSyncFirestore: () -> Unit = {},
    onUpdateTournamentMaxPlayers: (Int, Int) -> Unit = { _, _ -> },
    onRemoveParticipant: (Int) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = remember(tournaments, selectedFilter) {
        if (selectedFilter == "ALL") tournaments
        else tournaments.filter { it.status.equals(selectedFilter, ignoreCase = true) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "UPCOMING", "LIVE", "COMPLETED").forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        if (filteredList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No tournaments found matching status filter.", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredList, key = { it.id }) { tournament ->
                    ManageTournamentCard(
                        tournament = tournament,
                        onUpdateStatus = onUpdateStatus,
                        onUpdateRoom = onUpdateRoom,
                        onRecordResult = onRecordResult,
                        onCancelTournament = onCancelTournament,
                        getRegistrationsForTournament = getRegistrationsForTournament,
                        onUpdateLiveStats = onUpdateLiveStats,
                        onUpdateMaxPlayers = onUpdateTournamentMaxPlayers,
                        onRemoveParticipant = onRemoveParticipant
                    )
                }
            }
        }
    }
}

@Composable
fun ManageTournamentCard(
    tournament: TournamentEntity,
    onUpdateStatus: (Int, String) -> Unit,
    onUpdateRoom: (Int, String, String) -> Unit,
    onRecordResult: (RegistrationEntity, Int, Int, Double, Double) -> Unit,
    onCancelTournament: (Int) -> Unit,
    getRegistrationsForTournament: (Int) -> Flow<List<RegistrationEntity>>,
    onUpdateLiveStats: ((RegistrationEntity, Int, Int, Int, Boolean, Int) -> Unit)? = null,
    onUpdateMaxPlayers: (Int, Int) -> Unit = { _, _ -> },
    onRemoveParticipant: (Int) -> Unit = {}
) {
    var roomIdInput by remember { mutableStateOf(tournament.roomId) }
    var roomPassInput by remember { mutableStateOf(tournament.roomPassword) }
    var maxSlotsInput by remember(tournament.maxPlayers) { mutableStateOf(tournament.maxPlayers.toString()) }
    var showParticipants by remember { mutableStateOf(false) }

    val registrations by getRegistrationsForTournament(tournament.id).collectAsStateWithLifecycle(emptyList())

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tournament.title,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${tournament.game} • ${tournament.mode} • ${tournament.map}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Participants: ${registrations.size} / ${tournament.maxPlayers} Slots",
                        fontSize = 11.sp,
                        color = GoldAmber,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = when (tournament.status) {
                        "LIVE" -> CrimsonRed
                        "COMPLETED" -> DarkSurfaceVariant
                        "CANCELLED" -> TextMuted
                        else -> EmeraldGreen
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = tournament.status,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slot Limit Config Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = maxSlotsInput,
                    onValueChange = { maxSlotsInput = it },
                    label = { Text("Slot Limit (Default 50)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("admin_max_slots_input_${tournament.id}"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAmber)
                )
                Button(
                    onClick = {
                        val max = maxSlotsInput.toIntOrNull()
                        if (max != null && max > 0) {
                            onUpdateMaxPlayers(tournament.id, max)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAmber),
                    modifier = Modifier.testTag("admin_set_slots_button_${tournament.id}")
                ) {
                    Text("SET SLOTS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Match Status Switcher with Firestore Sync Label
            Text("UPDATE MATCH STATUS (FIRESTORE REALTIME):", fontSize = 10.sp, color = GoldAmber, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("UPCOMING", "LIVE", "COMPLETED").forEach { st ->
                    val isCurrent = tournament.status == st
                    Button(
                        onClick = { onUpdateStatus(tournament.id, st) },
                        modifier = Modifier.weight(1f).testTag("status_button_${tournament.id}_$st"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrent) NeonCyan else DarkBackground,
                            contentColor = if (isCurrent) Color.Black else TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(st, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Room Credentials Publisher
            Text("ROOM DETAILS (PUSHES TO FIRESTORE):", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = roomIdInput,
                    onValueChange = { roomIdInput = it },
                    label = { Text("Room ID") },
                    modifier = Modifier.weight(1f).testTag("admin_room_id_input_${tournament.id}"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = roomPassInput,
                    onValueChange = { roomPassInput = it },
                    label = { Text("Password") },
                    modifier = Modifier.weight(1f).testTag("admin_room_pass_input_${tournament.id}"),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onUpdateRoom(tournament.id, roomIdInput, roomPassInput) },
                modifier = Modifier.fillMaxWidth().testTag("admin_publish_room_button_${tournament.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("PUBLISH ROOM DETAILS TO FIRESTORE 🗝️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showParticipants = !showParticipants },
                    modifier = Modifier.weight(1f).testTag("manage_participants_button_${tournament.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (showParticipants) "HIDE ENTRIES" else "MANAGE ENTRIES (${registrations.size}/${tournament.maxPlayers})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { onCancelTournament(tournament.id) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CANCEL & REFUND", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (showParticipants) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DarkSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE TOURNAMENT ENTRIES (${registrations.size})",
                        fontWeight = FontWeight.Bold,
                        color = GoldAmber,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Per Kill Reward: ₹${tournament.perKillReward.toInt()}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (registrations.isEmpty()) {
                    Text("No players registered for this match yet.", fontSize = 12.sp, color = TextMuted)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        registrations.forEach { reg ->
                            ParticipantResultRow(
                                reg = reg,
                                perKillRate = tournament.perKillReward,
                                onRecord = onRecordResult,
                                onUpdateLiveStats = onUpdateLiveStats,
                                onRemoveParticipant = onRemoveParticipant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantResultRow(
    reg: RegistrationEntity,
    perKillRate: Double,
    onRecord: (RegistrationEntity, Int, Int, Double, Double) -> Unit,
    onUpdateLiveStats: ((RegistrationEntity, Int, Int, Int, Boolean, Int) -> Unit)? = null,
    onRemoveParticipant: (Int) -> Unit = {}
) {
    var killsText by remember { mutableStateOf(reg.kills.toString()) }
    var rankText by remember { mutableStateOf(if (reg.rank > 0) reg.rank.toString() else "1") }
    var rankPrizeText by remember { mutableStateOf(if (reg.payoutAmount > 0) reg.payoutAmount.toString() else "200") }
    var survivalMinutesText by remember { mutableStateOf(reg.survivalTimeMinutes.toString()) }
    var isAlive by remember { mutableStateOf(reg.isAlive) }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${reg.userInGameName} (UID: ${reg.userGameUid})",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "WA: ${reg.userWhatsapp} | UPI: ${reg.userUpi}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                FilterChip(
                    selected = isAlive,
                    onClick = {
                        isAlive = !isAlive
                        onUpdateLiveStats?.invoke(
                            reg,
                            killsText.toIntOrNull() ?: reg.kills,
                            survivalMinutesText.toIntOrNull() ?: reg.survivalTimeMinutes,
                            reg.rankPoints,
                            isAlive,
                            rankText.toIntOrNull() ?: reg.rank
                        )
                    },
                    label = { Text(if (isAlive) "ALIVE 💚" else "ELIMINATED 💀", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldGreen.copy(alpha = 0.2f),
                        selectedLabelColor = EmeraldGreen
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = killsText,
                    onValueChange = { killsText = it },
                    label = { Text("Kills") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("entry_kills_${reg.id}"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = rankText,
                    onValueChange = { rankText = it },
                    label = { Text("Rank") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("entry_rank_${reg.id}"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = rankPrizeText,
                    onValueChange = { rankPrizeText = it },
                    label = { Text("Rank Prize ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.2f).testTag("entry_prize_${reg.id}"),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val kills = killsText.toIntOrNull() ?: 0
                        val rank = rankText.toIntOrNull() ?: 0
                        val rankPrize = rankPrizeText.toDoubleOrNull() ?: 0.0
                        onRecord(reg, kills, rank, rankPrize, perKillRate)
                    },
                    modifier = Modifier.weight(1f).testTag("save_result_firestore_button_${reg.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Sync Result", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RECORD RESULT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                OutlinedButton(
                    onClick = { onRemoveParticipant(reg.id) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("remove_participant_button_${reg.id}")
                ) {
                    Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("KICK", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AnnouncementsSection(
    announcements: List<AnnouncementEntity>,
    onPostAnnouncement: (String, String, Boolean) -> Unit,
    onDeleteAnnouncement: (Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("POST BROADCAST ANNOUNCEMENT", fontWeight = FontWeight.Bold, color = GoldAmber, fontSize = 15.sp)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("announce_title_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .testTag("announce_msg_input")
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                        Text("Pin Announcement to Top", fontSize = 12.sp, color = TextPrimary)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && message.isNotBlank()) {
                                onPostAnnouncement(title, message, isPinned)
                                title = ""
                                message = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAmber),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_announcement_button")
                    ) {
                        Text("POST ANNOUNCEMENT 📢", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(announcements, key = { it.id }) { ann ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ann.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Text(ann.message, fontSize = 12.sp, color = TextSecondary)
                    }
                    IconButton(onClick = { onDeleteAnnouncement(ann.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonRed)
                    }
                }
            }
        }
    }
}

@Composable
fun PendingUpiVerificationSection(
    pendingRegistrations: List<RegistrationEntity>,
    tournaments: List<TournamentEntity>,
    onApprove: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAmber),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PendingActions, contentDescription = null, tint = GoldAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PENDING UPI VERIFICATIONS (${pendingRegistrations.size})",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Verify player UTR numbers against your UPI bank statement (vpa: singhsuraj52971-2@okaxis). Clicking approve instantly confirms their match slot.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        if (pendingRegistrations.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No pending UPI payments to verify. All slots clear! ✅", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(pendingRegistrations, key = { it.id }) { reg ->
                    val tournament = tournaments.find { it.id == reg.tournamentId }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tournament?.title ?: "Match #${reg.tournamentId}",
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    fontSize = 13.sp
                                )
                                Surface(
                                    color = GoldAmber.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "UTR: ${reg.utrNumber}",
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAmber,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("IGN: ${reg.userInGameName}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text("UID: ${reg.userGameUid}", fontSize = 11.sp, color = TextSecondary)
                                    Text("WhatsApp: ${reg.userWhatsapp}", fontSize = 11.sp, color = TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Amount: ₹${tournament?.entryFee?.toInt() ?: 0}", fontSize = 13.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                                    Text("Method: ${reg.paymentMethod}", fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onApprove(reg.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("APPROVE PAYMENT & CONFIRM SLOT ✅", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
