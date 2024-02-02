/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lenidh.concentricwf.data.watchface

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
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

/**
 * Represents watch face color style options the user can select (includes the unique id, the
 * complication style resource id, and general watch face color style resource ids).
 *
 * The companion object offers helper functions to translate a unique string id to the correct enum
 * and convert all the resource ids to their correct resources (with the Context passed in). The
 * renderer will use these resources to render the actual colors and ComplicationDrawables of the
 * watch face.
 */
class ColorStyleIdAndResourceIds(
    @DrawableRes val complicationStyleDrawableId: Int,
    val currentTimeColorId: Color,
    val minutesColorId: Color,
    val secondsColorId: Color,
    val bordersColorId: Color
) {

    companion object {
        /**
         * Translates the string id to the correct ColorStyleIdAndResourceIds object.
         */
        fun getColorStyleConfig(accentColorId: String = COLOR_OPTIONS[0]): ColorStyleIdAndResourceIds {
            return ColorStyleIdAndResourceIds(
                R.drawable.complication_white_style,
                Color.valueOf(Color.parseColor("#FFFFFF")),
                Color.valueOf(Color.parseColor("#767a80")),
                Color.valueOf(Color.parseColor(accentColorId)),
                Color.valueOf(Color.parseColor(accentColorId)),
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
                    UserStyleSetting.Option.Id(it), it, icon
                )
            }
        }
    }
}
