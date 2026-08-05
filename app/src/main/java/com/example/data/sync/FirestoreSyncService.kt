package com.example.data.sync

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entities.RegistrationEntity
import com.example.data.local.entities.TournamentEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Service to handle synchronization between local Room SQLite database
 * and Firebase Firestore for tournament and registration data.
 */
class FirestoreSyncService(private val db: AppDatabase) {

    private val tag = "FirestoreSyncService"

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "FirebaseApp is not initialized: ${e.message}")
            null
        }
    }

    private val tournamentDao = db.tournamentDao()
    private val registrationDao = db.registrationDao()

    /**
     * Pushes all local Room tournaments to Firebase Firestore.
     */
    suspend fun pushTournamentsToFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext Result.failure(
            IllegalStateException("Firebase is not initialized. Please add google-services.json or initialize Firebase.")
        )
        try {
            val tournaments = tournamentDao.getAllTournamentsFlow().first()
            var count = 0
            for (t in tournaments) {
                val docData = hashMapOf(
                    "id" to t.id,
                    "title" to t.title,
                    "game" to t.game,
                    "map" to t.map,
                    "mode" to t.mode,
                    "matchTime" to t.matchTime,
                    "entryFee" to t.entryFee,
                    "prizePool" to t.prizePool,
                    "perKillReward" to t.perKillReward,
                    "maxPlayers" to t.maxPlayers,
                    "status" to t.status,
                    "roomId" to t.roomId,
                    "roomPassword" to t.roomPassword,
                    "rules" to t.rules
                )
                fs.collection("tournaments")
                    .document(t.id.toString())
                    .set(docData, SetOptions.merge())
                    .await()
                count++
            }
            Log.d(tag, "Successfully pushed $count tournaments to Firestore.")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(tag, "Failed to push tournaments to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Pushes all local Room registrations to Firebase Firestore.
     */
    suspend fun pushRegistrationsToFirestore(): Result<Int> = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext Result.failure(
            IllegalStateException("Firebase is not initialized. Please add google-services.json or initialize Firebase.")
        )
        try {
            val registrations = registrationDao.getAllRegistrationsFlow().first()
            var count = 0
            for (reg in registrations) {
                val docData = hashMapOf(
                    "id" to reg.id,
                    "tournamentId" to reg.tournamentId,
                    "userId" to reg.userId,
                    "userGameUid" to reg.userGameUid,
                    "userInGameName" to reg.userInGameName,
                    "userWhatsapp" to reg.userWhatsapp,
                    "userUpi" to reg.userUpi,
                    "joinedAt" to reg.joinedAt,
                    "kills" to reg.kills,
                    "rank" to reg.rank,
                    "payoutAmount" to reg.payoutAmount,
                    "isRefunded" to reg.isRefunded,
                    "survivalTimeMinutes" to reg.survivalTimeMinutes,
                    "rankPoints" to reg.rankPoints,
                    "isAlive" to reg.isAlive
                )
                fs.collection("registrations")
                    .document(reg.id.toString())
                    .set(docData, SetOptions.merge())
                    .await()
                count++
            }
            Log.d(tag, "Successfully pushed $count registrations to Firestore.")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(tag, "Failed to push registrations to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Pushes a single tournament entity to Firebase Firestore in real-time.
     */
    suspend fun pushTournamentToFirestore(t: TournamentEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext Result.failure(
            IllegalStateException("Firebase is not initialized.")
        )
        try {
            val docData = hashMapOf(
                "id" to t.id,
                "title" to t.title,
                "game" to t.game,
                "map" to t.map,
                "mode" to t.mode,
                "matchTime" to t.matchTime,
                "entryFee" to t.entryFee,
                "prizePool" to t.prizePool,
                "perKillReward" to t.perKillReward,
                "maxPlayers" to t.maxPlayers,
                "status" to t.status,
                "roomId" to t.roomId,
                "roomPassword" to t.roomPassword,
                "rules" to t.rules
            )
            fs.collection("tournaments")
                .document(t.id.toString())
                .set(docData, SetOptions.merge())
                .await()
            Log.d(tag, "Pushed tournament ID ${t.id} to Firestore.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to push tournament ${t.id} to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Pushes a single registration entity to Firebase Firestore in real-time.
     */
    suspend fun pushRegistrationToFirestore(reg: RegistrationEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext Result.failure(
            IllegalStateException("Firebase is not initialized.")
        )
        try {
            val docData = hashMapOf(
                "id" to reg.id,
                "tournamentId" to reg.tournamentId,
                "userId" to reg.userId,
                "userGameUid" to reg.userGameUid,
                "userInGameName" to reg.userInGameName,
                "userWhatsapp" to reg.userWhatsapp,
                "userUpi" to reg.userUpi,
                "joinedAt" to reg.joinedAt,
                "kills" to reg.kills,
                "rank" to reg.rank,
                "payoutAmount" to reg.payoutAmount,
                "isRefunded" to reg.isRefunded,
                "survivalTimeMinutes" to reg.survivalTimeMinutes,
                "rankPoints" to reg.rankPoints,
                "isAlive" to reg.isAlive
            )
            fs.collection("registrations")
                .document(reg.id.toString())
                .set(docData, SetOptions.merge())
                .await()
            Log.d(tag, "Pushed registration ID ${reg.id} to Firestore.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to push registration ${reg.id} to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Listens to real-time changes in Firestore tournaments collection and updates Room.
     */
    fun startRealtimeTournamentSync(scope: CoroutineScope) {
        val fs = getFirestore() ?: return
        try {
            fs.collection("tournaments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Firestore tournament listener failed", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            for (doc in snapshot.documents) {
                                val id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: continue
                                val title = doc.getString("title") ?: continue
                                val game = doc.getString("game") ?: "Free Fire"
                                val map = doc.getString("map") ?: "Bermuda"
                                val mode = doc.getString("mode") ?: "Solo"
                                val matchTime = doc.getLong("matchTime") ?: System.currentTimeMillis()
                                val entryFee = doc.getDouble("entryFee") ?: 0.0
                                val prizePool = doc.getDouble("prizePool") ?: 0.0
                                val perKillReward = doc.getDouble("perKillReward") ?: 0.0
                                val maxPlayers = doc.getLong("maxPlayers")?.toInt() ?: 48
                                val status = doc.getString("status") ?: "UPCOMING"
                                val roomId = doc.getString("roomId") ?: ""
                                val roomPassword = doc.getString("roomPassword") ?: ""
                                val rules = doc.getString("rules") ?: ""

                                val entity = TournamentEntity(
                                    id = id,
                                    title = title,
                                    game = game,
                                    map = map,
                                    mode = mode,
                                    matchTime = matchTime,
                                    entryFee = entryFee,
                                    prizePool = prizePool,
                                    perKillReward = perKillReward,
                                    maxPlayers = maxPlayers,
                                    status = status,
                                    roomId = roomId,
                                    roomPassword = roomPassword,
                                    rules = rules
                                )
                                tournamentDao.insertTournament(entity)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Firestore listener", e)
        }
    }

    /**
     * Listens to real-time changes in Firestore registrations collection and updates Room.
     */
    fun startRealtimeRegistrationSync(scope: CoroutineScope) {
        val fs = getFirestore() ?: return
        try {
            fs.collection("registrations")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Firestore registration listener failed", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            for (doc in snapshot.documents) {
                                val id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: continue
                                val tournamentId = doc.getLong("tournamentId")?.toInt() ?: 0
                                val userId = doc.getLong("userId")?.toInt() ?: 0
                                val userGameUid = doc.getString("userGameUid") ?: ""
                                val userInGameName = doc.getString("userInGameName") ?: ""
                                val userWhatsapp = doc.getString("userWhatsapp") ?: ""
                                val userUpi = doc.getString("userUpi") ?: ""
                                val joinedAt = doc.getLong("joinedAt") ?: System.currentTimeMillis()
                                val kills = doc.getLong("kills")?.toInt() ?: 0
                                val rank = doc.getLong("rank")?.toInt() ?: 0
                                val payoutAmount = doc.getDouble("payoutAmount") ?: 0.0
                                val isRefunded = doc.getBoolean("isRefunded") ?: false
                                val survivalTimeMinutes = doc.getLong("survivalTimeMinutes")?.toInt() ?: 0
                                val rankPoints = doc.getLong("rankPoints")?.toInt() ?: 0
                                val isAlive = doc.getBoolean("isAlive") ?: true

                                val regEntity = RegistrationEntity(
                                    id = id,
                                    tournamentId = tournamentId,
                                    userId = userId,
                                    userGameUid = userGameUid,
                                    userInGameName = userInGameName,
                                    userWhatsapp = userWhatsapp,
                                    userUpi = userUpi,
                                    joinedAt = joinedAt,
                                    kills = kills,
                                    rank = rank,
                                    payoutAmount = payoutAmount,
                                    isRefunded = isRefunded,
                                    survivalTimeMinutes = survivalTimeMinutes,
                                    rankPoints = rankPoints,
                                    isAlive = isAlive
                                )
                                registrationDao.insertRegistration(regEntity)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Firestore registration listener", e)
        }
    }
}
