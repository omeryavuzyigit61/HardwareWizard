package com.panda.myapplication.presentation.tools

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.LocalAppCurrency
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import hardwarewizard.shared.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

object LenientStringListSerializer : kotlinx.serialization.KSerializer<List<String>> {
    override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =
        kotlinx.serialization.descriptors.buildClassSerialDescriptor("LenientStringList")

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: List<String>) {
        val jsonEncoder = encoder as? kotlinx.serialization.json.JsonEncoder
        if (jsonEncoder != null) {
            jsonEncoder.encodeJsonElement(
                kotlinx.serialization.json.JsonArray(value.map { kotlinx.serialization.json.JsonPrimitive(it) })
            )
        }
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): List<String> {
        val jsonDecoder = decoder as? kotlinx.serialization.json.JsonDecoder ?: return emptyList()
        val element = jsonDecoder.decodeJsonElement()
        return when (element) {
            is kotlinx.serialization.json.JsonArray -> {
                element.mapNotNull { (it as? kotlinx.serialization.json.JsonPrimitive)?.content }
            }
            is kotlinx.serialization.json.JsonPrimitive -> {
                val str = element.content
                if (str.contains("•")) {
                    str.split("•").map { it.trim() }.filter { it.isNotBlank() }
                } else if (str.isNotBlank()) {
                    listOf(str.trim())
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
}

@Serializable
data class GlossaryTermDto(
    val id: String,
    val term: String,
    val category: String,
    val shortDescription: String,
    val detailedExplanation: String,
    @Serializable(with = LenientStringListSerializer::class)
    val keyMetrics: List<String> = emptyList(),
    val relatedTerms: List<String> = emptyList(),
)

private val glossaryJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

@OptIn(
    ExperimentalResourceApi::class,
    FlowPreview::class,
    ExperimentalLayoutApi::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class,
)
@Composable
fun HardwareGlossaryScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var allTerms by remember { mutableStateOf<List<GlossaryTermDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQueryText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val searchQueryFlow = remember { MutableStateFlow("") }
    val debouncedQuery by remember {
        searchQueryFlow
            .debounce(250L)
            .mapLatest { it.trim().lowercase() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }.collectAsState(initial = "")

    val strings = LocalAppStrings.current
    val isTurkish = strings.isTurkish

    LaunchedEffect(isTurkish) {
        val loaded = withContext(Dispatchers.Default) {
            runCatching {
                val fileName = if (isTurkish) "files/hardware_glossary.json" else "files/hardware_glossary_en.json"
                val bytes = Res.readBytes(fileName)
                glossaryJson.decodeFromString<List<GlossaryTermDto>>(bytes.decodeToString())
            }.onFailure { err ->
                println("GLOSSARY PARSE ERROR: $err")
                err.printStackTrace()
            }.getOrDefault(emptyList())
        }
        allTerms = loaded
        isLoading = false
    }

    val categories = remember(allTerms, strings) {
        val dynamic = allTerms.map { it.category }.distinct()
        listOf(null to strings.glossaryCategoryAll) + dynamic.map { it to it }
    }

    val filteredEntries = remember(allTerms, debouncedQuery, selectedCategory) {
        allTerms.filter { entry ->
            val matchesCat = selectedCategory == null || entry.category == selectedCategory
            val q = debouncedQuery
            val matchesQuery = q.isEmpty() ||
                entry.term.lowercase().contains(q) ||
                entry.shortDescription.lowercase().contains(q) ||
                entry.detailedExplanation.lowercase().contains(q) ||
                entry.keyMetrics.any { it.lowercase().contains(q) } ||
                entry.relatedTerms.any { it.lowercase().contains(q) }
            matchesCat && matchesQuery
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = LocalAppStrings.current.drawerGlossaryTitle,
                subtitle = "${LocalAppStrings.current.drawerGlossarySubtitle} (${allTerms.size})",
                icon = "📚",
                onNavigateBack = onNavigateBack,
                onOpenDrawer = onOpenDrawer,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
            ) {
                OutlinedTextField(
                    value = searchQueryText,
                    onValueChange = {
                        searchQueryText = it
                        searchQueryFlow.value = it
                    },
                    placeholder = { Text(strings.glossarySearchPlaceholder) },
                    trailingIcon = {
                        if (searchQueryText.isNotEmpty()) {
                            Text(
                                text = "✕",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        searchQueryText = ""
                                        searchQueryFlow.value = ""
                                    }
                                    .padding(8.dp),
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    categories.forEach { (cat, label) ->
                        val selected = selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategory = cat },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan,
                            ),
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            } else if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.large),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = strings.glossaryEmpty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                ) {
                    items(
                        items = filteredEntries,
                        key = { it.id },
                    ) { entry ->
                        val categoryAccent = when (entry.category) {
                            "Anakart & Güç", "VRM & Güç", "Motherboard & Power", "VRM & Power" -> NeonAmber
                            "İşlemci & Çiplet", "İşlemci & Bellek", "CPU & Architecture", "CPU & Memory" -> ElectricBlue
                            "Bellek & RAM", "RAM & Memory" -> NeonPurple
                            "Ekran Kartı & Grafik", "GPU & Graphics" -> NeonRose
                            "Depolama (SSD)", "Storage (SSD/NVMe)" -> NeonCyan
                            "Soğutma & Kasa", "Soğutma", "Cooling & Chassis", "Thermal Solutions" -> NeonCyan
                            "Monitör & Panel", "Ekran & Panel", "Display & Panel" -> ElectricBlue
                            "Çevre Birimleri", "Peripherals & Input" -> NeonEmerald
                            else -> NeonCyan
                        }
                        FluidOrganicCard(
                            modifier = Modifier.fillMaxWidth(),
                            accentColor = categoryAccent,
                            containerColor = MaterialTheme.colorScheme.surface,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonCyan.copy(alpha = 0.16f),
                                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                                    ) {
                                        Text(
                                            text = entry.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = NeonCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        )
                                    }
                                }

                                Text(
                                    text = entry.term,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )

                                Text(
                                    text = entry.shortDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonAmber,
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                                Text(
                                    text = entry.detailedExplanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                if (entry.keyMetrics.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = NeonEmerald.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = "📐 ${strings.glossaryCriticalMetrics}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = NeonEmerald,
                                            )
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                entry.keyMetrics.forEach { metric ->
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                                        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
                                                    ) {
                                                        Text(
                                                            text = metric,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = NeonEmerald,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (entry.relatedTerms.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = strings.glossaryRelated,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        entry.relatedTerms.forEach { related ->
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = NeonCyan.copy(alpha = 0.10f),
                                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
                                                modifier = Modifier.clickable {
                                                    selectedCategory = null
                                                    searchQueryText = related
                                                    searchQueryFlow.value = related
                                                },
                                            ) {
                                                Text(
                                                    text = "🔗 $related",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = NeonCyan,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
