package de.lenidh.concentricwf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.view.SurfaceHolder
import androidx.core.graphics.and
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import de.lenidh.concentricwf.data.watchface.ColorStyleIdAndResourceIds
import de.lenidh.concentricwf.data.watchface.WatchFaceColorPalette.Companion.convertToWatchFaceColorPalette
import de.lenidh.concentricwf.data.watchface.WatchFaceData
import de.lenidh.concentricwf.utils.COLOR_STYLE_SETTING
import de.lenidh.concentricwf.utils.DRAW_HOUR_PIPS_STYLE_SETTING
import de.lenidh.concentricwf.utils.WATCH_HAND_LENGTH_STYLE_SETTING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

private const val FRAME_PERIOD_MS_DEFAULT: Long = 16L

class CwfWatchCanvasRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    private val complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    canvasType: Int
) : Renderer.CanvasRenderer2<CwfWatchCanvasRenderer.SharedAssets>(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    canvasType,
    FRAME_PERIOD_MS_DEFAULT,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false
) {
    class SharedAssets : Renderer.SharedAssets {
        override fun onDestroy() {}
    }

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // Represents all data needed to render the watch face. All value defaults are constants. Only
    // three values are changeable by the user (color scheme, ticks being rendered, and length of
    // the minute arm). Those dynamic values are saved in the watch face APIs and we update those
    // here (in the renderer) through a Kotlin Flow.
    private var watchFaceData: WatchFaceData = WatchFaceData()

    // Converts resource ids into Colors and ComplicationDrawable.
    private var watchFaceColors = convertToWatchFaceColorPalette(
        context, watchFaceData.activeColorStyle, watchFaceData.ambientColorStyle
    )

    // Initializes paint object for painting the clock hands with default values.
    private val clockHandPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth =
            context.resources.getDimensionPixelSize(R.dimen.clock_hand_stroke_width).toFloat()
    }

    private val indexPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = context.resources.getColor(R.color.index_default, null)
    }

    private val hourTextPaint = Paint().apply {
        isAntiAlias = true
        textSize = 32F
        typeface = context.resources.getFont(R.font.rubik_regular)
        color = context.resources.getColor(R.color.hour_default, null)
    }

    private val minuteTextPaint = Paint().apply {
        isAntiAlias = true
        textSize = 18F
        typeface = context.resources.getFont(R.font.rubik_regular)
        color = context.resources.getColor(R.color.minute_default, null)
    }

    private val minuteBorderPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 2F
        color = context.resources.getColor(R.color.minute_default, null)
    }

    private val minutesTextPaint = Paint().apply {
        isAntiAlias = true
        textSize = 12F
        typeface = context.resources.getFont(R.font.rubik_regular)
        color = context.resources.getColor(R.color.minutes_default, null)
    }

    private val secondsTextPaint = Paint().apply {
        isAntiAlias = true
        textSize = 12F
        typeface = context.resources.getFont(R.font.rubik_regular)
        color = context.resources.getColor(R.color.seconds_default, null)
    }

    private lateinit var hourHandFill: Path
    private lateinit var hourHandBorder: Path
    private lateinit var minuteHandFill: Path
    private lateinit var minuteHandBorder: Path
    private lateinit var secondHand: Path

    private var largeIndexLength = 0
    private var largeIndexWidth = 0
    private var smallIndexLength = 0
    private var smallIndexWidth = 0
    private var minutesIndexPadding = 0
    private var minutesTextPadding = 0
    private var secondsIndexPadding = 0
    private var secondsTextPadding = 0

    // Changed when setting changes cause a change in the minute hand arm (triggered by user in
    // updateUserStyle() via userStyleRepository.addUserStyleListener()).
    private var armLengthChangedRecalculateClockHands: Boolean = false

    // Default size of watch face drawing area, that is, a no size rectangle. Will be replaced with
    // valid dimensions from the system.
    private var currentWatchFaceSize = Rect(0, 0, 0, 0)

    init {
        scope.launch {
            currentUserStyleRepository.userStyle.collect { userStyle ->
                updateWatchFaceData(userStyle)
            }
        }
    }

    override suspend fun createSharedAssets(): SharedAssets {
        return SharedAssets()
    }

    /*
     * Triggered when the user makes changes to the watch face through the settings activity. The
     * function is called by a flow.
     */
    private fun updateWatchFaceData(userStyle: UserStyle) {
        Log.d(TAG, "updateWatchFace(): $userStyle")

        var newWatchFaceData: WatchFaceData = watchFaceData

        // Loops through user style and applies new values to watchFaceData.
        for (options in userStyle) {
            when (options.key.id.toString()) {
                COLOR_STYLE_SETTING -> {
                    val listOption =
                        options.value as UserStyleSetting.ListUserStyleSetting.ListOption

                    newWatchFaceData = newWatchFaceData.copy(
                        activeColorStyle = ColorStyleIdAndResourceIds.getColorStyleConfig(
                            listOption.id.toString()
                        )
                    )
                }

                DRAW_HOUR_PIPS_STYLE_SETTING -> {
                    val booleanValue =
                        options.value as UserStyleSetting.BooleanUserStyleSetting.BooleanOption

                    newWatchFaceData = newWatchFaceData.copy(
                        drawHourPips = booleanValue.value
                    )
                }

                WATCH_HAND_LENGTH_STYLE_SETTING -> {
                    val doubleValue =
                        options.value as UserStyleSetting.DoubleRangeUserStyleSetting.DoubleRangeOption

                    // The arm lengths are usually only calculated the first time the watch face is
                    // loaded to reduce the ops in the onDraw(). Because we updated the minute hand
                    // watch length, we need to trigger a recalculation.
                    armLengthChangedRecalculateClockHands = true

                    // Updates length of minute hand based on edits from user.
                    val newMinuteHandDimensions = newWatchFaceData.minuteHandDimensions.copy(
                        lengthFraction = doubleValue.value.toFloat()
                    )

                    newWatchFaceData = newWatchFaceData.copy(
                        minuteHandDimensions = newMinuteHandDimensions
                    )
                }
            }
        }

        // Only updates if something changed.
        if (watchFaceData != newWatchFaceData) {
            watchFaceData = newWatchFaceData

            // Recreates Color and ComplicationDrawable from resource ids.
            watchFaceColors = convertToWatchFaceColorPalette(
                context, watchFaceData.activeColorStyle, watchFaceData.ambientColorStyle
            )

            // Applies the user chosen complication color scheme changes. ComplicationDrawables for
            // each of the styles are defined in XML so we need to replace the complication's
            // drawables.
            for ((_, complication) in complicationSlotsManager.complicationSlots) {
                if (complication.renderer is CanvasComplicationDrawable) {
                    ComplicationDrawable.getDrawable(
                        context, watchFaceColors.complicationStyleDrawableId
                    )?.let {
                        (complication.renderer as CanvasComplicationDrawable).drawable = it
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        scope.cancel("AnalogWatchCanvasRenderer scope clear() request")
        super.onDestroy()
    }

    override fun renderHighlightLayer(
        canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets
    ) {
        canvas.drawColor(renderParameters.highlightLayer!!.backgroundTint)

        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            if (complication.enabled) {
                complication.renderHighlightLayer(canvas, zonedDateTime, renderParameters)
            }
        }
    }

    private fun recalculateDimensions(bounds: Rect) {
        currentWatchFaceSize = Rect(bounds)

        largeIndexLength = (bounds.width() * LARGE_INDEX_LENGTH_FRACTION).toInt()
        largeIndexWidth = (bounds.width() * LARGE_INDEX_WIDTH_FRACTION).toInt()
        smallIndexLength = (bounds.width() * SMALL_INDEX_LENGTH_FRACTION).toInt()
        smallIndexWidth = (bounds.width() * SMALL_INDEX_WIDTH_FRACTION).toInt()
        minutesIndexPadding = (bounds.width() * MINUTES_INDEX_PADDING_FRACTION).toInt()
        minutesTextPadding = (bounds.width() * MINUTES_TEXT_PADDING_FRACTION).toInt()
        secondsIndexPadding = (bounds.width() * SECONDS_INDEX_PADDING_FRACTION).toInt()
        secondsTextPadding = (bounds.width() * SECONDS_TEXT_PADDING_FRACTION).toInt()

        hourTextPaint.textSize = bounds.width() * HOUR_TEXT_SIZE_FRACTION
        minuteTextPaint.textSize = bounds.width() * MINUTE_TEXT_SIZE_FRACTION
        minutesTextPaint.textSize = bounds.width() * MINUTES_TEXT_SIZE_FRACTION
        secondsTextPaint.textSize = bounds.width() * SECONDS_TEXT_SIZE_FRACTION
    }

    override fun render(
        canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets
    ) {
        if (currentWatchFaceSize != bounds) {
            recalculateDimensions(bounds)
        }
        val isLowBitMode = renderParameters.drawMode != DrawMode.INTERACTIVE;

        canvas.drawColor(Color.BLACK)

        if (renderParameters.watchFaceLayers.contains(WatchFaceLayer.BASE)) {
            if (!isLowBitMode) {
                drawRims(canvas, bounds, zonedDateTime)
            }
            drawCurrentTime(canvas, bounds, zonedDateTime)
        }

        // CanvasComplicationDrawable already obeys rendererParameters.
        drawComplications(canvas, zonedDateTime)

        if (renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS_OVERLAY)) {
            //drawClockHands(canvas, bounds, zonedDateTime)
        }
    }

    // ----- All drawing functions -----
    private fun drawComplications(canvas: Canvas, zonedDateTime: ZonedDateTime) {
        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            if (complication.enabled) {
                complication.render(canvas, zonedDateTime, renderParameters)
            }
        }
    }

    private fun drawCurrentTime(
        canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime
    ) {
        val hourOfDay = zonedDateTime.toLocalTime().get(ChronoField.HOUR_OF_DAY);
        val hourText = format2Digits(hourOfDay)
        val hourPath = Path()
        hourTextPaint.getTextPath(hourText, 0, hourText.length, 0F, 0F, hourPath)
        val hourTextBounds = RectF()
        hourPath.computeBounds(hourTextBounds, true)
        val hourX = bounds.exactCenterX() - hourTextBounds.width() / 2F
        val hourY = bounds.exactCenterY() + hourTextBounds.height() / 2F

        canvas.withTranslation(hourX, hourY) {
            canvas.drawPath(hourPath, hourTextPaint)
        }

        val minuteOfHour = zonedDateTime.toLocalTime().get(ChronoField.MINUTE_OF_HOUR);
        val minuteText = format2Digits(minuteOfHour)
        val minutePath = Path()
        minuteTextPaint.getTextPath(minuteText, 0, minuteText.length, 0F, 0F, minutePath)
        val minuteTextBounds = RectF()
        minutePath.computeBounds(minuteTextBounds, true)
        val minuteXOffset = -minuteTextBounds.width() / 2F
        val minuteYOffset = minuteTextBounds.height() / 2F

        val fontMetrics = Paint.FontMetrics()
        minuteTextPaint.getFontMetrics(fontMetrics)
        val borderRadius = -fontMetrics.ascent
        val borderPath = Path()
        borderPath.arcTo(
            bounds.exactCenterX() + hourTextBounds.width(),
            bounds.exactCenterY(),
            borderRadius,
            90F,
            180F
        )
        borderPath.arcTo(
            bounds.right.toFloat(), bounds.exactCenterY(), borderRadius, -90F, 180F
        )
        borderPath.close()

        val bgPadding = minutesIndexPadding + largeIndexLength
        var borderBg = Path()
        borderBg.addCircle(
            bounds.exactCenterX(),
            bounds.exactCenterY(),
            bounds.right - bgPadding - bounds.exactCenterY(),
            Path.Direction.CW
        )
        borderBg = borderBg.and(borderPath)

        val bgPaint = Paint()
        bgPaint.color = Color.BLACK
        bgPaint.style = Paint.Style.FILL
        canvas.drawPath(borderBg, bgPaint)
        canvas.drawPath(borderPath, minuteBorderPaint)

        val tx =
            bounds.right - (bounds.width() * MINUTES_TEXT_PADDING_FRACTION).toInt() - minuteTextBounds.width()
        canvas.withTranslation(
            tx, bounds.exactCenterY()
        ) {
            canvas.withTranslation(minuteXOffset, minuteYOffset) {
                canvas.drawPath(minutePath, minuteTextPaint)
            }
        }
    }

    private fun drawRims(
        canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime
    ) {
        // Retrieve current time to calculate location/rotation of watch arms.
        val secondOfDay = zonedDateTime.toLocalTime().toSecondOfDay()
        val millisOfDay = zonedDateTime.toLocalTime().get(ChronoField.MILLI_OF_DAY)

        // Determine the rotation of the hour and minute hand.

        // Determine how many milliseconds it takes to make a complete rotation
        // for each rim.
        val millisPerHour = Duration.ofHours(1).toMillis()
        val millisPerMinute = Duration.ofMinutes(1).toMillis()

        // Determine the angle to draw each hand expressed as an angle in degrees from 0 to 360
        // Since each hand does more than one cycle a day, we are only interested in the remainder
        // of the secondOfDay modulo the hand interval
        val minuteRotation = millisOfDay.rem(millisPerHour) * 360.0f / millisPerHour

        val minuteIndexRim =
            IndexRim(largeIndexWidth, largeIndexLength, smallIndexWidth, smallIndexLength);
        minuteIndexRim.draw(canvas, bounds, minutesIndexPadding, minuteRotation, indexPaint)

        val minuteNumberRim = NumberRim()
        minuteNumberRim.draw(
            canvas, bounds, minutesTextPadding.toFloat(), minuteRotation, minutesTextPaint
        )

        val drawAmbient = renderParameters.drawMode == DrawMode.AMBIENT
        // Draw second hand if not in ambient mode
        if (!drawAmbient) {
            clockHandPaint.color = watchFaceColors.activeSecondaryColor

            // Second hand has a different color style (secondary color) and is only drawn in
            // active mode, so we calculate it here (not above with others).
            val secondsRotation = (zonedDateTime.toLocalTime().get(ChronoField.MILLI_OF_DAY)).rem(
                millisPerMinute
            ) * 360.0f / millisPerMinute
            clockHandPaint.color = watchFaceColors.activeSecondaryColor

            val secondIndexRim =
                IndexRim(largeIndexWidth, largeIndexLength, smallIndexWidth, smallIndexLength);
            secondIndexRim.draw(canvas, bounds, secondsIndexPadding, secondsRotation, indexPaint)

            val secondNumberRim = NumberRim()
            secondNumberRim.draw(
                canvas, bounds, secondsTextPadding.toFloat(), secondsRotation, secondsTextPaint
            )
        }
    }

    companion object {
        private const val TAG = "AnalogWatchCanvasRenderer"

        // Painted between pips on watch face for hour marks.
        private val HOUR_MARKS = arrayOf("3", "6", "9", "12")

        // Used to canvas.scale() to scale watch hands in proper bounds. This will always be 1.0.
        private const val WATCH_HAND_SCALE = 1.0f

        private const val SMALL_INDEX_WIDTH_FRACTION = 0.005F
        private const val SMALL_INDEX_LENGTH_FRACTION = 0.022F
        private const val LARGE_INDEX_WIDTH_FRACTION = 0.005F
        private const val LARGE_INDEX_LENGTH_FRACTION = 0.035F
        private const val MINUTES_TEXT_PADDING_FRACTION = 0.155F
        private const val MINUTES_INDEX_PADDING_FRACTION = 0.140F
        private const val SECONDS_TEXT_PADDING_FRACTION = 0.020F
        private const val SECONDS_INDEX_PADDING_FRACTION = 0.005F
        private const val HOUR_TEXT_SIZE_FRACTION = 0.211F
        private const val MINUTE_TEXT_SIZE_FRACTION = 0.079F
        private const val MINUTES_TEXT_SIZE_FRACTION = 0.053F
        private const val SECONDS_TEXT_SIZE_FRACTION = 0.053F
    }
}

