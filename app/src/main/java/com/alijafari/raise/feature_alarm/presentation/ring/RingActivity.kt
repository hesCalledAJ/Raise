package com.alijafari.raise.feature_alarm.presentation.ring

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.DragHandleSizes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.alijafari.raise.R
import com.alijafari.raise.core.ui.theme.Wakee2Theme
import com.alijafari.raise.core.utils.getTimeString
import com.alijafari.raise.feature_alarm.data.service.AlarmService
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_logs.domain.model.EventLog
import com.alijafari.raise.feature_logs.domain.repository.LogRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

const val ACTION_FINISH_RING_ACTIVITY = "com.alijafari.wakee2.ACTION_FINISH_RING_ACTIVITY"

@AndroidEntryPoint
class RingActivity : ComponentActivity() {
    @Inject
    lateinit var logRepository: LogRepository
    private var finishReceiver: BroadcastReceiver? = null
    private var alarmService: AlarmService? = null
    private var isBound = false
    private val viewModel: RingViewModel by viewModels()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val alarmBinder = binder as AlarmService.AlarmBinder
            alarmService = alarmBinder.getService()
            isBound = true
            alarmService?.let { viewModel.attachService(it) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            alarmService = null
            viewModel.detachService()
        }
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            viewModel.hideHeadsUpNotification()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        super.onCreate(savedInstanceState)

        registerFinishReceiver()

        enableEdgeToEdge()

