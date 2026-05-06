package com.zammy.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object ZammyColors {
    val Accent = Color(0xFF4F8EF7)

    val StatusOpen = Color(0xFF22C55E)
    val StatusPending = Color(0xFFF59E0B)
    val StatusClosed = Color(0xFF94A3B8)
    val StatusEscalated = Color(0xFFEF4444)
    val StatusMerged = Color(0xFFA78BFA)

    val PriorityLow = Color(0xFF94A3B8)
    val PriorityNormal = Color(0xFF4F8EF7)
    val PriorityHigh = Color(0xFFF59E0B)
    val PriorityUrgent = Color(0xFFEF4444)

    val ArticlePublicBg = Color(0xFFEBF2FF)
    val ArticleInternalBg = Color(0xFFFFFBEB)
    val ArticleCustomerBg = Color(0xFFFFFFFF)
    val ArticlePublicBgDark = Color(0xFF1A2744)
    val ArticleInternalBgDark = Color(0xFF2A2210)
    val ArticleCustomerBgDark = Color(0xFF1A1A22)
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F8EF7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEEBFF),
    onPrimaryContainer = Color(0xFF0A1E3F),
    secondary = Color(0xFF64748B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF1E293B),
    background = Color(0xFFF4F5F7),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF0F1F5),
    onSurfaceVariant = Color(0xFF64748B),
    error = Color(0xFFEF4444),
    onError = Color.White,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4F8EF7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A2744),
    onPrimaryContainer = Color(0xFFDEEBFF),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F0F13),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    background = Color(0xFF0F0F13),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF1A1A22),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF22222E),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = Color(0xFFEF4444),
    onError = Color.White,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private val ZammyTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)

@Composable
fun ZammyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = ZammyTypography,
        content = content
    )
}
