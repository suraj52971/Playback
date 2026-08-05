package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AnnouncementEntity
import com.example.data.local.entities.AppConfigEntity
import com.example.data.local.entities.RegistrationEntity
import com.example.data.local.entities.TournamentEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    fun getTotalUsersCountFlow(): Flow<Int>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUser(): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :identifier OR whatsappNumber = :identifier LIMIT 1")
    suspend fun getUserByEmailOrPhone(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE gameUid = :gameUid LIMIT 1")
    suspend fun getUserByGameUid(gameUid: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    suspend fun updatePassword(userId: Int, newPassword: String)

    @Query("UPDATE users SET walletBalance = walletBalance + :deltaAmount WHERE id = :userId")
    suspend fun updateWalletBalance(userId: Int, deltaAmount: Double)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY matchTime ASC")
    fun getAllTournamentsFlow(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    fun getTournamentByIdFlow(id: Int): Flow<TournamentEntity?>

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    suspend fun getTournamentById(id: Int): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity): Long

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Query("UPDATE tournaments SET status = :status WHERE id = :id")
    suspend fun updateTournamentStatus(id: Int, status: String)

    @Query("UPDATE tournaments SET maxPlayers = :maxPlayers WHERE id = :id")
    suspend fun updateTournamentMaxPlayers(id: Int, maxPlayers: Int)

    @Query("UPDATE tournaments SET roomId = :roomId, roomPassword = :roomPassword WHERE id = :id")
    suspend fun updateRoomCredentials(id: Int, roomId: String, roomPassword: String)

    @Query("DELETE FROM tournaments WHERE id = :id")
    suspend fun deleteTournament(id: Int)
}

@Dao
interface RegistrationDao {
    @Query("SELECT * FROM registrations")
    fun getAllRegistrationsFlow(): Flow<List<RegistrationEntity>>

    @Query("SELECT * FROM registrations WHERE paymentStatus = 'PENDING'")
    fun getPendingRegistrationsFlow(): Flow<List<RegistrationEntity>>

    @Query("SELECT * FROM registrations WHERE id = :id LIMIT 1")
    suspend fun getRegistrationById(id: Int): RegistrationEntity?

    @Query("SELECT * FROM registrations WHERE tournamentId = :tournamentId")
    fun getRegistrationsForTournamentFlow(tournamentId: Int): Flow<List<RegistrationEntity>>

    @Query("SELECT * FROM registrations WHERE tournamentId = :tournamentId")
    suspend fun getRegistrationsForTournament(tournamentId: Int): List<RegistrationEntity>

    @Query("SELECT * FROM registrations WHERE userId = :userId ORDER BY joinedAt DESC")
    fun getRegistrationsForUserFlow(userId: Int): Flow<List<RegistrationEntity>>

    @Query("SELECT COUNT(*) FROM registrations WHERE tournamentId = :tournamentId")
    fun getParticipantCountFlow(tournamentId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM registrations WHERE tournamentId = :tournamentId")
    suspend fun getParticipantCount(tournamentId: Int): Int

    @Query("SELECT * FROM registrations WHERE tournamentId = :tournamentId AND userId = :userId LIMIT 1")
    suspend fun getRegistration(tournamentId: Int, userId: Int): RegistrationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistration(registration: RegistrationEntity): Long

    @Update
    suspend fun updateRegistration(registration: RegistrationEntity)

    @Query("DELETE FROM registrations WHERE id = :id")
    suspend fun deleteRegistration(id: Int)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUserFlow(userId: Int): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY isPinned DESC, createdAt DESC")
    fun getAllAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: Int)
}

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AppConfigEntity?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: AppConfigEntity)
}
