package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Palette principale — Mode Clair
val PrimaryNavy = Color(0xFF101B36)
val PrimaryBlue = Color(0xFF356AE6)
val SecondaryBlue = Color(0xFF6B8FF0)
val BackgroundLight = Color(0xFFF4F6FA)
val SurfaceLight = Color(0xFFFFFFFF)
val ElevatedSurfaceLight = Color(0xFFFFFFFF)
val SurfaceSubtleLight = Color(0xFFEEF2F8)
val TextPrimaryLight = Color(0xFF172033)
val TextSecondaryLight = Color(0xFF788397)
val SuccessLight = Color(0xFF16A36A)
val WarningLight = Color(0xFFF59E0B)
val ErrorLight = Color(0xFFDC4545)
val SyntaxVioletLight = Color(0xFF7C3AED)
val BorderLight = Color(0xFFE6EAF0)

// Conteneurs sémantiques doux — Mode Clair
val BlueContainerLight = Color(0xFFEBF1FD)
val OnBlueContainerLight = Color(0xFF173680)
val SuccessContainerLight = Color(0xFFE6F7EF)
val OnSuccessContainerLight = Color(0xFF0A4D33)
val WarningContainerLight = Color(0xFFFEF5E5)
val OnWarningContainerLight = Color(0xFF7A4A06)
val ErrorContainerLight = Color(0xFFFCECEC)
val OnErrorContainerLight = Color(0xFF7C1F1F)
val SyntaxContainerLight = Color(0xFFF2ECFE)
val OnSyntaxContainerLight = Color(0xFF4C1D95)

// Palette principale — Mode Sombre
val BackgroundDark = Color(0xFF0B1220)
val SurfaceDark = Color(0xFF131E31)
val ElevatedSurfaceDark = Color(0xFF1B2940)
val SurfaceSubtleDark = Color(0xFF162338)
val TextPrimaryDark = Color(0xFFF5F7FC)
val TextSecondaryDark = Color(0xFFA6B2C5)
val PrimaryBlueDark = Color(0xFF7098FF)
val SecondaryBlueDark = Color(0xFF93B4FF)
val BorderDark = Color(0xFF24344D)
val SuccessDark = Color(0xFF34D399)
val WarningDark = Color(0xFFFBBF24)
val ErrorDark = Color(0xFFF87171)
val SyntaxVioletDark = Color(0xFFA78BFA)

// Conteneurs sémantiques doux — Mode Sombre
val BlueContainerDark = Color(0xFF1A2E57)
val OnBlueContainerDark = Color(0xFFDCE7FF)
val SuccessContainerDark = Color(0xFF0F3227)
val OnSuccessContainerDark = Color(0xFFA7F3D0)
val WarningContainerDark = Color(0xFF3A2910)
val OnWarningContainerDark = Color(0xFFFDE68A)
val ErrorContainerDark = Color(0xFF3D1A1F)
val OnErrorContainerDark = Color(0xFFFECACA)
val SyntaxContainerDark = Color(0xFF2A1D4D)
val OnSyntaxContainerDark = Color(0xFFDDD6FE)

/**
 * Espace de noms structuré des tokens chromatiques officiels FRANÇAIS PRO (Clair & Sombre).
 */
object FrancaisProPalette {
    object Light {
        val primaryNavy = PrimaryNavy
        val primaryBlue = PrimaryBlue
        val secondaryBlue = SecondaryBlue
        val background = BackgroundLight
        val surface = SurfaceLight
        val elevatedSurface = ElevatedSurfaceLight
        val surfaceSubtle = SurfaceSubtleLight
        val textPrimary = TextPrimaryLight
        val textSecondary = TextSecondaryLight
        val success = SuccessLight
        val warning = WarningLight
        val error = ErrorLight
        val syntaxViolet = SyntaxVioletLight
        val styleBlue = PrimaryBlue
        val border = BorderLight
    }

    object Dark {
        val primaryNavy = PrimaryNavy
        val background = BackgroundDark
        val surface = SurfaceDark
        val elevatedSurface = ElevatedSurfaceDark
        val surfaceSubtle = SurfaceSubtleDark
        val textPrimary = TextPrimaryDark
        val textSecondary = TextSecondaryDark
        val primaryBlue = PrimaryBlueDark
        val secondaryBlue = SecondaryBlueDark
        val success = SuccessDark
        val warning = WarningDark
        val error = ErrorDark
        val syntaxViolet = SyntaxVioletDark
        val styleBlue = PrimaryBlueDark
        val border = BorderDark
    }
}

