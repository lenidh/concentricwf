package de.lenidh.concentricwf.data.watchface

import android.content.Context
import androidx.wear.watchface.complications.rendering.ComplicationDrawable

/**
 * Color resources and drawable id needed to render the watch face. Translated from
 * [ColorStyleIdAndResourceIds] constant ids to actual resources with context at run time.
 *
 * This is only needed when the watch face is active.
 *
 * Note: We do not use the context to generate a [ComplicationDrawable] from the
 * complicationStyleDrawableId (representing the style), because a new, separate
 * [ComplicationDrawable] is needed for each complication. Because the renderer will loop through
 * all the complications and there can be more than one, this also allows the renderer to create
 * as many [ComplicationDrawable]s as needed.
 */
data class WatchFaceColorPalette(
    val activeCurrentTimeColor: Int,
    val activeMinutesColor: Int,
    val activeSecondsColor: Int,
    val activeBordersColor: Int,
    val ambientCurrentTimeColor: Int,
    val ambientMinutesColor: Int,
    val ambientSecondsColor: Int,
    val ambientBordersColor: Int
) {
    companion object {
        /**
         * Converts [ColorStyleIdAndResourceIds] to [WatchFaceColorPalette].
         */
        fun convertToWatchFaceColorPalette(
            context: Context,
            activeColorStyle: ColorStyleIdAndResourceIds,
        ): WatchFaceColorPalette {
            return WatchFaceColorPalette(
                // Active colors
                activeCurrentTimeColor = activeColorStyle.currentTimeColorId.toArgb(),
                activeMinutesColor = activeColorStyle.minutesColorId.toArgb(),
                activeSecondsColor = activeColorStyle.secondsColorId.toArgb(),
                activeBordersColor = activeColorStyle.bordersColorId.toArgb(),
                // Ambient colors
                ambientCurrentTimeColor = activeColorStyle.currentTimeColorId.toArgb(),
                ambientMinutesColor = activeColorStyle.minutesColorId.toArgb(),
                ambientSecondsColor = activeColorStyle.secondsColorId.toArgb(),
                ambientBordersColor = activeColorStyle.bordersColorId.toArgb(),
            )
        }
    }
}
