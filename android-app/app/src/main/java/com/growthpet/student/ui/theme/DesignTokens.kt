package com.growthpet.student.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Spacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
}

object Radius {
    val sm: Dp = 10.dp
    val md: Dp = 14.dp
    val lg: Dp = 20.dp
    val xl: Dp = 26.dp
    val pill: Dp = 999.dp
}

object Elevation {
    val flat: Dp = 0.dp
    val card: Dp = 2.dp
    val floating: Dp = 8.dp
}

object IconStyle {
    val sizeSmall: Dp = 18.dp
    val sizeStandard: Dp = 22.dp
    val sizeLarge: Dp = 26.dp
    val touchTargetMin: Dp = 48.dp
}

object AnimationDuration {
    const val fastMs: Int = 180
    const val standardMs: Int = 320
    const val breatheMs: Int = 3400
    const val speakPulseMs: Int = 850
}

object CardStyle {
    val shape = RoundedCornerShape(Radius.lg)
    val border = BorderStroke(1.dp, ColorTokens.BorderHairline)
    val innerPadding = Spacing.md
}

object ButtonStyle {
    val heightPrimary: Dp = 52.dp
    val heightCompact: Dp = 44.dp
    val shape = RoundedCornerShape(Radius.md)
}

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        color = ColorTokens.InkPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = ColorTokens.InkPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = ColorTokens.InkPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = ColorTokens.InkPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = ColorTokens.InkSecondary
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = ColorTokens.InkMuted
    )
)
