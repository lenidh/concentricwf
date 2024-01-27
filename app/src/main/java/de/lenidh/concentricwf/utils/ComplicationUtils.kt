/*
 * Copyright 2020 The Android Open Source Project
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
package de.lenidh.concentricwf.utils

import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import androidx.wear.watchface.CanvasComplicationFactory
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationStyle
import androidx.wear.watchface.style.CurrentUserStyleRepository
import de.lenidh.concentricwf.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Information needed for complications.
// Creates bounds for the locations of both right and left complications. (This is the
// location from 0.0 - 1.0.)
// Both left and right complications use the same top and bottom bounds.

private const val COMPLICATION_OFFSET = 0.05F
private const val COMPLICATION_RADIUS = 0.1F
private const val COMPLICATION_ANGLE = PI / 5

val COMPLICATION_1_LEFT_BOUND = computeComplicationLeftBound(0)
val COMPLICATION_1_RIGHT_BOUND = computeComplicationRightBound(0)
val COMPLICATION_1_TOP_BOUND = computeComplicationTopBound(0)
val COMPLICATION_1_BOTTOM_BOUND = computeComplicationBottomBound(0)

val COMPLICATION_2_LEFT_BOUND = computeComplicationLeftBound(1)
val COMPLICATION_2_RIGHT_BOUND = computeComplicationRightBound(1)
val COMPLICATION_2_TOP_BOUND = computeComplicationTopBound(1)
val COMPLICATION_2_BOTTOM_BOUND = computeComplicationBottomBound(1)

val COMPLICATION_3_LEFT_BOUND = computeComplicationLeftBound(2)
val COMPLICATION_3_RIGHT_BOUND = computeComplicationRightBound(2)
val COMPLICATION_3_TOP_BOUND = computeComplicationTopBound(2)
val COMPLICATION_3_BOTTOM_BOUND = computeComplicationBottomBound(2)

val COMPLICATION_4_LEFT_BOUND = computeComplicationLeftBound(3)
val COMPLICATION_4_RIGHT_BOUND = computeComplicationRightBound(3)
val COMPLICATION_4_TOP_BOUND = computeComplicationTopBound(3)
val COMPLICATION_4_BOTTOM_BOUND = computeComplicationBottomBound(3)

val COMPLICATION_5_LEFT_BOUND = computeComplicationLeftBound(4)
val COMPLICATION_5_RIGHT_BOUND = computeComplicationRightBound(4)
val COMPLICATION_5_TOP_BOUND = computeComplicationTopBound(4)
val COMPLICATION_5_BOTTOM_BOUND = computeComplicationBottomBound(4)

private const val RIGHT_COMPLICATION_LEFT_BOUND = 0.6F
private const val RIGHT_COMPLICATION_RIGHT_BOUND = 0.8F

private val DEFAULT_COMPLICATION_STYLE_DRAWABLE_ID = R.drawable.complication_red_style

private fun computeComplicationLeftBound(i: Int): Float {
    return (0.5F - COMPLICATION_RADIUS - COMPLICATION_OFFSET) * cos(PI + (2 - i) * COMPLICATION_ANGLE).toFloat() - COMPLICATION_RADIUS + 0.5F
}

private fun computeComplicationRightBound(i: Int): Float {
    return (0.5F - COMPLICATION_RADIUS - COMPLICATION_OFFSET) * cos(PI + (2 - i) * COMPLICATION_ANGLE).toFloat() + COMPLICATION_RADIUS + 0.5F
}

private fun computeComplicationTopBound(i: Int): Float {
    return (0.5F - COMPLICATION_RADIUS - COMPLICATION_OFFSET) * sin(PI + (2 - i) * COMPLICATION_ANGLE).toFloat() - COMPLICATION_RADIUS + 0.5F
}

private fun computeComplicationBottomBound(i: Int): Float {
    return (0.5F - COMPLICATION_RADIUS - COMPLICATION_OFFSET) * sin(PI + (2 - i) * COMPLICATION_ANGLE).toFloat() + COMPLICATION_RADIUS + 0.5F
}

// Unique IDs for each complication. The settings activity that supports allowing users
// to select their complication data provider requires numbers to be >= 0.
internal const val COMPLICATION_1_ID = 100
internal const val COMPLICATION_2_ID = 101
internal const val COMPLICATION_3_ID = 102
internal const val COMPLICATION_4_ID = 103
internal const val COMPLICATION_5_ID = 104

internal const val TAG = "ComplicationUtils"

/**
 * Represents the unique id associated with a complication and the complication types it supports.
 */
sealed class ComplicationConfig(val id: Int, val supportedTypes: List<ComplicationType>) {
    object Comp1 : ComplicationConfig(
        COMPLICATION_1_ID, listOf(
            ComplicationType.SMALL_IMAGE,
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        )
    )

    object Comp2 : ComplicationConfig(
        COMPLICATION_2_ID, listOf(
            ComplicationType.SMALL_IMAGE,
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        )
    )

    object Comp3 : ComplicationConfig(
        COMPLICATION_3_ID, listOf(
            ComplicationType.SMALL_IMAGE,
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        )
    )

    object Comp4 : ComplicationConfig(
        COMPLICATION_4_ID, listOf(
            ComplicationType.SMALL_IMAGE,
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        )
    )

    object Comp5 : ComplicationConfig(
        COMPLICATION_5_ID, listOf(
            ComplicationType.SMALL_IMAGE,
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        )
    )
}

