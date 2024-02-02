package de.lenidh.concentricwf.data.editor

import de.lenidh.concentricwf.R

val TP_LICENSE_INFOS = listOf(
    LicenseInfo(R.string.tp_rubik_fonts_name, R.string.tp_rubik_fonts_license_name, R.string.tp_rubik_fonts_license_text),
)
data class LicenseInfo (
    val subjectId: Int,
    val licenseNameId: Int,
    val licenseTextId: Int,
)
