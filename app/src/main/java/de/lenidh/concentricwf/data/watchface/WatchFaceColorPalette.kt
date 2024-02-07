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
