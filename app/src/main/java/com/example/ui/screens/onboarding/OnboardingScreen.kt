package com.example.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class OnboardingSlideData(
    val stepNumber: String,
    val headline: String,
    val tagalogHeadline: String,
    val description: String,
    val bgColor: Color,
    val isLast: Boolean = false
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = listOf(
        OnboardingSlideData(
            stepNumber = "1/3",
            headline = "Track every sari-sari item with ease.",
            tagalogHeadline = "Subaybayan ang bawat paninda sa tindahan.",
            description = "Manage stock quantities, unit costs, and retail prices in Philippine Pesos (₱). Get automated low-stock and expiry warnings before items run out.",
            bgColor = PastelPeach
        ),
        OnboardingSlideData(
            stepNumber = "2/3",
            headline = "Instant QR labels & camera scanning.",
            tagalogHeadline = "Mabilisang scanning sa bawat benta.",
            description = "Auto-generate sequential QR codes (TT-000001...) for every product. Print price labels and scan with your camera or manual lookup for 1-tap checkout.",
            bgColor = PastelBlue
        ),
        OnboardingSlideData(
            stepNumber = "3/3",
            headline = "Record cash sales & utang without internet.",
            tagalogHeadline = "Bantayan ang kita at pautang nang offline.",
            description = "Track daily cash sales with exact profit margins. Record customer utang with partial payment history, overpayment protection, and zero cloud dependency.",
            bgColor = PastelMint,
            isLast = true
        )
    )

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentSlide = slides[currentStepIndex]

    // Screen background animates smoothly to match active slide pastel color
    val animatedBgColor by animateColorAsState(
        targetValue = currentSlide.bgColor,
        label = "onboarding_bg_anim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animatedBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Logo Icon + Brand Title (left) & Skip Button (right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Forest),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₱",
                            color = Color.White,
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Tinda Track",
                            style = TindaTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = "Sari-Sari Store OS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Muted
                        )
                    }
                }

                if (!currentSlide.isLast) {
                    Surface(
                        onClick = onFinish,
                        shape = RoundedCornerShape(100.dp),
                        color = Color.Black.copy(alpha = 0.06f),
                        modifier = Modifier.testTag("skip_onboarding_button")
                    ) {
                        Text(
                            text = "Skip",
                            color = Ink,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Middle Content: Headline + Line Art Illustration
            AnimatedContent(
                targetState = currentStepIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width / 3 } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width / 3 } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width / 3 } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width / 3 } + fadeOut()
                    }
                },
                label = "onboarding_slide_transition",
                modifier = Modifier.weight(1f)
            ) { stepIdx ->
                val slide = slides[stepIdx]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Headline Block (Left aligned, modern bold typography)
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = slide.headline,
                            style = TindaTypography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            lineHeight = 36.sp,
                            fontSize = 28.sp
                        )

                        Text(
                            text = slide.tagalogHeadline,
                            style = TindaTypography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestSoft
                        )

                        Text(
                            text = slide.description,
                            style = TindaTypography.bodyMedium,
                            color = Muted,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Line Illustration in center (Inspired by reference)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (stepIdx) {
                            0 -> StepOneInventoryIllustration()
                            1 -> StepTwoScanningIllustration()
                            else -> StepThreeSalesUtangIllustration()
                        }
                    }
                }
            }

            // Bottom Bar: Step Indicator (1/3 ____) on left, Action Button on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Indicator: "1/3 ______"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = currentSlide.stepNumber,
                        fontFamily = DmSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Ink
                    )

                    // Line indicator with active fill
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Ink.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(
                                    when (currentStepIndex) {
                                        0 -> 0.33f
                                        1 -> 0.66f
                                        else -> 1.0f
                                    }
                                )
                                .clip(RoundedCornerShape(100.dp))
                                .background(Ink)
                        )
                    }
                }

                // Action Button: Circular arrow for 1/3 and 2/3, pill "enter" for 3/3
                if (currentSlide.isLast) {
                    // Pill button with "enter"
                    Button(
                        onClick = onFinish,
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Forest,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("onboarding_enter_button")
                    ) {
                        Text(
                            text = "enter",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = DmSansFontFamily,
                            color = Color.White
                        )
                    }
                } else {
                    // Circular button with Arrow
                    IconButton(
                        onClick = { currentStepIndex++ },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Forest)
                            .testTag("onboarding_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next step",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Step 1 Illustration: Sari-sari store shelf, basket, and 10000 ₱ badge with pin
 */
@Composable
private fun StepOneInventoryIllustration() {
    Canvas(modifier = Modifier.size(240.dp, 190.dp)) {
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val lineColor = Ink

        // Decorative dashed path
        val dashPath = Path().apply {
            moveTo(10f, 130f)
            quadraticTo(60f, 135f, 90f, 170f)
        }
        drawPath(
            path = dashPath,
            color = lineColor.copy(alpha = 0.5f),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        )

        // Store Shelving Box / Paninda container
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(105f, 90f),
            size = Size(110f, 80f),
            cornerRadius = CornerRadius(16f, 16f),
            style = stroke
        )
        // Shelf handles
        drawArc(
            color = lineColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(135f, 115f),
            size = Size(50f, 25f),
            style = stroke
        )

        // Pin Location / Map Badge on the left
        drawCircle(
            color = lineColor,
            radius = 18f,
            center = Offset(45f, 85f),
            style = stroke
        )
        drawCircle(
            color = lineColor,
            radius = 6f,
            center = Offset(45f, 85f),
            style = stroke
        )
        val pinTail = Path().apply {
            moveTo(33f, 94f)
            lineTo(45f, 118f)
            lineTo(57f, 94f)
        }
        drawPath(path = pinTail, color = lineColor, style = stroke)

        // "10000 ₱" stock count watermark text
        drawRoundRect(
            color = lineColor.copy(alpha = 0.15f),
            topLeft = Offset(130f, 65f),
            size = Size(80f, 22f),
            cornerRadius = CornerRadius(10f, 10f)
        )

        // Sparkle stars
        drawStarSparkle(Offset(225f, 75f), 10f, lineColor)
        drawStarSparkle(Offset(20f, 40f), 8f, lineColor)
    }
}

/**
 * Step 2 Illustration: QR Code matrix + Camera target frame + Moon & Sparkles
 */
@Composable
private fun StepTwoScanningIllustration() {
    Canvas(modifier = Modifier.size(240.dp, 190.dp)) {
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val lineColor = Ink

        // Crescent moon on top left
        val moonPath = Path().apply {
            moveTo(40f, 30f)
            cubicTo(60f, 40f, 60f, 80f, 35f, 90f)
            cubicTo(70f, 85f, 70f, 35f, 40f, 30f)
        }
        drawPath(path = moonPath, color = lineColor, style = stroke)

        // Camera Viewfinder corners
        val fSize = 130f
        val fLeft = 75f
        val fTop = 45f
        val cornerLen = 25f

        // Top-Left Corner
        drawLine(lineColor, Offset(fLeft, fTop), Offset(fLeft + cornerLen, fTop), 3.dp.toPx(), StrokeCap.Round)
        drawLine(lineColor, Offset(fLeft, fTop), Offset(fLeft, fTop + cornerLen), 3.dp.toPx(), StrokeCap.Round)

        // Top-Right Corner
        drawLine(lineColor, Offset(fLeft + fSize, fTop), Offset(fLeft + fSize - cornerLen, fTop), 3.dp.toPx(), StrokeCap.Round)
        drawLine(lineColor, Offset(fLeft + fSize, fTop), Offset(fLeft + fSize, fTop + cornerLen), 3.dp.toPx(), StrokeCap.Round)

        // Bottom-Left Corner
        drawLine(lineColor, Offset(fLeft, fTop + fSize), Offset(fLeft + cornerLen, fTop + fSize), 3.dp.toPx(), StrokeCap.Round)
        drawLine(lineColor, Offset(fLeft, fTop + fSize), Offset(fLeft, fTop + fSize - cornerLen), 3.dp.toPx(), StrokeCap.Round)

        // Bottom-Right Corner
        drawLine(lineColor, Offset(fLeft + fSize, fTop + fSize), Offset(fLeft + fSize - cornerLen, fTop + fSize), 3.dp.toPx(), StrokeCap.Round)
        drawLine(lineColor, Offset(fLeft + fSize, fTop + fSize), Offset(fLeft + fSize, fTop + fSize - cornerLen), 3.dp.toPx(), StrokeCap.Round)

        // QR Pattern inside the frame
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(fLeft + 20f, fTop + 20f),
            size = Size(30f, 30f),
            cornerRadius = CornerRadius(6f, 6f),
            style = stroke
        )
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(fLeft + fSize - 50f, fTop + 20f),
            size = Size(30f, 30f),
            cornerRadius = CornerRadius(6f, 6f),
            style = stroke
        )
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(fLeft + 20f, fTop + fSize - 50f),
            size = Size(30f, 30f),
            cornerRadius = CornerRadius(6f, 6f),
            style = stroke
        )

        // Red scanning laser line in the middle
        drawLine(
            color = AlertRed,
            start = Offset(fLeft + 15f, fTop + (fSize / 2f)),
            end = Offset(fLeft + fSize - 15f, fTop + (fSize / 2f)),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Shooting stars / sparkles
        drawStarSparkle(Offset(220f, 35f), 12f, lineColor)
        drawStarSparkle(Offset(235f, 65f), 7f, lineColor)
        drawStarSparkle(Offset(35f, 150f), 8f, lineColor)
    }
}

