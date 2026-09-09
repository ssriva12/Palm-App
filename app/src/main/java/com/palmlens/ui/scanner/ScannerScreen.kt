package com.palmlens.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmlens.domain.model.Hand
import com.palmlens.domain.model.PalmLine
import com.palmlens.domain.model.PalmReading
import com.palmlens.ui.components.GlassCard
import com.palmlens.ui.components.MysticScaffold
import com.palmlens.ui.components.PrimaryButton
import com.palmlens.ui.components.SecondaryButton
import com.palmlens.ui.scanner.camera.CameraCapture
import kotlin.math.roundToInt

@Composable
fun ScannerScreen(onBack: () -> Unit, onPaywall: () -> Unit) {
    val vm: ScannerViewModel = hiltViewModel()
    val scansRemaining by vm.scansRemaining.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                ScannerEvent.PaywallRequired -> onPaywall()
            }
        }
    }

    MysticScaffold(title = "Palm Scanner", onBack = onBack) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            when (vm.phase) {
                ScanPhase.GUIDE -> GuidePhase(
                    hand = vm.hand,
                    scansRemaining = scansRemaining,
                    onHand = vm::updateHand,
                    onCaptured = vm::capture,
                )

                ScanPhase.PROCESSING -> ProcessingPhase()

                ScanPhase.RESULT -> vm.reading?.let { reading ->
                    ResultPhase(reading = reading, onScanAgain = vm::scanAgain)
                }

                ScanPhase.ERROR -> ErrorPhase(onRetry = vm::scanAgain)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PalmFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(cs.surfaceVariant, cs.surface))),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun GuidePhase(
    hand: Hand,
    scansRemaining: Int,
    onHand: (Hand) -> Unit,
    onCaptured: (ByteArray) -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    Text(
        "Hold your hand flat, palm to the camera, in even light.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        if (scansRemaining > 0) "$scansRemaining free scan${if (scansRemaining == 1) "" else "s"} left" else "No free scans left",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Hand.entries.forEach { h ->
            FilterChip(
                selected = hand == h,
                onClick = { onHand(h) },
                label = { Text("${h.label} hand") },
            )
        }
    }
    Text(
        if (hand == Hand.RIGHT) "Dominant hand — your present and future." else "Non-dominant — inherited traits.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp),
    )

    Spacer(Modifier.height(16.dp))

    if (hasPermission) {
        PalmFrame {
            CameraCapture(
                modifier = Modifier.fillMaxSize(),
                onCaptured = onCaptured,
                onError = { /* Phase 4: surface a friendly camera error */ },
            )
        }
    } else {
        PalmFrame {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp),
            ) {
                Text("📷", fontSize = 44.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Palmlens needs the camera to scan your palm. The photo is sent for a reading and never saved.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Allow camera") {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}

@Composable
private fun ProcessingPhase() {
    PalmFrame {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                "Tracing your lines…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorPhase(onRetry: () -> Unit) {
    PalmFrame {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text("🌫️", fontSize = 44.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "The stars are cloudy. Try that scan again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
    Spacer(Modifier.height(16.dp))
    PrimaryButton("Try again", Modifier.fillMaxWidth(), onClick = onRetry)
}

@Composable
private fun ResultPhase(reading: PalmReading, onScanAgain: () -> Unit) {
    PalmFrame {
        Text("🖐", fontSize = 72.sp)
        PalmLinesOverlay(reading.lines, Modifier.fillMaxSize())
    }
    Spacer(Modifier.height(16.dp))
    Text(
        reading.summary,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(Modifier.height(16.dp))
    reading.lines.forEach { line ->
        LineCard(line)
        Spacer(Modifier.height(12.dp))
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard(Modifier.weight(1f), contentPadding = 14) {
            Text(
                "LUCKY NUMBER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                reading.luckyNumber.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        GlassCard(Modifier.weight(1f), contentPadding = 14) {
            Text(
                "LUCKY COLOUR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                reading.luckyColor,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    SecondaryButton("Scan again", Modifier.fillMaxWidth(), onClick = onScanAgain)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LineCard(line: PalmLine) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(line.id.emoji, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                line.id.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${(line.confidence * 100).roundToInt()}% sure",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            line.attributes.chips.forEach { Pill(it) }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            line.reading,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Pill(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
