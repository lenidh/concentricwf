package de.lenidh.concentricwf.data.watchface

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.util.Log
import androidx.wear.compose.material.contentColorFor
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.UserStyleSetting.ListUserStyleSetting
import de.lenidh.concentricwf.R

val COLOR_OPTIONS = listOf(
    "#FFFFFF", // White
    "#1ABC9C", // Turquoise
    "#16A085", // Greensea
    "#2ECC71", // Emerland
    "#27AE60", // Nephritis
    "#3498DB", // Peterriver
    "#2980B9", // Belizehole
    "#9B59B6", // Amethyst
    "#8E44AD", // Wisteria
    "#F1C40F", // Sunflower
    "#F39C12", // Orange
    "#E67E22", // Carrot
    "#D35400", // Pumpkin
    "#E74C3C", // Alizarin
    "#C0392B", // Pomegranate
    "#ECF0F1", // Clouds
    "#BDC3C7", // Silver
    "#95A5A6", // Concrete
    "#7F8C8D", // Asbestos
)

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
                Color.valueOf(Color.parseColor(accentColorId ?: COLOR_OPTIONS[0])),
                fontOptionId?.let { getFontOption(it)?.fontId } ?: FONT_OPTIONS[0].fontId,
            )
        }

        /**
         * Returns a list of [UserStyleSetting.ListUserStyleSetting.ListOption] for all
         * ColorStyleIdAndResourceIds enums. The watch face settings APIs use this to set up
         * options for the user to select a style.
         */
        fun getColorOptionList(context: Context): List<ListUserStyleSetting.ListOption> {

            return COLOR_OPTIONS.map {
                val icon = Icon.createWithResource(
                    context,
                    R.drawable.ic_color_style,
                )
                icon.setTint(Color.parseColor(it))
                ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(it), it, it, icon
                )
            }
        }

        fun getFontOptionList(context: Context): List<ListUserStyleSetting.ListOption> {
            return FONT_OPTIONS.map { option ->
                ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(option.id),
                    context.resources,
                    option.nameId,
                    option.nameId,
                    null
                )
            }
        }

        fun getFontOption(optionId: String): FontOption? {
            return FONT_OPTIONS.find { option -> option.id == optionId }
        }
    }
}