/**
 * Step 3 Illustration: Sari-sari owner / customer badge, ribbon, and verified profit mark
 */
@Composable
private fun StepThreeSalesUtangIllustration() {
    Canvas(modifier = Modifier.size(240.dp, 190.dp)) {
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val lineColor = Ink

        // Rosette / Award Badge on top right
        val badgeCenter = Offset(175f, 70f)
        drawCircle(
            color = lineColor,
            radius = 32f,
            center = badgeCenter,
            style = stroke
        )
        // Heart / checkmark inside the badge
        val heartPath = Path().apply {
            moveTo(badgeCenter.x, badgeCenter.y - 6f)
            cubicTo(badgeCenter.x - 10f, badgeCenter.y - 18f, badgeCenter.x - 22f, badgeCenter.y - 2f, badgeCenter.x, badgeCenter.y + 14f)
            cubicTo(badgeCenter.x + 22f, badgeCenter.y - 2f, badgeCenter.x + 10f, badgeCenter.y - 18f, badgeCenter.x, badgeCenter.y - 6f)
        }
        drawPath(path = heartPath, color = lineColor, style = stroke)

        // Ribbon tails below the badge
        val ribbonPath = Path().apply {
            moveTo(160f, 98f)
            lineTo(155f, 135f)
            lineTo(168f, 125f)
            lineTo(180f, 135f)
            lineTo(178f, 100f)
        }
        drawPath(path = ribbonPath, color = lineColor, style = stroke)

        // Tindera / Customer character head and shoulders
        drawCircle(
            color = lineColor,
            radius = 24f,
            center = Offset(60f, 75f),
            style = stroke
        )
        // Eye and smile
        drawCircle(color = lineColor, radius = 2.5f, center = Offset(66f, 72f))
        drawArc(
            color = lineColor,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(58f, 76f),
            size = Size(14f, 10f),
            style = stroke
        )

        // Arms & Ledger book
        val bodyPath = Path().apply {
            moveTo(25f, 160f)
            cubicTo(35f, 115f, 85f, 115f, 95f, 160f)
        }
        drawPath(path = bodyPath, color = lineColor, style = stroke)

        // Utang & Sales Ledger book held in hands
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(40f, 130f),
            size = Size(75f, 45f),
            cornerRadius = CornerRadius(8f, 8f),
            style = stroke
        )
        drawLine(lineColor, Offset(77f, 130f), Offset(77f, 175f), 2.dp.toPx())

        // Sparkle stars
        drawStarSparkle(Offset(120f, 40f), 10f, lineColor)
        drawStarSparkle(Offset(220f, 140f), 8f, lineColor)
    }
}

/**
 * Draws a clean minimalist 4-point sparkle star matching the reference graphic
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStarSparkle(
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
    }
    drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx()))
}
