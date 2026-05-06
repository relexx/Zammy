package com.zammy.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.zammy.app.R
import androidx.compose.ui.text.font.Font

// ─── Google Fonts provider ────────────────────────────────────────────────────

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

val DmSans: FontFamily = FontFamily(
    Font(GoogleFont("DM Sans"), fontProvider, FontWeight.Light),
    Font(GoogleFont("DM Sans"), fontProvider, FontWeight.Normal),
    Font(GoogleFont("DM Sans"), fontProvider, FontWeight.Normal, FontStyle.Italic),
    Font(GoogleFont("DM Sans"), fontProvider, FontWeight.Medium),
    Font(GoogleFont("DM Sans"), fontProvider, FontWeight.SemiBold),
    Font(GoogleFont("DM Sans"), fontProvider, FontWeight.Bold),
)

val JetBrainsMono: FontFamily = FontFamily(
    Font(GoogleFont("JetBrains Mono"), fontProvider, FontWeight.Normal),
    Font(GoogleFont("JetBrains Mono"), fontProvider, FontWeight.Medium),
)

// ─── Semantic color tokens ────────────────────────────────────────────────────

object ZammyColors {
    val Accent           = Color(0xFF4F8EF7)

    val StatusOpen       = Color(0xFF22C55E)
    val StatusPending    = Color(0xFFF59E0B)
    val StatusClosed     = Color(0xFF94A3B8)
    val StatusClosedDark = Color(0xFF555566)
    val StatusNew        = Color(0xFF4F8EF7)
    val StatusEscalated  = Color(0xFFEF4444)
    val StatusMerged     = Color(0xFFA78BFA)

    val PriorityLow    = Color(0xFF22C55E)
    val PriorityNormal = Color(0xFF4F8EF7)
    val PriorityHigh   = Color(0xFFF59E0B)
    val PriorityUrgent = Color(0xFFEF4444)

    // Article bubbles
    val ArticlePublicBg      = Color(0xFFEBF2FF)
    val ArticleInternalBg    = Color(0xFFFFFBEB)
    val ArticleCustomerBg    = Color(0xFFE8E9EF)
    val ArticlePublicBgDark  = Color(0xFF1A2744)
    val ArticleInternalBgDark = Color(0xFF2D2818)
    val ArticleCustomerBgDark = Color(0xFF2A2A38)
    val ArticleInternalBorder = Color(0x44F59E0B)

    // Surface palette (light)
    val BgLight       = Color(0xFFF4F5F7)
    val SurfaceLight  = Color(0xFFFFFFFF)
    val Surface2Light = Color(0xFFF0F1F5)
    val Surface3Light = Color(0xFFE8E9EF)

    // Surface palette (dark)
    val BgDark        = Color(0xFF0F0F13)
    val SurfaceDark   = Color(0xFF1A1A22)
    val Surface2Dark  = Color(0xFF22222E)
    val Surface3Dark  = Color(0xFF2A2A38)
}

// ─── ThemeMode ────────────────────────────────────────────────────────────────

enum class ThemeMode { LIGHT, DARK, SYSTEM }

// ─── Color schemes ────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary             = ZammyColors.Accent,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFDEEBFF),
    onPrimaryContainer  = Color(0xFF0A1E3F),
    secondary           = Color(0xFF64748B),
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF1E293B),
    background          = ZammyColors.BgLight,
    onBackground        = Color(0xFF111118),
    surface             = ZammyColors.SurfaceLight,
    onSurface           = Color(0xFF111118),
    surfaceVariant      = ZammyColors.Surface2Light,
    onSurfaceVariant    = Color(0x80111118),
    error               = ZammyColors.StatusEscalated,
    onError             = Color.White,
    outline             = Color(0x14000000),
    outlineVariant      = Color(0x14000000),
)

private val DarkColorScheme = darkColorScheme(
    primary             = ZammyColors.Accent,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF1A2744),
    onPrimaryContainer  = Color(0xFFDEEBFF),
    secondary           = Color(0xFF94A3B8),
    onSecondary         = Color(0xFF0F0F13),
    secondaryContainer  = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    background          = ZammyColors.BgDark,
    onBackground        = Color(0xFFF0F0F5),
    surface             = ZammyColors.SurfaceDark,
    onSurface           = Color(0xFFF0F0F5),
    surfaceVariant      = ZammyColors.Surface2Dark,
    onSurfaceVariant    = Color(0x8CF0F0F5),
    error               = ZammyColors.StatusEscalated,
    onError             = Color.White,
    outline             = Color(0x12FFFFFF),
    outlineVariant      = Color(0x12FFFFFF),
)

// ─── Typography ───────────────────────────────────────────────────────────────

val ZammyTypography = Typography(
    displayLarge   = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Bold,      fontSize = 57.sp,  lineHeight = 64.sp),
    displayMedium  = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Bold,      fontSize = 45.sp,  lineHeight = 52.sp),
    displaySmall   = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Bold,      fontSize = 36.sp,  lineHeight = 44.sp),
    headlineLarge  = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Bold,      fontSize = 28.sp,  lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Bold,      fontSize = 22.sp,  lineHeight = 28.sp),
    headlineSmall  = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp,  lineHeight = 24.sp),
    titleLarge     = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.SemiBold,  fontSize = 22.sp,  lineHeight = 28.sp),
    titleMedium    = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.SemiBold,  fontSize = 16.sp,  lineHeight = 24.sp),
    titleSmall     = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Medium,    fontSize = 14.sp,  lineHeight = 20.sp),
    bodyLarge      = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Normal,    fontSize = 16.sp,  lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Normal,    fontSize = 14.sp,  lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Normal,    fontSize = 12.sp,  lineHeight = 16.sp),
    labelLarge     = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.SemiBold,  fontSize = 14.sp,  lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp),
)

// ─── Composable entry point ───────────────────────────────────────────────────

@Composable
fun ZammyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = ZammyTypography,
        content     = content
    )
}
