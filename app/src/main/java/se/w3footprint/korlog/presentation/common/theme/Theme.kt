package se.w3footprint.korlog.presentation.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KorLogColorScheme = darkColorScheme(
    primary              = Blue600,
    onPrimary            = White,
    primaryContainer     = Blue900,
    onPrimaryContainer   = Blue50,

    secondary            = Green500,
    onSecondary          = White,
    secondaryContainer   = Green900,
    onSecondaryContainer = Green500,

    tertiary             = Amber500,
    onTertiary           = Slate900,

    error                = Red500,
    onError              = White,
    errorContainer       = Red700,
    onErrorContainer     = White,

    background           = Slate900,
    onBackground         = Slate100,

    surface              = Slate800,
    onSurface            = Slate300,
    surfaceVariant       = Slate700,
    onSurfaceVariant     = Slate400,

    outline              = Slate700,
    outlineVariant       = Slate800,
)

@Composable
fun KorLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KorLogColorScheme,
        typography  = KorLogTypography,
        content     = content
    )
}
