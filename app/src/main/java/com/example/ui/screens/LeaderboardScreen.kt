package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.RegistrationEntity
import com.example.data.local.entities.TournamentEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.*

enum class LeaderboardSortCategory {
    POINTS,
    KILLS,
    SURVIVAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    activeUser: UserEntity?,
    tournaments: List<TournamentEntity>,
    allRegistrations: List<RegistrationEntity>,
    selectedTournamentId: Int?,
    onSelectTournamentId: (Int?) -> Unit,
    onSimulateLiveTick: (Int) -> Unit,
    onUpdateLiveStats: (RegistrationEntity, Int, Int, Int, Boolean, Int) -> Unit
) {
    var sortCategory by remember { mutableStateOf(LeaderboardSortCategory.POINTS) }
    var searchQuery by remember { mutableStateOf("") }
    var editingParticipant by remember { mutableStateOf<RegistrationEntity?>(null) }

    // Currently selected tournament
    val currentTournament = tournaments.find { it.id == selectedTournamentId }

    // Filter registrations by selected tournament
    val tournamentRegistrations = remember(allRegistrations, selectedTournamentId) {
        if (selectedTournamentId == null) {
            allRegistrations
        } else {
            allRegistrations.filter { it.tournamentId == selectedTournamentId }
        }
    }

    // Apply search & sorting
    val filteredAndSortedRegistrations = remember(tournamentRegistrations, sortCategory, searchQuery) {
        tournamentRegistrations
            .filter { reg ->
                searchQuery.isBlank() ||
                reg.userInGameName.contains(searchQuery, ignoreCase = true) ||
                reg.userGameUid.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith { a, b ->
                when (sortCategory) {
                    LeaderboardSortCategory.POINTS -> {
                        val scoreA = a.rankPoints.takeIf { it > 0 } ?: (a.kills * 10 + (50 - a.rank).coerceAtLeast(0) * 2 + a.survivalTimeMinutes * 3)
                        val scoreB = b.rankPoints.takeIf { it > 0 } ?: (b.kills * 10 + (50 - b.rank).coerceAtLeast(0) * 2 + b.survivalTimeMinutes * 3)
                        scoreB.compareTo(scoreA)
                    }
                    LeaderboardSortCategory.KILLS -> b.kills.compareTo(a.kills)
                    LeaderboardSortCategory.SURVIVAL -> b.survivalTimeMinutes.compareTo(a.survivalTimeMinutes)
                }
            }
    }

    // My registration if available
    val myRegistration = remember(filteredAndSortedRegistrations, activeUser) {
        if (activeUser == null) null
        else filteredAndSortedRegistrations.find { it.userId == activeUser.id || it.userGameUid == activeUser.gameUid }
    }

    Scaffold(
        containerColor = DarkBackground,
        modifier = Modifier.testTag("leaderboard_screen")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                // Header
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = NeonCyan,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Leaderboard,
                                            contentDescription = "Leaderboard Icon",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "LEADERBOARD",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Real-Time Standings & Player Rankings",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (currentTournament?.status == "LIVE") {
                                Surface(
                                    color = CrimsonRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(20.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(CrimsonRed)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "LIVE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = CrimsonRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tournament Selector Chips
                item {
                    Column {
                        Text(
                            text = "SELECT TOURNAMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                val isSelected = selectedTournamentId == null
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectTournamentId(null) },
                                    label = { Text("🌍 ALL MATCHES (GLOBAL)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan,
                                        selectedLabelColor = Color.White,
                                        containerColor = DarkSurface,
                                        labelColor = TextSecondary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("filter_all_leaderboard")
                                )
                            }
                            items(tournaments) { tournament ->
                                val isSelected = selectedTournamentId == tournament.id
                                val statusDot = when (tournament.status) {
                                    "LIVE" -> "🔴 LIVE: "
                                    "COMPLETED" -> "🏁 "
                                    else -> "⏳ "
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectTournamentId(tournament.id) },
                                    label = {
                                        Text(
                                            text = "$statusDot${tournament.title}",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (tournament.status == "LIVE") CrimsonRed else NeonCyan,
                                        selectedLabelColor = Color.White,
                                        containerColor = DarkSurface,
                                        labelColor = TextSecondary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("filter_tournament_${tournament.id}")
                                )
                            }
                        }
                    }
                }

                // Selected Tournament Summary Card
                currentTournament?.let { tournament ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tournament.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${tournament.game} • ${tournament.map} • ${tournament.mode}",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Surface(
                                        color = when (tournament.status) {
                                            "LIVE" -> CrimsonRed
                                            "COMPLETED" -> EmeraldGreen
                                            else -> GoldAmber
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = tournament.status,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = DarkSurfaceVariant)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Prize Pool", fontSize = 10.sp, color = TextMuted)
                                        Text("₹${tournament.prizePool.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldAmber)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Kill Bounty", fontSize = 10.sp, color = TextMuted)
                                        Text("₹${tournament.perKillReward.toInt()}/kill", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Participants", fontSize = 10.sp, color = TextMuted)
                                        Text("${tournamentRegistrations.size}/${tournament.maxPlayers}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                }

                                if (tournament.status == "LIVE" || activeUser?.isAdmin == true) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (tournament.status == "LIVE") {
                                            Button(
                                                onClick = { onSimulateLiveTick(tournament.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("simulate_live_kill_button")
                                            ) {
                                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("SIMULATE LIVE KILL 🔥", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Top 3 Podium Cards (if enough participants exist)
                if (filteredAndSortedRegistrations.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "🏆 TOP STANDINGS PODIUM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // 2nd Place
                                if (filteredAndSortedRegistrations.size >= 2) {
                                    val second = filteredAndSortedRegistrations[1]
                                    PodiumCard(
                                        rank = 2,
                                        registration = second,
                                        medalColor = PrimaryContainerColor,
                                        badgeText = "2ND PLACE",
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                // 1st Place (Center & Taller)
                                val first = filteredAndSortedRegistrations[0]
                                PodiumCard(
                                    rank = 1,
                                    registration = first,
                                    medalColor = PrimaryContainerColor,
                                    badgeText = "👑 BOOYAH #1",
                                    isFirst = true,
                                    modifier = Modifier.weight(1.1f)
                                )

                                // 3rd Place
                                if (filteredAndSortedRegistrations.size >= 3) {
                                    val third = filteredAndSortedRegistrations[2]
                                    PodiumCard(
                                        rank = 3,
                                        registration = third,
                                        medalColor = PrimaryContainerColor,
                                        badgeText = "3RD PLACE",
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Search & Sort Bar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search player by In-Game Name or UID...", fontSize = 13.sp, color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("leaderboard_search_input")
                        )

                        // Sorting Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface, RoundedCornerShape(10.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LeaderboardSortCategory.values().forEach { category ->
                                val isSelected = sortCategory == category
                                val label = when (category) {
                                    LeaderboardSortCategory.POINTS -> "⭐ Rank Points"
                                    LeaderboardSortCategory.KILLS -> "🎯 Most Kills"
                                    LeaderboardSortCategory.SURVIVAL -> "⏱️ Survival Time"
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NeonCyan else Color.Transparent)
                                        .clickable { sortCategory = category }
                                        .padding(vertical = 8.dp)
                                        .testTag("sort_tab_${category.name.lowercase()}")
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Leaderboard Table Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.width(28.dp))
                        Text("PLAYER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1f))
                        Text("KILLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
                        Text("SURVIVAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                        Text("POINTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.width(55.dp), textAlign = TextAlign.End)
                    }
                }

                // Leaderboard List Rows
                if (filteredAndSortedRegistrations.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(32.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Standings Recorded Yet",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Register for tournaments or wait for match score validation!",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(filteredAndSortedRegistrations) { index, reg ->
                        val isMe = activeUser != null && (reg.userId == activeUser.id || reg.userGameUid == activeUser.gameUid)
                        val points = reg.rankPoints.takeIf { it > 0 } ?: (reg.kills * 10 + (50 - (index + 1)).coerceAtLeast(0) * 2 + reg.survivalTimeMinutes * 3)

                        LeaderboardRowItem(
                            rank = index + 1,
                            registration = reg,
                            points = points,
                            isMe = isMe,
                            isAdmin = activeUser?.isAdmin == true,
                            onEdit = { editingParticipant = reg }
                        )
                    }
                }
            }

            // My Standing Sticky Bottom Banner
            myRegistration?.let { myReg ->
                val myRankIndex = filteredAndSortedRegistrations.indexOfFirst { it.id == myReg.id } + 1
                if (myRankIndex > 0) {
                    Surface(
                        color = DarkSurfaceElevated,
                        shadowElevation = 12.dp,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .border(1.dp, NeonCyan, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .testTag("my_standing_sticky_bar")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = NeonCyan,
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#$myRankIndex",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = myReg.userInGameName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(color = GoldAmber, shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                text = "YOU",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${myReg.kills} Kills • ${myReg.survivalTimeMinutes}m Survived",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${myReg.rankPoints.takeIf { it > 0 } ?: (myReg.kills * 10 + myReg.survivalTimeMinutes * 3)} Pts",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan
                                )
                                if (myReg.payoutAmount > 0) {
                                    Text(
                                        text = "Won ₹${myReg.payoutAmount.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Admin Quick Edit Dialog
            editingParticipant?.let { reg ->
                AdminEditStatsDialog(
                    registration = reg,
                    onDismiss = { editingParticipant = null },
                    onSave = { kills, survival, pts, isAlive, rank ->
                        onUpdateLiveStats(reg, kills, survival, pts, isAlive, rank)
                        editingParticipant = null
                    }
                )
            }
        }
    }
}

@Composable
fun PodiumCard(
    rank: Int,
    registration: RegistrationEntity,
    medalColor: Color,
    badgeText: String,
    isFirst: Boolean = false,
    modifier: Modifier = Modifier
) {
    val points = registration.rankPoints.takeIf { it > 0 } ?: (registration.kills * 10 + (50 - rank) * 2 + registration.survivalTimeMinutes * 3)

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(2.dp, medalColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = medalColor,
                shape = CircleShape,
                modifier = Modifier.size(if (isFirst) 42.dp else 34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#$rank",
                        fontWeight = FontWeight.Black,
                        fontSize = if (isFirst) 16.sp else 13.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = medalColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = medalColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = registration.userInGameName,
                fontWeight = FontWeight.Bold,
                fontSize = if (isFirst) 13.sp else 11.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = "UID: ${registration.userGameUid}",
                fontSize = 9.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎯", fontSize = 10.sp)
                    Text("${registration.kills}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐", fontSize = 10.sp)
                    Text("$points", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                }
            }

            if (registration.payoutAmount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "₹${registration.payoutAmount.toInt()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardRowItem(
    rank: Int,
    registration: RegistrationEntity,
    points: Int,
    isMe: Boolean,
    isAdmin: Boolean,
    onEdit: () -> Unit
) {
    val backgroundColor = if (isMe) DarkSurfaceElevated else DarkSurface
    val borderColor = if (isMe) NeonCyan else DarkSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .border(if (isMe) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .testTag("leaderboard_row_$rank")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number / Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.width(28.dp)
            ) {
                val rankBg = when (rank) {
                    1, 2, 3 -> PrimaryContainerColor
                    else -> Color.Transparent
                }

                if (rank <= 3) {
                    Surface(
                        color = rankBg,
                        shape = CircleShape,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$rank",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Text(
                        text = "$rank",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Player Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = registration.userInGameName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(color = GoldAmber, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "YOU",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "UID: ${registration.userGameUid}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    if (registration.isAlive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("• ALIVE 💚", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    } else {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("• ELIMINATED ☠️", fontSize = 9.sp, color = TextMuted)
                    }
                }
            }

            // Kills
            Text(
                text = "${registration.kills}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(48.dp)
            )

            // Survival Time
            Text(
                text = "${registration.survivalTimeMinutes}m",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(60.dp)
            )

            // Points
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(55.dp)
            ) {
                Text(
                    text = "$points",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan
                )
                if (registration.payoutAmount > 0) {
                    Text(
                        text = "₹${registration.payoutAmount.toInt()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Stats", tint = GoldAmber, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AdminEditStatsDialog(
    registration: RegistrationEntity,
    onDismiss: () -> Unit,
    onSave: (kills: Int, survivalMins: Int, rankPts: Int, isAlive: Boolean, rank: Int) -> Unit
) {
    var killsText by remember { mutableStateOf(registration.kills.toString()) }
    var survivalText by remember { mutableStateOf(registration.survivalTimeMinutes.toString()) }
    var ptsText by remember { mutableStateOf(registration.rankPoints.toString()) }
    var rankText by remember { mutableStateOf(registration.rank.toString()) }
    var isAlive by remember { mutableStateOf(registration.isAlive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Edit Player Stats 🎯",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Player: ${registration.userInGameName} (${registration.userGameUid})",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = killsText,
                    onValueChange = { killsText = it },
                    label = { Text("Kills") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = survivalText,
                    onValueChange = { survivalText = it },
                    label = { Text("Survival Time (Minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ptsText,
                    onValueChange = { ptsText = it },
                    label = { Text("Rank Points") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rankText,
                    onValueChange = { rankText = it },
                    label = { Text("Rank / Placement Position") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("In-Game Status:", fontSize = 13.sp, color = TextPrimary)
                    FilterChip(
                        selected = isAlive,
                        onClick = { isAlive = !isAlive },
                        label = { Text(if (isAlive) "ALIVE 💚" else "ELIMINATED ☠️") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val k = killsText.toIntOrNull() ?: 0
                    val s = survivalText.toIntOrNull() ?: 0
                    val p = ptsText.toIntOrNull() ?: 0
                    val r = rankText.toIntOrNull() ?: 0
                    onSave(k, s, p, isAlive, r)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAmber)
            ) {
                Text("SAVE STATS", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextMuted)
            }
        }
    )
}
