package com.panda.myapplication.presentation.tools

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.SiliconLotteryTier
import com.panda.myapplication.domain.model.TuningComment
import com.panda.myapplication.domain.model.TuningProfile
import com.panda.myapplication.domain.model.TuningReactionType
import com.panda.myapplication.domain.model.TuningType
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.domain.repository.TuningVaultRepository
import com.panda.myapplication.domain.repository.UserRepository
import com.panda.myapplication.presentation.hardware_detail.TuningShareDialog
import com.panda.myapplication.presentation.hardware_detail.iconVector
import com.panda.myapplication.presentation.hardware_detail.shortBadge
import com.panda.myapplication.presentation.hardware_detail.tintColor
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GoldAccent
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.util.LocalAppStrings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityTuningVaultScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenAuthSheet: () -> Unit,
    modifier: Modifier = Modifier,
    tuningVaultRepository: TuningVaultRepository = koinInject(),
    hardwareRepository: HardwareRepository = koinInject(),
    activeBuildRepository: ActiveBuildRepository = koinInject(),
    userRepository: UserRepository = koinInject(),
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val approvedProfiles by tuningVaultRepository.observeAllApprovedTunings().collectAsState(initial = emptyList())
    val activeBuild by activeBuildRepository.activeBuild.collectAsState()
    val currentUser by userRepository.currentUser.collectAsState()
    val isLoggedIn by userRepository.isLoggedIn.collectAsState()

    var selectedTypeFilter by remember { mutableStateOf<TuningType?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var filterMyRigOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    // Dil Filtresi: Türkiye / Türkçe kullanıcılar için varsayılan "tr", aksi halde "ALL"
    var selectedLanguageFilter by remember { mutableStateOf(if (isTr) "tr" else "ALL") }

    var showShareDialog by remember { mutableStateOf(false) }
    var activeProofImageUrl by remember { mutableStateOf<String?>(null) }
    var activeDiscussionTuning by remember { mutableStateOf<TuningProfile?>(null) }

    val hasActiveRigCpuOrGpu = activeBuild.cpu != null || activeBuild.gpu != null

    val filteredProfiles = remember(
        approvedProfiles,
        selectedTypeFilter,
        selectedCategoryFilter,
        selectedLanguageFilter,
        filterMyRigOnly,
        activeBuild.cpu?.id,
        activeBuild.gpu?.id,
        searchQuery,
    ) {
        approvedProfiles.filter { profile ->
            val matchesLanguage = (selectedLanguageFilter == "ALL") || profile.languageCode.equals(selectedLanguageFilter, ignoreCase = true)
            val matchesType = selectedTypeFilter == null || profile.effectiveTuningTypes.contains(selectedTypeFilter)
            val matchesCategory = selectedCategoryFilter == null || profile.hardwareCategory.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesMyRig = !filterMyRigOnly ||
                (activeBuild.cpu != null && (profile.hardwareId == activeBuild.cpu?.id?.value || profile.hardwareName.equals(activeBuild.cpu?.displayName, ignoreCase = true))) ||
                (activeBuild.gpu != null && (profile.hardwareId == activeBuild.gpu?.id?.value || profile.hardwareName.equals(activeBuild.gpu?.displayName, ignoreCase = true)))
            val matchesQuery = searchQuery.isBlank() ||
                profile.hardwareName.contains(searchQuery, ignoreCase = true) ||
                profile.profileTitle.contains(searchQuery, ignoreCase = true) ||
                profile.userDisplayName.contains(searchQuery, ignoreCase = true) ||
                (profile.motherboardName?.contains(searchQuery, ignoreCase = true) == true) ||
                (profile.coolerName?.contains(searchQuery, ignoreCase = true) == true) ||
                (profile.ramSpec?.contains(searchQuery, ignoreCase = true) == true)
            matchesLanguage && matchesType && matchesCategory && matchesMyRig && matchesQuery
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ToolTopBar(
                title = strings.drawerTuningVaultTitle,
                subtitle = strings.drawerTuningVaultSubtitle,
                icon = "⚡",
                onNavigateBack = onNavigateBack,
                onOpenDrawer = onOpenDrawer,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = Spacing.medium),
        ) {
            Spacer(modifier = Modifier.height(Spacing.small))

            // 1. Top Action Hero Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "SILICON VAULT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 0.8.sp,
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonEmerald.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
                            ) {
                                Text(
                                    text = if (isTr) "${approvedProfiles.size} Profil" else "${approvedProfiles.size} Profiles",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                                    color = NeonEmerald,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (isLoggedIn) {
                                    showShareDialog = true
                                } else {
                                    onOpenAuthSheet()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (isTr) "Ayarını Paylaş" else "Share Tuning",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isTr) {
                            "Kullanıcıların test ettiği stabil voltaj, saat hızı ve uyumlu sistem donanım profilleri"
                        } else {
                            "Community-verified stable voltage, clock speed, and compatible rig profiles"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isTr) "Donanım (9900X, 7800X3D, RTX 4080), anakart, RAM veya yazar ara..." else "Search hardware, motherboard, RAM or author...",
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.closeButton,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2.5. Language Priority Chips (TR Default for TR users + Global Filter)
            val trCount = remember(approvedProfiles) { approvedProfiles.count { it.languageCode.equals("tr", ignoreCase = true) } }
            val globalCount = approvedProfiles.size
            val enCount = remember(approvedProfiles) { approvedProfiles.count { it.languageCode.equals("en", ignoreCase = true) } }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectedLanguageFilter == "tr") NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, if (selectedLanguageFilter == "tr") NeonCyan else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { selectedLanguageFilter = "tr" },
                ) {
                    Text(
                        text = if (isTr) "🇹🇷 Türkçe ($trCount)" else "🇹🇷 Turkish ($trCount)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selectedLanguageFilter == "tr") FontWeight.Bold else FontWeight.Medium),
                        color = if (selectedLanguageFilter == "tr") NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectedLanguageFilter == "ALL") NeonAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, if (selectedLanguageFilter == "ALL") NeonAmber else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { selectedLanguageFilter = "ALL" },
                ) {
                    Text(
                        text = if (isTr) "🌐 Tümü / Global ($globalCount)" else "🌐 Global / All ($globalCount)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selectedLanguageFilter == "ALL") FontWeight.Bold else FontWeight.Medium),
                        color = if (selectedLanguageFilter == "ALL") NeonAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }

                if (enCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selectedLanguageFilter == "en") Color(0xFF60A5FA).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, if (selectedLanguageFilter == "en") Color(0xFF60A5FA) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { selectedLanguageFilter = "en" },
                    ) {
                        Text(
                            text = "🇬🇧 English ($enCount)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selectedLanguageFilter == "en") FontWeight.Bold else FontWeight.Medium),
                            color = if (selectedLanguageFilter == "en") Color(0xFF60A5FA) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Filter Chips (My Rig Match, Category & Soft-Rounded Tuning Types)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // All Chip
                FilterChip(
                    selected = selectedTypeFilter == null && selectedCategoryFilter == null && !filterMyRigOnly,
                    onClick = {
                        selectedTypeFilter = null
                        selectedCategoryFilter = null
                        filterMyRigOnly = false
                    },
                    label = {
                        Text(
                            text = if (isTr) "Tümü (${approvedProfiles.size})" else "All (${approvedProfiles.size})",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                        selectedLabelColor = NeonCyan,
                    ),
                )

                // "My Rig Match" Quick Filter Chip (Active when user has CPU or GPU in PC Wizard)
                if (hasActiveRigCpuOrGpu) {
                    FilterChip(
                        selected = filterMyRigOnly,
                        onClick = { filterMyRigOnly = !filterMyRigOnly },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Build,
                                contentDescription = null,
                                tint = if (filterMyRigOnly) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                        },
                        label = {
                            Text(
                                text = if (isTr) "Sistemimle Eşleşenler" else "My Rig Match",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (filterMyRigOnly) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = NeonEmerald,
                        ),
                    )
                }

                // CPU Category
                FilterChip(
                    selected = selectedCategoryFilter == "CPU",
                    onClick = {
                        selectedCategoryFilter = if (selectedCategoryFilter == "CPU") null else "CPU"
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Memory,
                            contentDescription = null,
                            tint = if (selectedCategoryFilter == "CPU") NeonAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                    },
                    label = {
                        Text(
                            text = if (isTr) "İşlemci (CPU)" else "Processor (CPU)",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonAmber.copy(alpha = 0.2f),
                        selectedLabelColor = NeonAmber,
                    ),
                )

                // GPU Category
                FilterChip(
                    selected = selectedCategoryFilter == "GPU",
                    onClick = {
                        selectedCategoryFilter = if (selectedCategoryFilter == "GPU") null else "GPU"
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.VideogameAsset,
                            contentDescription = null,
                            tint = if (selectedCategoryFilter == "GPU") ElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                    },
                    label = {
                        Text(
                            text = if (isTr) "Ekran Kartı (GPU)" else "Graphics (GPU)",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                        selectedLabelColor = ElectricBlue,
                    ),
                )

                // Tuning Types with Soft-Rounded Vector Icons
                TuningType.entries.forEach { type ->
                    val isSelected = selectedTypeFilter == type
                    val typeTint = type.tintColor()
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedTypeFilter = if (isSelected) null else type
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = type.iconVector(),
                                contentDescription = null,
                                tint = if (isSelected) typeTint else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                        },
                        label = {
                            Text(
                                text = type.shortBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = typeTint.copy(alpha = 0.2f),
                            selectedLabelColor = typeTint,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // 4. Profiles List
            if (filteredProfiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(32.dp),
                        )
                        Text(
                            text = if (isTr) "Henüz bu filtreye uygun bir ayar bulunamadı." else "No tuning profiles match the current filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (isTr) {
                                "Kendi donanım ayarlarınızı 'Ayarını Paylaş' butonuna tıklayarak ilk siz ekleyin ve +50 itibar puanı kazanın!"
                            } else {
                                "Share your verified hardware settings first and earn +50 reputation points!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        if (selectedLanguageFilter == "tr" && approvedProfiles.isNotEmpty()) {
                            Button(
                                onClick = { selectedLanguageFilter = "ALL" },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber.copy(alpha = 0.18f), contentColor = NeonAmber),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(top = 4.dp),
                            ) {
                                Text(
                                    text = if (isTr) "🌐 Tüm Dillerdeki Ayarları Göster (${approvedProfiles.size})" else "Show All Profiles (${approvedProfiles.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    items(filteredProfiles, key = { it.id }) { profile ->
                        CommunityTuningCard(
                            profile = profile,
                            isTr = isTr,
                            onProofImageClick = { activeProofImageUrl = profile.proofImageUrl },
                            onDiscussionClick = { activeDiscussionTuning = profile },
                            onToggleReaction = { type ->
                                val userId = currentUser?.id ?: "guest-${System.currentTimeMillis()}"
                                if (profile.userId == userId || profile.userDisplayName == currentUser?.username) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (isTr) "Kendi paylaştığınız ayara tepki veremezsiniz." else "You cannot react to your own tuning profile."
                                        )
                                    }
                                } else {
                                    coroutineScope.launch {
                                        val res = tuningVaultRepository.toggleReaction(profile.id, type, userId)
                                        if (res is com.panda.myapplication.domain.common.DomainResult.Failure) {
                                            snackbarHostState.showSnackbar(
                                                if (isTr) "İşlem gerçekleştirilemedi." else "Action could not be completed."
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    // Lightbox Dialog for Proof Image
    activeProofImageUrl?.let { imageUrl ->
        AlertDialog(
            onDismissRequest = { activeProofImageUrl = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoCamera,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = if (isTr) "Doğrulama & Stres Testi Kanıtı" else "Verification & Stress Test Proof",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    IconButton(onClick = { activeProofImageUrl = null }, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = strings.closeButton)
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = if (isTr) "Test Kanıtı" else "Test Proof",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { activeProofImageUrl = null }) {
                    Text(strings.closeButton)
                }
            },
        )
    }

    // Comments / Discussion Bottom Sheet
    activeDiscussionTuning?.let { tuning ->
        @Suppress("DEPRECATION")
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val comments by tuningVaultRepository.observeComments(tuning.id).collectAsState(initial = emptyList())
        var newCommentText by remember { mutableStateOf("") }
        var isPosting by remember { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest = { activeDiscussionTuning = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium)
                    .padding(bottom = Spacing.large),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChatBubbleOutline,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = if (isTr) "Tartışma & Yorumlar" else "Discussion & Comments",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                            )
                        }
                        Text(
                            text = "${tuning.hardwareName} • ${tuning.profileTitle}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonPurple.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = if (isTr) "${comments.size} Yorum" else "${comments.size} Comments",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPurple,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Comments List
                if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isTr) {
                                "Bu ayar için henüz yorum yazılmamış. İlk yorumu siz yazın!"
                            } else {
                                "No comments yet for this profile. Be the first to share your thoughts!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(comments, key = { it.id }) { c ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = "@${c.userDisplayName}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = NeonAmber.copy(alpha = 0.18f),
                                            ) {
                                                Text(
                                                    text = c.userBadge,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = NeonAmber,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                )
                                            }
                                        }
                                        Text(
                                            text = "+${c.userReputation}p",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = NeonEmerald,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = c.commentText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Post Comment Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = {
                            Text(
                                text = if (isTr) "Yorumunuzu veya test deneyiminizi yazın..." else "Write your comment or test feedback...",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )

                    Button(
                        onClick = {
                            if (newCommentText.isNotBlank() && !isPosting) {
                                isPosting = true
                                val user = currentUser
                                val comment = TuningComment(
                                    id = "com-${System.currentTimeMillis()}-${(100..999).random()}",
                                    tuningId = tuning.id,
                                    userId = user?.id ?: "guest-${System.currentTimeMillis()}",
                                    userDisplayName = user?.username ?: (if (isTr) "Topluluk Üyesi" else "Community Member"),
                                    userBadge = user?.effectiveBadge ?: "Lehim Çırağı",
                                    userReputation = user?.reputationScore ?: 10,
                                    commentText = newCommentText.trim(),
                                    createdAtEpochMs = System.currentTimeMillis(),
                                )
                                coroutineScope.launch {
                                    val res = tuningVaultRepository.addComment(comment)
                                    if (res is DomainResult.Failure) {
                                        val msg = (res.error as? DomainError.Unknown)?.cause?.message ?: "Yorum gönderilemedi."
                                        snackbarHostState.showSnackbar(msg)
                                    } else {
                                        newCommentText = ""
                                    }
                                    isPosting = false
                                }
                            }
                        },
                        enabled = newCommentText.isNotBlank() && !isPosting,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // Tuning Share Dialog (Full System Rig Registration)
    if (showShareDialog) {
        TuningShareDialog(
            userId = currentUser?.id ?: "guest-${System.currentTimeMillis()}",
            userDisplayName = currentUser?.username ?: (if (isTr) "Topluluk Üyesi" else "Community Member"),
            userBadge = currentUser?.effectiveBadge ?: "Lehim Çırağı",
            hardwareRepository = hardwareRepository,
            activeBuildRepository = activeBuildRepository,
            tuningVaultRepository = tuningVaultRepository,
            onDismiss = { showShareDialog = false },
            onSubmit = { profile ->
                coroutineScope.launch {
                    val res = tuningVaultRepository.submitTuningProfile(profile)
                    showShareDialog = false
                    if (res is DomainResult.Failure) {
                        val msg = (res.error as? DomainError.Unknown)?.cause?.message ?: (if (isTr) "Ayar gönderilemedi." else "Submission failed.")
                        snackbarHostState.showSnackbar(msg)
                    } else {
                        snackbarHostState.showSnackbar(
                            if (isTr) {
                                "Ayarınız moderasyon onayına gönderildi! Onaylandığında +50 puan profilinize eklenecektir."
                            } else {
                                "Your tuning profile was sent for moderation! You will earn +50 reputation upon approval."
                            }
                        )
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommunityTuningCard(
    profile: TuningProfile,
    isTr: Boolean,
    onProofImageClick: () -> Unit,
    onDiscussionClick: () -> Unit,
    onToggleReaction: (TuningReactionType) -> Unit,
) {
    val lotteryTier = profile.siliconLotteryTier

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(
            width = 1.dp,
            color = when (lotteryTier) {
                SiliconLotteryTier.GOLD -> GoldAccent.copy(alpha = 0.55f)
                SiliconLotteryTier.SILVER -> NeonCyan.copy(alpha = 0.45f)
                SiliconLotteryTier.VERIFIED -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            },
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header: User Info & Multi-Select Tuning Type Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonPurple.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = profile.userDisplayName.take(1).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "@${profile.userDisplayName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NeonAmber.copy(alpha = 0.18f),
                            ) {
                                Text(
                                    text = profile.userBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = NeonAmber,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Multi-Select Tuning Type Badges (e.g., UNDERVOLT + PBO)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    profile.effectiveTuningTypes.forEach { type ->
                        val tint = type.tintColor()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tint.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, tint.copy(alpha = 0.6f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = type.iconVector(),
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = type.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                                    color = tint,
                                )
                            }
                        }
                    }
                }
            }

            // Target Hardware, Silicon Lottery Badge & Profile Title
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = profile.hardwareName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    SiliconLotteryTierPill(tier = lotteryTier, isTr = isTr)
                }
                Text(
                    text = profile.profileTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan,
                )
            }

            // Rig Specs Box (Motherboard, Cooler, RAM, Secondary HW) with Soft-Rounded Vector Icons
            val hasRigSpecs = !profile.motherboardName.isNullOrBlank() ||
                !profile.coolerName.isNullOrBlank() ||
                !profile.ramSpec.isNullOrBlank() ||
                !profile.secondaryHardware.isNullOrBlank()

            if (hasRigSpecs) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = if (isTr) "TEST EDİLEN SİSTEM BİLEŞENLERİ" else "VERIFIED SYSTEM RIG SPECS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.6.sp,
                        )

                        profile.motherboardName?.let {
                            RigSpecLine(
                                icon = Icons.Rounded.DeveloperBoard,
                                iconTint = NeonCyan,
                                label = if (isTr) "Anakart" else "Motherboard",
                                value = it,
                            )
                        }
                        profile.coolerName?.let {
                            RigSpecLine(
                                icon = Icons.Rounded.AcUnit,
                                iconTint = ElectricBlue,
                                label = if (isTr) "Soğutucu" else "Cooler",
                                value = it,
                            )
                        }
                        profile.ramSpec?.let {
                            RigSpecLine(
                                icon = Icons.Rounded.Memory,
                                iconTint = NeonPurple,
                                label = if (isTr) "RAM Kiti" else "Memory Kit",
                                value = it,
                            )
                        }
                        profile.secondaryHardware?.let {
                            RigSpecLine(
                                icon = Icons.Rounded.VideogameAsset,
                                iconTint = NeonAmber,
                                label = if (isTr) "Ek Donanım" else "Companion HW",
                                value = it,
                            )
                        }
                    }
                }
            }

            // Metrics Row (Voltage, Clock, Temperature delta)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                profile.voltageOffsetMv?.let {
                    MetricChip(
                        label = if (isTr) "Voltaj / CO" else "Voltage / CO",
                        value = "${if (it > 0) "+" else ""}${it.toInt()} mV",
                        color = NeonCyan,
                        modifier = Modifier.weight(1f),
                    )
                }

                profile.clockMhz?.let {
                    MetricChip(
                        label = if (isTr) "Frekans" else "Clock",
                        value = "${it.toInt()} MHz",
                        color = NeonAmber,
                        modifier = Modifier.weight(1f),
                    )
                }

                profile.tempDelta?.let { delta ->
                    MetricChip(
                        label = if (isTr) "Sıcaklık Farkı" else "Temp Delta",
                        value = "${if (delta > 0) "+" else ""}${delta.toInt()} °C",
                        color = if (delta <= 0) NeonEmerald else NeonRose,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Visual Before / After Temperature & Benchmark Delta Bar
            if ((profile.tempBeforeC != null && profile.tempAfterC != null) ||
                (profile.scoreBefore != null && profile.scoreAfter != null)
            ) {
                VisualBeforeAfterDeltaCard(profile = profile, isTr = isTr)
            }

            // User Experience & Guide Description Box
            if (!profile.guideNotes.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NeonAmber.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = if (isTr) "Kullanıcı Deneyimi & Rehber Notları:" else "User Experience & Guide Notes:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = profile.guideNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Proof Image Thumbnail Button (if available)
            if (!profile.proofImageUrl.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonEmerald.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onProofImageClick() },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isTr) "Doğrulanmış Test Kanıtı Görseli Mevcut" else "Verified Stress Test Proof Available",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonEmerald,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = if (isTr) "Görüntüle ↗" else "View ↗",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald,
                        )
                    }
                }
            }

            // Interactions Row (5 Reactions + Comments Drawer Trigger)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ReactionPill(emoji = "🔥", count = profile.reactionHotCount, onClick = { onToggleReaction(TuningReactionType.HOT) })
                    ReactionPill(emoji = "❄️", count = profile.reactionColdCount, onClick = { onToggleReaction(TuningReactionType.COLD) })
                    ReactionPill(emoji = "⚡", count = profile.reactionEfficientCount, onClick = { onToggleReaction(TuningReactionType.EFFICIENT) })
                    ReactionPill(emoji = "🧪", count = profile.reactionStableCount, onClick = { onToggleReaction(TuningReactionType.STABLE) })
                    ReactionPill(emoji = "⚠️", count = profile.reactionUnstableCount, onClick = { onToggleReaction(TuningReactionType.UNSTABLE) })
                }

                // Comments Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDiscussionClick() },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(imageVector = Icons.Rounded.ChatBubbleOutline, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                        Text(
                            text = "${profile.commentsCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SiliconLotteryTierPill(
    tier: SiliconLotteryTier,
    isTr: Boolean,
) {
    val (label, color, icon) = when (tier) {
        SiliconLotteryTier.GOLD -> Triple(
            if (isTr) "GOLD SAMPLE" else "GOLD SAMPLE",
            GoldAccent,
            Icons.Rounded.EmojiEvents,
        )
        SiliconLotteryTier.SILVER -> Triple(
            if (isTr) "SILVER TIER" else "SILVER TIER",
            NeonCyan,
            Icons.Rounded.WorkspacePremium,
        )
        SiliconLotteryTier.VERIFIED -> Triple(
            if (isTr) "DOĞRULANMIŞ" else "VERIFIED",
            NeonEmerald,
            Icons.Rounded.Verified,
        )
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(11.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                color = color,
            )
        }
    }
}

@Composable
private fun VisualBeforeAfterDeltaCard(
    profile: TuningProfile,
    isTr: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Temperature Before -> After bar
            val tBefore = profile.tempBeforeC
            val tAfter = profile.tempAfterC
            if (tBefore != null && tAfter != null && tBefore > 0) {
                val ratio = (tAfter / tBefore).toFloat().coerceIn(0.25f, 1f)
                val animatedRatio by animateFloatAsState(targetValue = ratio, animationSpec = tween(550))
                val delta = tAfter - tBefore
                val barColor = if (delta <= 0) NeonEmerald else NeonRose

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Thermostat,
                                contentDescription = null,
                                tint = barColor,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = if (isTr) "Termal Değişim (Önce → Sonra)" else "Thermal Shift (Before → After)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "${tBefore.toInt()}°C → ${tAfter.toInt()}°C (${if (delta > 0) "+" else ""}${delta.toInt()}°C)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                            color = barColor,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { animatedRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = barColor,
                        trackColor = NeonRose.copy(alpha = 0.25f),
                    )
                }
            }

            // Benchmark Before -> After bar
            val sBefore = profile.scoreBefore
            val sAfter = profile.scoreAfter
            if (sBefore != null && sAfter != null && sBefore > 0) {
                val pct = profile.scoreDeltaPercent ?: (((sAfter - sBefore) / sBefore) * 100.0)
                val pctRounded = (pct * 10.0).roundToInt() / 10.0
                val barColor = if (pct >= 0) ElectricBlue else NeonAmber
                val progress = (sAfter / (sBefore * 1.15)).toFloat().coerceIn(0.3f, 1f)
                val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(550))

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = null,
                                tint = barColor,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = profile.benchmarkName?.takeIf { it.isNotBlank() } ?: (if (isTr) "Sentetik Skor" else "Benchmark Score"),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "${sBefore.toInt()} → ${sAfter.toInt()} (${if (pctRounded >= 0) "+" else ""}$pctRounded%)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                            color = barColor,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RigSpecLine(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = color,
            )
        }
    }
}

@Composable
private fun ReactionPill(
    emoji: String,
    count: Int,
    onClick: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    )
    val borderColor by animateColorAsState(
        targetValue = if (count > 0) NeonCyan.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    )

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                pressed = !pressed
                onClick()
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = emoji, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
            if (count > 0) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