private class IndexRim(
    private val largeIndexWidth: Int,
    private val largeIndexLength: Int,
    private val smallIndexWidth: Int,
    private val smallIndexLength: Int
) {
    fun draw(canvas: Canvas, bounds: Rect, padding: Int, rotation: Float, paint: Paint) {
        val largeIndex = Path()
        val right = (bounds.right - padding).toFloat()
        largeIndex.addRoundRect(
            right - largeIndexLength,
            bounds.centerY() + largeIndexWidth / 2F,
            right,
            bounds.centerY() - largeIndexWidth / 2F,
            2F,
            2F,
            Path.Direction.CW
        )
        val smallIndex = Path()
        smallIndex.addRoundRect(
            right - smallIndexLength,
            bounds.centerY() + smallIndexWidth / 2F,
            right,
            bounds.centerY() - smallIndexWidth / 2F,
            2F,
            2F,
            Path.Direction.CW
        )

        val m = Matrix()

        val rim = Path();
        for (i in 0..59) {
            m.setRotate(360F / 60 * i, bounds.centerX().toFloat(), bounds.centerY().toFloat())
            if (i % 5 == 0) {
                rim.addPath(largeIndex, m)
            } else {
                rim.addPath(smallIndex, m)
            }
        }

        canvas.withRotation(rotation, bounds.exactCenterX(), bounds.exactCenterY()) {
            drawPath(rim, paint)
        }
    }
}

