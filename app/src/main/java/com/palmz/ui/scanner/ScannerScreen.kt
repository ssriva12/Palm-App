package com.palmz.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmz.BuildConfig
import com.palmz.R
import com.palmz.ads.rememberAdManager
import com.palmz.domain.model.Hand
import com.palmz.domain.model.LineId
import com.palmz.domain.model.PalmLine
import com.palmz.domain.model.PalmReading
import com.palmz.ui.components.ClayCard
import com.palmz.ui.components.MysticScaffold
import com.palmz.ui.components.PrimaryButton
import com.palmz.ui.components.SecondaryButton
import com.palmz.ui.components.SectionHeader
import com.palmz.ui.components.StatTile
import com.palmz.ui.scanner.camera.CameraCapture
import com.palmz.ui.theme.ClayShape
import com.palmz.ui.theme.clay
import kotlin.math.roundToInt
import com.palmz.ui.theme.Spacing

@Composable
fun ScannerScreen(onBack: () -> Unit, onPaywall: () -> Unit) {
    val vm: ScannerViewModel = hiltViewModel()
    val scansRemaining by vm.scansRemaining.collectAsStateWithLifecycle()
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* granted or not, the worker fails open */ }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                ScannerEvent.PaywallRequired -> onPaywall()
                ScannerEvent.RequestNotificationPermission ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
            }
        }
    }

    val adManager = rememberAdManager()
    val activity = LocalActivity.current
    var showDebug by remember { mutableStateOf(false) }

    // Leaving the reading — via "Scan again" or Back — is where the interstitial lands.
    fun leaveResult(back: Boolean) {
        val proceed = { if (back) onBack() else vm.scanAgain() }
        if (vm.showAdOnExit && activity != null) {
            adManager.showInterstitial(activity) { proceed() }
        } else {
            proceed()
        }
    }

    BackHandler(enabled = vm.phase == ScanPhase.RESULT) { leaveResult(back = true) }

    MysticScaffold(
        title = stringResource(R.string.feature_palm_scanner_title),
        onBack = { if (vm.phase == ScanPhase.RESULT) leaveResult(back = true) else onBack() },
        actions = {
            if (vm.phase == ScanPhase.RESULT) {
                SecondaryButton(
                    text = stringResource(R.string.action_scan_again),
                    compact = true,
                    onClick = { leaveResult(back = false) },
                )
            }
        },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.space20),
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
                    ResultPhase(
                        reading = reading,
                        image = vm.capturedImage,
                        onLongPress = { showDebug = true },
                    )
                }

                ScanPhase.ERROR -> ErrorPhase(onRetry = vm::scanAgain)
            }
            Spacer(Modifier.height(Spacing.space24))
        }
    }

    if (BuildConfig.DEBUG && showDebug) {
        DebugOverlay(vm = vm, onDismiss = { showDebug = false })
    }
}

