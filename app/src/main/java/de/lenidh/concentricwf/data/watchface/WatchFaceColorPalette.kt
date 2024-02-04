package de.lenidh.concentricwf.data.watchface

import android.content.Context
import de.lenidh.concentricwf.R

/**
 * Color resources needed to render the watch face.
 * Translated from [WatchFaceUserStyle] constant ids to actual resources with context at run time.
 */
data class WatchFaceColorPalette(
    val activeCurrentHourColor: Int,
    val activeCurrentMinuteColor: Int,
    val activeMinutesColor: Int,
    val activeSecondsColor: Int,
    val activeBordersColor: Int,
    val activeComplicationTextColor: Int,
    val activeComplicationIconColor: Int,
    val ambientCurrentHourColor: Int,
    val ambientCurrentMinuteColor: Int,
    val ambientMinutesColor: Int,
    val ambientSecondsColor: Int,
    val ambientBordersColor: Int,
    val ambientComplicationTextColor: Int,
    val ambientComplicationIconColor: Int,
) {
    companion object {
        /**
         * Converts [WatchFaceUserStyle] to [WatchFaceColorPalette].
         */
        fun convertToWatchFaceColorPalette(
            context: Context,
            activeColorStyle: WatchFaceUserStyle,
        ): WatchFaceColorPalette {
            return WatchFaceColorPalette(
                // Active colors
                activeCurrentHourColor = context.getColor(R.color.white_90),
                activeCurrentMinuteColor = context.getColor(R.color.white_90),
                activeMinutesColor = context.getColor(R.color.minutes_default),
                activeSecondsColor = activeColorStyle.accentColor.toArgb(),
                activeBordersColor = activeColorStyle.accentColor.toArgb(),
                activeComplicationTextColor = context.getColor(R.color.white_90),
                activeComplicationIconColor = activeColorStyle.accentColor.toArgb(),
                // Ambient colors
                ambientCurrentHourColor = context.getColor(R.color.white_70),
                ambientCurrentMinuteColor = context.getColor(R.color.white_70),
                ambientMinutesColor = context.getColor(R.color.minutes_default),
                ambientSecondsColor = activeColorStyle.accentColor.toArgb() and 0xB3FFFFFF.toInt(),
                ambientBordersColor = activeColorStyle.accentColor.toArgb() and 0xB3FFFFFF.toInt(),
                ambientComplicationTextColor = context.getColor(R.color.white_70),
                ambientComplicationIconColor = activeColorStyle.accentColor.toArgb() and 0xB3FFFFFF.toInt(),
            )
        }
    }
}
