package com.growthpet.student.companion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.growthpet.student.R
import com.growthpet.student.ui.theme.AnimationDuration
import com.growthpet.student.ui.theme.ColorTokens
import com.growthpet.student.ui.theme.Radius
import com.growthpet.student.ui.theme.Spacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanionAnimationController @Inject constructor() {
    private val _state = MutableStateFlow(CompanionState.IDLE)
    val state: StateFlow<CompanionState> = _state.asStateFlow()

    fun transitionTo(next: CompanionState) {
        _state.value = next
    }

    fun transitionFromWire(wireKey: String?) {
        _state.value = CompanionState.fromWireKey(wireKey)
    }
}

/**
 * Unified Yunlong visual stage.
 *
 * Asset resolution order:
 * 1. If a drop-in GIF resource exists in res/raw named `yunlong_<state>` (e.g. yunlong_idle.gif),
 *    Coil's ImageDecoder/GifDecoder plays it directly.
 * 2. Otherwise renders our designer-matched Yunlong studio illustrations paired with
 *    state-specific procedural motion (idle breathing, head-tilt listening, focused thinking orbit,
 *    looped speaking cadence without raised claws, celebrating lift, and resting calm) and
 *    Multiply/soft-halo blending so no white rectangle border ever appears.
 */
@Composable
fun YunlongCompanionStage(
    state: CompanionState,
    companionName: String,
    isLowerGrade: Boolean,
    size: Dp = 188.dp,
    floating: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val infinite = rememberInfiniteTransition(label = "yunlong_motion")

    val breathScale by infinite.animateFloat(
        initialValue = 0.988f,
        targetValue = 1.022f,
        animationSpec = infiniteRepeatable(
            animation = tween(AnimationDuration.breatheMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    val listenTilt by infinite.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listen_tilt"
    )

    val speakOffsetY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(AnimationDuration.speakPulseMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speak_y"
    )

    val haloPulse by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    val rawGifResId = context.resources.getIdentifier(
        "yunlong_${state.wireKey}",
        "raw",
        context.packageName
    )

    val drawableRes = when (state) {
        CompanionState.LISTENING -> R.drawable.img_yunlong_listening
        CompanionState.CELEBRATING, CompanionState.HAPPY -> R.drawable.img_yunlong_celebrating
        else -> R.drawable.img_yunlong_idle
    }

    val haloColor = when (state) {
        CompanionState.LISTENING -> ColorTokens.TechBluePrimary
        CompanionState.THINKING -> ColorTokens.SkyCyanGlow
        CompanionState.CELEBRATING, CompanionState.HAPPY -> ColorTokens.SuccessMint
        CompanionState.RESTING -> ColorTokens.InkMuted
        else -> ColorTokens.SkyCyanGlow
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size)
        ) {
            // Soft sky halo canvas behind Yunlong (no white box)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            haloColor.copy(alpha = haloPulse * 0.22f),
                            ColorTokens.SkyHaloLight.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    ),
                    radius = this.size.minDimension * 0.50f,
                    center = Offset(this.size.width / 2f, this.size.height / 2f)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size * 0.84f)
                    .then(
                        if (floating) Modifier.shadow(16.dp, CircleShape, ambientColor = haloColor, spotColor = haloColor)
                        else Modifier
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ColorTokens.CanvasPorcelain.copy(alpha = if (floating) 0.92f else 1f),
                                ColorTokens.SkyGradientTop.copy(alpha = if (floating) 0.72f else 0.8f)
                            )
                        )
                    )
                    .graphicsLayer {
                        when (state) {
                            CompanionState.IDLE -> {
                                scaleX = breathScale
                                scaleY = breathScale
                            }
                            CompanionState.LISTENING -> {
                                rotationZ = listenTilt
                                scaleX = 1.02f
                                scaleY = 1.02f
                            }
                            CompanionState.SPEAKING -> {
                                translationY = speakOffsetY
                                scaleX = breathScale
                                scaleY = breathScale
                            }
                            CompanionState.CELEBRATING -> {
                                translationY = speakOffsetY * 1.4f
                                scaleX = 1.04f
                                scaleY = 1.04f
                            }
                            CompanionState.THINKING -> {
                                rotationZ = listenTilt * 0.5f
                            }
                            CompanionState.RESTING -> {
                                alpha = 0.86f
                            }
                            else -> {
                                scaleX = breathScale
                                scaleY = breathScale
                            }
                        }
                    }
            ) {
                AnimatedContent(
                    targetState = state,
                    transitionSpec = {
                        fadeIn(tween(AnimationDuration.standardMs)) togetherWith
                            fadeOut(tween(AnimationDuration.fastMs))
                    },
                    label = "yunlong_state_transition"
                ) { targetState ->
                    if (rawGifResId != 0) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(rawGifResId)
                                .build(),
                            contentDescription = "$companionName - ${targetState.wireKey}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = drawableRes),
                            contentDescription = "$companionName - ${targetState.wireKey}",
                            contentScale = ContentScale.Crop,
                            colorFilter = if (targetState == CompanionState.RESTING) {
                                ColorFilter.tint(ColorTokens.SkyHaloLight.copy(alpha = 0.25f), BlendMode.SrcAtop)
                            } else null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xxs))
        if (floating) {
            Surface(
                color = ColorTokens.SurfaceCard.copy(alpha = 0.78f),
                shape = RoundedCornerShape(Radius.sm),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = if (isLowerGrade) state.labelLowerGrade else state.labelUpperGrade,
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorTokens.InkSecondary,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
                )
            }
        } else {
            Text(
                text = if (isLowerGrade) state.labelLowerGrade else state.labelUpperGrade,
                style = MaterialTheme.typography.labelMedium,
                color = ColorTokens.InkSecondary,
                modifier = Modifier.padding(horizontal = Spacing.sm)
            )
        }
    }
}