@Composable
private fun PalmFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .clay(ClayShape, fill = MaterialTheme.colorScheme.surfaceVariant),
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
        stringResource(R.string.scanner_guide_instruction),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(Spacing.space4))
    Text(
        if (scansRemaining > 0) {
            pluralStringResource(R.plurals.free_scans_remaining, scansRemaining, scansRemaining)
        } else {
            stringResource(R.string.scanner_no_scans_left)
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(Spacing.space16))

    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space10)) {
        Hand.entries.forEach { h ->
            FilterChip(
                selected = hand == h,
                onClick = { onHand(h) },
                label = { Text(stringResource(R.string.scanner_hand_label, h.label)) },
            )
        }
    }
    Text(
        if (hand == Hand.RIGHT) {
            stringResource(R.string.scanner_hand_dominant_desc)
        } else {
            stringResource(R.string.scanner_hand_nondominant_desc)
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Spacing.space6),
    )

    Spacer(Modifier.height(Spacing.space16))

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
                modifier = Modifier.padding(Spacing.space24),
            ) {
                Icon(
                    Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.height(Spacing.space12))
                Text(
                    stringResource(R.string.scanner_camera_permission_rationale),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(Spacing.space16))
                PrimaryButton(stringResource(R.string.action_allow_camera)) {
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
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(Spacing.space16))
            Text(
                stringResource(R.string.scanner_processing_message),
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
            modifier = Modifier.padding(Spacing.space24),
        ) {
            Icon(
                Icons.Outlined.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(Spacing.space12))
            Text(
                stringResource(R.string.scanner_error_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
    Spacer(Modifier.height(Spacing.space16))
    PrimaryButton(stringResource(R.string.action_try_again), Modifier.fillMaxWidth(), onClick = onRetry)
}

@Composable
private fun ResultPhase(
    reading: PalmReading,
    image: ImageBitmap?,
    onLongPress: () -> Unit,
) {
    var expanded by remember { mutableStateOf(emptySet<LineId>()) }
    var expandedAreas by remember { mutableStateOf(emptySet<LifeAreaId>()) }
    val aspect = image?.let { it.width.toFloat() / it.height }?.coerceIn(0.5f, 1.6f) ?: 0.78f

    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clay(ClayShape, fill = Color(0xFF241F1A)) // dark so the traced lines always read
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress() }) },
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = stringResource(R.string.cd_your_palm),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                Icons.Outlined.BackHand,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(96.dp),
            )
        }
    }
    Spacer(Modifier.height(Spacing.space16))
    Text(
        reading.summary,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(Modifier.height(Spacing.space6))
    Text(
        stringResource(R.string.scanner_tap_line_hint),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(Modifier.height(Spacing.space20))
    SectionHeader(stringResource(R.string.scanner_life_areas_title))
    Spacer(Modifier.height(Spacing.space10))
    listOf(
        LifeAreaId.MONEY to reading.lifeAreas.money,
        LifeAreaId.CAREER to reading.lifeAreas.career,
        LifeAreaId.RELATIONSHIPS to reading.lifeAreas.relationships,
        LifeAreaId.MARRIAGE to reading.lifeAreas.marriage,
        LifeAreaId.FAMILY to reading.lifeAreas.family,
    ).forEach { (id, text) ->
        LifeAreaCard(
            id = id,
            text = text,
            expanded = id in expandedAreas,
            onToggle = {
                expandedAreas = if (id in expandedAreas) expandedAreas - id else expandedAreas + id
            },
        )
        Spacer(Modifier.height(Spacing.space12))
    }

    Spacer(Modifier.height(Spacing.space8))
    reading.lines.forEach { line ->
        LineCard(
            line = line,
            expanded = line.id in expanded,
            onToggle = {
                expanded = if (line.id in expanded) expanded - line.id else expanded + line.id
            },
        )
        Spacer(Modifier.height(Spacing.space12))
    }

    Row(
        Modifier.height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(Spacing.space12),
    ) {
        StatTile(
            stringResource(R.string.scanner_lucky_number_label),
            reading.luckyNumber.toString(),
            Modifier.weight(1f).fillMaxHeight(),
        )
        StatTile(
            stringResource(R.string.scanner_lucky_color_label),
            reading.luckyColor,
            Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LineCard(line: PalmLine, expanded: Boolean, onToggle: () -> Unit) {
    ClayCard(Modifier.fillMaxWidth(), onClick = onToggle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                lineIcon(line.id),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(Spacing.space8))
            Text(
                line.id.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.scanner_confidence, (line.confidence * 100).roundToInt()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(Spacing.space6))
            Icon(
                if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = stringResource(if (expanded) R.string.cd_collapse else R.string.cd_expand),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        if (!expanded) {
            Spacer(Modifier.height(Spacing.space6))
            Text(
                line.teaser,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(Spacing.space12))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space6)) {
                    line.attributes.chips.forEach { Pill(it) }
                }
                Spacer(Modifier.height(Spacing.space10))
                Text(
                    line.reading,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun lineIcon(id: LineId): ImageVector = when (id) {
    LineId.LIFE -> Icons.Outlined.Spa
    LineId.HEAD -> Icons.Outlined.Lightbulb
    LineId.HEART -> Icons.Outlined.FavoriteBorder
    LineId.FATE -> Icons.Outlined.Explore
}

private enum class LifeAreaId { MONEY, CAREER, RELATIONSHIPS, MARRIAGE, FAMILY }

@Composable
private fun LifeAreaCard(id: LifeAreaId, text: String, expanded: Boolean, onToggle: () -> Unit) {
    ClayCard(Modifier.fillMaxWidth(), onClick = onToggle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                lifeAreaIcon(id),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(Spacing.space8))
            Text(
                lifeAreaLabel(id),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = stringResource(if (expanded) R.string.cd_collapse else R.string.cd_expand),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        if (!expanded) {
            Spacer(Modifier.height(Spacing.space6))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(Spacing.space10))
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun lifeAreaIcon(id: LifeAreaId): ImageVector = when (id) {
    LifeAreaId.MONEY -> Icons.Outlined.AttachMoney
    LifeAreaId.CAREER -> Icons.Outlined.Work
    LifeAreaId.RELATIONSHIPS -> Icons.Outlined.FavoriteBorder
    LifeAreaId.MARRIAGE -> Icons.Outlined.Groups
    LifeAreaId.FAMILY -> Icons.Outlined.Home
}

@Composable
private fun lifeAreaLabel(id: LifeAreaId): String = stringResource(
    when (id) {
        LifeAreaId.MONEY -> R.string.scanner_facet_money
        LifeAreaId.CAREER -> R.string.horoscope_facet_career
        LifeAreaId.RELATIONSHIPS -> R.string.scanner_facet_relationships
        LifeAreaId.MARRIAGE -> R.string.scanner_facet_marriage
        LifeAreaId.FAMILY -> R.string.scanner_facet_family
    },
)

@Composable
private fun Pill(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.space8, vertical = Spacing.space4),
    )
}
