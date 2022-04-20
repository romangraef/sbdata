package moe.nea89.sbdata.utils

import java.util.*


fun String.base64decode() = Base64.getDecoder().decode(this)

fun Double.interpolate(left: Double, right: Double): Double = left * (1 - this) + right * this
