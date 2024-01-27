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

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import de.lenidh.concentricwf.data.watchface.ColorStyleIdAndResourceIds
import de.lenidh.concentricwf.databinding.ActivityWatchFaceConfigBinding
import de.lenidh.concentricwf.editor.WatchFaceConfigStateHolder.Companion.MINUTE_HAND_LENGTH_DEFAULT_FOR_SLIDER
import de.lenidh.concentricwf.editor.WatchFaceConfigStateHolder.Companion.MINUTE_HAND_LENGTH_MAXIMUM_FOR_SLIDER
import de.lenidh.concentricwf.editor.WatchFaceConfigStateHolder.Companion.MINUTE_HAND_LENGTH_MINIMUM_FOR_SLIDER
import de.lenidh.concentricwf.utils.COMPLICATION_1_BOTTOM_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_1_ID
import de.lenidh.concentricwf.utils.COMPLICATION_1_LEFT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_1_RIGHT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_1_TOP_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_2_BOTTOM_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_2_ID
import de.lenidh.concentricwf.utils.COMPLICATION_2_LEFT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_2_RIGHT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_2_TOP_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_3_BOTTOM_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_3_ID
import de.lenidh.concentricwf.utils.COMPLICATION_3_LEFT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_3_RIGHT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_3_TOP_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_4_BOTTOM_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_4_ID
import de.lenidh.concentricwf.utils.COMPLICATION_4_LEFT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_4_RIGHT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_4_TOP_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_5_BOTTOM_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_5_ID
import de.lenidh.concentricwf.utils.COMPLICATION_5_LEFT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_5_RIGHT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_5_TOP_BOUND
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Allows user to edit certain parts of the watch face (color style, ticks displayed, minute arm
 * length) by using the [WatchFaceConfigStateHolder]. (All widgets are disabled until data is
 * loaded.)
 */
class WatchFaceConfigActivity : ComponentActivity() {
    private val stateHolder: WatchFaceConfigStateHolder by lazy {
        WatchFaceConfigStateHolder(
            lifecycleScope, this@WatchFaceConfigActivity
        )
    }

    private lateinit var binding: ActivityWatchFaceConfigBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")

        binding = ActivityWatchFaceConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Disable widgets until data loads and values are set.
        binding.colorStylePickerButton.isEnabled = false
        binding.ticksEnabledSwitch.isEnabled = false
        binding.minuteHandLengthSlider.isEnabled = false

        // Set max and min.
        binding.minuteHandLengthSlider.valueTo = MINUTE_HAND_LENGTH_MAXIMUM_FOR_SLIDER
        binding.minuteHandLengthSlider.valueFrom = MINUTE_HAND_LENGTH_MINIMUM_FOR_SLIDER
        binding.minuteHandLengthSlider.value = MINUTE_HAND_LENGTH_DEFAULT_FOR_SLIDER

        binding.minuteHandLengthSlider.addOnChangeListener { slider, value, fromUser ->
            Log.d(TAG, "addOnChangeListener(): $slider, $value, $fromUser")
            if (fromUser) {
                stateHolder.setMinuteHandArmLength(value)
            }
        }

        val layoutWidth = (binding.preview.root as View).width;
        val layoutHeight = (binding.preview.root as View).height;
        Log.d(TAG, "layout size: $layoutWidth $layoutHeight")

        var params: ConstraintLayout.LayoutParams

        params = binding.preview.complication1.layoutParams as ConstraintLayout.LayoutParams
        params.matchConstraintPercentWidth = COMPLICATION_1_RIGHT_BOUND - COMPLICATION_1_LEFT_BOUND
        params.horizontalBias = COMPLICATION_1_LEFT_BOUND + params.matchConstraintPercentWidth / 2
        params.matchConstraintPercentHeight = COMPLICATION_1_BOTTOM_BOUND - COMPLICATION_1_TOP_BOUND
        params.verticalBias = COMPLICATION_1_TOP_BOUND + params.matchConstraintPercentHeight / 2
        binding.preview.complication1.layoutParams = params

        params = binding.preview.complication2.layoutParams as ConstraintLayout.LayoutParams
        params.matchConstraintPercentWidth = COMPLICATION_2_RIGHT_BOUND - COMPLICATION_2_LEFT_BOUND
        params.horizontalBias = COMPLICATION_2_LEFT_BOUND + params.matchConstraintPercentWidth / 2
        params.matchConstraintPercentHeight = COMPLICATION_2_BOTTOM_BOUND - COMPLICATION_2_TOP_BOUND
        params.verticalBias = COMPLICATION_2_TOP_BOUND + params.matchConstraintPercentHeight / 2
        binding.preview.complication2.layoutParams = params

