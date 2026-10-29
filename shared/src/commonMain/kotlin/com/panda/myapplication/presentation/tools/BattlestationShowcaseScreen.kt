package com.panda.myapplication.presentation.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Mouse
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.RigShowcase
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.domain.repository.RigShowcaseRepository
import com.panda.myapplication.domain.repository.UserRepository
import com.panda.myapplication.presentation.components.HardwarePickerDialog
import com.panda.myapplication.presentation.components.rememberProofImagePicker
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.iconEmoji
import com.panda.myapplication.presentation.util.localized
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import com.panda.myapplication.domain.model.ContentReport
import com.panda.myapplication.domain.model.ReportReason
import com.panda.myapplication.domain.model.ReportTargetType
import com.panda.myapplication.domain.repository.CommunityReportRepository
import com.panda.myapplication.presentation.components.ReportContentDialog
import androidx.compose.material.icons.rounded.Flag
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattlestationShowcaseScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenAuthSheet: () -> Unit,
    rigShowcaseRepository: RigShowcaseRepository = koinInject(),
    userRepository: UserRepository = koinInject(),
    activeBuildRepository: ActiveBuildRepository = koinInject(),
    hardwareRepository: HardwareRepository = koinInject(),
    reportRepository: CommunityReportRepository = koinInject(),
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val showcases by rigShowcaseRepository.observeAllShowcases().collectAsState(initial = emptyList())
    val currentUser by userRepository.currentUser.collectAsState()
    val activeBuild by activeBuildRepository.activeBuild.collectAsState()

    val isLoggedIn = currentUser != null

    var showShareDialog by remember { mutableStateOf(false) }
    var activeInspectSpecsShowcase by remember { mutableStateOf<RigShowcase?>(null) }
    var activeFullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    var reportingTargetShowcase by remember { mutableStateOf<RigShowcase?>(null) }

    val onTriggerShare: () -> Unit = {
        if (!isLoggedIn) {
            onOpenAuthSheet()
        } else if (currentUser?.isSuspended == true) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    currentUser?.remainingSuspensionText
                        ?: (if (isTr) "Hesabınız askıya alınmıştır." else "Account suspended.")
                )
            }
        } else {
            showShareDialog = true
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onTriggerShare,
                containerColor = NeonCyan,
                contentColor = Color.Black,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                text = {
                    Text(
                        text = strings.battlestationShareRig,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                },
                modifier = Modifier.navigationBarsPadding(),
            )
        },
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            IconButton(
                                onClick = onOpenDrawer,
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Menu,
                                    contentDescription = "Menü",
                                    tint = NeonCyan,
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.battlestationTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = if (isTr) "Topluluk Masa & Kasa Vitrini" else "Community Battlestation Rig Wall",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Button(
                            onClick = onTriggerShare,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color.Black,
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.battlestationShareRig,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (showcases.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NeonCyan.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)),
                        modifier = Modifier.size(72.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.PhotoCamera,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.battlestationTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.battlestationEmptyFeed,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (!isLoggedIn) onOpenAuthSheet() else showShareDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = strings.battlestationShareRig,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    items(showcases, key = { it.id }) { showcase ->
                        val isSelf = currentUser?.id == showcase.userId || (currentUser?.username != null && showcase.userDisplayName == currentUser?.username)
                        BattlestationCard(
                            showcase = showcase,
                            isTr = isTr,
                            strings = strings,
                            isSelf = isSelf,
                            onLikeClick = {
                                if (!isLoggedIn) {
                                    onOpenAuthSheet()
                                } else {
                                    coroutineScope.launch {
                                        rigShowcaseRepository.toggleLike(
                                            showcaseId = showcase.id,
                                            userId = currentUser?.id ?: ""
                                        )
                                    }
                                }
                            },
                            onPhotoClick = { url ->
                                activeFullscreenImageUrl = url
                            },
                            onViewSpecsClick = {
                                activeInspectSpecsShowcase = showcase
                            },
                            onReportClick = {
                                reportingTargetShowcase = showcase
                            }
                        )
                    }
                }
            }
        }
    }

    // Specs Bottom Sheet
    activeInspectSpecsShowcase?.let { showcase ->
        @Suppress("DEPRECATION")
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { activeInspectSpecsShowcase = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeveloperBoard,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp),
                        )
                        Column {
                            Text(
                                text = strings.battlestationSpecsTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = showcase.title,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    IconButton(onClick = { activeInspectSpecsShowcase = null }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val parsedSpecs = remember(showcase.hardwareSpecsJson) {
                    parseSpecsJson(showcase.hardwareSpecsJson)
                }

                if (parsedSpecs.isEmpty()) {
                    Text(
                        text = if (isTr) "Bu vitrin için özel donanım parçası belirtilmedi." else "No specific hardware parts listed for this rig.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        parsedSpecs.forEach { (category, partName) ->
                            SpecRowItem(
                                category = category,
                                partName = partName,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                val isInspectSelf = currentUser?.id == showcase.userId || (currentUser?.username != null && showcase.userDisplayName == currentUser?.username)
                OutlinedButton(
                    onClick = {
                        val target = showcase
                        activeInspectSpecsShowcase = null
                        reportingTargetShowcase = target
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isInspectSelf) NeonCyan.copy(alpha = 0.6f) else NeonRose.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isInspectSelf) NeonCyan else NeonRose,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.Flag, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isInspectSelf) (if (isTr) "Bu Sistem Vitrinini Yayından Kaldır" else "Retract This Setup")
                        else (if (isTr) "Bu Sistem Vitrinini Raporla / Şikayet Et" else "Report This Setup"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Fullscreen Image Dialog
    activeFullscreenImageUrl?.let { imageUrl ->
        Dialog(
            onDismissRequest = { activeFullscreenImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .clickable { activeFullscreenImageUrl = null }
            ) {
                IconButton(
                    onClick = { activeFullscreenImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Kapat",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }

                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }

    // Share Rig Showcase Dialog
    if (showShareDialog) {
        ShareRigShowcaseDialog(
            userId = currentUser?.id ?: "guest",
            userDisplayName = currentUser?.username ?: (if (isTr) "Topluluk Mimarı" else "Rig Architect"),
            userBadge = currentUser?.effectiveBadge ?: "👑 Kasa Mimarı",
            activeBuild = activeBuild,
            rigShowcaseRepository = rigShowcaseRepository,
            hardwareRepository = hardwareRepository,
            isTr = isTr,
            strings = strings,
            onDismiss = { showShareDialog = false },
            onSubmit = { newShowcase ->
                coroutineScope.launch {
                    val result = rigShowcaseRepository.submitShowcase(newShowcase)
                    showShareDialog = false
                    if (result is DomainResult.Success) {
                        snackbarHostState.showSnackbar(
                            if (isTr) {
                                "Kasa vitrininiz başarıyla paylaşıldı! +100 itibar puanı profilinize tanımlandı."
                            } else {
                                "Battlestation published! +100 reputation points granted."
                            }
                        )
                    } else if (result is DomainResult.Failure) {
                        val errMsg = (result.error as? DomainError.Unknown)?.cause?.message
                            ?: (if (isTr) "Vitrin paylaşılamadı." else "Submission failed.")
                        snackbarHostState.showSnackbar(errMsg)
                    }
                }
            }
        )
    }

    reportingTargetShowcase?.let { target ->
        val isSelf = currentUser?.id == target.userId || target.userDisplayName == currentUser?.username
        ReportContentDialog(
            targetType = ReportTargetType.BATTLESTATION,
            targetTitle = target.title,
            reportedUserName = target.userDisplayName,
            isSelfReport = isSelf,
            isTr = isTr,
            onDismiss = { reportingTargetShowcase = null },
            onSubmitReport = { reason, details ->
                coroutineScope.launch {
                    val now = System.currentTimeMillis()
                    val report = ContentReport(
                        id = "rep-bs-$now",
                        targetType = ReportTargetType.BATTLESTATION,
                        targetId = target.id,
                        targetTitle = target.title,
                        reportedUserId = target.userId,
                        reportedUserDisplayName = target.userDisplayName,
                        reporterUserId = currentUser?.id ?: "guest",
                        reporterUserDisplayName = currentUser?.username ?: (if (isTr) "Misafir" else "Guest"),
                        isSelfReport = isSelf,
                        reason = reason,
                        details = details,
                        timestampMillis = now,
                    )
                    reportRepository.submitReport(report)
                    if (isSelf) {
                        rigShowcaseRepository.deleteShowcase(target.id)
                        snackbarHostState.showSnackbar(
                            if (isTr) "Sistem vitrini paylaşımınız yayından kaldırıldı."
                            else "Your setup showcase has been retracted."
                        )
                    } else {
                        snackbarHostState.showSnackbar(
                            if (isTr) "Raporunuz alındı. Moderatörler tarafından incelenecektir."
                            else "Report submitted. It will be reviewed by moderators."
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun BattlestationCard(
    showcase: RigShowcase,
    isTr: Boolean,
    strings: com.panda.myapplication.presentation.util.AppStrings,
    isSelf: Boolean = false,
    onLikeClick: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onViewSpecsClick: () -> Unit,
    onReportClick: () -> Unit,
) {
    var selectedPhotoTab by remember { mutableStateOf(0) } // 0: Kasa İçi, 1: Masa Düzeni
    val currentPhotoUrl = if (selectedPhotoTab == 0) showcase.photoRigUrl else showcase.photoSetupUrl

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // User Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NeonCyan.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.size(38.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = showcase.userDisplayName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                fontSize = 16.sp,
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = showcase.userDisplayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NeonPurple.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, NeonPurple.copy(alpha = 0.4f)),
                            ) {
                                Text(
                                    text = showcase.userBadge,
                                    fontSize = 10.sp,
                                    color = NeonPurple,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelf) NeonCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(0.5.dp, if (isSelf) NeonCyan.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onReportClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = if (isSelf) "Kaldır" else "Raporla",
                            tint = if (isSelf) NeonCyan else NeonRose,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = if (isSelf) (if (isTr) "Kaldır" else "Retract") else (if (isTr) "Bildir" else "Report"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelf) NeonCyan else NeonRose,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Description
            Text(
                text = showcase.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (!showcase.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = showcase.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2'li Fotoğraf Geçiş Sekmeleri (Dual Photo Switcher)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedPhotoTab == 0) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        1.dp,
                        if (selectedPhotoTab == 0) NeonCyan else Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedPhotoTab = 0 }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.battlestationTabRig,
                            fontSize = 12.sp,
                            fontWeight = if (selectedPhotoTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedPhotoTab == 0) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedPhotoTab == 1) ElectricBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        1.dp,
                        if (selectedPhotoTab == 1) ElectricBlue else Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedPhotoTab = 1 }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.battlestationTabSetup,
                            fontSize = 12.sp,
                            fontWeight = if (selectedPhotoTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedPhotoTab == 1) ElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Photo Viewer Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable { onPhotoClick(currentPhotoUrl) }
            ) {
                AsyncImage(
                    model = currentPhotoUrl,
                    contentDescription = showcase.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Magnify / Zoom Icon Pill
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ZoomIn,
                            contentDescription = "Büyüt",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Photo Indicator Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (selectedPhotoTab == 0) "1/2 • ${strings.battlestationTabRig}" else "2/2 • ${strings.battlestationTabSetup}",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Like Button & Specs Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Like Button
                val heartColor by animateColorAsState(
                    targetValue = if (showcase.isLikedByMe) NeonRose else MaterialTheme.colorScheme.onSurfaceVariant
                )
                val likeScale by animateFloatAsState(
                    targetValue = if (showcase.isLikedByMe) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (showcase.isLikedByMe) NeonRose.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(
                        0.5.dp,
                        if (showcase.isLikedByMe) NeonRose.copy(alpha = 0.5f) else Color.Transparent
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onLikeClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = if (showcase.isLikedByMe) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Beğen",
                            tint = heartColor,
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer(scaleX = likeScale, scaleY = likeScale)
                        )
                        Text(
                            text = "${showcase.likesCount}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = heartColor,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Specs Button
                    if (!showcase.hardwareSpecsJson.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = onViewSpecsClick,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NeonCyan,
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeveloperBoard,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = strings.battlestationViewSpecs,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    // Direct, highly visible Report / Retract Action Button
                    OutlinedButton(
                        onClick = onReportClick,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (isSelf) NeonCyan.copy(alpha = 0.6f) else NeonRose.copy(alpha = 0.45f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isSelf) NeonCyan else NeonRose,
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSelf) (if (isTr) "Kaldır" else "Retract") else (if (isTr) "Bildir" else "Report"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecRowItem(
    category: String,
    partName: String,
) {
    val lower = category.lowercase()
    val (icon, color) = when {
        lower.contains("cpu") || lower.contains("işlemci") -> Pair(Icons.Rounded.Memory, NeonCyan)
        lower.contains("gpu") || lower.contains("ekran kartı") -> Pair(Icons.Rounded.DeveloperBoard, NeonPurple)
        lower.contains("motherboard") || lower.contains("anakart") -> Pair(Icons.Rounded.DeveloperBoard, ElectricBlue)
        lower.contains("cooler") || lower.contains("soğutucu") -> Pair(Icons.Rounded.AutoAwesome, NeonCyan)
        lower.contains("ram") || lower.contains("bellek") -> Pair(Icons.Rounded.Memory, NeonEmerald)
        lower.contains("case") || lower.contains("kasa") -> Pair(Icons.Rounded.PhotoCamera, NeonAmber)
        lower.contains("monitor") || lower.contains("monitör") -> Pair(Icons.Rounded.Tv, NeonEmerald)
        lower.contains("mousepad") -> Pair(Icons.Rounded.GridOn, NeonCyan)
        lower.contains("mouse") -> Pair(Icons.Rounded.Mouse, NeonCyan)
        lower.contains("keyboard") || lower.contains("klavye") -> Pair(Icons.Rounded.Keyboard, NeonPurple)
        lower.contains("headset") || lower.contains("kulaklık") -> Pair(Icons.Rounded.Headphones, NeonAmber)
        lower.contains("mic") || lower.contains("mikrofon") -> Pair(Icons.Rounded.Mic, NeonRose)
        lower.contains("webcam") || lower.contains("kamera") -> Pair(Icons.Rounded.Videocam, ElectricBlue)
        lower.contains("chair") || lower.contains("koltuk") -> Pair(Icons.Rounded.Chair, NeonPurple)
        else -> Pair(Icons.Rounded.DeveloperBoard, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(30.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
                Text(
                    text = partName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private val CORE_SHOWCASE_SLOTS = listOf(
    HardwareCategory.CPU,
    HardwareCategory.MOTHERBOARD,
    HardwareCategory.RAM,
    HardwareCategory.GPU,
    HardwareCategory.COOLER,
    HardwareCategory.CASE,
    HardwareCategory.STORAGE,
    HardwareCategory.PSU,
)

private val SETUP_PERIPHERAL_SLOTS = listOf(
    HardwareCategory.MONITOR,
    HardwareCategory.MOUSE,
    HardwareCategory.MOUSEPAD,
    HardwareCategory.KEYBOARD,
    HardwareCategory.HEADSET,
    HardwareCategory.MICROPHONE,
    HardwareCategory.WEBCAM,
    HardwareCategory.GAMING_CHAIR,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareRigShowcaseDialog(
    userId: String,
    userDisplayName: String,
    userBadge: String,
    activeBuild: ActiveBuild,
    rigShowcaseRepository: RigShowcaseRepository,
    hardwareRepository: HardwareRepository,
    isTr: Boolean,
    strings: com.panda.myapplication.presentation.util.AppStrings,
    onDismiss: () -> Unit,
    onSubmit: (RigShowcase) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var photoRigUrl by remember { mutableStateOf("") }
    var photoSetupUrl by remember { mutableStateOf("") }

    var showUrlInput1 by remember { mutableStateOf(false) }
    var showUrlInput2 by remember { mutableStateOf(false) }

    // Compatibility-Aware Interactive Build State for Showcase
    var tempBuild by remember { mutableStateOf(activeBuild) }
    var activePickerCategory by remember { mutableStateOf<HardwareCategory?>(null) }
    var selectedHardwareSectionTab by remember { mutableStateOf(0) } // 0: Kasa İçi, 1: Masa & Ekipman

    var isUploading1 by remember { mutableStateOf(false) }
    var isUploading2 by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    var pickingSlot by remember { mutableStateOf(1) }

    val imagePickerLauncher = rememberProofImagePicker(
        onImageCompressed = { bytes ->
            val slot = pickingSlot
            coroutineScope.launch {
                if (slot == 1) {
                    isUploading1 = true
                    val fileName = "showcase_rig_${userId}_${System.currentTimeMillis()}.jpg"
                    when (val res = rigShowcaseRepository.uploadShowcaseImage(bytes, fileName)) {
                        is DomainResult.Success -> {
                            photoRigUrl = res.data
                            uploadError = null
                        }
                        is DomainResult.Failure -> {
                            uploadError = if (isTr) "Kasa görseli yüklenemedi." else "Rig image upload failed."
                        }
                    }
                    isUploading1 = false
                } else {
                    isUploading2 = true
                    val fileName = "showcase_setup_${userId}_${System.currentTimeMillis()}.jpg"
                    when (val res = rigShowcaseRepository.uploadShowcaseImage(bytes, fileName)) {
                        is DomainResult.Success -> {
                            photoSetupUrl = res.data
                            uploadError = null
                        }
                        is DomainResult.Failure -> {
                            uploadError = if (isTr) "Setup görseli yüklenemedi." else "Setup image upload failed."
                        }
                    }
                    isUploading2 = false
                }
            }
        },
        onError = { err ->
            uploadError = err
        }
    )

    activePickerCategory?.let { slotCategory ->
        HardwarePickerDialog(
            title = "${slotCategory.localized(strings)} ${if (isTr) "Seçin" else "Select"}",
            targetCategories = listOf(slotCategory),
            hardwareRepository = hardwareRepository,
            activeBuild = tempBuild,
            onDismiss = { activePickerCategory = null },
            onSelectItem = { chosenItem ->
                tempBuild = tempBuild.withComponent(chosenItem)
                activePickerCategory = null
            },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .heightIn(max = 720.dp),
        content = {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.battlestationTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = if (isTr) "Kasa içi + Masa düzeni 2'li fotoğrafı ve uyumlu parçalarınızı seçin" else "Add dual photo (Rig + Desk Setup) & pick compatible parts",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Kapat")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (uploadError != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonRose.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, NeonRose.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uploadError!!,
                                fontSize = 12.sp,
                                color = NeonRose,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Title Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (isTr) "Vitrin Başlığı *" else "Showcase Title *") },
                        placeholder = { Text(if (isTr) "Örn: Kar Beyazı Ryzen 7 9800X3D + RX 9070 XT" else "e.g. Pure White Arctic Setup") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description Field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(if (isTr) "Açıklama / Masa Hikayesi" else "Description / Story") },
                        placeholder = { Text(if (isTr) "RGB modları, akustik düzen, koltuk ve ekipman notları..." else "Cable management, ambient lighting notes...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Photo 1 Slot: Kasa İçi
                    PhotoUploadCard(
                        title = strings.battlestationTabRig,
                        subtitle = if (isTr) "Kasa içi kablolama & bileşen detayları" else "Internal cabling & rig hardware",
                        imageUrl = photoRigUrl,
                        isUploading = isUploading1,
                        showUrlInput = showUrlInput1,
                        isTr = isTr,
                        onToggleUrlInput = { showUrlInput1 = !showUrlInput1 },
                        onUrlChanged = { photoRigUrl = it },
                        onLaunchCamera = {
                            pickingSlot = 1
                            imagePickerLauncher.launchCamera()
                        },
                        onLaunchGallery = {
                            pickingSlot = 1
                            imagePickerLauncher.launchGallery()
                        },
                        onClear = { photoRigUrl = "" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Photo 2 Slot: Masa Düzeni
                    PhotoUploadCard(
                        title = strings.battlestationTabSetup,
                        subtitle = if (isTr) "Masa, monitör, mikrofon ve oyuncu koltuğu atmosferi" else "Desk workspace, monitor & room ambient",
                        imageUrl = photoSetupUrl,
                        isUploading = isUploading2,
                        showUrlInput = showUrlInput2,
                        isTr = isTr,
                        onToggleUrlInput = { showUrlInput2 = !showUrlInput2 },
                        onUrlChanged = { photoSetupUrl = it },
                        onLaunchCamera = {
                            pickingSlot = 2
                            imagePickerLauncher.launchCamera()
                        },
                        onLaunchGallery = {
                            pickingSlot = 2
                            imagePickerLauncher.launchGallery()
                        },
                        onClear = { photoSetupUrl = "" }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interactive Compatible Hardware Slot Selector Section
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.battlestationSpecsTitle,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = if (isTr) "Soket, DDR ve kasa uyumluluğu otomatik denetlenir" else "Socket, DDR & case clearance automatically validated",
                                        fontSize = 10.sp,
                                        color = NeonEmerald,
                                    )
                                }

                                TextButton(
                                    onClick = { tempBuild = activeBuild },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(15.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.battlestationImportFromActiveBuild,
                                        fontSize = 11.sp,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Section Switcher: Kasa İçi vs Masa & Ekipmanlar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                val coreCount = CORE_SHOWCASE_SLOTS.count { tempBuild.getSlot(it) != null }
                                val setupCount = SETUP_PERIPHERAL_SLOTS.count { tempBuild.getSlot(it) != null }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedHardwareSectionTab == 0) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (selectedHardwareSectionTab == 0) NeonCyan else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedHardwareSectionTab = 0 }
                                ) {
                                    Text(
                                        text = if (isTr) "🖥️ Kasa İçi ($coreCount/8)" else "🖥️ Core Rig ($coreCount/8)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedHardwareSectionTab == 0) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                        maxLines = 1,
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedHardwareSectionTab == 1) NeonPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (selectedHardwareSectionTab == 1) NeonPurple else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedHardwareSectionTab = 1 }
                                ) {
                                    Text(
                                        text = if (isTr) "🎙️ Masa & Ekipman ($setupCount/8)" else "🎙️ Peripherals ($setupCount/8)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedHardwareSectionTab == 1) NeonPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                        maxLines = 1,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val slotsToShow = if (selectedHardwareSectionTab == 0) CORE_SHOWCASE_SLOTS else SETUP_PERIPHERAL_SLOTS
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                slotsToShow.forEach { category ->
                                    val selectedItem = tempBuild.getSlot(category)
                                    ShowcaseSlotPickerRow(
                                        category = category,
                                        selectedItem = selectedItem,
                                        strings = strings,
                                        isTr = isTr,
                                        onClick = { activePickerCategory = category },
                                        onClear = { tempBuild = tempBuild.withoutCategory(category) },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Submit Button
                    val isFormValid = title.isNotBlank() && photoRigUrl.isNotBlank() && photoSetupUrl.isNotBlank()
                    Button(
                        onClick = {
                            if (isFormValid) {
                                val specsMap = buildJsonObject {
                                    tempBuild.cpu?.let { put("cpu", it.displayName) }
                                    tempBuild.gpu?.let { put("gpu", it.displayName) }
                                    tempBuild.motherboard?.let { put("motherboard", it.displayName) }
                                    tempBuild.ram?.let { put("ram", it.displayName) }
                                    tempBuild.cooler?.let { put("cooler", it.displayName) }
                                    tempBuild.case?.let { put("case", it.displayName) }
                                    tempBuild.storage?.let { put("storage", it.displayName) }
                                    tempBuild.psu?.let { put("psu", it.displayName) }
                                    tempBuild.monitor?.let { put("monitor", it.displayName) }
                                    tempBuild.mouse?.let { put("mouse", it.displayName) }
                                    tempBuild.mousepad?.let { put("mousepad", it.displayName) }
                                    tempBuild.keyboard?.let { put("keyboard", it.displayName) }
                                    tempBuild.headset?.let { put("headset", it.displayName) }
                                    tempBuild.microphone?.let { put("microphone", it.displayName) }
                                    tempBuild.webcam?.let { put("webcam", it.displayName) }
                                    tempBuild.gamingChair?.let { put("gaming_chair", it.displayName) }
                                }

                                val showcase = RigShowcase(
                                    id = "rig_${userId}_${System.currentTimeMillis()}",
                                    userId = userId,
                                    userDisplayName = userDisplayName,
                                    userBadge = userBadge,
                                    title = title.trim(),
                                    description = description.trim().takeIf { it.isNotEmpty() },
                                    photoRigUrl = photoRigUrl.trim(),
                                    photoSetupUrl = photoSetupUrl.trim(),
                                    hardwareSpecsJson = specsMap.toString(),
                                    likesCount = 0,
                                    isLikedByMe = false,
                                    createdAtEpochMs = System.currentTimeMillis(),
                                )
                                onSubmit(showcase)
                            }
                        },
                        enabled = isFormValid && !isUploading1 && !isUploading2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = if (isTr) "✨ Vitrine Gönder (+100 Puan)" else "✨ Publish to Rig Wall (+100 Rep)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ShowcaseSlotPickerRow(
    category: HardwareCategory,
    selectedItem: HardwareItem?,
    strings: com.panda.myapplication.presentation.util.AppStrings,
    isTr: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selectedItem != null) NeonCyan.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selectedItem != null) NeonCyan.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = category.iconEmoji(),
                    fontSize = 18.sp,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.localized(strings),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedItem != null) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = selectedItem?.displayName ?: (if (isTr) "Dokun ve katalogdan uyumlu parça seç..." else "Tap to pick compatible part from catalog..."),
                        fontSize = 12.sp,
                        fontWeight = if (selectedItem != null) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedItem != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (selectedItem != null) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Temizle",
                        tint = NeonRose,
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonCyan.copy(alpha = 0.14f),
                ) {
                    Text(
                        text = if (isTr) "+ Seç" else "+ Pick",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoUploadCard(
    title: String,
    subtitle: String,
    imageUrl: String,
    isUploading: Boolean,
    showUrlInput: Boolean,
    isTr: Boolean,
    onToggleUrlInput: () -> Unit,
    onUrlChanged: (String) -> Unit,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (imageUrl.isNotBlank()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Temizle",
                            tint = NeonRose,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (isTr) "Fotoğraf sıkıştırılıp yükleniyor..." else "Compressing & uploading image...",
                            fontSize = 12.sp,
                            color = NeonCyan,
                        )
                    }
                }
            } else if (imageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Surface(
                        shape = CircleShape,
                        color = NeonEmerald,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onLaunchCamera,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTr) "Kamera" else "Camera", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onLaunchGallery,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTr) "Galeri" else "Gallery", fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = onToggleUrlInput,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = "URL",
                            tint = if (showUrlInput) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                if (showUrlInput) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = onUrlChanged,
                        label = { Text("Görsel URL") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                    )
                }
            }
        }
    }
}

private fun parseSpecsJson(jsonStr: String?): List<Pair<String, String>> {
    if (jsonStr.isNullOrBlank()) return emptyList()
    return runCatching {
        val json = Json { ignoreUnknownKeys = true }
        val obj = json.parseToJsonElement(jsonStr) as? JsonObject ?: return emptyList()
        obj.entries.mapNotNull { (key, element) ->
            val value = element.jsonPrimitive.content
            if (value.isNotBlank()) {
                val label = when (key.lowercase()) {
                    "cpu" -> "İşlemci (CPU)"
                    "gpu" -> "Ekran Kartı (GPU)"
                    "motherboard" -> "Anakart"
                    "ram" -> "Bellek (RAM)"
                    "cooler" -> "Soğutucu"
                    "case" -> "Kasa"
                    "storage" -> "Depolama (SSD)"
                    "psu" -> "Güç Kaynağı (PSU)"
                    "monitor" -> "Monitör"
                    "mouse" -> "Oyuncu Mouse"
                    "mousepad" -> "Oyuncu Mousepad"
                    "keyboard" -> "Mekanik / Manyetik Klavye"
                    "headset" -> "Kulaklık"
                    "microphone" -> "Yayıncı Mikrofonu"
                    "webcam" -> "Yayıncı Kamerası (Webcam)"
                    "gaming_chair" -> "Oyuncu & Ergonomi Koltuğu"
                    else -> key
                }
                label to value
            } else null
        }
    }.getOrDefault(emptyList())
}