        setContent {
            val alarm by viewModel.alarm.collectAsState()
            Wakee2Theme {
                val isSnoozed by viewModel.isSnoozed.collectAsState(initial = false)

                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    RingScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        alarm = alarm,
                        isSnoozed = isSnoozed,
                        onDismiss = { viewModel.onDismiss() },
                        onSnooze = { viewModel.onSnooze() },
                        onSkipSnooze = {viewModel.onSkipSnooze()}
                    )
                }
            }
        }
    }

    private fun registerFinishReceiver() {
        finishReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                logRepository.logEvent(
                    EventLog(
                        event = "Ring Activity",
                        info = "Finish Received"
                    )
                )
                finish()
            }

        }
        val filter = IntentFilter(ACTION_FINISH_RING_ACTIVITY)
        val listenToBroadcastsFromOtherApps = false
        val receiverFlags = if (listenToBroadcastsFromOtherApps) {
            ContextCompat.RECEIVER_EXPORTED
        } else {
            ContextCompat.RECEIVER_NOT_EXPORTED
        }
        ContextCompat.registerReceiver(this, finishReceiver, filter, receiverFlags)
    }

    override fun onStart() {
        super.onStart()
        Intent(this, AlarmService::class.java).also {
            bindService(it, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        viewModel.detachService()
        finishReceiver?.let {
            unregisterReceiver(it)
        }
    }
}

enum class RingDragState {
    IDLE, DRAGGING_DOWN, DRAGGING_DOWN_DONE,DRAGGING_UP ,DRAGGING_UP_DONE
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun RingScreen(
    modifier: Modifier = Modifier,
    alarm: Alarm?,
    isSnoozed: Boolean,
    onDismiss: () -> Unit,
    onSkipSnooze: () -> Unit,
    onSnooze: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var isDragging by remember { mutableStateOf(false) }
    val dragOffset = remember { Animatable(0f) }
    val sheetFraction = remember { Animatable(0f) }

    val density = LocalDensity.current
    val dragThresholdPx = with(density) { 140.dp.toPx() }
    val maxSheetFraction = 0.6f

    val screenHeightPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    val effectiveSheetFraction by remember {
        derivedStateOf {
            val fraction = (-dragOffset.value / screenHeightPx).coerceIn(0f, maxSheetFraction)
            max(fraction, sheetFraction.value)
        }
    }

    val screenState: RingDragState by remember(dragOffset.value, sheetFraction.value, isDragging) {
        derivedStateOf {
            when {
                !isDragging && dragOffset.value == 0f -> RingDragState.IDLE
                dragOffset.value > 0 -> {
                    if (dragOffset.value >= dragThresholdPx) RingDragState.DRAGGING_DOWN_DONE
                    else RingDragState.DRAGGING_DOWN
                }
                dragOffset.value < 0 -> {
                    if (sheetFraction.value >= maxSheetFraction) RingDragState.DRAGGING_UP_DONE
                    else RingDragState.DRAGGING_UP
                }
                else -> RingDragState.IDLE
            }
        }
    }

    val topShapeHeightDp by remember {
        derivedStateOf {
            with(density) { max(0f, dragOffset.value).toDp() }
        }
    }

    val scale = if (dragOffset.value < dragThresholdPx / 2) 1f else animateFloatAsState(
        targetValue = if (screenState == RingDragState.DRAGGING_DOWN_DONE) .94f else 1f,
        animationSpec = spring(
            dampingRatio = if (screenState == RingDragState.DRAGGING_DOWN_DONE && isDragging) Spring.DampingRatioHighBouncy else Spring.DampingRatioNoBouncy,
            stiffness = 500f
        ),
        label = "bounceScale"
    ).value

    val animatedCorner = if (dragOffset.value < dragThresholdPx / 2) 0.dp else animateDpAsState(
        targetValue = if (screenState == RingDragState.DRAGGING_DOWN_DONE) 35.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = if (screenState == RingDragState.DRAGGING_DOWN_DONE && isDragging) Spring.DampingRatioLowBouncy else Spring.DampingRatioNoBouncy,
            stiffness = 500f,
        ),
        label = "cornerRadius",
    ).value.coerceIn(minimumValue = 0.dp, maximumValue = null)

    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {

            val bottomSheetRelease: () -> Unit = {
                scope.launch {
                    val shouldStayOpen = sheetFraction.value > maxSheetFraction * 0.7f
                    val target = if (shouldStayOpen) maxSheetFraction else 0f

                    sheetFraction.animateTo(
                        target,
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }
            if (screenState in listOf(
                    RingDragState.DRAGGING_DOWN,
                    RingDragState.DRAGGING_DOWN_DONE
                )
            ) {
                Box(
                    modifier = Modifier
                        .height(topShapeHeightDp)
                        .fillMaxWidth()
                        .scale(scale)
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(
                                animatedCorner,
                                animatedCorner,
                                35.dp,
                                35.dp
                            )
                        )
                        .padding(8.dp),
                )
                {
                    AnimatedContent(
                        targetState = screenState,
                        transitionSpec = {
                            scaleIn(
                                initialScale = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioHighBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ).togetherWith(fadeOut())
                        },
                        label = "content"
                    ) { state ->
                        if (state == RingDragState.DRAGGING_DOWN_DONE) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Snooze,
                                    contentDescription = "Snooze",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(
                                        R.string.n_minutes,
                                        alarm!!.snoozeMinutes
                                    ),
                                    style = MaterialTheme.typography.headlineSmallEmphasized,
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Spacer(Modifier)
                                Text(
                                    color = MaterialTheme.colorScheme.onError,
                                    style = MaterialTheme.typography.bodyMedium,
                                    text = "drag down to snooze"
                                )
                                Icon(
                                    Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Drag down",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            )
            {
                if (alarm == null) {
                    LoadingIndicator()
                    return@Column
                }

                if (isSnoozed) {
                    Text(stringResource(R.string.snoozed), style = MaterialTheme.typography.bodySmall)
                }

                AnimatedVisibility(screenState == RingDragState.IDLE) {
                    HelperArrows(true,stringResource(R.string.dismiss))
                }
                val scale = animateFloatAsState(
                    targetValue = if (!isDragging) .95f else 1.05f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = 500f
                    ),
                    label = "bounceScale"
                ).value
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .offset {
                            val sheetHeightPx = screenHeightPx * sheetFraction.value
                            val normalized =
                                (sheetFraction.value / maxSheetFraction).coerceIn(0f, 1f)
                            val dragBased = (dragOffset.value * 0.5f)
                            val maxUpOffset = -sheetHeightPx / 2f
                            val interpolated =
                                dragBased * (1f - normalized) + maxUpOffset * normalized
                            val finalOffset =
                                if (interpolated < maxUpOffset) maxUpOffset else interpolated
                            IntOffset(0, finalOffset.roundToInt())
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    isDragging = true
                                    change.consume()

                                    val newOffset = (dragOffset.value + dragAmount).coerceIn(
                                        -screenHeightPx,
                                        screenHeightPx
                                    )

                                    scope.launch {
                                        dragOffset.snapTo(newOffset)

                                        if (newOffset < 0f) {
                                            val targetFraction =
                                                (-newOffset / screenHeightPx).coerceIn(
                                                    0f,
                                                    maxSheetFraction
                                                )
                                            sheetFraction.snapTo(targetFraction)
                                        } else if (sheetFraction.value > 0f && dragAmount > 0f) {
                                            val collapseAmount = dragAmount / screenHeightPx
                                            sheetFraction.snapTo(
                                                (sheetFraction.value - collapseAmount).coerceAtLeast(
                                                    0f
                                                )
                                            )
                                        }
                                    }
                                },
                                onDragEnd = {
                                    isDragging = false
                                    scope.launch {
                                        if (dragOffset.value >= dragThresholdPx) {
                                            delay(600)
                                            onSnooze()
                                        } else if (dragOffset.value < 0f) {
                                            if (screenState == RingDragState.DRAGGING_UP_DONE) {
                                                delay(600)
                                                if (isSnoozed) onSkipSnooze() else onDismiss()
                                            }
                                            bottomSheetRelease()
                                        }
                                        dragOffset.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                    Log.e("TAG", "end:${screenState.name} ",)
                                }
                            )
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                )
                {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AlarmFace(
                            isSnoozed = isSnoozed,
                            dragProgress = if (isSnoozed) 1f else (effectiveSheetFraction / maxSheetFraction).coerceIn(0f, 1f),
                            alarm = alarm
                        )
                    }
                }
                AnimatedVisibility(screenState == RingDragState.IDLE) {
                    HelperArrows(false,stringResource(R.string.snooze))
                }
            }

            val bottomSheetDragDown: (Float) -> Unit = { deltaPx ->
                scope.launch {
                    val newFraction = (sheetFraction.value - (deltaPx / screenHeightPx))
                        .coerceIn(0f, maxSheetFraction)
                    sheetFraction.snapTo(newFraction)
                }
            }

            val scrimModifier = if (effectiveSheetFraction == maxSheetFraction) Modifier.pointerInput(Unit){
                detectTapGestures {
                    scope.launch {
                        sheetFraction.animateTo(
                            0f,
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }
            } else Modifier
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(
                            alpha = effectiveSheetFraction.coerceIn(
                                minimumValue = null,
                                maximumValue = .4f
                            )
                        )
                    )
                    .then(scrimModifier))
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                )
                {
                    BottomSheetOverlay(
                        fraction = effectiveSheetFraction,
                        onDragDown = { deltaPx ->
                            bottomSheetDragDown(deltaPx)
                        },
                        onRelease = {
                            bottomSheetRelease()
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            VerticalDragHandle(
                                sizes = DragHandleSizes(
                                    size = DpSize(40.dp,4.dp),
                                    pressedSize = DpSize(55.dp,7.dp),
                                    draggedSize = DpSize(50.dp,5.dp)
                                )
                            )
                            Spacer(modifier.height(7.dp))
                            Text(
                                text = if (screenState == RingDragState.DRAGGING_UP_DONE) "Release to dismiss" else "Drag up to dismiss",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomSheetOverlay(
    fraction: Float,
    onDragDown: (deltaPx: Float) -> Unit,
    onRelease: () -> Unit,
    content: @Composable () -> Unit
) {
    val screenHeightPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    val sheetHeightPx = (screenHeightPx * fraction)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(with(LocalDensity.current) { sheetHeightPx.toDp() })
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0f) onDragDown(dragAmount)
                    },
                    onDragEnd = { onRelease() }
                )
            }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmFace(
    isSnoozed: Boolean,
    dragProgress : Float,
    alarm: Alarm
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationSpeed = lerp(1f, 0.4f, dragProgress)
    val rotation by infiniteTransition.animateFloat(
        0f, 360f, infiniteRepeatable(
            animation = tween((8000 / rotationSpeed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val animatedPadding by animateDpAsState(
        targetValue = if (isSnoozed) 5.dp else 0.dp, animationSpec = tween(400)
    )

    val animatedBgColor by animateColorAsState(
        targetValue = lerp(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.surface,
            dragProgress
        ),
        animationSpec = tween(400)
    )

    val animatedContentColor by animateColorAsState(
        targetValue = lerp(
            MaterialTheme.colorScheme.onPrimary,
            MaterialTheme.colorScheme.primary,
            dragProgress
        ),
        animationSpec = tween(400)
    )

    val strokeBackgroundModifier = if (isSnoozed) Modifier
        .background(
            shape = MaterialShapes.Cookie12Sided.toShape(),
            color = MaterialTheme.colorScheme.primary
        )
        .padding(animatedPadding) else Modifier
    Box(
        modifier = Modifier
            .padding(15.dp)
            .graphicsLayer(rotationZ = rotation)
            .background(
                shape = MaterialShapes.Cookie12Sided.toShape(),
                color = animatedBgColor
            )
            .then(strokeBackgroundModifier)
            .aspectRatio(1f)
            .wrapContentSize(Alignment.Center)
    ) {
        //Alarm Data
        Column(
            modifier = Modifier.graphicsLayer(rotationZ = rotation * -1),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = alarm.getTimeString(),
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
                color = animatedContentColor
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = alarm.label,
                style = MaterialTheme.typography.bodyLarge,
                color = animatedContentColor
            )
        }
    }
}

@Composable
fun HelperArrows(isDraggingUp: Boolean,hint : String) {
    val icon = if (isDraggingUp) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        @Composable
        fun hintText() = Text(
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .8f),
            text = hint
        )
        if (!isDraggingUp) {
            hintText()
            Spacer(Modifier.height(5.dp))
        }
        repeat(3) { _index ->
            val index = _index.takeIf { !isDraggingUp }?:(3-_index)

            val transition = rememberInfiniteTransition()

            val offset = index * 100
            val duration = 900
            val delay = 500

            val alpha by transition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = duration,
                        delayMillis = delay,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(offset)
                )
            )

            val scale by transition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = duration,
                        delayMillis = delay,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(offset)
                )
            )

            val offsetY by transition.animateValue(
                initialValue = if (isDraggingUp) 5.dp else  0.dp,
                targetValue = if (isDraggingUp) 0.dp else 5.dp,
                typeConverter = Dp.VectorConverter,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        delayMillis = delay,
                        durationMillis = duration,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(offset)
                )
            )

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                modifier = Modifier
                    .size(20.dp)
                    .scale(scale)
                    .offset(y = offsetY)
                    .zIndex(-1f)
            )
            Spacer(Modifier.height(5.dp))
        }
        if (isDraggingUp) {
            hintText()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RingScreenPreview() {
    Wakee2Theme {
        RingScreen(
            modifier = Modifier,
            alarm = Alarm(),
            isSnoozed = false,
            onDismiss = {},
            onSkipSnooze = {},
        ) { }
    }
}