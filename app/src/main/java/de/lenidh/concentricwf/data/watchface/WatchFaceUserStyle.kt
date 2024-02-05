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
    ColorOption("#FFFFFF", R.string.color_option_white),
    ColorOption("#1ABC9C", R.string.color_option_turquoise),
    ColorOption("#16A085", R.string.color_option_greensea),
    ColorOption("#2ECC71", R.string.color_option_emerland),
    ColorOption("#27AE60", R.string.color_option_nephritis),
    ColorOption("#3498DB", R.string.color_option_peterriver),
    ColorOption("#2980B9", R.string.color_option_belizehole),
    ColorOption("#9B59B6", R.string.color_option_amethyst),
    ColorOption("#8E44AD", R.string.color_option_wisteria),
    ColorOption("#F1C40F", R.string.color_option_sunflower),
    ColorOption("#F39C12", R.string.color_option_orange),
    ColorOption("#E67E22", R.string.color_option_carrot),
    ColorOption("#D35400", R.string.color_option_pumpkin),
    ColorOption("#E74C3C", R.string.color_option_alizarin),
    ColorOption("#C0392B", R.string.color_option_pomegranate),
    ColorOption("#ECF0F1", R.string.color_option_clouds),
    ColorOption("#BDC3C7", R.string.color_option_silver),
    ColorOption("#95A5A6", R.string.color_option_concrete),
    ColorOption("#7F8C8D", R.string.color_option_asbestos),
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
