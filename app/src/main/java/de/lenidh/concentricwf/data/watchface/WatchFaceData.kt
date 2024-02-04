package de.lenidh.concentricwf.data.watchface

/**
 * Represents all data needed to render the watch face.
 */
data class WatchFaceData(
    val userStyle: WatchFaceUserStyle = WatchFaceUserStyle.getColorStyleConfig(),
)