        params = binding.preview.complication3.layoutParams as ConstraintLayout.LayoutParams
        params.matchConstraintPercentWidth = COMPLICATION_3_RIGHT_BOUND - COMPLICATION_3_LEFT_BOUND
        params.horizontalBias = COMPLICATION_3_LEFT_BOUND + params.matchConstraintPercentWidth / 2
        params.matchConstraintPercentHeight = COMPLICATION_3_BOTTOM_BOUND - COMPLICATION_3_TOP_BOUND
        params.verticalBias = COMPLICATION_3_TOP_BOUND + params.matchConstraintPercentHeight / 2
        binding.preview.complication3.layoutParams = params

        params = binding.preview.complication4.layoutParams as ConstraintLayout.LayoutParams
        params.matchConstraintPercentWidth = COMPLICATION_4_RIGHT_BOUND - COMPLICATION_4_LEFT_BOUND
        params.horizontalBias = COMPLICATION_4_LEFT_BOUND + params.matchConstraintPercentWidth / 2
        params.matchConstraintPercentHeight = COMPLICATION_4_BOTTOM_BOUND - COMPLICATION_4_TOP_BOUND
        params.verticalBias = COMPLICATION_4_TOP_BOUND + params.matchConstraintPercentHeight / 2
        binding.preview.complication4.layoutParams = params

        params = binding.preview.complication5.layoutParams as ConstraintLayout.LayoutParams
        params.matchConstraintPercentWidth = COMPLICATION_5_RIGHT_BOUND - COMPLICATION_5_LEFT_BOUND
        params.horizontalBias = COMPLICATION_5_LEFT_BOUND + params.matchConstraintPercentWidth / 2
        params.matchConstraintPercentHeight = COMPLICATION_5_BOTTOM_BOUND - COMPLICATION_5_TOP_BOUND
        params.verticalBias = COMPLICATION_5_TOP_BOUND + params.matchConstraintPercentHeight / 2
        binding.preview.complication5.layoutParams = params

        lifecycleScope.launch(Dispatchers.Main.immediate) {
            stateHolder.uiState.collect { uiState: WatchFaceConfigStateHolder.EditWatchFaceUiState ->
                when (uiState) {
                    is WatchFaceConfigStateHolder.EditWatchFaceUiState.Loading -> {
                        Log.d(TAG, "StateFlow Loading: ${uiState.message}")
                    }

                    is WatchFaceConfigStateHolder.EditWatchFaceUiState.Success -> {
                        Log.d(TAG, "StateFlow Success.")
                        updateWatchFaceEditorPreview(uiState.userStylesAndPreview)
                    }

                    is WatchFaceConfigStateHolder.EditWatchFaceUiState.Error -> {
                        Log.e(TAG, "Flow error: ${uiState.exception}")
                    }
                }
            }
        }
    }

    private fun updateWatchFaceEditorPreview(
        userStylesAndPreview: WatchFaceConfigStateHolder.UserStylesAndPreview
    ) {
        Log.d(TAG, "updateWatchFacePreview: $userStylesAndPreview")

        val colorStyleId: String = userStylesAndPreview.colorStyleId
        Log.d(TAG, "\tselected color style: $colorStyleId")

        binding.ticksEnabledSwitch.isChecked = userStylesAndPreview.ticksEnabled
        binding.minuteHandLengthSlider.value = userStylesAndPreview.minuteHandLength
        binding.preview.watchFaceBackground.setImageBitmap(userStylesAndPreview.previewImage)

        enabledWidgets()
    }

    private fun enabledWidgets() {
        binding.colorStylePickerButton.isEnabled = true
        binding.ticksEnabledSwitch.isEnabled = true
        binding.minuteHandLengthSlider.isEnabled = true
    }

    fun onClickColorStylePickerButton(view: View) {
        Log.d(TAG, "onClickColorStylePickerButton() $view")

        // TODO (codingjeremy): Replace with a RecyclerView to choose color style (next CL)
        // Selects a random color style from list.
        val colorStyleIdAndResourceIdsList = enumValues<ColorStyleIdAndResourceIds>()
        val newColorStyle: ColorStyleIdAndResourceIds = colorStyleIdAndResourceIdsList.random()

        stateHolder.setColorStyle(newColorStyle.id)
    }

    fun onClickComplication1Button(view: View) {
        Log.d(TAG, "onClickComplication1Button() $view")
        stateHolder.setComplication(COMPLICATION_1_ID)
    }

    fun onClickComplication2Button(view: View) {
        Log.d(TAG, "onClickComplication2Button() $view")
        stateHolder.setComplication(COMPLICATION_2_ID)
    }

    fun onClickComplication3Button(view: View) {
        Log.d(TAG, "onClickComplication3Button() $view")
        stateHolder.setComplication(COMPLICATION_3_ID)
    }

    fun onClickComplication4Button(view: View) {
        Log.d(TAG, "onClickComplication4Button() $view")
        stateHolder.setComplication(COMPLICATION_4_ID)
    }

    fun onClickComplication5Button(view: View) {
        Log.d(TAG, "onClickComplication5Button() $view")
        stateHolder.setComplication(COMPLICATION_5_ID)
    }

    companion object {
        const val TAG = "WatchFaceConfigActivity"
    }
}
