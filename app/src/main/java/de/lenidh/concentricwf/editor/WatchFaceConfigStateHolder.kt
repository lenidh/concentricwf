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
package de.lenidh.concentricwf.editor

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.editor.EditorSession
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import de.lenidh.concentricwf.utils.COLOR_STYLE_SETTING
import de.lenidh.concentricwf.utils.COMPLICATION_1_ID
import de.lenidh.concentricwf.utils.COMPLICATION_2_ID
import de.lenidh.concentricwf.utils.COMPLICATION_3_ID
import de.lenidh.concentricwf.utils.COMPLICATION_4_ID
import de.lenidh.concentricwf.utils.COMPLICATION_5_ID
import de.lenidh.concentricwf.utils.FONT_STYLE_SETTING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.yield

/**
 * Maintains the [WatchFaceConfigActivity] state, i.e., handles reads and writes to the
 * [EditorSession] which is basically the watch face data layer. This allows the user to edit their
 * watch face through [WatchFaceConfigActivity].
 *
 * Note: This doesn't use an Android ViewModel because the [EditorSession]'s constructor requires a
 * ComponentActivity and Intent (needed for the library's complication editing UI which is triggered
 * through the [EditorSession]). Generally, Activities and Views shouldn't be passed to Android
 * ViewModels, so this is named StateHolder to avoid confusion.
 *
 * Also, the scope is passed in and we recommend you use the of the lifecycleScope of the Activity.
 *
 * For the [EditorSession] itself, this class uses the keys, [UserStyleSetting], for each of our
 * user styles and sets their values [UserStyleSetting.Option]. After a new value is set, creates a
 * new image preview via screenshot class and triggers a listener (which creates new data for the
 * [StateFlow] that feeds back to the Activity).
 */
