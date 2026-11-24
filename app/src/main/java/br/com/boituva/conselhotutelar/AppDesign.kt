package br.com.boituva.conselhotutelar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class Tone(val background: Color, val ink: Color) {
    GREEN(Color(0xFFDFF1E8), Color(0xFF12584A)),
    BLUE(Color(0xFFE8EEF9), Color(0xFF344E83)),
    RED(Color(0xFFFAEAE6), Color(0xFF903530)),
    AMBER(Color(0xFFF8EDDA), Color(0xFF735016)),
}

private val colors = lightColorScheme(
    primary = Tone.GREEN.ink,
    onPrimary = Color.White,
    primaryContainer = Tone.GREEN.background,
    onPrimaryContainer = Color(0xFF143F34),
    secondary = Tone.BLUE.ink,
    secondaryContainer = Tone.BLUE.background,
    onSecondaryContainer = Tone.BLUE.ink,
    background = Color(0xFFF5F7F3),
    onBackground = Color(0xFF172F29),
    surface = Color.White,
    onSurface = Color(0xFF172F29),
    surfaceVariant = Color(0xFFE9EFEB),
    onSurfaceVariant = Color(0xFF465B53),
    outline = Color(0xFF70867B),
    outlineVariant = Color(0xFFDBE5DD),
    error = Tone.RED.ink,
    errorContainer = Tone.RED.background,
    onErrorContainer = Tone.RED.ink,
    inverseSurface = Color(0xFF173B32),
    inverseOnSurface = Color.White,
)

private val typography = Typography(
    headlineLarge = TextStyle(fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.8).sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.45).sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 27.sp, letterSpacing = (-0.25).sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
)

@Composable
internal fun CouncilTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}

@Composable
internal fun AppIcon(@DrawableRes icon: Int, modifier: Modifier = Modifier, tint: Color = Color.Unspecified) {
    val painter = painterResource(icon)
    if (tint == Color.Unspecified) {
        Icon(painter, contentDescription = null, modifier = modifier)
    } else {
        Icon(painter, contentDescription = null, modifier = modifier, tint = tint)
    }
}

@Composable
internal fun IconTile(@DrawableRes icon: Int, tone: Tone, prominent: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (prominent) 54.dp else 44.dp)
            .clip(RoundedCornerShape(if (prominent) 18.dp else 14.dp))
            .background(tone.background),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(icon, Modifier.size(if (prominent) 28.dp else 22.dp), tone.ink)
    }
}

@Composable
internal fun BrandHero(onHelp: () -> Unit) {
    val ink = Color(0xFFF1FFF5)
    val shape = RoundedCornerShape(30.dp)
    Box(
        Modifier.fillMaxWidth().clip(shape).background(
            Brush.linearGradient(listOf(Color(0xFF133B34), Color(0xFF195D4F))),
        ),
    ) {
        Canvas(Modifier.matchParentSize()) {
            val center = Offset(x = size.width * 0.98f, y = size.height * 0.44f)
            val spacing = 27.dp.toPx()
            for (index in 1..5) {
                drawCircle(
                    color = Color(0xFFC7F5D2).copy(alpha = 0.12f),
                    center = center,
                    radius = spacing * index,
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            drawCircle(Color(0xFFCEEFBB).copy(alpha = 0.65f), 3.dp.toPx(), center.copy(x = center.x - spacing * 2))
        }
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            IconTile(R.drawable.ic_shield, Tone.GREEN, prominent = true)
            Text(
                "Conselho Tutelar de Boituva",
                Modifier.semantics { heading() },
                color = ink,
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                "Projeto de extensão da UNIASSELVI",
                color = Color(0xFFD2E8DC),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = onHelp,
                modifier = Modifier.padding(top = 6.dp).fillMaxWidth().heightIn(min = 54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDFF4CE),
                    contentColor = Color(0xFF193F31),
                ),
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 13.dp),
            ) {
                AppIcon(R.drawable.ic_phone, Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Preciso de ajuda", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                AppIcon(R.drawable.ic_arrow_right, Modifier.size(20.dp))
            }
        }
    }
}

@Composable
internal fun PageIntro(
    title: String,
    @DrawableRes icon: Int,
    tone: Tone,
    summary: String? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = tone.background,
        contentColor = tone.ink,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(icon, Modifier.size(25.dp))
            }
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
            if (summary != null) Text(summary, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
internal fun BodyPanel(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
internal fun ContentCard(
    title: String,
    @DrawableRes icon: Int,
    tone: Tone = Tone.GREEN,
    summary: String? = null,
    emphasized: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
        color = if (emphasized) tone.background else colors.surface,
        contentColor = if (emphasized) tone.ink else colors.onSurface,
        shape = RoundedCornerShape(18.dp),
        border = if (emphasized) null else BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconTile(icon, tone)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (summary != null) {
                    Text(summary, style = MaterialTheme.typography.bodyMedium,
                        color = if (emphasized) tone.ink else colors.onSurfaceVariant)
                }
            }
            AppIcon(R.drawable.ic_chevron_right, Modifier.size(18.dp), tone.ink)
        }
    }
}

@Composable
internal fun ScreenAction(label: String, @DrawableRes icon: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        AppIcon(icon, Modifier.size(19.dp))
        Spacer(Modifier.width(9.dp))
        Text(label, Modifier.weight(1f))
    }
}

@Composable
internal fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.semantics { heading() },
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
internal fun InfoNote(text: String) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppIcon(R.drawable.ic_info, Modifier.size(19.dp), MaterialTheme.colorScheme.primary)
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
