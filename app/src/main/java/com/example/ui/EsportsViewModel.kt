package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AnnouncementEntity
import com.example.data.local.entities.AppConfigEntity
import com.example.data.local.entities.RegistrationEntity
import com.example.data.local.entities.TournamentEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.EsportsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    MY_MATCHES,
    LEADERBOARD,
    WALLET,
    PROFILE,
    ADMIN
}

class EsportsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = EsportsRepository(db)

    init {
        repository.startFirestoreSync(viewModelScope)
    }

    fun syncToFirestore() {
        viewModelScope.launch {
            repository.syncAllToFirestore()
                .onSuccess { msg -> showMessage("🔥 Firestore: $msg") }
                .onFailure { err -> showMessage("⚡ Firestore Sync: ${err.message}") }
        }
    }

    val activeUser: StateFlow<UserEntity?> = repository.activeUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedTab = MutableStateFlow(MainTab.HOME)
    val selectedGameFilter = MutableStateFlow("ALL")
    val searchQuery = MutableStateFlow("")

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.announcementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTournaments: StateFlow<List<TournamentEntity>> = repository.allTournamentsFlow
        .combine(selectedGameFilter) { list, filter ->
            if (filter == "ALL") list else list.filter { it.game.equals(filter, ignoreCase = true) }
        }
        .combine(searchQuery) { list, query ->
            if (query.isBlank()) list else list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.game.contains(query, ignoreCase = true) ||
                it.map.contains(query, ignoreCase = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions: StateFlow<List<TransactionEntity>> = activeUser
        .flatMapLatest { user ->
            if (user != null) repository.getUserTransactionsFlow(user.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userRegistrations: StateFlow<List<RegistrationEntity>> = activeUser
        .flatMapLatest { user ->
            if (user != null) repository.getUserRegistrationsFlow(user.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRegistrations: StateFlow<List<RegistrationEntity>> = repository.allRegistrationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRegistrations: StateFlow<List<RegistrationEntity>> = repository.pendingRegistrationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appConfig: StateFlow<AppConfigEntity> = repository.appConfigFlow
        .map { it ?: AppConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppConfigEntity())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedLeaderboardTournamentId = MutableStateFlow<Int?>(null)

    val isDarkMode = MutableStateFlow<Boolean?>(null) // null = system default

    fun toggleThemeMode(currentIsDark: Boolean) {
        isDarkMode.value = !currentIsDark
    }

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }

    // --- AUTH ACTIONS ---
    fun register(
        name: String,
        email: String,
        gameUid: String,
        inGameName: String,
        whatsapp: String,
        upi: String,
        password: String,
        dob: String,
        recoveryPin: String
    ) {
        viewModelScope.launch {
            val result = repository.registerUser(name, email, gameUid, inGameName, whatsapp, upi, password, dob, recoveryPin)
            result.onSuccess {
                showMessage("Account Created Successfully! Free Fire ₹100 bonus added to wallet 🎉")
            }.onFailure {
                showMessage("Registration Failed: ${it.message}")
            }
        }
    }

    fun login(identifier: String, password: String = "", adminCode: String = "") {
        viewModelScope.launch {
            val result = repository.loginUser(identifier, password, adminCode)
            result.onSuccess { user ->
                if (user.isAdmin || adminCode.trim() == "surajsingh52971@") {
                    selectedTab.value = MainTab.ADMIN
                    showMessage("ADMIN LOGIN SUCCESSFUL! Redirected to Restricted Admin Portal 🛡️")
                } else {
                    selectedTab.value = MainTab.HOME
                    showMessage("Welcome back, ${user.name}! 🎮")
                }
            }.onFailure {
                showMessage(it.message ?: "Login failed")
            }
        }
    }

    fun resetPassword(identifier: String, dob: String, recoveryPin: String, newPassword: String) {
        viewModelScope.launch {
            val result = repository.resetPasswordWithPin(identifier, dob, recoveryPin, newPassword)
            result.onSuccess {
                showMessage("Password Reset Successfully! You can now log in with your new password. 🗝️")
            }.onFailure {
                showMessage("Password Reset Failed: ${it.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
            showMessage("Logged out successfully.")
        }
    }

    fun updateProfile(
        name: String,
        inGameName: String,
        gameUid: String,
        whatsapp: String,
        upi: String
    ) {
        viewModelScope.launch {
            val result = repository.updateUserProfile(name, inGameName, gameUid, whatsapp, upi)
            result.onSuccess {
                showMessage("Profile updated successfully! ✨")
            }.onFailure {
                showMessage(it.message ?: "Failed to update profile")
            }
        }
    }

    fun toggleAdminMode(enable: Boolean) {
        viewModelScope.launch {
            repository.toggleAdminMode(enable)
            if (enable) showMessage("Admin Mode Activated 🛡️")
            else showMessage("Switched to Normal User Mode")
        }
    }

    // --- WALLET ACTIONS ---
    fun depositFunds(amount: Double, paymentRef: String) {
        viewModelScope.launch {
            val user = activeUser.value ?: return@launch
            val res = repository.depositFunds(user.id, amount, paymentRef)
            res.onSuccess {
                showMessage("₹${amount.toInt()} Added to Wallet Successfully! 💳")
            }.onFailure {
                showMessage("Deposit Failed: ${it.message}")
            }
        }
    }

    fun withdrawFunds(amount: Double, upiId: String) {
        viewModelScope.launch {
            val user = activeUser.value ?: return@launch
            val res = repository.requestWithdrawal(user.id, amount, upiId)
            res.onSuccess {
                showMessage("Withdrawal request of ₹${amount.toInt()} submitted! Payout processing... 💸")
            }.onFailure {
                showMessage(it.message ?: "Withdrawal failed")
            }
        }
    }

    // --- TOURNAMENT ACTIONS ---
    fun joinTournament(
        tournamentId: Int,
        inGameName: String,
        gameUid: String,
        whatsapp: String,
        upi: String
    ) {
        viewModelScope.launch {
            val user = activeUser.value ?: run {
                showMessage("Please log in to join tournaments!")
                return@launch
            }
            val res = repository.joinTournament(tournamentId, user.id, inGameName, gameUid, whatsapp, upi)
            res.onSuccess {
                showMessage("Successfully Registered for Tournament! Good luck 🏆")
            }.onFailure {
                showMessage(it.message ?: "Failed to join tournament")
            }
        }
    }

    fun joinTournamentViaUpi(
        tournamentId: Int,
        inGameName: String,
        gameUid: String,
        whatsapp: String,
        upi: String,
        utrNumber: String
    ) {
        viewModelScope.launch {
            val user = activeUser.value ?: run {
                showMessage("Please log in to register!")
                return@launch
            }
            val res = repository.joinTournamentViaUpi(tournamentId, user.id, inGameName, gameUid, whatsapp, upi, utrNumber)
            res.onSuccess {
                showMessage("UTR Submitted! Pending Admin Approval. Room credentials will reveal upon verification. 🔥")
            }.onFailure {
                showMessage(it.message ?: "Failed to submit UTR registration")
            }
        }
    }

    fun approveUpiRegistration(registrationId: Int) {
        viewModelScope.launch {
            val res = repository.approveUpiRegistration(registrationId)
            res.onSuccess {
                showMessage("UPI Registration Approved & Synced to Firestore! Slot Confirmed. ✅")
            }.onFailure {
                showMessage("Approval Failed: ${it.message}")
            }
        }
    }

    // --- ADMIN ACTIONS ---
    fun createTournament(
        title: String,
        game: String,
        map: String,
        mode: String,
        matchTime: Long,
        entryFee: Double,
        prizePool: Double,
        perKillReward: Double,
        maxPlayers: Int,
        rules: String
    ) {
        viewModelScope.launch {
            val tournament = TournamentEntity(
                title = title,
                game = game,
                map = map,
                mode = mode,
                matchTime = matchTime,
                entryFee = entryFee,
                prizePool = prizePool,
                perKillReward = perKillReward,
                maxPlayers = maxPlayers,
                rules = rules,
                status = "UPCOMING"
            )
            repository.createTournament(tournament)
            showMessage("New Tournament Created! 🔥")
        }
    }

    fun updateTournamentStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateTournamentStatus(id, status)
            showMessage("Tournament status set to $status")
        }
    }

    fun updateRoomCredentials(id: Int, roomId: String, roomPass: String) {
        viewModelScope.launch {
            repository.updateRoomCredentials(id, roomId, roomPass)
            showMessage("Room ID & Password published to players! 🔑")
        }
    }

    fun updateParticipantResult(
        reg: RegistrationEntity,
        kills: Int,
        rank: Int,
        rankPrize: Double,
        perKillRate: Double
    ) {
        viewModelScope.launch {
            val res = repository.updateParticipantResult(reg, kills, rank, rankPrize, perKillRate)
            res.onSuccess { payout ->
                showMessage("Result Recorded! ₹${payout.toInt()} credited to ${reg.userInGameName} 🏆")
            }
        }
    }

    fun cancelTournament(tournamentId: Int) {
        viewModelScope.launch {
            val res = repository.cancelTournamentAndRefund(tournamentId)
            res.onSuccess { count ->
                showMessage("Match Cancelled. Full refunds issued to $count registered players! 💸")
            }
        }
    }

    fun updateParticipantLiveStats(
        reg: RegistrationEntity,
        kills: Int,
        survivalTimeMinutes: Int,
        rankPoints: Int,
        isAlive: Boolean,
        rank: Int
    ) {
        viewModelScope.launch {
            repository.updateParticipantLiveStats(reg, kills, survivalTimeMinutes, rankPoints, isAlive, rank)
            showMessage("Live stats updated for ${reg.userInGameName}! 🎯")
        }
    }

    fun simulateLiveMatchTick(tournamentId: Int) {
        viewModelScope.launch {
            val registrations = repository.getRegistrationsForTournamentFlow(tournamentId)
            // collect single emission or get list
            val currentList = allRegistrations.value.filter { it.tournamentId == tournamentId }
            if (currentList.isEmpty()) {
                showMessage("No registered players in this match to simulate!")
                return@launch
            }
            val alivePlayers = currentList.filter { it.isAlive }
            if (alivePlayers.isNotEmpty()) {
                val killer = alivePlayers.random()
                val newKills = killer.kills + 1
                val newSurvival = (killer.survivalTimeMinutes + 1).coerceAtMost(30)
                val newPts = killer.rankPoints + 15
                repository.updateParticipantLiveStats(
                    killer,
                    kills = newKills,
                    survivalTimeMinutes = newSurvival,
                    rankPoints = newPts,
                    isAlive = true,
                    rank = killer.rank
                )
                showMessage("⚡ LIVE UPDATE: ${killer.userInGameName} secured a kill! (+1 Kill, +15 Points)")
            } else {
                showMessage("Match concluded! All players eliminated or match ended.")
            }
        }
    }

    fun postAnnouncement(title: String, message: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.createAnnouncement(title, message, isPinned)
            showMessage("Announcement Posted! 📢")
        }
    }

    fun deleteAnnouncement(id: Int) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id)
            showMessage("Announcement removed.")
        }
    }

    fun updateAdminUpiId(newUpi: String) {
        viewModelScope.launch {
            repository.updateAdminUpiId(newUpi)
                .onSuccess { showMessage("Admin UPI Receiver ID Updated to '$newUpi'! 💳") }
                .onFailure { showMessage(it.message ?: "Failed to update UPI ID") }
        }
    }

    fun updateTournamentMaxPlayers(tournamentId: Int, maxSlots: Int) {
        viewModelScope.launch {
            repository.updateTournamentMaxPlayers(tournamentId, maxSlots)
                .onSuccess { showMessage("Slot Limit updated to $maxSlots players! ⚡") }
                .onFailure { showMessage(it.message ?: "Failed to update slot limit") }
        }
    }

    fun removeParticipant(registrationId: Int) {
        viewModelScope.launch {
            repository.removeParticipant(registrationId)
                .onSuccess { showMessage("Player removed from tournament roster. Slot freed up! 🚫") }
                .onFailure { showMessage(it.message ?: "Failed to remove player") }
        }
    }
}