private class NumberRim {
    private var currentWatchBounds = Rect()
    private var currentPadding = 0F
    private var currentPaint = Paint()

    private var texts = emptyArray<String>()
    private var textPaths = emptyArray<Path>()
    private var x = 0F
    private var y = 0F

    private fun recalculate(bounds: Rect, padding: Float, paint: Paint) {
        currentWatchBounds = Rect(bounds)
        currentPadding = padding
        currentPaint = Paint(paint)

        texts = Array(RANGE.count()) { "" }
        textPaths = Array(RANGE.count()) { _ -> Path() }

        var maxBound = 0F
        val tmpPath = Path()
        for (i in RANGE) {
            tmpPath.reset()

            val text = format2Digits(i * STEP)
            texts[i] = text
            paint.getTextPath(text, 0, text.length, 0F, 0F, tmpPath)
            val textBounds = RectF()
            tmpPath.computeBounds(textBounds, true)
            val textX = -textBounds.width() / 2F
            val textY = textBounds.height() / 2F
            textPaths[i].addPath(tmpPath, textX, textY)
            maxBound = maxOf(maxBound, textBounds.width(), textBounds.height())
        }

        x = bounds.right - padding - maxBound
        y = bounds.exactCenterY()
    }

    fun draw(canvas: Canvas, bounds: Rect, padding: Float, rotation: Float, paint: Paint) {
        if (currentWatchBounds != bounds || currentPadding != padding || currentPaint != paint) {
            recalculate(bounds, padding, paint)
        }

        for (i in RANGE) {
            val numberRotation = rotation + 360F / LIMIT * i * STEP
            canvas.withRotation(numberRotation, bounds.exactCenterX(), bounds.exactCenterY()) {
                canvas.withTranslation(x, y) {
                    canvas.withRotation(-numberRotation) {
                        canvas.drawPath(textPaths[i], paint)
                    }
                }
            }
        }
    }

    companion object {
        private const val STEP = 5;
        private const val LIMIT = 60;
        private val RANGE = 0 until LIMIT / STEP;
    }
}

fun format2Digits(value: Int): String {
    return "%1$02d".format(value)
}

fun Path.arcTo(
    centerX: Float, centerY: Float, radius: Float, startAngle: Float, sweepAngle: Float
) {
    this.arcTo(
        centerX - radius,
        centerY - radius,
        centerX + radius,
        centerY + radius,
        startAngle,
        sweepAngle,
        false
    )
}
