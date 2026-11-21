package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Grille d'espacement stricte basée sur 4 dp et rayons officiels du Design System.
 */
object FrancaisProDimens {
    val Space4 = 4.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Space20 = 20.dp
    val Space24 = 24.dp
    val Space32 = 32.dp

    val RadiusSmall = 10.dp
    val RadiusButton = 14.dp
    val RadiusCard = 18.dp
    val RadiusSurface = 24.dp
    val RadiusPill = 999.dp

    val BorderStrokeWidth = 1.dp
    val CardElevation = 1.dp
    val ElevatedCardElevation = 2.dp

    val MinTouchTarget = 48.dp
    val MaxContentWidth = 720.dp

    fun screenHorizontalPadding(availableWidth: Dp): Dp =
        if (availableWidth >= 412.dp) Space20 else Space16
}

/**
 * Tokens d'espacement injectés via CompositionLocal (grille stricte 4 dp).
 */
@Immutable
data class FrancaisProSpacing(
    val space4: Dp = FrancaisProDimens.Space4,
    val space8: Dp = FrancaisProDimens.Space8,
    val space12: Dp = FrancaisProDimens.Space12,
    val space16: Dp = FrancaisProDimens.Space16,
    val space20: Dp = FrancaisProDimens.Space20,
    val space24: Dp = FrancaisProDimens.Space24,
    val space32: Dp = FrancaisProDimens.Space32,
    val borderWidth: Dp = FrancaisProDimens.BorderStrokeWidth,
    val cardElevation: Dp = FrancaisProDimens.CardElevation,
    val elevatedCardElevation: Dp = FrancaisProDimens.ElevatedCardElevation,
    val minTouchTarget: Dp = FrancaisProDimens.MinTouchTarget,
    val maxContentWidth: Dp = FrancaisProDimens.MaxContentWidth
) {
    fun screenHorizontalPadding(availableWidth: Dp): Dp =
        if (availableWidth >= 412.dp) space20 else space16
}

val LocalSpacing = staticCompositionLocalOf { FrancaisProSpacing() }

/**
 * Couleurs sémantiques centralisées (aucun code hexadécimal dispersé dans les écrans).
 */
@Immutable
data class SemanticColors(
    val primaryNavy: Color,
    val primaryBlue: Color,
    val secondaryBlue: Color,
    val elevatedSurface: Color,
    val surfaceSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
    // Codes couleur sémantiques du Correcteur
    val spellingColor: Color,
    val spellingContainer: Color,
    val grammarColor: Color,
    val grammarContainer: Color,
    val syntaxColor: Color,
    val syntaxContainer: Color,
    val styleColor: Color,
    val styleContainer: Color,
    val navyHeader: Color,
    val onNavyHeader: Color
)

private val LightSemanticColors = SemanticColors(
    primaryNavy = PrimaryNavy,
    primaryBlue = PrimaryBlue,
    secondaryBlue = SecondaryBlue,
    elevatedSurface = ElevatedSurfaceLight,
    surfaceSubtle = SurfaceSubtleLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    border = BorderLight,
    success = SuccessLight,
    successContainer = SuccessContainerLight,
    onSuccessContainer = OnSuccessContainerLight,
    warning = WarningLight,
    warningContainer = WarningContainerLight,
    onWarningContainer = OnWarningContainerLight,
    danger = ErrorLight,
    dangerContainer = ErrorContainerLight,
    onDangerContainer = OnErrorContainerLight,
    spellingColor = ErrorLight,
    spellingContainer = ErrorContainerLight,
    grammarColor = WarningLight,
    grammarContainer = WarningContainerLight,
    syntaxColor = SyntaxVioletLight,
    syntaxContainer = SyntaxContainerLight,
    styleColor = PrimaryBlue,
    styleContainer = BlueContainerLight,
    navyHeader = PrimaryNavy,
    onNavyHeader = SurfaceLight
)

