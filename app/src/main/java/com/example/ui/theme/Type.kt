package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val FrauncesFontFamily = FontFamily(
    Font(R.font.fraunces, FontWeight.Normal),
    Font(R.font.fraunces, FontWeight.SemiBold),
    Font(R.font.fraunces, FontWeight.Bold)
)

val PlusJakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

/**
 * Échelle typographique officielle FRANÇAIS PRO (9 niveaux nommés) :
 * Display, Heading 1, Heading 2, Heading 3, Body Large, Body Medium, Body Small, Caption, Label.
 */
object FrancaisProTextStyles {
    val Display = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    )
    val Heading1 = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp
    )
    val Heading2 = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.15).sp
    )
    val Heading3 = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp
    )
    val BodyLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    val BodyMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    )
    val BodySmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    )
    val Caption = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
    val Label = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    )
    val MonoMetric = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}

val FrancaisProTypography = Typography(
    displayLarge = FrancaisProTextStyles.Display,
    displayMedium = FrancaisProTextStyles.Display.copy(fontSize = 28.sp, lineHeight = 34.sp),
    headlineLarge = FrancaisProTextStyles.Heading1,
    headlineMedium = FrancaisProTextStyles.Heading2,
    headlineSmall = FrancaisProTextStyles.Heading3,
    titleLarge = FrancaisProTextStyles.Heading2.copy(fontSize = 19.sp, lineHeight = 25.sp),
    titleMedium = FrancaisProTextStyles.Heading3,
    titleSmall = FrancaisProTextStyles.BodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge = FrancaisProTextStyles.BodyLarge,
    bodyMedium = FrancaisProTextStyles.BodyMedium,
    bodySmall = FrancaisProTextStyles.BodySmall,
    labelLarge = FrancaisProTextStyles.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = FrancaisProTextStyles.Label,
    labelSmall = FrancaisProTextStyles.Caption
)
