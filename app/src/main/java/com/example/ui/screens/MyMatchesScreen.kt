package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.RegistrationEntity
import com.example.data.local.entities.TournamentEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyMatchesScreen(
    userRegistrations: List<RegistrationEntity>,
    allTournaments: List<TournamentEntity>,
    onOpenRoomKeys: (TournamentEntity) -> Unit,
    onOpenLeaderboard: (TournamentEntity) -> Unit
) {
    var selectedMatchTab by remember { mutableStateOf("JOINED") }

    val registeredTournamentMap = allTournaments.associateBy { it.id }
    val joinedItems = userRegistrations.mapNotNull { reg ->
        registeredTournamentMap[reg.tournamentId]?.let { t -> reg to t }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "MY TOURNAMENTS",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            ),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Match Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("JOINED", "COMPLETED").forEach { tab ->
                val isSelected = selectedMatchTab == tab
                Button(
                    onClick = { selectedMatchTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) NeonCyan else DarkSurface,
                        contentColor = if (isSelected) Color.White else TextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(tab, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filteredItems = joinedItems.filter { (_, t) ->
            if (selectedMatchTab == "JOINED") t.status == "UPCOMING" || t.status == "LIVE"
            else t.status == "COMPLETED" || t.status == "CANCELLED"
        }

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VideogameAssetOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No tournaments in this section yet.", color = TextMuted, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredItems, key = { (reg, t) -> "${reg.id}_${t.id}" }) { (reg, tournament) ->
                    MyMatchCard(
                        reg = reg,
                        tournament = tournament,
                        onOpenRoomKeys = { onOpenRoomKeys(tournament) },
                        onOpenLeaderboard = { onOpenLeaderboard(tournament) }
                    )
                }
            }
        }
    }
}

@Composable
fun MyMatchCard(
    reg: RegistrationEntity,
    tournament: TournamentEntity,
    onOpenRoomKeys: () -> Unit,
    onOpenLeaderboard: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(tournament.matchTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp))
            .testTag("my_match_card_${tournament.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = NeonCyan,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = tournament.game.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = when (tournament.status) {
                        "LIVE" -> CrimsonRed
                        "COMPLETED" -> DarkSurfaceVariant
                        "CANCELLED" -> TextMuted
                        else -> EmeraldGreen
                    },
                    shape = RoundedCornerShape(10.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tournament.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "$formattedTime • ${tournament.map} (${tournament.mode})",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Player Registration Details
            Surface(
                color = DarkBackground,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("YOUR GAME UID", fontSize = 9.sp, color = TextMuted)
                        Text(reg.userGameUid, fontSize = 12.sp, color = NeonCyanLight, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("IGN", fontSize = 9.sp, color = TextMuted)
                        Text(reg.userInGameName, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    if (tournament.status == "COMPLETED") {
                        Column {
                            Text("RANK / KILLS", fontSize = 9.sp, color = TextMuted)
                            Text("#${reg.rank} (${reg.kills} Kills)", fontSize = 12.sp, color = GoldAmber, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (tournament.status == "COMPLETED") {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("MATCH WINNINGS CREDIT", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = if (reg.payoutAmount > 0) "+₹${reg.payoutAmount.toInt()}" else "₹0",
                            fontWeight = FontWeight.Black,
                            color = if (reg.payoutAmount > 0) EmeraldGreen else TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onOpenRoomKeys,
                        modifier = Modifier.weight(1f).height(42.dp).testTag("my_match_room_keys_button_${tournament.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ROOM KEYS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = onOpenLeaderboard,
                    modifier = Modifier.height(42.dp).testTag("my_match_leaderboard_button_${tournament.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GoldAmber,
                        containerColor = DarkSurfaceElevated
                    )
                ) {
                    Icon(Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LEADERBOARD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
