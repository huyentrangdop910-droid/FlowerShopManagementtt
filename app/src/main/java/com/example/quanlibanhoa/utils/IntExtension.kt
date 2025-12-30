package com.example.quanlibanhoa.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Suppress("DEPRECATION")
fun Int.toVNOnlyK(): String {
    // Tạo DecimalFormat với dấu phân cách là dấu chấm
    val symbols = DecimalFormatSymbols(Locale("vi", "VN")).apply {
        decimalSeparator = ','
        groupingSeparator = '.'
    }
    val formattedValue = DecimalFormat("#,###", symbols).format(this)  // Định dạng số với dấu chấm
    return "${formattedValue}k"
}