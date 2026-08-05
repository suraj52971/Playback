package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["gameUid"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val gameUid: String,
    val inGameName: String,
    val whatsappNumber: String,
    val upiReceiverId: String,
    val password: String = "123456",
    val dob: String = "01/01/2000",
    val recoveryPin: String = "1234",
    val walletBalance: Double = 100.0, // Initial sign up bonus ₹100
    val isLoggedIn: Boolean = true,
    val isAdmin: Boolean = false
)

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val game: String = "Free Fire", // Default Free Fire
    val map: String = "Bermuda", // Bermuda, Kalahari, Purgatory, Alpine, Nexterra, CS 4v4
    val mode: String = "Solo", // Solo, Duo, Squad
    val matchTime: Long, // Epoch timestamp in ms
    val entryFee: Double,
    val prizePool: Double,
    val perKillReward: Double,
    val maxPlayers: Int = 48,
    val status: String = "UPCOMING", // "UPCOMING", "LIVE", "COMPLETED", "CANCELLED"
    val roomId: String = "",
    val roomPassword: String = "",
    val rules: String = "1. Mobile only (No Emulators/Hacks).\n2. Room details revealed 15 min prior.\n3. Send victory screenshot for payout."
)

@Entity(
    tableName = "registrations",
    indices = [
        Index(value = ["tournamentId", "userId"], unique = true)
    ]
)
data class RegistrationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tournamentId: Int,
    val userId: Int,
    val userGameUid: String,
    val userInGameName: String,
    val userWhatsapp: String,
    val userUpi: String,
    val joinedAt: Long = System.currentTimeMillis(),
    val kills: Int = 0,
    val rank: Int = 0,
    val payoutAmount: Double = 0.0,
    val isRefunded: Boolean = false,
    val survivalTimeMinutes: Int = 0,
    val rankPoints: Int = 0,
    val isAlive: Boolean = true,
    val paymentStatus: String = "APPROVED", // "APPROVED", "PENDING"
    val utrNumber: String = "",
    val paymentMethod: String = "WALLET" // "WALLET", "DIRECT_UPI"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val type: String, // "DEPOSIT", "ENTRY_FEE", "KILL_REWARD", "WINNINGS", "REFUND", "WITHDRAWAL"
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS"
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val adminUpiId: String = "suraj52971-2@okaxis",
    val supportPhone: String = "+919876543210"
)