private val DarkSemanticColors = SemanticColors(
    primaryNavy = PrimaryNavy,
    primaryBlue = PrimaryBlueDark,
    secondaryBlue = SecondaryBlueDark,
    elevatedSurface = ElevatedSurfaceDark,
    surfaceSubtle = SurfaceSubtleDark,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    border = BorderDark,
    success = SuccessDark,
    successContainer = SuccessContainerDark,
    onSuccessContainer = OnSuccessContainerDark,
    warning = WarningDark,
    warningContainer = WarningContainerDark,
    onWarningContainer = OnWarningContainerDark,
    danger = ErrorDark,
    dangerContainer = ErrorContainerDark,
    onDangerContainer = OnErrorContainerDark,
    spellingColor = ErrorDark,
    spellingContainer = ErrorContainerDark,
    grammarColor = WarningDark,
    grammarContainer = WarningContainerDark,
    syntaxColor = SyntaxVioletDark,
    syntaxContainer = SyntaxContainerDark,
    styleColor = PrimaryBlueDark,
    styleContainer = BlueContainerDark,
    navyHeader = ElevatedSurfaceDark,
    onNavyHeader = TextPrimaryDark
)

val LocalSemanticColors = staticCompositionLocalOf { LightSemanticColors }

/**
 * Respecte le paramètre système Android « Réduire les animations ».
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

val FrancaisProLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceLight,
    primaryContainer = BlueContainerLight,
    onPrimaryContainer = OnBlueContainerLight,
    secondary = PrimaryNavy,
    onSecondary = SurfaceLight,
    secondaryContainer = SurfaceSubtleLight,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = SuccessLight,
    onTertiary = SurfaceLight,
    tertiaryContainer = SuccessContainerLight,
    onTertiaryContainer = OnSuccessContainerLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceSubtleLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLowest = SurfaceLight,
    surfaceContainerLow = BackgroundLight,
    surfaceContainer = SurfaceSubtleLight,
    surfaceContainerHigh = BorderLight,
    surfaceContainerHighest = BorderLight,
    outline = BorderLight,
    outlineVariant = BorderLight,
    error = ErrorLight,
    onError = SurfaceLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

val FrancaisProDarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = BackgroundDark,
    primaryContainer = BlueContainerDark,
    onPrimaryContainer = OnBlueContainerDark,
    secondary = SecondaryBlueDark,
    onSecondary = BackgroundDark,
    secondaryContainer = ElevatedSurfaceDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = SuccessDark,
    onTertiary = BackgroundDark,
    tertiaryContainer = SuccessContainerDark,
    onTertiaryContainer = OnSuccessContainerDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = ElevatedSurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = BackgroundDark,
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = ElevatedSurfaceDark,
    surfaceContainerHigh = BorderDark,
    surfaceContainerHighest = BorderDark,
    outline = BorderDark,
    outlineVariant = BorderDark,
    error = ErrorDark,
    onError = BackgroundDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

/**
 * Accesseur ergonomique aux tokens du Design System depuis n'importe quel Composable.
 */
object FrancaisProThemeTokens {
    val colors: SemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSemanticColors.current

    val spacing: FrancaisProSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current

    val dimens: FrancaisProDimens = FrancaisProDimens

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val textStyles: FrancaisProTextStyles = FrancaisProTextStyles
}

/**
 * Rayons officiels :
 * small = 10 dp, medium (boutons/inputs) = 14 dp, large (cartes) = 18 dp, extraLarge (grandes surfaces) = 24 dp.
 */
val FrancaisProShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(FrancaisProDimens.RadiusSmall),
    medium = RoundedCornerShape(FrancaisProDimens.RadiusButton),
    large = RoundedCornerShape(FrancaisProDimens.RadiusCard),
    extraLarge = RoundedCornerShape(FrancaisProDimens.RadiusSurface)
)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun FrancaisProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val reducedMotion = remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
    val spacing = remember { FrancaisProSpacing() }

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity() ?: return@SideEffect
            runCatching {
                val controller = WindowCompat.getInsetsController(activity.window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val colorScheme = if (darkTheme) FrancaisProDarkColorScheme else FrancaisProLightColorScheme
    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors

    CompositionLocalProvider(
        LocalSemanticColors provides semanticColors,
        LocalSpacing provides spacing,
        LocalReducedMotion provides reducedMotion
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FrancaisProTypography,
            shapes = FrancaisProShapes,
            content = content
        )
    }
}