class WatchFaceConfigStateHolder(
    private val scope: CoroutineScope,
    private val activity: ComponentActivity
) {
    private lateinit var editorSession: EditorSession

    // Keys from Watch Face Data Structure
    private lateinit var colorStyleKey: UserStyleSetting.ListUserStyleSetting
    private lateinit var fontStyleKey: UserStyleSetting.ListUserStyleSetting

    val uiState: StateFlow<EditWatchFaceUiState> =
        flow<EditWatchFaceUiState> {
            editorSession = EditorSession.createOnWatchEditorSession(
                activity = activity
            )

            extractsUserStyles(editorSession.userStyleSchema)

            emitAll(
                combine(
                    editorSession.userStyle,
                    editorSession.complicationsPreviewData
                ) { userStyle, complicationsPreviewData ->
                    yield()
                    EditWatchFaceUiState.Success(
                        createWatchFacePreview(userStyle, complicationsPreviewData)
                    )
                }
            )
        }
            .stateIn(
                scope + Dispatchers.Main.immediate,
                SharingStarted.Eagerly,
                EditWatchFaceUiState.Loading("Initializing")
            )

    private fun extractsUserStyles(userStyleSchema: UserStyleSchema) {
        // Loops through user styles and retrieves user editable styles.
        for (setting in userStyleSchema.rootUserStyleSettings) {
            when (setting.id.toString()) {
                COLOR_STYLE_SETTING -> {
                    colorStyleKey = setting as UserStyleSetting.ListUserStyleSetting
                }
                FONT_STYLE_SETTING -> {
                    fontStyleKey = setting as UserStyleSetting.ListUserStyleSetting
                }
            }
        }
    }

    /* Creates a new bitmap render of the updated watch face and passes it along (with all the other
     * updated values) to the Activity to render.
     */
    private fun createWatchFacePreview(
        userStyle: UserStyle,
        complicationsPreviewData: Map<Int, ComplicationData>
    ): UserStylesAndPreview {
        Log.d(TAG, "updatesWatchFacePreview()")

        val bitmap = editorSession.renderWatchFaceToBitmap(
            RenderParameters(
                DrawMode.INTERACTIVE,
                WatchFaceLayer.ALL_WATCH_FACE_LAYERS
            ),
            editorSession.previewReferenceInstant,
            complicationsPreviewData
        )

        val colorStyle =
            userStyle[colorStyleKey] as UserStyleSetting.ListUserStyleSetting.ListOption
        val fontStyle = userStyle[fontStyleKey] as UserStyleSetting.ListUserStyleSetting.ListOption

        Log.d(TAG, "/new values: $colorStyle $fontStyle")

        return UserStylesAndPreview(
            colorStyleId = colorStyle.id.toString(),
            fontStyleId = fontStyle.id.toString(),
            previewImage = bitmap
        )
    }

    fun setComplication(complicationLocation: Int) {
        val complicationSlotId = when (complicationLocation) {
            COMPLICATION_1_ID -> {
                COMPLICATION_1_ID
            }
            COMPLICATION_2_ID -> {
                COMPLICATION_2_ID
            }
            COMPLICATION_3_ID -> {
                COMPLICATION_3_ID
            }
            COMPLICATION_4_ID -> {
                COMPLICATION_4_ID
            }
            COMPLICATION_5_ID -> {
                COMPLICATION_5_ID
            }
            else -> {
                return
            }
        }
        scope.launch(Dispatchers.Main.immediate) {
            editorSession.openComplicationDataSourceChooser(complicationSlotId)
        }
    }

    fun setColorStyle(newColorStyleId: String) {
        val userStyleSettingList = editorSession.userStyleSchema.rootUserStyleSettings

        // Loops over all UserStyleSettings (basically the keys in the map) to find the setting for
        // the color style (which contains all the possible options for that style setting).
        for (userStyleSetting in userStyleSettingList) {
            if (userStyleSetting.id == UserStyleSetting.Id(COLOR_STYLE_SETTING)) {
                val colorUserStyleSetting =
                    userStyleSetting as UserStyleSetting.ListUserStyleSetting

                // Loops over the UserStyleSetting.Option colors (all possible values for the key)
                // to find the matching option, and if it exists, sets it as the color style.
                for (colorOptions in colorUserStyleSetting.options) {
                    if (colorOptions.id.toString() == newColorStyleId) {
                        setUserStyleOption(colorStyleKey, colorOptions)
                        return
                    }
                }
            }
        }
    }

    fun setFontStyle(newFontStyleId: String) {
        val userStyleSettingList = editorSession.userStyleSchema.rootUserStyleSettings

        // Loops over all UserStyleSettings (basically the keys in the map) to find the setting for
        // the font style (which contains all the possible options for that style setting).
        for (userStyleSetting in userStyleSettingList) {
            if (userStyleSetting.id == UserStyleSetting.Id(FONT_STYLE_SETTING)) {
                val fontUserStyleSetting =
                    userStyleSetting as UserStyleSetting.ListUserStyleSetting

                // Loops over the UserStyleSetting.Option fonts (all possible values for the key)
                // to find the matching option, and if it exists, sets it as the font style.
                for (fontOptions in fontUserStyleSetting.options) {
                    if (fontOptions.id.toString() == newFontStyleId) {
                        setUserStyleOption(fontStyleKey, fontOptions)
                        return
                    }
                }
            }
        }
    }

    // Saves User Style Option change back to the back to the EditorSession.
    // Note: The UI widgets in the Activity that can trigger this method (through the 'set' methods)
    // will only be enabled after the EditorSession has been initialized.
    private fun setUserStyleOption(
        userStyleSetting: UserStyleSetting,
        userStyleOption: UserStyleSetting.Option
    ) {
        Log.d(TAG, "setUserStyleOption()")
        Log.d(TAG, "\tuserStyleSetting: $userStyleSetting")
        Log.d(TAG, "\tuserStyleOption: $userStyleOption")

        // TODO: As of watchface 1.0.0-beta01 We can't use MutableStateFlow.compareAndSet, or
        //       anything that calls through to that (like MutableStateFlow.update) because
        //       MutableStateFlow.compareAndSet won't properly update the user style.
        val mutableUserStyle = editorSession.userStyle.value.toMutableUserStyle()
        mutableUserStyle[userStyleSetting] = userStyleOption
        editorSession.userStyle.value = mutableUserStyle.toUserStyle()
    }

    sealed class EditWatchFaceUiState {
        data class Success(val userStylesAndPreview: UserStylesAndPreview) : EditWatchFaceUiState()
        data class Loading(val message: String) : EditWatchFaceUiState()
        data class Error(val exception: Throwable) : EditWatchFaceUiState()
    }

    data class UserStylesAndPreview(
        val colorStyleId: String,
        val fontStyleId: String,
        val previewImage: Bitmap
    )

    companion object {
        private const val TAG = "WatchFaceConfigStateHolder"
    }
}
