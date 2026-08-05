package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entities.TournamentEntity
import com.example.ui.EsportsViewModel
import com.example.ui.MainTab
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainAppEntry()
        }
    }
}

@Composable
fun MainAppEntry(viewModel: EsportsViewModel = viewModel()) {
    val activeUser by viewModel.activeUser.collectAsStateWithLifecycle()
    val allTournaments by viewModel.allTournaments.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val userTransactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val userRegistrations by viewModel.userRegistrations.collectAsStateWithLifecycle()
    val allRegistrations by viewModel.allRegistrations.collectAsStateWithLifecycle()
    val pendingRegistrations by viewModel.pendingRegistrations.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val selectedLeaderboardTournamentId by viewModel.selectedLeaderboardTournamentId.collectAsStateWithLifecycle()

    val isDarkModeState by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val effectiveIsDark = isDarkModeState ?: isSystemDark

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedGameFilter by viewModel.selectedGameFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog & Sheet States
    var selectedTournamentForDetail by remember { mutableStateOf<TournamentEntity?>(null) }
    var selectedTournamentForRoomKey by remember { mutableStateOf<TournamentEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    PlayZoneTheme(darkTheme = effectiveIsDark) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = DarkBackground,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = DarkSurfaceElevated,
                        contentColor = TextPrimary,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
        bottomBar = {
            if (activeUser != null) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == MainTab.HOME,
                        onClick = { viewModel.selectedTab.value = MainTab.HOME },
                        icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainTab.MY_MATCHES,
                        onClick = { viewModel.selectedTab.value = MainTab.MY_MATCHES },
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "My Matches") },
                        label = { Text("Matches", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_matches")
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainTab.LEADERBOARD,
                        onClick = { viewModel.selectedTab.value = MainTab.LEADERBOARD },
                        icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                        label = { Text("Rankings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_leaderboard")
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainTab.WALLET,
                        onClick = { viewModel.selectedTab.value = MainTab.WALLET },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                        label = { Text("Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoldAmber,
                            selectedTextColor = GoldAmber,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_wallet")
                    )

                    NavigationBarItem(
                        selected = selectedTab == MainTab.PROFILE,
                        onClick = { viewModel.selectedTab.value = MainTab.PROFILE },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )

                    if (activeUser?.isAdmin == true) {
                        NavigationBarItem(
                            selected = selectedTab == MainTab.ADMIN,
                            onClick = { viewModel.selectedTab.value = MainTab.ADMIN },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                            label = { Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoldAmber,
                                selectedTextColor = GoldAmber,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = DarkSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_admin")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeUser == null) {
                LoginRegisterScreen(
                    onLogin = { id, pass, code -> viewModel.login(id, pass, code) },
                    onRegister = { name, email, uid, ign, wa, upi, pass, dob, pin ->
                        viewModel.register(name, email, uid, ign, wa, upi, pass, dob, pin)
                    },
                    onResetPassword = { id, dob, pin, newPass ->
                        viewModel.resetPassword(id, dob, pin, newPass)
                    }
                )
            } else {
                Crossfade(targetState = selectedTab, label = "tab_crossfade") { tab ->
                    when (tab) {
                        MainTab.HOME -> HomeScreen(
                            activeUser = activeUser,
                            tournaments = allTournaments,
                            announcements = announcements,
                            userRegistrations = userRegistrations,
                            selectedGameFilter = selectedGameFilter,
                            searchQuery = searchQuery,
                            isDarkTheme = effectiveIsDark,
                            onToggleTheme = { viewModel.toggleThemeMode(effectiveIsDark) },
                            onFilterSelect = { viewModel.selectedGameFilter.value = it },
                            onSearchChange = { viewModel.searchQuery.value = it },
                            onSelectTournament = { selectedTournamentForDetail = it },
                            onOpenWallet = { viewModel.selectedTab.value = MainTab.WALLET },
                            onOpenRoomKeys = { selectedTournamentForRoomKey = it },
                            onOpenLeaderboard = { tournament ->
                                viewModel.selectedLeaderboardTournamentId.value = tournament.id
                                viewModel.selectedTab.value = MainTab.LEADERBOARD
                            }
                        )

                        MainTab.MY_MATCHES -> MyMatchesScreen(
                            userRegistrations = userRegistrations,
                            allTournaments = allTournaments,
                            onOpenRoomKeys = { selectedTournamentForRoomKey = it },
                            onOpenLeaderboard = { tournament ->
                                viewModel.selectedLeaderboardTournamentId.value = tournament.id
                                viewModel.selectedTab.value = MainTab.LEADERBOARD
                            }
                        )

                        MainTab.LEADERBOARD -> LeaderboardScreen(
                            activeUser = activeUser,
                            tournaments = allTournaments,
                            allRegistrations = allRegistrations,
                            selectedTournamentId = selectedLeaderboardTournamentId,
                            onSelectTournamentId = { viewModel.selectedLeaderboardTournamentId.value = it },
                            onSimulateLiveTick = { tId -> viewModel.simulateLiveMatchTick(tId) },
                            onUpdateLiveStats = { reg, k, s, p, alive, r ->
                                viewModel.updateParticipantLiveStats(reg, k, s, p, alive, r)
                            }
                        )

                        MainTab.WALLET -> WalletScreen(
                            activeUser = activeUser,
                            transactions = userTransactions,
                            onDeposit = { amount, ref -> viewModel.depositFunds(amount, ref) },
                            onWithdraw = { amount, upi -> viewModel.withdrawFunds(amount, upi) }
                        )

                        MainTab.PROFILE -> ProfileScreen(
                            activeUser = activeUser,
                            onUpdateProfile = { name, ign, uid, wa, upi ->
                                viewModel.updateProfile(name, ign, uid, wa, upi)
                            },
                            onToggleAdmin = { enable -> viewModel.toggleAdminMode(enable) },
                            onLogout = { viewModel.logout() }
                        )

                        MainTab.ADMIN -> AdminDashboardScreen(
                            activeUser = activeUser,
                            tournaments = allTournaments,
                            announcements = announcements,
                            pendingRegistrations = pendingRegistrations,
                            allRegistrations = allRegistrations,
                            allUsers = allUsers,
                            adminUpiId = appConfig?.adminUpiId ?: "suraj52971-2@okaxis",
                            onCreateTournament = { title, game, map, mode, time, entry, prize, perKill, slots, rules ->
                                viewModel.createTournament(title, game, map, mode, time, entry, prize, perKill, slots, rules)
                            },
                            onUpdateStatus = { id, status -> viewModel.updateTournamentStatus(id, status) },
                            onUpdateRoom = { id, room, pass -> viewModel.updateRoomCredentials(id, room, pass) },
                            onRecordResult = { reg, kills, rank, prize, perKill ->
                                viewModel.updateParticipantResult(reg, kills, rank, prize, perKill)
                            },
                            onCancelTournament = { id -> viewModel.cancelTournament(id) },
                            onPostAnnouncement = { title, msg, pin -> viewModel.postAnnouncement(title, msg, pin) },
                            onDeleteAnnouncement = { id -> viewModel.deleteAnnouncement(id) },
                            getRegistrationsForTournament = { tId ->
                                viewModel.repository.getRegistrationsForTournamentFlow(tId)
                            },
                            onUpdateLiveStats = { reg, k, s, p, alive, r ->
                                viewModel.updateParticipantLiveStats(reg, k, s, p, alive, r)
                            },
                            onToggleAdminRole = { enable -> viewModel.toggleAdminMode(enable) },
                            onApproveUpiRegistration = { regId -> viewModel.approveUpiRegistration(regId) },
                            onSyncFirestore = { viewModel.syncToFirestore() },
                            onUpdateAdminUpiId = { upi -> viewModel.updateAdminUpiId(upi) },
                            onUpdateTournamentMaxPlayers = { tId, max -> viewModel.updateTournamentMaxPlayers(tId, max) },
                            onRemoveParticipant = { regId -> viewModel.removeParticipant(regId) }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Tournament Details & Join
    selectedTournamentForDetail?.let { tournament ->
        val isRegistered = userRegistrations.any { it.tournamentId == tournament.id }
        TournamentDetailBottomSheet(
            tournament = tournament,
            activeUser = activeUser,
            isRegistered = isRegistered,
            upiReceiverVpa = appConfig?.adminUpiId ?: "suraj52971-2@okaxis",
            onDismiss = { selectedTournamentForDetail = null },
            onConfirmJoin = { ign, uid, wa, upi ->
                viewModel.joinTournament(tournament.id, ign, uid, wa, upi)
            },
            onConfirmUpiJoin = { ign, uid, wa, upi, utr ->
                viewModel.joinTournamentViaUpi(tournament.id, ign, uid, wa, upi, utr)
            },
            onOpenWallet = { viewModel.selectedTab.value = MainTab.WALLET },
            onOpenRoomKeys = { selectedTournamentForRoomKey = tournament }
        )
    }

    // Room Details Dialog
    selectedTournamentForRoomKey?.let { tournament ->
        RoomDetailsDialog(
            tournament = tournament,
            onDismiss = { selectedTournamentForRoomKey = null }
        )
    }
}
}
