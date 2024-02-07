package de.lenidh.concentricwf.utils

import android.content.Context
import android.graphics.RectF
import androidx.core.content.res.ResourcesCompat
import androidx.wear.watchface.CanvasComplicationFactory
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import de.lenidh.concentricwf.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Information needed for complications.
// Creates bounds for the locations of both right and left complications. (This is the
// location from 0.0 - 1.0.)
// Both left and right complications use the same top and bottom bounds.

const val COMPLICATION_OFFSET = 0.025F
const val COMPLICATION_RADIUS = 0.1F
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

fun computeComplicationAngle(i: Int): Float {
    return (PI + (2 - i) * COMPLICATION_ANGLE).toFloat()
}

private fun computeComplicationLeftBound(i: Int): Float {
    return (0.5F - COMPLICATION_RADIUS - COMPLICATION_OFFSET) * cos(computeComplicationAngle(i)) - COMPLICATION_RADIUS + 0.5F
}

private fun computeComplicationRightBound(i: Int): Float {
    return (0.5F - COMPLICATION_RADIUS - COMPLICATION_OFFSET) * cos(computeComplicationAngle(i)) + COMPLICATION_RADIUS + 0.5F
}

private fun computeComplicationTopBound(i: Int): Float {
    return (0.5F - COMPLICATION_RADIUS - COMPLICATION_OFFSET) * sin(computeComplicationAngle(i)) - COMPLICATION_RADIUS + 0.5F
}

private fun computeComplicationBottomBound(i: Int): Float {
    return (0.5F - COMPLICATION_RADIUS - COMPLICATION_OFFSET) * sin(computeComplicationAngle(i)) + COMPLICATION_RADIUS + 0.5F
}

// Unique IDs for each complication. The settings activity that supports allowing users
// to select their complication data provider requires numbers to be >= 0.
internal const val COMPLICATION_1_ID = 100
internal const val COMPLICATION_2_ID = 101
internal const val COMPLICATION_3_ID = 102
internal const val COMPLICATION_4_ID = 103
internal const val COMPLICATION_5_ID = 104

/**
 * Represents the unique id associated with a complication and the complication types it supports.
 */
sealed class ComplicationConfig(val id: Int, val supportedTypes: List<ComplicationType>) {
    data object Comp1 : ComplicationConfig(
        COMPLICATION_1_ID, listOf(
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        )
    )

    data object Comp2 : ComplicationConfig(
        COMPLICATION_2_ID, listOf(
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        )
    )

    data object Comp3 : ComplicationConfig(
        COMPLICATION_3_ID, listOf(
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        )
    )

    data object Comp4 : ComplicationConfig(
        COMPLICATION_4_ID, listOf(
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        )
    )

    data object Comp5 : ComplicationConfig(
        COMPLICATION_5_ID, listOf(
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        )
    )
}

// Utility function that initializes default complication slots (left and right).
fun createComplicationSlotManager(
    context: Context,
    currentUserStyleRepository: CurrentUserStyleRepository,
    drawableId: Int = R.drawable.complication_style
): ComplicationSlotsManager {
    val defaultCanvasComplicationFactory = CanvasComplicationFactory { watchState, listener ->
        val drawable = ComplicationDrawable.getDrawable(context, drawableId)!!
        ResourcesCompat.getFont(context, R.font.rubik_regular)?.let {
            drawable.activeStyle.setTextTypeface(it)
            drawable.activeStyle.setTitleTypeface(it)
            drawable.ambientStyle.setTextTypeface(it)
            drawable.ambientStyle.setTitleTypeface(it)
        }
        CanvasComplicationDrawable(drawable, watchState, listener)
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