// Utility function that initializes default complication slots (left and right).
fun createComplicationSlotManager(
    context: Context,
    currentUserStyleRepository: CurrentUserStyleRepository,
    drawableId: Int = DEFAULT_COMPLICATION_STYLE_DRAWABLE_ID
): ComplicationSlotsManager {
    val defaultCanvasComplicationFactory = CanvasComplicationFactory { watchState, listener ->
        CanvasComplicationDrawable(
            ComplicationDrawable.getDrawable(context, drawableId)!!, watchState, listener
        )
    }

    val complication1 = ComplicationSlot.createRoundRectComplicationSlotBuilder(
        id = ComplicationConfig.Comp1.id,
        canvasComplicationFactory = defaultCanvasComplicationFactory,
        supportedTypes = ComplicationConfig.Comp1.supportedTypes,
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
            SystemDataSources.DATA_SOURCE_DAY_OF_WEEK, ComplicationType.SHORT_TEXT
        ),
        bounds = ComplicationSlotBounds(
            RectF(
                COMPLICATION_1_LEFT_BOUND,
                COMPLICATION_1_TOP_BOUND,
                COMPLICATION_1_RIGHT_BOUND,
                COMPLICATION_1_BOTTOM_BOUND
            )
        )
    ).build()
    val complication2 = ComplicationSlot.createRoundRectComplicationSlotBuilder(
        id = ComplicationConfig.Comp2.id,
        canvasComplicationFactory = defaultCanvasComplicationFactory,
        supportedTypes = ComplicationConfig.Comp2.supportedTypes,
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
            SystemDataSources.DATA_SOURCE_STEP_COUNT, ComplicationType.SHORT_TEXT
        ),
        bounds = ComplicationSlotBounds(
            RectF(
                COMPLICATION_2_LEFT_BOUND,
                COMPLICATION_2_TOP_BOUND,
                COMPLICATION_2_RIGHT_BOUND,
                COMPLICATION_2_BOTTOM_BOUND,
            )
        )
    ).build()
    val complication3 = ComplicationSlot.createRoundRectComplicationSlotBuilder(
        id = ComplicationConfig.Comp3.id,
        canvasComplicationFactory = defaultCanvasComplicationFactory,
        supportedTypes = ComplicationConfig.Comp3.supportedTypes,
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
            SystemDataSources.DATA_SOURCE_STEP_COUNT, ComplicationType.SHORT_TEXT
        ),
        bounds = ComplicationSlotBounds(
            RectF(
                COMPLICATION_3_LEFT_BOUND,
                COMPLICATION_3_TOP_BOUND,
                COMPLICATION_3_RIGHT_BOUND,
                COMPLICATION_3_BOTTOM_BOUND,
            )
        )
    ).build()
    val complication4 = ComplicationSlot.createRoundRectComplicationSlotBuilder(
        id = ComplicationConfig.Comp4.id,
        canvasComplicationFactory = defaultCanvasComplicationFactory,
        supportedTypes = ComplicationConfig.Comp4.supportedTypes,
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
            SystemDataSources.DATA_SOURCE_STEP_COUNT, ComplicationType.SHORT_TEXT
        ),
        bounds = ComplicationSlotBounds(
            RectF(
                COMPLICATION_4_LEFT_BOUND,
                COMPLICATION_4_TOP_BOUND,
                COMPLICATION_4_RIGHT_BOUND,
                COMPLICATION_4_BOTTOM_BOUND,
            )
        )
    ).build()
    val complication5 = ComplicationSlot.createRoundRectComplicationSlotBuilder(
        id = ComplicationConfig.Comp5.id,
        canvasComplicationFactory = defaultCanvasComplicationFactory,
        supportedTypes = ComplicationConfig.Comp5.supportedTypes,
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
            SystemDataSources.DATA_SOURCE_STEP_COUNT, ComplicationType.SHORT_TEXT
        ),
        bounds = ComplicationSlotBounds(
            RectF(
                COMPLICATION_5_LEFT_BOUND,
                COMPLICATION_5_TOP_BOUND,
                COMPLICATION_5_RIGHT_BOUND,
                COMPLICATION_5_BOTTOM_BOUND,
            )
        )
    ).build()

    return ComplicationSlotsManager(
        listOf(complication1, complication2, complication3, complication4, complication5),
        currentUserStyleRepository
    )
}

/**
 * Returns a [ComplicationStyle] based on the provided `style` but with colors
 * restricted to black, white or transparent. All text and icon colors in the returned style
 * will be set to white.
 */
private fun lowBitAmbientStyleFrom(style: ComplicationStyle): ComplicationStyle {
    val newStyle = ComplicationStyle(style)
    if (style.backgroundColor != Color.BLACK) {
        newStyle.backgroundColor = Color.TRANSPARENT
    }
    newStyle.textColor = Color.WHITE
    newStyle.titleColor = Color.WHITE
    newStyle.iconColor = Color.WHITE
    if (style.borderColor != Color.BLACK && style.borderColor != Color.TRANSPARENT) {
        newStyle.borderColor = Color.WHITE
    }
    newStyle.rangedValuePrimaryColor = Color.WHITE
    if (style.rangedValueSecondaryColor != Color.BLACK) {
        newStyle.rangedValueSecondaryColor = Color.TRANSPARENT
    }
    return newStyle
}
