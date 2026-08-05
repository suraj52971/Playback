package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AnnouncementEntity
import com.example.data.local.entities.AppConfigEntity
import com.example.data.local.entities.RegistrationEntity
import com.example.data.local.entities.TournamentEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserEntity
import com.example.data.sync.FirestoreSyncService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EsportsRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val tournamentDao = db.tournamentDao()
    private val registrationDao = db.registrationDao()
    private val transactionDao = db.transactionDao()
    private val announcementDao = db.announcementDao()
    private val appConfigDao = db.appConfigDao()

    val firestoreSyncService = FirestoreSyncService(db)

    val appConfigFlow: Flow<AppConfigEntity?> = appConfigDao.getConfigFlow()
    val allUsersFlow: Flow<List<UserEntity>> = userDao.getAllUsersFlow()

    fun startFirestoreSync(scope: CoroutineScope) {
        firestoreSyncService.startRealtimeTournamentSync(scope)
        firestoreSyncService.startRealtimeRegistrationSync(scope)
    }

    suspend fun syncAllToFirestore(): Result<String> = withContext(Dispatchers.IO) {
        val tourRes = firestoreSyncService.pushTournamentsToFirestore()
        val regRes = firestoreSyncService.pushRegistrationsToFirestore()

        if (tourRes.isSuccess && regRes.isSuccess) {
            Result.success("Synced ${tourRes.getOrDefault(0)} tournaments and ${regRes.getOrDefault(0)} registrations to Firestore!")
        } else {
            val errorMsg = tourRes.exceptionOrNull()?.message ?: regRes.exceptionOrNull()?.message ?: "Sync error"
            Result.failure(Exception("Firestore Sync completed with warning: $errorMsg"))
        }
    }

    // --- USER / AUTH ---
    val activeUserFlow: Flow<UserEntity?> = userDao.getActiveUserFlow()

    suspend fun getActiveUser(): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getActiveUser()
    }

    suspend fun registerUser(
        name: String,
        email: String,
        gameUid: String,
        inGameName: String,
        whatsappNumber: String,
        upiReceiverId: String,
        password: String = "123456",
        dob: String = "01/01/2000",
        recoveryPin: String = "1234"
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim().ifEmpty { "123456" }

        val existingEmail = userDao.getUserByEmail(cleanEmail)
        if (existingEmail != null) {
            return@withContext Result.failure(Exception("An account with this Email already exists!"))
        }

        val existingUid = userDao.getUserByGameUid(gameUid.trim())
        if (existingUid != null) {
            return@withContext Result.failure(Exception("An account with Game UID '$gameUid' is already registered!"))
        }

        // Firebase Authentication integration
        val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        if (auth != null && cleanEmail.contains("@")) {
            try {
                auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword).await()
                Log.d("EsportsRepository", "Successfully registered user with Firebase Auth: $cleanEmail")
            } catch (e: Exception) {
                Log.w("EsportsRepository", "Firebase Auth registration notice: ${e.message}")
                // If user exists in Firebase Auth, attempt sign-in
                try {
                    auth.signInWithEmailAndPassword(cleanEmail, cleanPassword).await()
                } catch (e2: Exception) {
                    Log.w("EsportsRepository", "Firebase Auth sign-in fallback notice: ${e2.message}")
                }
            }
        }

        // Log out any active user
        userDao.logoutAllUsers()

        val newUser = UserEntity(
            name = name.trim(),
            email = cleanEmail,
            gameUid = gameUid.trim(),
            inGameName = inGameName.trim(),
            whatsappNumber = whatsappNumber.trim(),
            upiReceiverId = upiReceiverId.trim(),
            password = cleanPassword,
            dob = dob.trim().ifEmpty { "01/01/2000" },
            recoveryPin = recoveryPin.trim().ifEmpty { "1234" },
            walletBalance = 100.0,
            isLoggedIn = true,
            isAdmin = false
        )

        val id = userDao.insertUser(newUser)
        val createdUser = newUser.copy(id = id.toInt())

        // SignUp bonus transaction
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = createdUser.id,
                amount = 100.0,
                type = "DEPOSIT",
                description = "Sign-Up Free Fire Welcome Bonus Credit",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(createdUser)
    }

    suspend fun loginUser(
        identifier: String,
        password: String = "",
        adminCode: String = ""
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanIdentifier = identifier.trim()
        val cleanPassword = password.trim()

        val user = userDao.getUserByEmailOrPhone(cleanIdentifier)
            ?: return@withContext Result.failure(Exception("No account found matching '$cleanIdentifier'"))

        val isAdminPasscodeMatch = adminCode.trim() == "surajsingh52971@"

        if (!isAdminPasscodeMatch && cleanPassword.isNotBlank() && user.password.isNotBlank() && user.password != cleanPassword) {
            return@withContext Result.failure(Exception("Incorrect password! Please try again or click Forgot Password."))
        }

        // Firebase Authentication integration
        val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        if (auth != null && cleanIdentifier.contains("@") && cleanPassword.isNotBlank()) {
            try {
                auth.signInWithEmailAndPassword(cleanIdentifier, cleanPassword).await()
                Log.d("EsportsRepository", "Successfully authenticated with Firebase Auth: $cleanIdentifier")
            } catch (e: Exception) {
                Log.w("EsportsRepository", "Firebase Auth login notice: ${e.message}")
            }
        }

        userDao.logoutAllUsers()
        val isElevatedAdmin = user.isAdmin || isAdminPasscodeMatch
        val updatedUser = user.copy(isLoggedIn = true, isAdmin = isElevatedAdmin)
        userDao.updateUser(updatedUser)
        Result.success(updatedUser)
    }

    suspend fun resetPasswordWithPin(
        identifier: String,
        dob: String,
        recoveryPin: String,
        newPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmailOrPhone(identifier.trim())
            ?: return@withContext Result.failure(Exception("Account not found with provided Email / WhatsApp number"))

        if (!user.dob.equals(dob.trim(), ignoreCase = true)) {
            return@withContext Result.failure(Exception("Date of Birth does not match our records!"))
        }

        if (user.recoveryPin != recoveryPin.trim()) {
            return@withContext Result.failure(Exception("Incorrect 4-Digit Recovery PIN!"))
        }

        userDao.updatePassword(user.id, newPassword.trim())
        Result.success(Unit)
    }

    suspend fun logoutUser() = withContext(Dispatchers.IO) {
        userDao.logoutAllUsers()
    }

    suspend fun updateUserProfile(
        name: String,
        inGameName: String,
        gameUid: String,
        whatsapp: String,
        upi: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val activeUser = userDao.getActiveUser()
            ?: return@withContext Result.failure(Exception("User not logged in"))

        val updated = activeUser.copy(
            name = name,
            inGameName = inGameName,
            gameUid = gameUid,
            whatsappNumber = whatsapp,
            upiReceiverId = upi
        )
        userDao.updateUser(updated)
        Result.success(Unit)
    }

    suspend fun toggleAdminMode(enableAdmin: Boolean) = withContext(Dispatchers.IO) {
        val activeUser = userDao.getActiveUser() ?: return@withContext
        userDao.updateUser(activeUser.copy(isAdmin = enableAdmin))
    }

    // --- WALLET & TRANSACTIONS ---
    fun getUserTransactionsFlow(userId: Int): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForUserFlow(userId)

    suspend fun depositFunds(userId: Int, amount: Double, paymentRef: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (amount <= 0) return@withContext Result.failure(Exception("Amount must be greater than ₹0"))

            userDao.updateWalletBalance(userId, amount)
            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = userId,
                    amount = amount,
                    type = "DEPOSIT",
                    description = "UPI Deposit (Ref: $paymentRef)",
                    timestamp = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        }

    suspend fun requestWithdrawal(userId: Int, amount: Double, upiId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val user = userDao.getUserById(userId)
                ?: return@withContext Result.failure(Exception("User not found"))

            if (amount > user.walletBalance) {
                return@withContext Result.failure(Exception("Insufficient wallet balance for withdrawal!"))
            }

            userDao.updateWalletBalance(userId, -amount)
            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = userId,
                    amount = -amount,
                    type = "WITHDRAWAL",
                    description = "Payout request to UPI: $upiId",
                    timestamp = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        }

    // --- TOURNAMENTS & REGISTRATION ---
    val allTournamentsFlow: Flow<List<TournamentEntity>> = tournamentDao.getAllTournamentsFlow()
    val allRegistrationsFlow: Flow<List<RegistrationEntity>> = registrationDao.getAllRegistrationsFlow()
    val pendingRegistrationsFlow: Flow<List<RegistrationEntity>> = registrationDao.getPendingRegistrationsFlow()

    fun getTournamentByIdFlow(id: Int): Flow<TournamentEntity?> = tournamentDao.getTournamentByIdFlow(id)

    fun getRegistrationsForTournamentFlow(tournamentId: Int): Flow<List<RegistrationEntity>> =
        registrationDao.getRegistrationsForTournamentFlow(tournamentId)

    fun getParticipantCountFlow(tournamentId: Int): Flow<Int> =
        registrationDao.getParticipantCountFlow(tournamentId)

    fun getUserRegistrationsFlow(userId: Int): Flow<List<RegistrationEntity>> =
        registrationDao.getRegistrationsForUserFlow(userId)

    suspend fun joinTournamentViaUpi(
        tournamentId: Int,
        userId: Int,
        inGameName: String,
        gameUid: String,
        whatsapp: String,
        upi: String,
        utrNumber: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val tournament = tournamentDao.getTournamentById(tournamentId)
            ?: return@withContext Result.failure(Exception("Tournament not found"))

        if (tournament.status != "UPCOMING") {
            return@withContext Result.failure(Exception("Registration is closed for this match!"))
        }

        val existing = registrationDao.getRegistration(tournamentId, userId)
        if (existing != null) {
            return@withContext Result.failure(Exception("You have already submitted entry for this match!"))
        }

        val newReg = RegistrationEntity(
            tournamentId = tournamentId,
            userId = userId,
            userGameUid = gameUid,
            userInGameName = inGameName,
            userWhatsapp = whatsapp,
            userUpi = upi,
            joinedAt = System.currentTimeMillis(),
            paymentStatus = "PENDING",
            utrNumber = utrNumber.trim(),
            paymentMethod = "DIRECT_UPI"
        )
        val regId = registrationDao.insertRegistration(newReg)
        val savedReg = newReg.copy(id = regId.toInt())
        firestoreSyncService.pushRegistrationToFirestore(savedReg)

        Result.success(Unit)
    }

    suspend fun approveUpiRegistration(registrationId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val reg = registrationDao.getRegistrationById(registrationId)
            ?: return@withContext Result.failure(Exception("Registration record not found"))

        val approved = reg.copy(paymentStatus = "APPROVED")
        registrationDao.updateRegistration(approved)
        firestoreSyncService.pushRegistrationToFirestore(approved)
        Result.success(Unit)
    }

    suspend fun joinTournament(
        tournamentId: Int,
        userId: Int,
        inGameName: String,
        gameUid: String,
        whatsapp: String,
        upi: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val tournament = tournamentDao.getTournamentById(tournamentId)
            ?: return@withContext Result.failure(Exception("Tournament not found"))

        if (tournament.status != "UPCOMING") {
            return@withContext Result.failure(Exception("Registration is closed for this match!"))
        }

        val currentCount = registrationDao.getParticipantCount(tournamentId)
        if (currentCount >= tournament.maxPlayers) {
            return@withContext Result.failure(Exception("Tournament is FULL! All slots taken."))
        }

        val existing = registrationDao.getRegistration(tournamentId, userId)
        if (existing != null) {
            return@withContext Result.failure(Exception("You have already joined this tournament!"))
        }

        val user = userDao.getUserById(userId)
            ?: return@withContext Result.failure(Exception("User session error"))

        if (user.walletBalance < tournament.entryFee) {
            return@withContext Result.failure(Exception("Insufficient wallet balance. Please add money to wallet!"))
        }

        // Deduct entry fee
        userDao.updateWalletBalance(userId, -tournament.entryFee)

        // Add entry fee transaction
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                amount = -tournament.entryFee,
                type = "ENTRY_FEE",
                description = "Entry fee for: ${tournament.title}",
                timestamp = System.currentTimeMillis()
            )
        )

        // Save registration
        val newReg = RegistrationEntity(
            tournamentId = tournamentId,
            userId = userId,
            userGameUid = gameUid,
            userInGameName = inGameName,
            userWhatsapp = whatsapp,
            userUpi = upi,
            joinedAt = System.currentTimeMillis()
        )
        val regId = registrationDao.insertRegistration(newReg)
        firestoreSyncService.pushRegistrationToFirestore(newReg.copy(id = regId.toInt()))

        Result.success(Unit)
    }

    // --- ADMIN ACTIONS ---
    suspend fun createTournament(tournament: TournamentEntity): Result<Long> = withContext(Dispatchers.IO) {
        val id = tournamentDao.insertTournament(tournament)
        val saved = tournament.copy(id = id.toInt())
        firestoreSyncService.pushTournamentToFirestore(saved)
        Result.success(id)
    }

    suspend fun updateTournamentStatus(id: Int, status: String) = withContext(Dispatchers.IO) {
        tournamentDao.updateTournamentStatus(id, status)
        tournamentDao.getTournamentById(id)?.let { t ->
            firestoreSyncService.pushTournamentToFirestore(t)
        }
    }

    suspend fun updateRoomCredentials(id: Int, roomId: String, roomPassword: String) = withContext(Dispatchers.IO) {
        tournamentDao.updateRoomCredentials(id, roomId, roomPassword)
        tournamentDao.getTournamentById(id)?.let { t ->
            firestoreSyncService.pushTournamentToFirestore(t)
        }
    }

    suspend fun updateParticipantResult(
        reg: RegistrationEntity,
        kills: Int,
        rank: Int,
        rankPrize: Double,
        perKillRate: Double,
        survivalTimeMinutes: Int = 15,
        rankPoints: Int = 0
    ): Result<Double> = withContext(Dispatchers.IO) {
        val killReward = kills * perKillRate
        val totalWinnings = rankPrize + killReward
        val calculatedPoints = if (rankPoints > 0) rankPoints else (kills * 10 + (50 - rank).coerceAtLeast(0) * 2 + survivalTimeMinutes * 3)

        val updatedReg = reg.copy(
            kills = kills,
            rank = rank,
            payoutAmount = totalWinnings,
            survivalTimeMinutes = survivalTimeMinutes,
            rankPoints = calculatedPoints,
            isAlive = false
        )

        registrationDao.updateRegistration(updatedReg)
        firestoreSyncService.pushRegistrationToFirestore(updatedReg)

        if (totalWinnings > 0) {
            userDao.updateWalletBalance(reg.userId, totalWinnings)
            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = reg.userId,
                    amount = totalWinnings,
                    type = "WINNINGS",
                    description = "Winnings Payout (Rank #$rank, $kills Kills)",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        Result.success(totalWinnings)
    }

    suspend fun updateParticipantLiveStats(
        reg: RegistrationEntity,
        kills: Int,
        survivalTimeMinutes: Int,
        rankPoints: Int,
        isAlive: Boolean,
        rank: Int
    ) = withContext(Dispatchers.IO) {
        val updated = reg.copy(
            kills = kills,
            survivalTimeMinutes = survivalTimeMinutes,
            rankPoints = rankPoints,
            isAlive = isAlive,
            rank = rank
        )
        registrationDao.updateRegistration(updated)
        firestoreSyncService.pushRegistrationToFirestore(updated)
    }

    suspend fun cancelTournamentAndRefund(tournamentId: Int): Result<Int> = withContext(Dispatchers.IO) {
        val tournament = tournamentDao.getTournamentById(tournamentId)
            ?: return@withContext Result.failure(Exception("Tournament not found"))

        val registrations = registrationDao.getRegistrationsForTournament(tournamentId)
        var refundCount = 0

        for (reg in registrations) {
            if (!reg.isRefunded) {
                userDao.updateWalletBalance(reg.userId, tournament.entryFee)
                transactionDao.insertTransaction(
                    TransactionEntity(
                        userId = reg.userId,
                        amount = tournament.entryFee,
                        type = "REFUND",
                        description = "Automated Refund: Match Cancelled (${tournament.title})",
                        timestamp = System.currentTimeMillis()
                    )
                )
                val refundedReg = reg.copy(isRefunded = true)
                registrationDao.updateRegistration(refundedReg)
                firestoreSyncService.pushRegistrationToFirestore(refundedReg)
                refundCount++
            }
        }

        tournamentDao.updateTournamentStatus(tournamentId, "CANCELLED")
        tournamentDao.getTournamentById(tournamentId)?.let { t ->
            firestoreSyncService.pushTournamentToFirestore(t)
        }
        Result.success(refundCount)
    }

    suspend fun deleteTournament(id: Int) = withContext(Dispatchers.IO) {
        tournamentDao.deleteTournament(id)
    }

    // --- ANNOUNCEMENTS ---
    val announcementsFlow: Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncementsFlow()

    suspend fun createAnnouncement(title: String, message: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        announcementDao.insertAnnouncement(
            AnnouncementEntity(
                title = title,
                message = message,
                createdAt = System.currentTimeMillis(),
                isPinned = isPinned
            )
        )
    }

    suspend fun deleteAnnouncement(id: Int) = withContext(Dispatchers.IO) {
        announcementDao.deleteAnnouncement(id)
    }

    // --- APP CONFIG & ADMIN CONTROLS ---
    suspend fun updateAdminUpiId(newUpiId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (newUpiId.isBlank() || !newUpiId.contains("@")) {
            return@withContext Result.failure(Exception("Please enter a valid UPI ID (e.g. name@upi)"))
        }
        val currentConfig = appConfigDao.getConfig() ?: AppConfigEntity()
        appConfigDao.saveConfig(currentConfig.copy(adminUpiId = newUpiId.trim()))
        Result.success(Unit)
    }

    suspend fun updateTournamentMaxPlayers(tournamentId: Int, maxPlayers: Int): Result<Unit> = withContext(Dispatchers.IO) {
        if (maxPlayers < 1) {
            return@withContext Result.failure(Exception("Max slot limit must be at least 1!"))
        }
        tournamentDao.updateTournamentMaxPlayers(tournamentId, maxPlayers)
        tournamentDao.getTournamentById(tournamentId)?.let { t ->
            firestoreSyncService.pushTournamentToFirestore(t)
        }
        Result.success(Unit)
    }

    suspend fun removeParticipant(registrationId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val reg = registrationDao.getRegistrationById(registrationId)
        if (reg != null) {
            registrationDao.deleteRegistration(registrationId)
            firestoreSyncService.pushRegistrationToFirestore(reg.copy(paymentStatus = "REMOVED"))
            Result.success(Unit)
        } else {
            Result.failure(Exception("Registration record not found!"))
        }
    }
}
