package com.panda.myapplication.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.Gender
import com.panda.myapplication.domain.model.SpiritHardware
import com.panda.myapplication.domain.repository.UserRepository
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.CardSurface
import com.panda.myapplication.presentation.theme.CardSurfaceElevated
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GlassBorder
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSheet(
    userRepository: UserRepository,
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit,
) {
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Giriş, 1 = Kayıt Ol

    // Login Fields
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isLoginPasswordVisible by remember { mutableStateOf(false) }

    // Signup Fields
    var signupUsername by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var isSignupPasswordVisible by remember { mutableStateOf(false) }
    var birthDateText by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.MALE) }
    var selectedSpiritHardware by remember { mutableStateOf(SpiritHardware.OVERCLOCKED_CPU) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = Spacing.large),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = if (selectedTabIndex == 0) strings.authLoginTitle else strings.authRegisterTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                    )
                    Text(
                        text = if (selectedTabIndex == 0)
                            tr(isTr, "Hesabınızla giriş yapın, fırsat bildirin ve sisteminizi değerlendirin.", "Sign in to submit deals, track builds, and participate in community discussions.")
                        else
                            tr(isTr, "Kendi donanım ruhunuzu seçin ve Hardware Wizard topluluğuna katılın.", "Choose your hardware persona and join the Hardware Wizard community."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Tabs: Giriş Yap / Kayıt Ol
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CardSurfaceElevated,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = NeonCyan,
                    )
                },
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        selectedTabIndex = 0
                        errorMessage = null
                    },
                    text = {
                        Text(
                            text = strings.authTabLogin,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 0) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        selectedTabIndex = 1
                        errorMessage = null
                    },
                    text = {
                        Text(
                            text = strings.authTabRegister,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == 1) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }

            // Tab 0: LOGIN
            if (selectedTabIndex == 0) {
                OutlinedTextField(
                    value = loginIdentifier,
                    onValueChange = {
                        loginIdentifier = it
                        errorMessage = null
                    },
                    label = { Text(strings.authEmailOrUsername) },
                    placeholder = { Text(strings.authEmailOrUsername) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrectEnabled = false),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                    ),
                )

                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = {
                        loginPassword = it
                        errorMessage = null
                    },
                    label = { Text(strings.authPassword) },
                    placeholder = { Text("••••••••") },
                    visualTransformation = if (isLoginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isLoginPasswordVisible = !isLoginPasswordVisible }) {
                            Text(
                                text = if (isLoginPasswordVisible) "🙈" else "👁️",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                    ),
                )

                Button(
                    onClick = {
                        if (loginIdentifier.isBlank() || loginPassword.isBlank()) {
                            errorMessage = tr(isTr, "Lütfen kullanıcı adı / e-posta ve şifrenizi girin.", "Please enter your username / email and password.")
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = userRepository.login(loginIdentifier, loginPassword)
                            isLoading = false
                            when (result) {
                                is DomainResult.Success -> {
                                    successMessage = tr(isTr, "Hoş geldiniz, ${result.data.username}!", "Welcome, ${result.data.username}!")
                                    onAuthSuccess()
                                    onDismiss()
                                }
                                is DomainResult.Failure -> {
                                    errorMessage = (result.error as? DomainError.Unknown)?.cause?.message
                                        ?: strings.authLoginFailed
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                ) {
                    Text(
                        text = if (isLoading) strings.authLoggingIn else strings.authLoginTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                }
            } else {
                // Tab 1: SIGNUP
                OutlinedTextField(
                    value = signupUsername,
                    onValueChange = {
                        signupUsername = it
                        errorMessage = null
                    },
                    label = { Text(strings.authUsernameReq) },
                    placeholder = { Text(tr(isTr, "Kullanıcı adınız", "Your username")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                    ),
                )

                OutlinedTextField(
                    value = signupEmail,
                    onValueChange = {
                        signupEmail = it
                        errorMessage = null
                    },
                    label = { Text(strings.authEmailReq) },
                    placeholder = { Text("eposta@domain.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                    ),
                )

                OutlinedTextField(
                    value = signupPassword,
                    onValueChange = {
                        signupPassword = it
                        errorMessage = null
                    },
                    label = { Text(strings.authPasswordReq) },
                    visualTransformation = if (isSignupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isSignupPasswordVisible = !isSignupPasswordVisible }) {
                            Text(
                                text = if (isSignupPasswordVisible) "🙈" else "👁️",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                    ),
                )

                // Doğum Tarihi & Yaş Önizleme
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = birthDateText,
                        onValueChange = { birthDateText = it },
                        label = { Text(tr(isTr, "Doğum Tarihi (YYYY-AA-GG) *", "Birth Date (YYYY-MM-DD) *")) },
                        placeholder = { Text("2000-01-15") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrectEnabled = false),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )

                    // Otomatik Yaş Önizleme Rozeti
                    val previewAge = remember(birthDateText) {
                        val birthYear = birthDateText.split("-").firstOrNull()?.toIntOrNull()
                        if (birthYear != null && birthYear in 1940..2025) {
                            (2026 - birthYear).coerceAtLeast(0)
                        } else null
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NeonCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    ) {
                        Text(
                            text = if (previewAge != null) "${tr(isTr, "Yaş", "Age")}: $previewAge" else "${tr(isTr, "Yaş", "Age")}: -",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                }

                // Cinsiyet Seçimi
                Text(
                    text = tr(isTr, "Cinsiyet *", "Gender *"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Gender.entries.forEach { gender ->
                        val isSelected = selectedGender == gender
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGender = gender },
                            label = {
                                Text("${gender.emoji} ${gender.displayName}")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan,
                            ),
                        )
                    }
                }

                // "Kendini Hangi Donanım Parçası Olarak Görüyorsun?"
                Text(
                    text = tr(isTr, "Kendini Hangi Donanım Parçası Olarak Görüyorsun? *", "Which Hardware Component Represents Your Spirit? *"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = NeonAmber,
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SpiritHardware.entries.forEach { spirit ->
                        val isSelected = selectedSpiritHardware == spirit
                        val spiritColor = Color(spirit.colorHex)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) spiritColor.copy(alpha = 0.18f) else CardSurface.copy(alpha = 0.4f),
                            border = BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, if (isSelected) spiritColor else GlassBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSpiritHardware = spirit },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = spirit.emoji,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(end = 10.dp),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = spirit.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) spiritColor else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = spirit.motto,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        text = "✓",
                                        fontWeight = FontWeight.Black,
                                        color = spiritColor,
                                        fontSize = 18.sp,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (signupUsername.isBlank() || signupEmail.isBlank() || signupPassword.isBlank()) {
                            errorMessage = tr(isTr, "Lütfen tüm zorunlu alanları doldurun.", "Please fill in all required fields.")
                            return@Button
                        }
                        if (signupPassword.length < 6) {
                            errorMessage = strings.authPasswordMinError
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = userRepository.signUp(
                                email = signupEmail,
                                password = signupPassword,
                                username = signupUsername,
                                birthDateIso = birthDateText,
                                gender = selectedGender,
                                spiritHardware = selectedSpiritHardware,
                            )
                            isLoading = false
                            when (result) {
                                is DomainResult.Success -> {
                                    successMessage = strings.authRegisterSuccess.replace("%s", result.data.username)
                                    onAuthSuccess()
                                    onDismiss()
                                }
                                is DomainResult.Failure -> {
                                    errorMessage = strings.authRegisterFailed
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                ) {
                    Text(
                        text = if (isLoading) strings.authRegistering else strings.authRegisterTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                }
            }

            // Error & Success Feedback
            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonRose.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, NeonRose),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "⚠️ $errorMessage",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonRose,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }

            if (successMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, NeonEmerald),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "✓ $successMessage",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
        }
    }
}

private fun tr(isTr: Boolean, trText: String, enText: String): String = if (isTr) trText else enText
