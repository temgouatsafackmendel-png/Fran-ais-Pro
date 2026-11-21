package com.example.core.design

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.navigation.NavigationMotion
import com.example.ui.theme.FrancaisProDimens
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.LocalSemanticColors
import com.example.ui.theme.LocalSpacing

/**
 * 1. PrimaryButton — Action principale (rayon 14 dp, cible tactile ≥ 48 dp, retour tactile sobre).
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val interactive = enabled && !isLoading
    val spacing = LocalSpacing.current
    val reducedMotion = LocalReducedMotion.current
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && interactive && !reducedMotion) 0.985f else 1f,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 120),
        label = "primary_btn_scale"
    )
    Button(
        onClick = onClick,
        enabled = interactive,
        interactionSource = interactionSource,
        modifier = modifier
            .heightIn(min = 50.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            },
        shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
        contentPadding = PaddingValues(horizontal = spacing.space16 + 2.dp, vertical = spacing.space12),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.38f),
            disabledContentColor = contentColor.copy(alpha = 0.72f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = contentColor,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 2. SecondaryButton — Action secondaire sobre avec bordure fine (rayon 14 dp, cible tactile ≥ 48 dp).
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val semantic = LocalSemanticColors.current
    val spacing = LocalSpacing.current
    val reducedMotion = LocalReducedMotion.current
    val interactive = enabled && !isLoading
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && interactive && !reducedMotion) 0.985f else 1f,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 120),
        label = "secondary_btn_scale"
    )
    OutlinedButton(
        onClick = onClick,
        enabled = interactive,
        interactionSource = interactionSource,
        modifier = modifier
            .heightIn(min = spacing.minTouchTarget)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            },
        shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
        contentPadding = PaddingValues(horizontal = spacing.space16, vertical = spacing.space12),
        border = BorderStroke(
            spacing.borderWidth,
            if (interactive) semantic.border else semantic.border.copy(alpha = 0.5f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            disabledContentColor = contentColor.copy(alpha = 0.45f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = contentColor,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 3. AppIconButton — Bouton icône accessible (cible tactile ≥ 48 dp).
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = Color.Transparent
) {
    val semantic = LocalSemanticColors.current
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(FrancaisProDimens.MinTouchTarget)
            .clip(RoundedCornerShape(FrancaisProDimens.RadiusSmall))
            .background(containerColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else semantic.textSecondary.copy(alpha = 0.45f),
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * 4. AppTopBar — Barre supérieure sobre, cohérente et aérée.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    actions: @Composable () -> Unit = {}
) {
    val semantic = LocalSemanticColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(FrancaisProDimens.MinTouchTarget)
                            .testTag("top_bar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            },
            actions = { actions() },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )
        if (showDivider) {
            HorizontalDivider(thickness = 1.dp, color = semantic.border.copy(alpha = 0.65f))
        }
    }
}

@Composable
fun FrancaisProTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) = AppTopBar(title = title, subtitle = subtitle, onBack = onBack, actions = actions)

data class BottomNavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

/**
 * 5. AppBottomNavigation — Barre inférieure moderne à 5 destinations avec transitions fluides.
 */
@Composable
fun AppBottomNavigation(
    destinations: List<BottomNavDestination>,
    currentRoute: String?,
    onSelect: (String) -> Unit
) {
    val semantic = LocalSemanticColors.current
    val reducedMotion = LocalReducedMotion.current
    val iconAnimDuration = if (reducedMotion) 0 else NavigationMotion.TAB_ICON_DURATION_MS
    val scaleAnimDuration = if (reducedMotion) 0 else NavigationMotion.TAB_SCALE_DURATION_MS

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = FrancaisProDimens.BorderStrokeWidth, color = semantic.border)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets.navigationBars
        ) {
            destinations.forEach { dest ->
                val selected = currentRoute == dest.route
                val iconTint by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else semantic.textSecondary,
                    animationSpec = tween(durationMillis = iconAnimDuration, easing = FastOutSlowInEasing),
                    label = "nav_tint_${dest.route}"
                )
                val labelColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else semantic.textSecondary,
                    animationSpec = tween(durationMillis = iconAnimDuration, easing = FastOutSlowInEasing),
                    label = "nav_label_${dest.route}"
                )
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.08f else 1.0f,
                    animationSpec = tween(durationMillis = scaleAnimDuration, easing = FastOutSlowInEasing),
                    label = "nav_scale_${dest.route}"
                )
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(dest.route) },
                    icon = {
                        Crossfade(
                            targetState = selected,
                            animationSpec = tween(durationMillis = iconAnimDuration, easing = FastOutSlowInEasing),
                            label = "nav_icon_crossfade_${dest.route}"
                        ) { isSelected ->
                            Icon(
                                imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.label,
                                tint = iconTint,
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                            )
                        }
                    },
                    label = {
                        Text(
                            text = dest.label,
                            color = labelColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = semantic.textSecondary,
                        unselectedTextColor = semantic.textSecondary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag(dest.testTag)
                )
            }
        }
    }
}

