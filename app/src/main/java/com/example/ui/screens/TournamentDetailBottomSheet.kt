package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.TournamentEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailBottomSheet(
    tournament: TournamentEntity,
    activeUser: UserEntity?,
    isRegistered: Boolean,
    upiReceiverVpa: String = "suraj52971-2@okaxis",
    onDismiss: () -> Unit,
    onConfirmJoin: (String, String, String, String) -> Unit,
    onConfirmUpiJoin: (String, String, String, String, String) -> Unit, // ign, uid, whatsapp, upi, utr
    onOpenWallet: () -> Unit,
    onOpenRoomKeys: () -> Unit
) {
    val context = LocalContext.current

    var inGameName by remember { mutableStateOf(activeUser?.inGameName ?: "") }
    var gameUid by remember { mutableStateOf(activeUser?.gameUid ?: "") }
    var whatsapp by remember { mutableStateOf(activeUser?.whatsappNumber ?: "") }
    var upi by remember { mutableStateOf(activeUser?.upiReceiverId ?: "") }
    var utrNumber by remember { mutableStateOf("") }
    var selectedPaymentTab by remember { mutableStateOf("UPI_INTENT") } // "UPI_INTENT" or "WALLET"

    val walletBalance = activeUser?.walletBalance ?: 0.0
    val hasSufficientBalance = walletBalance >= tournament.entryFee

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Title
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
                        text = "FREE FIRE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "${tournament.map} • ${tournament.mode}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tournament.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Winner Prize Breakdown
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏆 WINNER PRIZE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("₹${tournament.prizePool.toInt()}", fontSize = 18.sp, color = GoldAmber, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(DarkSurfaceVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎟️ ENTRY FEE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (tournament.entryFee == 0.0) "FREE" else "₹${tournament.entryFee.toInt()}",
                            fontSize = 18.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(DarkSurfaceVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👥 MAX SLOTS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("${tournament.maxPlayers}", fontSize = 18.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Match Rules
            Text(text = "FREE FIRE MATCH RULES", fontWeight = FontWeight.Bold, color = GoldAmber, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = DarkBackground,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = tournament.rules,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isRegistered) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TextPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("YOU ARE REGISTERED!", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Room ID & Password will appear 15 mins before start time.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onOpenRoomKeys()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("dialog_view_room_keys_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("OPEN CUSTOM ROOM KEYS", fontWeight = FontWeight.Bold)
                }
            } else {
                // Confirm Registration Form
                Text(text = "CONFIRM PLAYER DETAILS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inGameName,
                    onValueChange = { inGameName = it },
                    label = { Text("Free Fire In-Game Name (IGN)") },
                    modifier = Modifier.fillMaxWidth().testTag("join_ign_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = gameUid,
                    onValueChange = { gameUid = it },
                    label = { Text("Free Fire UID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("join_game_uid_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method Selector
                Text("SELECT ENTRY PAYMENT METHOD", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldAmber)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedPaymentTab == "UPI_INTENT",
                        onClick = { selectedPaymentTab = "UPI_INTENT" },
                        label = { Text("⚡ Direct UPI App", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldAmber,
                            selectedLabelColor = Color.Black
                        )
                    )

                    FilterChip(
                        selected = selectedPaymentTab == "WALLET",
                        onClick = { selectedPaymentTab = "WALLET" },
                        label = { Text("💳 Wallet (₹${walletBalance.toInt()})", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedPaymentTab == "UPI_INTENT") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAmber)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = GoldAmber)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Pay ₹${tournament.entryFee.toInt()} via GPay/PhonePe/Paytm", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                    Text("VPA: $upiReceiverVpa", fontSize = 12.sp, color = NeonCyan)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val upiUri = Uri.parse("upi://pay?pa=$upiReceiverVpa&pn=FreeFireTournament&am=${tournament.entryFee}&cu=INR&tn=FF_Match_${tournament.id}")
                                    val intent = Intent(Intent.ACTION_VIEW, upiUri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Fallback if no app available
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAmber),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("OPEN UPI APP TO PAY ₹${tournament.entryFee.toInt()}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = utrNumber,
                                onValueChange = { utrNumber = it },
                                label = { Text("12-Digit UPI Ref / UTR Number") },
                                placeholder = { Text("e.g. 423819058192") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("utr_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAmber)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (inGameName.isNotBlank() && gameUid.isNotBlank() && utrNumber.isNotBlank()) {
                                onConfirmUpiJoin(inGameName, gameUid, whatsapp, upi, utrNumber)
                                onDismiss()
                            }
                        },
                        enabled = inGameName.isNotBlank() && gameUid.isNotBlank() && utrNumber.length >= 6,
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("confirm_upi_pay_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAmber)
                    ) {
                        Text(
                            text = "SUBMIT UTR FOR ADMIN APPROVAL ⚡",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // Wallet Check
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Your Wallet Balance", fontSize = 12.sp, color = TextSecondary)
                                Text("₹${walletBalance.toInt()}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            }
                            if (!hasSufficientBalance) {
                                TextButton(onClick = {
                                    onDismiss()
                                    onOpenWallet()
                                }) {
                                    Text("ADD MONEY (+)", color = GoldAmber, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (hasSufficientBalance && inGameName.isNotBlank() && gameUid.isNotBlank()) {
                                onConfirmJoin(inGameName, gameUid, whatsapp, upi)
                                onDismiss()
                            }
                        },
                        enabled = hasSufficientBalance && inGameName.isNotBlank() && gameUid.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("confirm_pay_entry_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text(
                            text = if (hasSufficientBalance) "PAY ₹${tournament.entryFee.toInt()} FROM WALLET" else "INSUFFICIENT WALLET BALANCE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
