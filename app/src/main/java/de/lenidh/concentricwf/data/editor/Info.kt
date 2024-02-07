/*
 * Copyright (c) 2024 Moritz Heindl
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.lenidh.concentricwf.data.editor

import de.lenidh.concentricwf.R

val SELF_LICENSE_INFO = LicenseInfo(R.string.app_name, R.string.app_license_name, R.string.app_license_text)

val TP_LICENSE_INFOS = listOf(
    LicenseInfo(R.string.tp_rubik_fonts_name, R.string.tp_rubik_fonts_license_name, R.string.tp_rubik_fonts_license_text),
    LicenseInfo(R.string.tp_manrope_fonts_name, R.string.tp_manrope_fonts_license_name, R.string.tp_manrope_fonts_license_text),
    LicenseInfo(R.string.tp_ebgaramond_fonts_name, R.string.tp_ebgaramond_fonts_license_name, R.string.tp_ebgaramond_fonts_license_text),
    LicenseInfo(R.string.tp_chakrapetch_fonts_name, R.string.tp_chakrapetch_fonts_license_name, R.string.tp_chakrapetch_fonts_license_text),
)
data class LicenseInfo (
    val subjectId: Int,
    val licenseNameId: Int,
    val licenseTextId: Int,
)