/**
 * Variante NavigationRail adaptative pour tablettes et écrans ≥ 600 dp.
 */
@Composable
fun AppNavigationRail(
    destinations: List<BottomNavDestination>,
    currentRoute: String?,
    onSelect: (String) -> Unit
) {
    val semantic = LocalSemanticColors.current
    val reducedMotion = LocalReducedMotion.current
    val iconAnimDuration = if (reducedMotion) 0 else NavigationMotion.TAB_ICON_DURATION_MS
    val scaleAnimDuration = if (reducedMotion) 0 else NavigationMotion.TAB_SCALE_DURATION_MS

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(Modifier.height(12.dp))
        destinations.forEach { dest ->
            val selected = currentRoute == dest.route
            val iconTint by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary else semantic.textSecondary,
                animationSpec = tween(durationMillis = iconAnimDuration, easing = FastOutSlowInEasing),
                label = "rail_tint_${dest.route}"
            )
            val iconScale by animateFloatAsState(
                targetValue = if (selected) 1.08f else 1.0f,
                animationSpec = tween(durationMillis = scaleAnimDuration, easing = FastOutSlowInEasing),
                label = "rail_scale_${dest.route}"
            )
            NavigationRailItem(
                selected = selected,
                onClick = { onSelect(dest.route) },
                icon = {
                    Crossfade(
                        targetState = selected,
                        animationSpec = tween(durationMillis = iconAnimDuration, easing = FastOutSlowInEasing),
                        label = "rail_icon_crossfade_${dest.route}"
                    ) { isSelected ->
                        Icon(
                            imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = dest.label,
                            tint = iconTint,
                            modifier = Modifier.graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                        )
                    }
                },
                label = {
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = semantic.textSecondary,
                    unselectedTextColor = semantic.textSecondary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag(dest.testTag)
            )
        }
    }
}

/**
 * 6. AppSearchBar — Champ de recherche fluide, rayon 14 dp, bouton d'effacement intégré.
 */
@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val semantic = LocalSemanticColors.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = semantic.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Rechercher",
                tint = semantic.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Effacer la recherche",
                            tint = semantic.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                trailingContent?.invoke()
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
        shape = RoundedCornerShape(FrancaisProDimens.RadiusButton),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = semantic.border
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Surface éditoriale standard (rayon 18 dp, bordure fine 1 dp, élévation maîtrisée 1–2 dp).
 */
@Composable
fun EditorialCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = LocalSemanticColors.current.border,
    contentPadding: Dp = LocalSpacing.current.space16,
    elevated: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalSpacing.current
    val shape = RoundedCornerShape(FrancaisProDimens.RadiusCard)
    val cardModifier = if (onClick != null) {
        modifier.clip(shape).clickable(role = Role.Button, onClick = onClick)
    } else {
        modifier
    }
    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(spacing.borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (elevated) spacing.elevatedCardElevation else spacing.cardElevation
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Badge sémantique discret (rayon 10 dp).
 */
@Composable
fun CategoryBadge(
    label: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Pastille de niveau CECRL (A1–C2).
 */
@Composable
fun CefrLevelPill(level: String, modifier: Modifier = Modifier) {
    val semantic = LocalSemanticColors.current
    val (bg, fg) = when (level) {
        "A1", "A2" -> semantic.successContainer to semantic.success
        "B1", "B2" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        else -> semantic.warningContainer to semantic.warning
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
        color = bg
    ) {
        Text(
            text = level,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * En-tête de section structuré avec icône moderne optionnelle, sous-titre et action à droite.
 */
@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null
) {
    val semantic = LocalSemanticColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (icon != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (actionText != null && onAction != null) {
            Box(
                modifier = Modifier
                    .heightIn(min = FrancaisProDimens.MinTouchTarget)
                    .clip(RoundedCornerShape(FrancaisProDimens.RadiusSmall))
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Indicateur discret « Fonctionne hors ligne » — réservé uniquement à l'Onboarding et aux Paramètres (§14).
 */
@Composable
fun DiscreetOfflineBadge(modifier: Modifier = Modifier) {
    val semantic = LocalSemanticColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
        color = semantic.successContainer,
        border = BorderStroke(1.dp, semantic.success.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CloudOff,
                contentDescription = "Fonctionne hors ligne",
                tint = semantic.success,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Fonctionne hors ligne",
                style = MaterialTheme.typography.labelMedium,
                color = semantic.onSuccessContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Avatar circulaire local aux initiales de l'utilisateur.
 */
@Composable
fun UserInitialsAvatar(
    displayName: String,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val initials = displayName.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "FP" }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(semantic.primaryNavy)
            .semantics { contentDescription = "Profil de $displayName" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
