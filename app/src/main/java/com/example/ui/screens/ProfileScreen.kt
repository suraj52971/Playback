package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    activeUser: UserEntity?,
    onUpdateProfile: (String, String, String, String, String) -> Unit,
    onToggleAdmin: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    var showEditModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- PROFILE HEADER CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, PrimaryContainerColor, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activeUser?.name?.take(1)?.uppercase() ?: "P",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = activeUser?.name ?: "Player",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Text(
                    text = activeUser?.email ?: "",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showEditModal = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    modifier = Modifier.testTag("edit_profile_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EDIT PLAYER DETAILS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- PLAYER GAME CREDS ---
        Text("GAME CREDENTIALS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileDetailRow(icon = Icons.Default.Badge, label = "Game UID", value = activeUser?.gameUid ?: "Not set")
                Divider(color = DarkSurfaceVariant)
                ProfileDetailRow(icon = Icons.Default.VideogameAsset, label = "In-Game Name (IGN)", value = activeUser?.inGameName ?: "Not set")
                Divider(color = DarkSurfaceVariant)
                ProfileDetailRow(icon = Icons.Default.Phone, label = "WhatsApp Number", value = activeUser?.whatsappNumber ?: "Not set")
                Divider(color = DarkSurfaceVariant)
                ProfileDetailRow(icon = Icons.Default.AccountBalanceWallet, label = "Winnings UPI ID", value = activeUser?.upiReceiverId ?: "Not set")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- ADMIN & SYSTEM SETTINGS ---
        Text("PLATFORM PERMISSIONS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldAmber)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Enable Admin Mode", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Text("Unlocks tournament creation & result management", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Switch(
                    checked = activeUser?.isAdmin == true,
                    onCheckedChange = onToggleAdmin,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryContainerColor),
                    modifier = Modifier.testTag("admin_mode_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("LOG OUT OF ACCOUNT", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(90.dp))
    }

    if (showEditModal && activeUser != null) {
        EditProfileModal(
            user = activeUser,
            onDismiss = { showEditModal = false },
            onSave = { name, ign, uid, wa, upi ->
                onUpdateProfile(name, ign, uid, wa, upi)
                showEditModal = false
            }
        )
    }
}

@Composable
fun ProfileDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 10.sp, color = TextMuted)
            Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EditProfileModal(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var inGameName by remember { mutableStateOf(user.inGameName) }
    var gameUid by remember { mutableStateOf(user.gameUid) }
    var whatsapp by remember { mutableStateOf(user.whatsappNumber) }
    var upi by remember { mutableStateOf(user.upiReceiverId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("EDIT PROFILE DETAILS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = inGameName, onValueChange = { inGameName = it }, label = { Text("In-Game Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = gameUid, onValueChange = { gameUid = it }, label = { Text("Game UID") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = upi, onValueChange = { upi = it }, label = { Text("UPI Receiver ID") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, inGameName, gameUid, whatsapp, upi) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("SAVE CHANGES", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = TextMuted) }
        }
    )
}
