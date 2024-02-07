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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Icon
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.ListUserStyleSetting
import de.lenidh.concentricwf.R

val COLOR_OPTIONS = listOf(
    ColorOption("#D3FFFFFF", R.string.color_option_default),
    ColorOption("#ECF0F1", R.string.color_option_clouds),
    ColorOption("#BDC3C7", R.string.color_option_silver),
    ColorOption("#95A5A6", R.string.color_option_concrete),
    ColorOption("#7F8C8D", R.string.color_option_asbestos),
    ColorOption("#7FFFD4", R.string.color_option_aquamarine),
    ColorOption("#1ABC9C", R.string.color_option_turquoise),
    ColorOption("#16A085", R.string.color_option_greensea),
    ColorOption("#98FB98", R.string.color_option_palegreen),
    ColorOption("#00FF7F", R.string.color_option_springgreen),
    ColorOption("#2ECC71", R.string.color_option_emerland),
    ColorOption("#27AE60", R.string.color_option_nephritis),
    ColorOption("#87CEEB", R.string.color_option_sky),
    ColorOption("#3498DB", R.string.color_option_peterriver),
    ColorOption("#2980B9", R.string.color_option_belizehole),
    ColorOption("#4682B4", R.string.color_option_steelblue),
    ColorOption("#1E90FF", R.string.color_option_dodgerblue),
    ColorOption("#D8BFD8", R.string.color_option_thistle),
    ColorOption("#DDA0DD", R.string.color_option_plum),
    ColorOption("#EE82EE", R.string.color_option_violet),
    ColorOption("#DA70D6", R.string.color_option_orchid),
    ColorOption("#9B59B6", R.string.color_option_amethyst),
    ColorOption("#8E44AD", R.string.color_option_wisteria),
    ColorOption("#FFE4B5", R.string.color_option_moccasin),
    ColorOption("#F1C40F", R.string.color_option_sunflower),
    ColorOption("#F39C12", R.string.color_option_orange),
    ColorOption("#E67E22", R.string.color_option_carrot),
    ColorOption("#D35400", R.string.color_option_pumpkin),
    ColorOption("#C0392B", R.string.color_option_pomegranate),
    ColorOption("#FA8072", R.string.color_option_salmon),
    ColorOption("#FF7F50", R.string.color_option_coral),
    ColorOption("#E74C3C", R.string.color_option_alizarin),
    ColorOption("#D2B48C", R.string.color_option_tan),
    ColorOption("#CD853F", R.string.color_option_peru),
    ColorOption("#A0522D", R.string.color_option_sienna),
)

data class ColorOption(val id: String, val nameId: Int) {
    val colorInt: Int = Color.parseColor(id)
    val color: Color = Color.valueOf(colorInt)
}

val FONT_OPTIONS = listOf(
    FontOption("1", R.font.rubik_regular, R.string.font_option_rubik),
    FontOption("2", R.font.manrope_regular, R.string.font_option_manrope),
    FontOption("3", R.font.ebgaramond_medium, R.string.font_option_ebgaradmond),
    FontOption("4", R.font.chakrapetch_regular, R.string.font_option_chakrapetch),
)

data class FontOption(val id: String, val fontId: Int, val nameId: Int)

/**
 * Represents watch face style options the user can select.
 *
 * The companion object offers helper functions to translate a unique string id to the correct value
 * and convert all the resource ids to their correct resources (with the Context passed in). The
 * renderer will use these resources to render the watch face.
 */
class WatchFaceUserStyle(
    val accentColor: Color,
    val fontId: Int,
) {

    companion object {
        /**
         * Translates the string id to the correct WatchFaceUserStyle object.
         */
        fun getStyleConfig(
            accentColorId: String? = null,
            fontOptionId: String? = null
        ): WatchFaceUserStyle {
            return WatchFaceUserStyle(
                getColorOptionOrDefault(accentColorId).color,
                getFontOptionOrDefault(fontOptionId).fontId,
            )
        }

        /**
         * Returns a list of [UserStyleSetting.ListUserStyleSetting.ListOption] for all
         * ColorStyleIdAndResourceIds enums. The watch face settings APIs use this to set up
         * options for the user to select a style.
         */
        fun getColorOptionList(context: Context): List<ListUserStyleSetting.ListOption> {
            return COLOR_OPTIONS.map { option ->
                val icon = Icon.createWithResource(
                    context,
                    R.drawable.ic_color_style,
                )
                icon.setTint(option.colorInt)
                ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(option.id),
                    context.resources,
                    option.nameId,
                    option.nameId,
                    icon
                )
            }
        }

        fun getFontOptionList(context: Context): List<ListUserStyleSetting.ListOption> {
            val icons = createFontOptionIcons(context, FONT_OPTIONS)
            return FONT_OPTIONS.zip(icons).map { (option, icon ) ->
                ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(option.id),
                    context.resources,
                    option.nameId,
                    option.nameId,
                    icon
                )
            }
        }

        private fun createFontOptionIcons(context: Context, options: List<FontOption>): List<Icon> {
            val fontPaint = Paint().apply { textSize = 36F; textAlign = Paint.Align.CENTER ; color = Color.BLACK }
            val bgPaint = Paint().apply { color = Color.WHITE }
            return options.map { option ->
                fontPaint.typeface = context.resources.getFont(option.fontId)
                val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawCircle(24F, 24F,24F, bgPaint)
                canvas.drawText("09", 24F, 36F, fontPaint)
                Icon.createWithBitmap(bitmap)
            }
        }

        fun getColorOption(id: String): ColorOption? {
            return COLOR_OPTIONS.find { option -> option.id == id }
        }

        private fun getColorOptionOrDefault(id: String?): ColorOption {
            return id?.let { getColorOption(it) } ?: COLOR_OPTIONS[0]
        }

        fun getFontOption(id: String): FontOption? {
            return FONT_OPTIONS.find { option -> option.id == id }
        }

        private fun getFontOptionOrDefault(id: String?): FontOption {
            return id?.let { getFontOption(it) } ?: FONT_OPTIONS[0]
        }
    }
}
