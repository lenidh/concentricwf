/*
 * Copyright (c) 2024 Moritz Heindl
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.lenidh.concentricwf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.format.DateFormat
import android.util.Log
import android.view.SurfaceHolder
import androidx.core.graphics.and
import androidx.core.graphics.or
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.data.EmptyComplicationData
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import de.lenidh.concentricwf.data.watchface.WatchFaceColorPalette.Companion.convertToWatchFaceColorPalette
import de.lenidh.concentricwf.data.watchface.WatchFaceData
import de.lenidh.concentricwf.data.watchface.WatchFaceUserStyle
import de.lenidh.concentricwf.utils.COLOR_STYLE_SETTING
import de.lenidh.concentricwf.utils.COMPLICATION_OFFSET
import de.lenidh.concentricwf.utils.COMPLICATION_RADIUS
import de.lenidh.concentricwf.utils.FONT_STYLE_SETTING
import de.lenidh.concentricwf.utils.computeComplicationAngle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import kotlin.math.floor

private const val FRAME_PERIOD_MS_DEFAULT: Long = 32L

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
        context, watchFaceData.userStyle
    )

    private var watchTypeface = convertToWatchTypeface(watchFaceData.userStyle)

    private val bgPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = context.resources.getColor(R.color.bg_default, null)
    }

    private val indexPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = context.resources.getColor(R.color.index_default, null)
    }

    private val hourTextPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = context.resources.getColor(R.color.hour_default, null)
    }

    private val minuteTextPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = context.resources.getColor(R.color.minute_default, null)
    }

    private val minutesTextPaint = Paint().apply {
        isAntiAlias = true
        color = context.resources.getColor(R.color.minutes_default, null)
    }

    private val secondsTextPaint = Paint().apply {
        isAntiAlias = true
        color = context.resources.getColor(R.color.seconds_default, null)
    }

    private val borderPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 2F
        color = context.resources.getColor(R.color.border_default, null)
    }

    private var largeIndexLength = 0
    private var largeIndexWidth = 0
    private var smallIndexLength = 0
    private var smallIndexWidth = 0
    private var minutesIndexPadding = 0
    private var minutesTextPadding = 0
    private var secondsIndexPadding = 0
    private var secondsTextPadding = 0

    private var minuteCenterX = 0F
    private var minuteCenterY = 0F

    // Default size of watch face drawing area, that is, a no size rectangle. Will be replaced with
    // valid dimensions from the system.
    private var currentWatchFaceSize = Rect(0, 0, 0, 0)

    private var minuteIndexRim = IndexRim(
        largeIndexWidth, largeIndexLength, smallIndexWidth, smallIndexLength
    )
    private var minuteNumberRim = NumberRim()
    private var secondIndexRim = IndexRim(
        largeIndexWidth, largeIndexLength, smallIndexWidth, smallIndexLength
    )
    private var secondNumberRim = NumberRim()
    private var currentTime = CurrentTime(minuteCenterX, minuteCenterY)
    private var minuteFrame = MinuteFrame(
        largeIndexLength, smallIndexLength, minutesIndexPadding, minuteCenterX, minuteCenterY
    )
    private var complicationFrame = ComplicationFrame()

    init {
        scope.launch {
            currentUserStyleRepository.userStyle.collect { userStyle ->
                updateWatchFaceData(userStyle)
                // WORKAROUND for https://github.com/lenidh/concentricwf/issues/3#issue-2117452899
                // Forcing the complication to reload fixes them sometimes not being rendered at
                // startup. This is caused by ComplicationRenderer.mBounds being zero ant therefore
                // ignoring requests to draw itself. By reloading the complication drawables with
                // loadDrawablesAsynchronous = true we force ComplicationRenderer to update its
                // mBounds property
                reloadComplicationsDrawableAsync()
            }
        }
    }

    private fun reloadComplicationsDrawableAsync() {
        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            if (complication.renderer is CanvasComplicationDrawable) {
                val renderer = (complication.renderer as CanvasComplicationDrawable)
                renderer.loadData(complication.complicationData.value, true)
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
        Log.d(TAG, "updateWatchFace(userStyle=$userStyle)")

        var newWatchFaceData: WatchFaceData = watchFaceData

        var colorOptionId: String? = null
        var fontOptionId: String? = null

        // Loops through user style and applies new values to watchFaceData.
        for (options in userStyle) {
            when (options.key.id.toString()) {
                COLOR_STYLE_SETTING -> {
                    val listOption =
                        options.value as UserStyleSetting.ListUserStyleSetting.ListOption

                    colorOptionId = listOption.id.toString()
                }
                FONT_STYLE_SETTING -> {
                    val listOption =
                        options.value as UserStyleSetting.ListUserStyleSetting.ListOption

                    fontOptionId = listOption.id.toString()
                }
            }
        }

        newWatchFaceData = newWatchFaceData.copy(userStyle = WatchFaceUserStyle.getStyleConfig(colorOptionId, fontOptionId))

        // Only updates if something changed.
        if (watchFaceData != newWatchFaceData) {
            watchFaceData = newWatchFaceData

            // Recreates Color scheme.
            watchFaceColors = convertToWatchFaceColorPalette(
                context, watchFaceData.userStyle
            )
            watchTypeface = convertToWatchTypeface(watchFaceData.userStyle)

            // Apply the color scheme to the complication slots.
            for ((_, complication) in complicationSlotsManager.complicationSlots) {
                if (complication.renderer is CanvasComplicationDrawable) {
                    val drawable = (complication.renderer as CanvasComplicationDrawable).drawable
                    drawable.activeStyle.titleColor = watchFaceColors.activeComplicationTextColor
                    drawable.activeStyle.textColor = watchFaceColors.activeComplicationTextColor
                    drawable.activeStyle.iconColor = watchFaceColors.activeComplicationIconColor
                    drawable.activeStyle.setTitleTypeface(watchTypeface)
                    drawable.activeStyle.setTextTypeface(watchTypeface)
                    drawable.ambientStyle.titleColor = watchFaceColors.ambientComplicationTextColor
                    drawable.ambientStyle.textColor = watchFaceColors.ambientComplicationTextColor
                    drawable.ambientStyle.iconColor = watchFaceColors.ambientComplicationIconColor
                    drawable.ambientStyle.setTitleTypeface(watchTypeface)
                    drawable.ambientStyle.setTextTypeface(watchTypeface)
                }
            }
        }
    }

    private fun convertToWatchTypeface(userStyle: WatchFaceUserStyle): Typeface {
        return context.resources.getFont(userStyle.fontId)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        scope.cancel("CwfWatchCanvasRenderer scope clear() request")
        super.onDestroy()
    }

    override fun renderHighlightLayer(
        canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets
    ) {
        Log.v(TAG, """renderHighlightLayer()
                     |    canvas=$canvas
                     |    bounds=$bounds
                     |    zonedDateTime=$zonedDateTime
                     |    sharedAssets=$sharedAssets
                     |)""".trimMargin())

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

        val minuteRefPath = Path()
        minuteTextPaint.getTextPath("00", 0, 2, 0F, 0F, minuteRefPath)
        val minuteRefTextBounds = RectF()
        minuteRefPath.computeBounds(minuteRefTextBounds, true)
        minuteCenterX = bounds.right - minutesTextPadding - minuteRefTextBounds.width() / 2F
        minuteCenterY = bounds.exactCenterY()

        minuteIndexRim =
            IndexRim(largeIndexWidth, largeIndexLength, smallIndexWidth, smallIndexLength)
        secondIndexRim =
            IndexRim(largeIndexWidth, largeIndexLength, smallIndexWidth, smallIndexLength)
        currentTime = CurrentTime(minuteCenterX, minuteCenterY)
        minuteFrame = MinuteFrame(
            largeIndexLength, smallIndexLength, minutesIndexPadding, minuteCenterX, minuteCenterY
        )
    }

    override fun render(
        canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets
    ) {
        Log.v(TAG, """render(
                     |    canvas=$canvas
                     |    bounds=$bounds
                     |    zonedDateTime=$zonedDateTime
                     |    sharedAssets=$sharedAssets
                     |)""".trimMargin())

        if (currentWatchFaceSize != bounds) {
            recalculateDimensions(bounds)
        }
        val isLowBitMode = renderParameters.drawMode != DrawMode.INTERACTIVE

        canvas.drawColor(bgPaint.color)

        hourTextPaint.typeface = watchTypeface
        minuteTextPaint.typeface = watchTypeface
        minutesTextPaint.typeface = watchTypeface
        secondsTextPaint.typeface = watchTypeface

        if (!isLowBitMode) {
            hourTextPaint.color = watchFaceColors.activeCurrentHourColor
            minuteTextPaint.color = watchFaceColors.activeCurrentMinuteColor
            minutesTextPaint.color = watchFaceColors.activeMinutesColor
            secondsTextPaint.color = watchFaceColors.activeSecondsColor
            borderPaint.color = watchFaceColors.activeBordersColor
        } else {
            hourTextPaint.color = watchFaceColors.ambientCurrentHourColor
            minuteTextPaint.color = watchFaceColors.ambientCurrentMinuteColor
            minutesTextPaint.color = watchFaceColors.ambientMinutesColor
            secondsTextPaint.color = watchFaceColors.ambientSecondsColor
            borderPaint.color = watchFaceColors.ambientBordersColor
        }

        if (renderParameters.watchFaceLayers.contains(WatchFaceLayer.BASE)) {
            if (!isLowBitMode) {
                drawRims(canvas, bounds, zonedDateTime)
            }
            minuteFrame.draw(canvas, bounds, minuteTextPaint, bgPaint, borderPaint, isLowBitMode)
            drawCurrentTime(canvas, bounds, zonedDateTime)
        }

        if (renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS)) {
            if (!isLowBitMode) {
                complicationFrame.draw(
                    canvas, bounds, complicationSlotsManager, bgPaint, borderPaint
                )
            }
        }
        // CanvasComplicationDrawable already obeys rendererParameters.
        drawComplications(canvas, zonedDateTime)
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
        val is24HourFormat = DateFormat.is24HourFormat(context)
        val time = zonedDateTime.toLocalTime()
        val hourOfDay =
            if (is24HourFormat) time.get(ChronoField.HOUR_OF_DAY) else time.get(ChronoField.CLOCK_HOUR_OF_AMPM)
        val minuteOfHour = time.get(ChronoField.MINUTE_OF_HOUR)
        currentTime.draw(canvas, bounds, hourOfDay, minuteOfHour, hourTextPaint, minuteTextPaint)
    }

    private fun drawRims(
        canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime
    ) {
        // Retrieve current time to calculate location/rotation of watch arms.
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

        minuteIndexRim.draw(canvas, bounds, minutesIndexPadding, minuteRotation, indexPaint)

        minuteNumberRim.draw(
            canvas, bounds, minutesTextPadding, minuteRotation, minutesTextPaint
        )

        val drawAmbient = renderParameters.drawMode == DrawMode.AMBIENT
        // Draw second hand if not in ambient mode
        if (!drawAmbient) {
            // Second hand has a different color style (secondary color) and is only drawn in
            // active mode, so we calculate it here (not above with others).
            val secondsRotation = (zonedDateTime.toLocalTime().get(ChronoField.MILLI_OF_DAY)).rem(
                millisPerMinute
            ) * 360.0f / millisPerMinute

            secondIndexRim.draw(canvas, bounds, secondsIndexPadding, secondsRotation, indexPaint)

            secondNumberRim.draw(
                canvas, bounds, secondsTextPadding, secondsRotation, secondsTextPaint
            )
        }
    }

    companion object {
        private const val TAG = "CwfWatchCanvasRenderer"

        private const val SMALL_INDEX_WIDTH_FRACTION = 0.005F
        private const val SMALL_INDEX_LENGTH_FRACTION = 0.022F
        private const val LARGE_INDEX_WIDTH_FRACTION = 0.005F
        private const val LARGE_INDEX_LENGTH_FRACTION = 0.035F
        private const val MINUTES_TEXT_PADDING_FRACTION = 0.185F
        private const val MINUTES_INDEX_PADDING_FRACTION = 0.140F
        private const val SECONDS_TEXT_PADDING_FRACTION = 0.050F
        private const val SECONDS_INDEX_PADDING_FRACTION = 0.005F
        private const val HOUR_TEXT_SIZE_FRACTION = 0.235F
        private const val MINUTE_TEXT_SIZE_FRACTION = 0.1F
        private const val MINUTES_TEXT_SIZE_FRACTION = 0.063F
        private const val SECONDS_TEXT_SIZE_FRACTION = 0.063F
    }
}

private class CurrentTime(
    private val minuteCenterX: Float,
    private val minuteCenterY: Float,
) {

    private var currentWatchBounds = Rect()
    private var currentHour = -1
    private var currentMinute = -1
    private var currentHourTextPaint = Paint()
    private var currentMinuteTextPaint = Paint()

    private var hourX = -1F
    private var hourY = -1F
    private var minuteX = -1F
    private var minuteY = -1F
    private var hourPath = Path()
    private var minutePath = Path()

    private fun recalculate(
        bounds: Rect,
        hour: Int,
        minute: Int,
        hourTextPaint: Paint,
        minuteTextPaint: Paint
    ) {
        Log.d(TAG, """recalculate
            |    bounds: $currentWatchBounds -> $bounds
            |    hour: $currentHour -> $hour
            |    minute: $currentMinute -> $minute
            |    hourTextPaint: ${currentHourTextPaint.fontMetricsInt} -> ${hourTextPaint.fontMetricsInt}
            |    minuteTextPaint: ${currentMinuteTextPaint.fontMetricsInt} -> ${minuteTextPaint.fontMetricsInt}
        """.trimMargin())

        currentWatchBounds = Rect(bounds)
        currentHour = hour
        currentMinute = minute
        currentHourTextPaint = Paint(hourTextPaint)
        currentMinuteTextPaint = Paint(minuteTextPaint)

        // HOUR
        val hourText = format2Digits(hour)
        hourPath.reset()
        hourTextPaint.getTextPath(hourText, 0, hourText.length, 0F, 0F, hourPath)
        val hourTextBounds = RectF()
        hourPath.computeBounds(hourTextBounds, true)
        hourX = bounds.exactCenterX() // textAlign == CENTER => no horizontal offset required
        hourY = bounds.exactCenterY() + hourTextBounds.height() / 2F

        // MINUTE
        val minuteText = format2Digits(minute)
        minutePath.reset()
        minuteTextPaint.getTextPath(minuteText, 0, minuteText.length, 0F, 0F, minutePath)
        val minuteTextBounds = RectF()
        minutePath.computeBounds(minuteTextBounds, true)
        minuteX = minuteCenterX // textAlign == CENTER => no horizontal offset required
        minuteY = minuteCenterY + minuteTextBounds.height() / 2F
    }

    fun draw(
        canvas: Canvas,
        bounds: Rect,
        hour: Int,
        minute: Int,
        hourTextPaint: Paint,
        minuteTextPaint: Paint
    ) {
        if (currentWatchBounds != bounds
            || currentHour != hour
            || currentMinute != minute
            || !currentHourTextPaint.equalsForTextMeasurement(hourTextPaint)
            || !currentMinuteTextPaint.equalsForTextMeasurement(minuteTextPaint)) {
            recalculate(bounds, hour, minute, hourTextPaint, minuteTextPaint)
        }

        canvas.withTranslation(hourX, hourY) {
            canvas.drawPath(hourPath, hourTextPaint)
        }
        canvas.withTranslation(minuteX, minuteY) {
            canvas.drawPath(minutePath, minuteTextPaint)
        }
    }

    companion object {
        private const val TAG = "CurrentTime"
    }
}

private class MinuteFrame(
    private val largeIndexLength: Int,
    private val smallIndexLength: Int,
    private val minutesIndexPadding: Int,
    private val minuteCenterX: Float,
    private val minuteCenterY: Float,
) {

    private var currentWatchBounds = Rect()
    private var currentMinuteTextPaint = Paint()
    private var currentIsLowBitMode = false

    private var borderPath = Path()
    private var bgPath = Path()

    private fun recalculate(bounds: Rect, minuteTextPaint: Paint, isLowBitMode: Boolean) {
        Log.d(TAG, """recalculate
            |    bounds: $currentWatchBounds -> $bounds
            |    minuteTextPaint: ${currentMinuteTextPaint.fontMetricsInt} -> ${minuteTextPaint.fontMetricsInt}
            |    isLowBitMode: $currentIsLowBitMode -> $isLowBitMode
        """.trimMargin())

        currentWatchBounds = Rect(bounds)
        currentMinuteTextPaint = Paint(minuteTextPaint)
        currentIsLowBitMode = isLowBitMode

        val fontMetrics = Paint.FontMetrics()
        minuteTextPaint.getFontMetrics(fontMetrics)
        val borderRadius = -fontMetrics.ascent
        borderPath = Path()
        borderPath.arcTo(
            minuteCenterX - smallIndexLength / 2F, minuteCenterY, borderRadius, 90F, 180F
        )
        borderPath.arcTo(
            if (isLowBitMode) minuteCenterX + smallIndexLength / 2F else bounds.right.toFloat(),
            minuteCenterY,
            borderRadius,
            -90F,
            180F
        )
        borderPath.close()

        val bgPadding = minutesIndexPadding + largeIndexLength
        val borderBg = Path()
        borderBg.addCircle(
            bounds.exactCenterX(),
            bounds.exactCenterY(),
            bounds.right - bgPadding - bounds.exactCenterY(),
            Path.Direction.CW
        )
        bgPath = borderBg.and(borderPath)
    }

    fun draw(
        canvas: Canvas,
        bounds: Rect,
        minuteTextPaint: Paint,
        bgPaint: Paint,
        borderPaint: Paint,
        isLowBitMode: Boolean
    ) {
        if (currentWatchBounds != bounds
            || !currentMinuteTextPaint.equalsForTextMeasurement(minuteTextPaint)
            || currentIsLowBitMode != isLowBitMode) {
            recalculate(bounds, minuteTextPaint, isLowBitMode)
        }

        canvas.drawPath(bgPath, bgPaint)
        canvas.drawPath(borderPath, borderPaint)
    }

    companion object {
        private const val TAG = "MinuteFrame"
    }
}

private class ComplicationFrame {

    private var currentWatchBounds = Rect()
    private var currentMin = -1
    private var currentMax = -1
    private var currentStrokeWidth = 0F
    private var path = Path()

    private fun recalculate(bounds: Rect, min: Int, max: Int, strokeWidth: Float) {
        Log.d(TAG, """recalculate
            |    bounds: $currentWatchBounds -> $bounds
            |    min: $currentMin -> $min
            |    max: $currentMax -> $max
            |    strokeWidth: $currentStrokeWidth -> $strokeWidth
        """.trimMargin())

        currentWatchBounds = Rect(bounds)
        currentMin = min
        currentMax = max
        currentStrokeWidth = strokeWidth

        val edgeRadius = bounds.width() * COMPLICATION_RADIUS
        val padding = bounds.width() * COMPLICATION_OFFSET
        val width = bounds.width() * 2 * (COMPLICATION_RADIUS + COMPLICATION_OFFSET) - edgeRadius
        val startAngle = floor(toDegrees(computeComplicationAngle(min)))
        val endAngle = floor(toDegrees(computeComplicationAngle(max)))

        val m = Matrix()

        val startPath = Path()
        startPath.moveTo(bounds.right.toFloat() + strokeWidth, bounds.exactCenterY())
        startPath.rLineTo(0F, edgeRadius + padding)
        startPath.rLineTo(-width - strokeWidth, 0F)
        startPath.arcTo(bounds.right - width, bounds.exactCenterY() + padding, edgeRadius, 90F, 90F)
        startPath.rLineTo(0F, -padding)
        startPath.close()
        m.setRotate(startAngle, bounds.exactCenterX(), bounds.exactCenterY())
        startPath.transform(m)

        val middlePath = Path()
        middlePath.moveTo(bounds.right.toFloat() + strokeWidth, bounds.exactCenterY())
        middlePath.arcTo(
            bounds.exactCenterX(),
            bounds.exactCenterY(),
            bounds.right - bounds.exactCenterX() + strokeWidth,
            0F,
            -startAngle + endAngle
        )
        middlePath.arcTo(
            bounds.exactCenterX(),
            bounds.exactCenterY(),
            bounds.right - bounds.exactCenterX() - width - edgeRadius,
            -startAngle + endAngle,
            startAngle - endAngle
        )
        middlePath.close()
        m.setRotate(startAngle, bounds.exactCenterX(), bounds.exactCenterY())
        middlePath.transform(m)

        val endPath = Path()
        endPath.moveTo(bounds.right.toFloat() + strokeWidth, bounds.exactCenterY())
        endPath.rLineTo(0F, -edgeRadius - padding)
        endPath.rLineTo(-width - strokeWidth, 0F)
        endPath.arcTo(bounds.right - width, bounds.exactCenterY() - padding, edgeRadius, -90F, -90F)
        endPath.rLineTo(0F, padding)
        endPath.close()
        m.setRotate(endAngle, bounds.exactCenterX(), bounds.exactCenterY())
        endPath.transform(m)

        path = startPath.or(middlePath).or(endPath)
    }

    fun draw(
        canvas: Canvas,
        bounds: Rect,
        complicationSlotsManager: ComplicationSlotsManager,
        bgPaint: Paint,
        borderPaint: Paint
    ) {
        val activeIndices = complicationSlotsManager.complicationSlots
            .toSortedMap()
            .entries.withIndex()
            .filter { (_, entry) -> entry.value.enabled && entry.value.complicationData.value !is EmptyComplicationData }
            .map { (i, _) -> i }

        if (activeIndices.isEmpty()) return
        val min = activeIndices.first()
        val max = activeIndices.last()

        if (currentWatchBounds != bounds
            || currentMin != min
            || currentMax != max
            || currentStrokeWidth != borderPaint.strokeWidth) {
            recalculate(bounds, min, max, borderPaint.strokeWidth)
        }

        canvas.drawPath(path, bgPaint)
        canvas.drawPath(path, borderPaint)
    }

    companion object {
        private const val TAG = "ComplicationFrame"
    }
}

private class IndexRim(
    private val largeIndexWidth: Int,
    private val largeIndexLength: Int,
    private val smallIndexWidth: Int,
    private val smallIndexLength: Int
) {
    private var currentWatchBounds = Rect()
    private var currentPadding = 0

    private val path = Path()

    private fun recalculate(bounds: Rect, padding: Int) {
        Log.d(TAG, """recalculate
            |    bounds: $currentWatchBounds -> $bounds
            |    padding: $currentPadding -> $padding
        """.trimMargin())

        currentWatchBounds = Rect(bounds)
        currentPadding = padding

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

        path.reset()
        for (i in 0..59) {
            m.setRotate(360F / 60 * i, bounds.centerX().toFloat(), bounds.centerY().toFloat())
            if (i % 5 == 0) {
                path.addPath(largeIndex, m)
            } else {
                path.addPath(smallIndex, m)
            }
        }
    }

    fun draw(canvas: Canvas, bounds: Rect, padding: Int, rotation: Float, paint: Paint) {
        if (currentWatchBounds != bounds || currentPadding != padding) {
            recalculate(bounds, padding)
        }

        canvas.withRotation(rotation, bounds.exactCenterX(), bounds.exactCenterY()) {
            drawPath(path, paint)
        }
    }

    companion object {
        private const val TAG = "IndexRim"
    }
}

private class NumberRim {
    private var currentWatchBounds = Rect()
    private var currentPadding = 0
    private var currentPaint = Paint()

    private var texts = emptyArray<String>()
    private var textPaths = emptyArray<Path>()
    private var x = 0F
    private var y = 0F

    private fun recalculate(bounds: Rect, padding: Int, paint: Paint) {
        Log.d(TAG, """recalculate
            |    bounds: $currentWatchBounds -> $bounds
            |    padding: $currentPadding -> $padding
            |    paint: ${currentPaint.fontMetricsInt} -> ${paint.fontMetricsInt}
        """.trimMargin())

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

        x = bounds.right - padding - maxBound / 2F
        y = bounds.exactCenterY()
    }

    fun draw(canvas: Canvas, bounds: Rect, padding: Int, rotation: Float, paint: Paint) {
        if (currentWatchBounds != bounds
            || currentPadding != padding
            || !currentPaint.equalsForTextMeasurement(paint)) {
            recalculate(bounds, padding, paint)
        }

        for (i in RANGE) {
            val numberRotation = rotation + 360F / LIMIT * (LIMIT - i) * STEP
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
        private const val TAG = "NumberRim"
        private const val STEP = 5
        private const val LIMIT = 60
        private val RANGE = 0 until LIMIT / STEP
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

private fun toDegrees(value: Float): Float {
    return (value * 180 / Math.PI).toFloat()
}
