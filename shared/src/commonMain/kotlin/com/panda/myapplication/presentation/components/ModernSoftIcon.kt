package com.panda.myapplication.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Mouse
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SquareFoot
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.TableRestaurant
import androidx.compose.material.icons.rounded.TabletAndroid
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.util.accentColor

/**
 * Authoritative mapping from [HardwareCategory] to a clean, modern rounded [ImageVector].
 */
fun HardwareCategory.iconVector(): ImageVector = when (this) {
    HardwareCategory.CPU -> Icons.Rounded.Memory
    HardwareCategory.GPU -> Icons.Rounded.VideogameAsset
    HardwareCategory.RAM -> Icons.Rounded.Straighten
    HardwareCategory.MOTHERBOARD -> Icons.Rounded.DeveloperBoard
    HardwareCategory.PSU -> Icons.Rounded.Bolt
    HardwareCategory.STORAGE -> Icons.Rounded.Storage
    HardwareCategory.COOLER -> Icons.Rounded.AcUnit
    HardwareCategory.CASE -> Icons.Rounded.Inbox
    HardwareCategory.MONITOR -> Icons.Rounded.Tv
    HardwareCategory.MOUSE -> Icons.Rounded.Mouse
    HardwareCategory.MOUSEPAD -> Icons.Rounded.GridOn
    HardwareCategory.KEYBOARD -> Icons.Rounded.Keyboard
    HardwareCategory.HEADSET -> Icons.Rounded.Headphones
    HardwareCategory.MICROPHONE -> Icons.Rounded.Mic
    HardwareCategory.WEBCAM -> Icons.Rounded.Videocam
    HardwareCategory.GAMING_CHAIR -> Icons.Rounded.Chair
    HardwareCategory.CASE_FAN -> Icons.Rounded.Air
    HardwareCategory.UPS -> Icons.Rounded.BatteryChargingFull
    HardwareCategory.GAMING_DESK -> Icons.Rounded.TableRestaurant
    HardwareCategory.GAMING_LIGHTING -> Icons.Rounded.Lightbulb
    HardwareCategory.SMARTWATCH -> Icons.Rounded.Watch
    HardwareCategory.ROUTER -> Icons.Rounded.Router
    HardwareCategory.MODEM -> Icons.Rounded.Dns
    HardwareCategory.ACCESS_POINT -> Icons.Rounded.CellTower
    HardwareCategory.RANGE_EXTENDER -> Icons.Rounded.Sensors
    HardwareCategory.PREBUILT_PC -> Icons.Rounded.Computer
    HardwareCategory.LAPTOP -> Icons.Rounded.Laptop
    HardwareCategory.SMARTPHONE -> Icons.Rounded.Smartphone
    HardwareCategory.TABLET -> Icons.Rounded.TabletAndroid
}

/**
 * Modern soft icon container with customizable accent tint, delicate border, and rounded shape.
 */
@Composable
fun ModernSoftIcon(
    icon: ImageVector,
    accentColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    shape: Shape = RoundedCornerShape(10.dp),
    borderAlpha: Float = 0.35f,
    bgAlpha: Float = 0.14f,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
) {
    val resolvedColor = when {
        accentColor != Color.Unspecified -> accentColor
        tint != Color.Unspecified -> tint
        else -> NeonCyan
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(resolvedColor.copy(alpha = bgAlpha))
            .border(1.dp, resolvedColor.copy(alpha = borderAlpha), shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = resolvedColor,
            modifier = Modifier.size(iconSize),
        )
    }
}

/**
 * Convenience wrapper for [ModernSoftIcon] that automatically applies the category's accent color and icon.
 */
@Composable
fun SoftCategoryIcon(
    category: HardwareCategory,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    shape: Shape = RoundedCornerShape(10.dp),
    accentColor: Color = category.accentColor(),
) {
    ModernSoftIcon(
        icon = category.iconVector(),
        accentColor = accentColor,
        modifier = modifier,
        size = size,
        iconSize = iconSize,
        shape = shape,
        contentDescription = category.name,
    )
}

/**
 * Modern soft pill/chip with icon and text, used for categories, modes, and filters.
 */
@Composable
fun SoftCategoryPill(
    category: HardwareCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = category.accentColor(),
) {
    val containerBg = if (isSelected) accentColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val borderCol = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
    val textColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = containerBg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderCol),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = category.iconVector(),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}
