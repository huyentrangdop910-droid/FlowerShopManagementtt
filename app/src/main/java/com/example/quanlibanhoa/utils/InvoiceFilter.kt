package com.example.quanlibanhoa.utils

import com.example.quanlibanhoa.data.entity.InvoiceWithDetails
import java.util.*

object InvoiceFilter {

    private val vietnamTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
    private const val MILLIS_PER_DAY = 86_400_000L

    // 🔧 Chuẩn hóa ngày về 00:00:00.000 theo múi giờ Việt Nam (tối ưu hiệu năng)
    private fun normalizeDateToStartOfDayFast(date: Date): Long {
        val offset = vietnamTimeZone.getOffset(date.time)
        return (date.time + offset) / MILLIS_PER_DAY * MILLIS_PER_DAY - offset
    }

    // 🔧 Lấy khoảng thời gian bắt đầu & kết thúc theo kỳ (ngày / tuần / tháng / năm)
    private fun getStartAndEndDates(period: String): Pair<Date, Date> {
        val calendar = Calendar.getInstance(vietnamTimeZone)

        return when (period) {
            "today" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.time

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.time
                Pair(start, end)
            }

            "thisWeek" -> {
                calendar.firstDayOfWeek = Calendar.MONDAY
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.time

                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val end = calendar.time
                Pair(start, end)
            }

            "thisMonth" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.time

                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val end = calendar.time
                Pair(start, end)
            }

            "thisYear" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.time

                calendar.add(Calendar.YEAR, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val end = calendar.time
                Pair(start, end)
            }

            else -> throw IllegalArgumentException("Invalid period: $period")
        }
    }

    // 🔹 Lọc hóa đơn chưa đến hạn
    fun filterNotYetDue(invoices: List<InvoiceWithDetails>): List<InvoiceWithDetails> {
        val now = normalizeDateToStartOfDayFast(Date())
        return invoices.asSequence()
            .filter {
                !it.invoice.isCompleted && normalizeDateToStartOfDayFast(it.invoice.date) > now
            }
            .sortedBy { it.invoice.date }
            .toList()
    }

    // 🔹 Lọc hóa đơn đến hạn hôm nay
    fun filterDueToday(invoices: List<InvoiceWithDetails>): List<InvoiceWithDetails> {
        val now = normalizeDateToStartOfDayFast(Date())
        return invoices.asSequence()
            .filter {
                !it.invoice.isCompleted && normalizeDateToStartOfDayFast(it.invoice.date) == now
            }
            .sortedBy { it.invoice.date }
            .toList()
    }

    // 🔹 Lọc hóa đơn quá hạn
    fun filterOverdue(invoices: List<InvoiceWithDetails>): List<InvoiceWithDetails> {
        val now = normalizeDateToStartOfDayFast(Date())
        return invoices.asSequence()
            .filter {
                !it.invoice.isCompleted && normalizeDateToStartOfDayFast(it.invoice.date) < now
            }
            .sortedBy { it.invoice.date }
            .toList()
    }

    // 🔹 Lọc hóa đơn hoàn thành theo kỳ (ngày / tuần / tháng / năm)
    fun filterInvoices(invoices: List<InvoiceWithDetails>, period: String): List<InvoiceWithDetails> {
        val (start, end) = getStartAndEndDates(period)
        val startMs = start.time
        val endMs = end.time
        val offset = vietnamTimeZone.getOffset(startMs)

        return invoices.asSequence()
            .filter { inv ->
                if (!inv.invoice.isCompleted) return@filter false
                val dateMs = inv.invoice.date.time + offset
                dateMs in startMs..endMs
            }
            .sortedByDescending { it.invoice.createdAt }
            .toList()
    }
}
