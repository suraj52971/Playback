package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LoginRegisterScreen(
    onLogin: (String, String, String) -> Unit, // identifier, password, adminCode
    onRegister: (String, String, String, String, String, String, String, String, String) -> Unit, // name, email, gameUid, ign, whatsapp, upi, password, dob, recoveryPin
    onResetPassword: (String, String, String, String) -> Unit // identifier, dob, recoveryPin, newPassword
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Form fields
    var name by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gameUid by remember { mutableStateOf("") }
    var inGameName by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var upi by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var recoveryPin by remember { mutableStateOf("") }
    var adminCode by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    1.dp,
                    PrimaryContainerColor,
                    RoundedCornerShape(20.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo & Header Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Esports Logo",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "FREE FIRE ESPORTS ARENA",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isRegisterMode) "Register to compete in daily tournaments" else "Compete in daily matches & win real cash prizes",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRegisterMode) GoldAmber else TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                // Mode Selector Tabs (LOG IN vs CREATE ACCOUNT)
                Surface(
                    color = DarkBackground,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { isRegisterMode = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isRegisterMode) NeonCyan else Color.Transparent,
                                contentColor = if (!isRegisterMode) Color.White else TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("tab_login")
                        ) {
                            Text("LOG IN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { isRegisterMode = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRegisterMode) PrimaryContainerColor else Color.Transparent,
                                contentColor = if (isRegisterMode) Color.White else TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("tab_register")
                        ) {
                            Text("SIGN UP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // --- LOGIN MODE ---
                if (!isRegisterMode) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = emailOrPhone,
                            onValueChange = { emailOrPhone = it },
                            label = { Text("Email or WhatsApp Number") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonCyan) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        OutlinedTextField(
                            value = adminCode,
                            onValueChange = { adminCode = it },
                            label = { Text("Admin Secret Passcode (Optional)") },
                            placeholder = { Text("Enter secret passcode if admin") },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = GoldAmber) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_code_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldAmber,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showForgotPasswordDialog = true },
                                modifier = Modifier.testTag("forgot_password_button")
                            ) {
                                Text("Forgot Password?", color = GoldAmber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = {
                                if (emailOrPhone.isNotBlank() || adminCode.isNotBlank()) {
                                    onLogin(emailOrPhone, password, adminCode)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryContainerColor
                            )
                        ) {
                            Text(
                                text = if (adminCode == "surajsingh52971@") "LOGIN TO ADMIN PORTAL 🛡️" else "LOG IN TO ARENA",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    // --- SIGN UP MODE (CLEAN & STRAIGHTFORWARD) ---
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section 1: Player Credentials
                        Text("1. ACCOUNT INFORMATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyanLight, letterSpacing = 0.8.sp)

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonCyan) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        // Section 2: Free Fire Details
                        Text("2. FREE FIRE GAMER DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAmber, letterSpacing = 0.8.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = gameUid,
                                onValueChange = { gameUid = it },
                                label = { Text("Free Fire UID") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = GoldAmber) },
                                modifier = Modifier.weight(1f).testTag("reg_game_uid_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldAmber,
                                    unfocusedBorderColor = DarkSurfaceVariant,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                )
                            )

                            OutlinedTextField(
                                value = inGameName,
                                onValueChange = { inGameName = it },
                                label = { Text("In-Game Name") },
                                leadingIcon = { Icon(Icons.Default.VideogameAsset, contentDescription = null, tint = GoldAmber) },
                                modifier = Modifier.weight(1f).testTag("reg_ign_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldAmber,
                                    unfocusedBorderColor = DarkSurfaceVariant,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                )
                            )
                        }

                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp Number (For Room Notifications)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldGreen) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_whatsapp_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        // Section 3: Security & Password
                        Text("3. PASSWORD & SECURITY RECOVERY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricViolet, letterSpacing = 0.8.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = dob,
                                onValueChange = { dob = it },
                                label = { Text("Date of Birth") },
                                placeholder = { Text("DD/MM/YYYY") },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ElectricViolet) },
                                modifier = Modifier.weight(1f).testTag("reg_dob_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricViolet,
                                    unfocusedBorderColor = DarkSurfaceVariant,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                )
                            )

                            OutlinedTextField(
                                value = recoveryPin,
                                onValueChange = { if (it.length <= 4) recoveryPin = it },
                                label = { Text("4-Digit Sec PIN") },
                                placeholder = { Text("1234") },
                                leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = GoldAmber) },
                                modifier = Modifier.weight(1f).testTag("reg_recovery_pin_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldAmber,
                                    unfocusedBorderColor = DarkSurfaceVariant,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                )
                            )
                        }

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Set Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        OutlinedTextField(
                            value = upi,
                            onValueChange = { upi = it },
                            label = { Text("UPI Receiver ID (For Winnings Payout)") },
                            placeholder = { Text("yourname@upi") },
                            leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = NeonCyan) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_upi_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                if (name.isNotBlank() && email.isNotBlank() && gameUid.isNotBlank() && inGameName.isNotBlank() && password.isNotBlank()) {
                                    onRegister(name, email, gameUid, inGameName, whatsapp, upi, password, dob, recoveryPin)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("register_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainerColor)
                        ) {
                            Text(
                                text = "SIGN UP ⚡",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Forgot Password Recovery Dialog
    if (showForgotPasswordDialog) {
        var resetIdentifier by remember { mutableStateOf("") }
        var resetDob by remember { mutableStateOf("") }
        var resetPin by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = GoldAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PASSWORD RECOVERY", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Verify your identity with registered Email/WhatsApp, Date of Birth, and 4-Digit Security PIN.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = resetIdentifier,
                        onValueChange = { resetIdentifier = it },
                        label = { Text("Email or WhatsApp Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = resetDob,
                        onValueChange = { resetDob = it },
                        label = { Text("Date of Birth (DOB)") },
                        placeholder = { Text("DD/MM/YYYY") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = resetPin,
                        onValueChange = { if (it.length <= 4) resetPin = it },
                        label = { Text("4-Digit Security PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetIdentifier.isNotBlank() && resetDob.isNotBlank() && resetPin.isNotBlank() && newPassword.isNotBlank()) {
                            onResetPassword(resetIdentifier, resetDob, resetPin, newPassword)
                            showForgotPasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAmber)
                ) {
                    Text("RESET PASSWORD", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}
