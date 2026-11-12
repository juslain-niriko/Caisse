package mg.caisse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import mg.caisse.data.repository.ThemeMode

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = Color(0xFF00302B),
    secondary = TealLight,
    onSecondary = Color.White,
    background = BgLight,
    onBackground = Color(0xFF111318),
    surface = SurfaceLight,
    onSurface = Color(0xFF111318),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = Red,
    outline = Color(0xFFC9D0D6)
)

private val DarkColors = darkColorScheme(
    primary = TealLight,
    onPrimary = Color(0xFF00201C),
    primaryContainer = Color(0xFF0B3B36),
    onPrimaryContainer = TealContainer,
    secondary = TealLight,
    onSecondary = Color(0xFF00201C),
    background = BgDark,
    onBackground = Color(0xFFE6E9EC),
    surface = SurfaceDark,
    onSurface = Color(0xFFE6E9EC),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = Color(0xFFFF6B6B),
    outline = Color(0xFF3A434C)
)

@Composable
fun CaisseTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = CaisseTypography,
        content = content
    )
}
