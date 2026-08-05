package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AnnouncementDao
import com.example.data.local.dao.AppConfigDao
import com.example.data.local.dao.RegistrationDao
import com.example.data.local.dao.TournamentDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entities.AnnouncementEntity
import com.example.data.local.entities.AppConfigEntity
import com.example.data.local.entities.RegistrationEntity
import com.example.data.local.entities.TournamentEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TournamentEntity::class,
        RegistrationEntity::class,
        TransactionEntity::class,
        AnnouncementEntity::class,
        AppConfigEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun registrationDao(): RegistrationDao
    abstract fun transactionDao(): TransactionDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun appConfigDao(): AppConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "playzone_esports_database"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val userDao = db.userDao()
            val tournamentDao = db.tournamentDao()
            val announcementDao = db.announcementDao()
            val transactionDao = db.transactionDao()
            val appConfigDao = db.appConfigDao()

            // Initialize App Configuration
            appConfigDao.saveConfig(AppConfigEntity(id = 1, adminUpiId = "suraj52971-2@okaxis"))

            // Default User (Not logged in by default)
            val defaultUser = UserEntity(
                id = 1,
                name = "Suraj Singh",
                email = "singhsuraj52971@gmail.com",
                gameUid = "5820194832",
                inGameName = "⚡SURAJ_FF⚡",
                whatsappNumber = "+919876543210",
                upiReceiverId = "suraj52971-2@okaxis",
                password = "password123",
                dob = "01/01/2000",
                recoveryPin = "1234",
                walletBalance = 250.0,
                isLoggedIn = false,
                isAdmin = false
            )
            userDao.insertUser(defaultUser)

            // Initial Welcome Transaction
            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = 1,
                    amount = 250.0,
                    type = "DEPOSIT",
                    description = "Welcome SignUp Bonus & Starter Wallet Credit",
                    timestamp = System.currentTimeMillis() - 86400000L
                )
            )

            // Initial Tournaments
            val now = System.currentTimeMillis()

            val ffTournament1 = TournamentEntity(
                id = 1,
                title = "Free Fire Battle Royale Grand Clash #101",
                game = "Free Fire",
                map = "Bermuda",
                mode = "Solo",
                matchTime = now + 1800000L, // 30 mins from now
                entryFee = 30.0,
                prizePool = 1200.0,
                perKillReward = 15.0,
                maxPlayers = 48,
                status = "UPCOMING",
                roomId = "849201",
                roomPassword = "789",
                rules = "1. Only mobile players allowed.\n2. Team-up will lead to permanent ban.\n3. Take screenshot of victory for verification."
            )

            val bgmiTournament = TournamentEntity(
                id = 2,
                title = "BGMI Erangel Squad Championship #204",
                game = "BGMI",
                map = "Erangel",
                mode = "Squad",
                matchTime = now + 7200000L, // 2 hours from now
                entryFee = 50.0,
                prizePool = 2000.0,
                perKillReward = 20.0,
                maxPlayers = 100,
                status = "UPCOMING",
                roomId = "",
                roomPassword = "",
                rules = "1. All squad members must register with valid BGMI UIDs.\n2. Flare guns & glitches prohibited."
            )

            val ffTournament2 = TournamentEntity(
                id = 3,
                title = "Free Fire Clash Squad 4v4 Showdown",
                game = "Free Fire",
                map = "Kalahari",
                mode = "Duo",
                matchTime = now + 86400000L, // Tomorrow
                entryFee = 20.0,
                prizePool = 800.0,
                perKillReward = 10.0,
                maxPlayers = 24,
                status = "UPCOMING",
                roomId = "",
                roomPassword = "",
                rules = "1. Character skills allowed.\n2. Limited ammo mode active."
            )

            val codTournament = TournamentEntity(
                id = 4,
                title = "COD Mobile Sniper Only Warfare #12",
                game = "Call of Duty",
                map = "Crash",
                mode = "Solo",
                matchTime = now - 3600000L, // Completed 1 hour ago
                entryFee = 40.0,
                prizePool = 1500.0,
                perKillReward = 25.0,
                maxPlayers = 30,
                status = "COMPLETED",
                roomId = "550123",
                roomPassword = "321",
                rules = "Sniper weapons only."
            )

            val ffLiveTournament = TournamentEntity(
                id = 5,
                title = "Free Fire Pro Arena Live Showdown #05",
                game = "Free Fire",
                map = "Purgatory",
                mode = "Solo",
                matchTime = now - 600000L, // Started 10 mins ago
                entryFee = 50.0,
                prizePool = 2500.0,
                perKillReward = 20.0,
                maxPlayers = 48,
                status = "LIVE",
                roomId = "994012",
                roomPassword = "555",
                rules = "Live Match in progress! Leaderboards update in real-time."
            )

            tournamentDao.insertTournament(ffTournament1)
            tournamentDao.insertTournament(bgmiTournament)
            tournamentDao.insertTournament(ffTournament2)
            tournamentDao.insertTournament(codTournament)
            tournamentDao.insertTournament(ffLiveTournament)

            val regDao = db.registrationDao()

            // Auto-register default user for FF Tournament #101
            regDao.insertRegistration(
                RegistrationEntity(
                    tournamentId = 1,
                    userId = 1,
                    userGameUid = "5820194832",
                    userInGameName = "⚡SURAJ_FF⚡",
                    userWhatsapp = "+919876543210",
                    userUpi = "suraj52971@upi",
                    joinedAt = System.currentTimeMillis() - 3600000L,
                    kills = 0,
                    rank = 0,
                    payoutAmount = 0.0,
                    survivalTimeMinutes = 0,
                    rankPoints = 0
                )
            )

            // Seed participants for COMPLETED match (COD Mobile Sniper #12, tournamentId = 4)
            val codWinners = listOf(
                RegistrationEntity(tournamentId = 4, userId = 101, userGameUid = "771029102", userInGameName = "👑 Vektor_Sniper", userWhatsapp = "+919811111111", userUpi = "vektor@upi", kills = 12, rank = 1, payoutAmount = 800.0, survivalTimeMinutes = 22, rankPoints = 120, isAlive = false),
                RegistrationEntity(tournamentId = 4, userId = 1, userGameUid = "5820194832", userInGameName = "⚡SURAJ_FF⚡", userWhatsapp = "+919876543210", userUpi = "suraj52971@upi", kills = 8, rank = 2, payoutAmount = 450.0, survivalTimeMinutes = 20, rankPoints = 95, isAlive = false),
                RegistrationEntity(tournamentId = 4, userId = 103, userGameUid = "881023910", userInGameName = "🔥 DeadShot_Pro", userWhatsapp = "+919833333333", userUpi = "deadshot@upi", kills = 6, rank = 3, payoutAmount = 250.0, survivalTimeMinutes = 18, rankPoints = 75, isAlive = false),
                RegistrationEntity(tournamentId = 4, userId = 104, userGameUid = "990182391", userInGameName = "🎯 Shadow_Ninja", userWhatsapp = "+919844444444", userUpi = "shadow@upi", kills = 4, rank = 4, payoutAmount = 100.0, survivalTimeMinutes = 15, rankPoints = 55, isAlive = false),
                RegistrationEntity(tournamentId = 4, userId = 105, userGameUid = "661029310", userInGameName = "⚡ BlazeRider", userWhatsapp = "+919855555555", userUpi = "blaze@upi", kills = 3, rank = 5, payoutAmount = 75.0, survivalTimeMinutes = 12, rankPoints = 40, isAlive = false)
            )
            codWinners.forEach { regDao.insertRegistration(it) }

            // Seed participants for LIVE match (Free Fire Live Showdown #05, tournamentId = 5)
            val livePlayers = listOf(
                RegistrationEntity(tournamentId = 5, userId = 1, userGameUid = "5820194832", userInGameName = "⚡SURAJ_FF⚡", userWhatsapp = "+919876543210", userUpi = "suraj52971@upi", kills = 9, rank = 1, payoutAmount = 0.0, survivalTimeMinutes = 14, rankPoints = 110, isAlive = true),
                RegistrationEntity(tournamentId = 5, userId = 106, userGameUid = "441029381", userInGameName = "💥 Phoenix_God", userWhatsapp = "+919866666666", userUpi = "phoenix@upi", kills = 7, rank = 2, payoutAmount = 0.0, survivalTimeMinutes = 14, rankPoints = 90, isAlive = true),
                RegistrationEntity(tournamentId = 5, userId = 107, userGameUid = "331029482", userInGameName = "🛡️ Titan_Gamer", userWhatsapp = "+919877777777", userUpi = "titan@upi", kills = 5, rank = 3, payoutAmount = 0.0, survivalTimeMinutes = 13, rankPoints = 70, isAlive = true),
                RegistrationEntity(tournamentId = 5, userId = 108, userGameUid = "221029483", userInGameName = "🏹 ArrowX", userWhatsapp = "+919888888888", userUpi = "arrow@upi", kills = 3, rank = 4, payoutAmount = 0.0, survivalTimeMinutes = 11, rankPoints = 50, isAlive = false),
                RegistrationEntity(tournamentId = 5, userId = 109, userGameUid = "111029484", userInGameName = "☠️ Venom_Slayer", userWhatsapp = "+919899999999", userUpi = "venom@upi", kills = 2, rank = 5, payoutAmount = 0.0, survivalTimeMinutes = 8, rankPoints = 35, isAlive = false)
            )
            livePlayers.forEach { regDao.insertRegistration(it) }

            // Announcements
            announcementDao.insertAnnouncement(
                AnnouncementEntity(
                    id = 1,
                    title = "🔥 Season 5 Free Fire Mega League Announced!",
                    message = "Participate in daily tournaments to climb the leaderboard. Total prize pool ₹50,000 this month! Room credentials released 15 mins before match.",
                    isPinned = true
                )
            )
            announcementDao.insertAnnouncement(
                AnnouncementEntity(
                    id = 2,
                    title = "⚡ Instant Wallet Payouts Active",
                    message = "All winnings and kill rewards are automatically credited to your PlayZone Wallet right after match result validation.",
                    isPinned = false
                )
            )
        }
    }
}
