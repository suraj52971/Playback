package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.TournamentEntity
import com.example.ui.theme.*

@Composable
fun RoomDetailsDialog(
    tournament: TournamentEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val hasCredentials = tournament.roomId.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Key, contentDescription = null, tint = ElectricViolet)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MATCH ROOM KEYS",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tournament.title,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (hasCredentials) {
                    // Room ID Card
                    Surface(
                        color = DarkBackground,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ROOM ID", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(
                                    text = tournament.roomId,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Room ID", tournament.roomId))
                                    Toast.makeText(context, "Room ID Copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("copy_room_id_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Room ID", tint = NeonCyan)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Room Password Card
                    Surface(
                        color = DarkBackground,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAmber),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ROOM PASSWORD", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(
                                    text = tournament.roomPassword,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldAmber,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Room Password", tournament.roomPassword))
                                    Toast.makeText(context, "Room Password Copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("copy_room_pass_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = GoldAmber)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "💡 Open ${tournament.game}, select Custom Room, and enter the Room ID & Password above.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = GoldAmber, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ROOM KEYS NOT RELEASED YET",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Admin will release Room ID & Password 15 minutes before match start. Check back soon!",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_room_dialog_button")
            ) {
                Text("CLOSE", color = NeonCyan, fontWeight = FontWeight.Bold)
            }
        }
    )
}
