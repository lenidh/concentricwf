package de.lenidh.concentricwf.utils

import android.content.Context
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import de.lenidh.concentricwf.R
import de.lenidh.concentricwf.data.watchface.ColorStyleIdAndResourceIds

// Keys to matched content in the  the user style settings. We listen for changes to these
// values in the renderer and if new, we will update the database and update the watch face
// being rendered.
const val COLOR_STYLE_SETTING = "color_style_setting"

/*
 * Creates user styles in the settings activity associated with the watch face, so users can
 * edit different parts of the watch face. In the renderer (after something has changed), the
 * watch face listens for a flow from the watch face API data layer and updates the watch face.
 */
fun createUserStyleSchema(context: Context): UserStyleSchema {
    // Allows user to change the accent color of the watch face.
    val colorStyleSetting =
        UserStyleSetting.ListUserStyleSetting(
            UserStyleSetting.Id(COLOR_STYLE_SETTING),
            context.resources,
            R.string.colors_style_setting,
            R.string.colors_style_setting_description,
            null,
            ColorStyleIdAndResourceIds.getColorOptionList(context),
            listOf(
                WatchFaceLayer.BASE,
                WatchFaceLayer.COMPLICATIONS,
                WatchFaceLayer.COMPLICATIONS_OVERLAY
            )
        )

    return UserStyleSchema(
        listOf(
            colorStyleSetting,
        )
    )
}
