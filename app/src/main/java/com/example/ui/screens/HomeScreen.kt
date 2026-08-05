package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AnnouncementEntity
import com.example.data.local.entities.RegistrationEntity
import com.example.data.local.entities.TournamentEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    activeUser: UserEntity?,
    tournaments: List<TournamentEntity>,
    announcements: List<AnnouncementEntity>,
    userRegistrations: List<RegistrationEntity>,
    selectedGameFilter: String,
    searchQuery: String,
    isDarkTheme: Boolean = true,
    onToggleTheme: (() -> Unit)? = null,
    onFilterSelect: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectTournament: (TournamentEntity) -> Unit,
    onOpenWallet: () -> Unit,
    onOpenRoomKeys: (TournamentEntity) -> Unit,
    onOpenLeaderboard: (TournamentEntity) -> Unit
) {
    val games = listOf("ALL", "Free Fire", "BGMI", "Call of Duty", "Valorant")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // --- TOP HEADER BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "App Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PLAYZONE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Esports Arena",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyanLight
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Wallet Balance Chip
                Surface(
                    onClick = onOpenWallet,
                    shape = RoundedCornerShape(20.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAmber),
                    modifier = Modifier.testTag("header_wallet_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = GoldAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${activeUser?.walletBalance?.toInt() ?: 0}",
                            fontWeight = FontWeight.Bold,
                            color = GoldAmber,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(GoldAmber),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Funds",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                if (onToggleTheme != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp, top = 16.dp)
        ) {
            // --- ANNOUNCEMENT BANNER ---
            if (announcements.isNotEmpty()) {
                item {
                    val activeAnnouncement = announcements.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, AnnouncementBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = AnnouncementBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Announcement",
                                tint = AnnouncementText,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeAnnouncement.title,
                                        fontWeight = FontWeight.Bold,
                                        color = AnnouncementText,
                                        fontSize = 15.sp
                                    )
                                    if (activeAnnouncement.isPinned) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = CrimsonRed,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "PINNED",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeAnnouncement.message,
                                    fontSize = 12.sp,
                                    color = AnnouncementText.copy(alpha = 0.85f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // --- SEARCH & CATEGORY FILTER TABS ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search Free Fire, BGMI, Erangel...", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        )
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(games) { game ->
                            val isSelected = selectedGameFilter.equals(game, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onFilterSelect(game) },
                                label = { Text(game, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurface,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) NeonCyan else DarkSurfaceVariant
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("game_filter_$game")
                            )
                        }
                    }
                }
            }

            // --- TOURNAMENT CARDS SECTION ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UPCOMING MATCHES",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${tournaments.size} Available",
                        color = NeonCyanLight,
                        fontSize = 12.sp
                    )
                }
            }

            if (tournaments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("empty_tournaments_card"),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (searchQuery.isNotBlank() || !selectedGameFilter.equals("ALL", ignoreCase = true)) 
                                        Icons.Default.SearchOff else Icons.Default.EmojiEvents,
                                    contentDescription = "No Matches",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (searchQuery.isNotBlank() || !selectedGameFilter.equals("ALL", ignoreCase = true))
                                    "No Matching Tournaments"
                                else
                                    "No Upcoming Matches",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (searchQuery.isNotBlank() || !selectedGameFilter.equals("ALL", ignoreCase = true))
                                    "We couldn't find any tournaments for '$searchQuery' under '$selectedGameFilter'. Try adjusting your search query or game category."
                                else
                                    "There are currently no active or upcoming esports matches listed. Check back soon or stay tuned for updates!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            if (searchQuery.isNotBlank() || !selectedGameFilter.equals("ALL", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(18.dp))
                                OutlinedButton(
                                    onClick = {
                                        onSearchChange("")
                                        onFilterSelect("ALL")
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = NeonCyan
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                                    modifier = Modifier.testTag("reset_filters_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAltOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SHOW ALL MATCHES", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                items(tournaments, key = { it.id }) { tournament ->
                    val isRegistered = userRegistrations.any { it.tournamentId == tournament.id }
                    TournamentCard(
                        tournament = tournament,
                        isRegistered = isRegistered,
                        onSelect = { onSelectTournament(tournament) },
                        onOpenRoomKeys = { onOpenRoomKeys(tournament) },
                        onOpenLeaderboard = { onOpenLeaderboard(tournament) }
                    )
                }
            }
        }
    }
}

@Composable
fun TournamentCard(
    tournament: TournamentEntity,
    isRegistered: Boolean,
    onSelect: () -> Unit,
    onOpenRoomKeys: () -> Unit,
    onOpenLeaderboard: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(tournament.matchTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isRegistered) NeonCyan else DarkSurfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .testTag("tournament_card_${tournament.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Game Badge & Map/Mode Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when (tournament.game) {
                            "Free Fire" -> FlameOrange
                            "BGMI" -> GoldAmber
                            "Call of Duty" -> ElectricViolet
                            else -> NeonCyan
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = tournament.game.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${tournament.map} • ${tournament.mode}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Match Status Badge
                Surface(
                    color = when (tournament.status) {
                        "LIVE" -> CrimsonRed
                        "COMPLETED" -> DarkSurfaceVariant
                        "CANCELLED" -> TextMuted
                        else -> EmeraldGreen
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = tournament.status,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tournament Title
            Text(
                text = tournament.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Date / Time Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formattedTime,
                    fontSize = 12.sp,
                    color = NeonCyanLight,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Winner Prize & Entry Fee Breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆 WINNER PRIZE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Black)
                    Text("₹${tournament.prizePool.toInt()}", fontSize = 16.sp, color = GoldAmber, fontWeight = FontWeight.Black)
                }

                Box(modifier = Modifier.width(1.dp).height(28.dp).background(DarkSurfaceVariant))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎟️ ENTRY FEE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Black)
                    Text(
                        text = if (tournament.entryFee == 0.0) "FREE ENTRY" else "₹${tournament.entryFee.toInt()}",
                        fontSize = 15.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(modifier = Modifier.width(1.dp).height(28.dp).background(DarkSurfaceVariant))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👥 SLOTS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Black)
                    Text("${tournament.maxPlayers} Max", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isRegistered) {
                    Button(
                        onClick = onOpenRoomKeys,
                        modifier = Modifier.weight(1f).height(42.dp).testTag("room_keys_button_${tournament.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ROOM KEYS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onSelect,
                        modifier = Modifier.weight(1f).height(42.dp).testTag("join_tournament_button_${tournament.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text(
                            text = if (tournament.status == "UPCOMING") "JOIN MATCH" else "DETAILS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenLeaderboard,
                    modifier = Modifier.height(42.dp).testTag("card_leaderboard_button_${tournament.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GoldAmber,
                        containerColor = DarkSurfaceElevated
                    )
                ) {
                    Icon(Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("STANDINGS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
