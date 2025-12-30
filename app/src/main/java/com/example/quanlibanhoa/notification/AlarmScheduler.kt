package com.example.quanlibanhoa.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*
import java.util.concurrent.TimeUnit

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Kiểm tra xem ứng dụng có quyền đặt báo thức chính xác hay không (API 31+).
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Mặc định là có trên các phiên bản cũ hơn
        }
    }

    fun scheduleExactAlarm(timeString: String) {
        // 1. Kiểm tra quyền BẮT BUỘC
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms()) {
            // Nếu không có quyền, chúng ta KHÔNG ĐẶT BÁO THỨC CHÍNH XÁC.
            // Thông báo cho người dùng hoặc sử dụng WorkManager (lập lịch không chính xác) làm dự phòng.
            // Việc này sẽ được xử lý trong HomeActivity để yêu cầu quyền từ người dùng.
            return
        }

        val parts = timeString.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            // Đặt múi giờ VN cho việc tính toán (nên đảm bảo đồng bộ với Worker)
            timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            if (timeInMillis <= System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_TIME_SLOT", timeString)
            action = "com.yourpackage.ACTION_REMINDER_$timeString"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timeString.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Chỉ gọi setExactAndAllowWhileIdle() khi đã xác nhận hoặc ở API cũ hơn
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (_: SecurityException) {
            // Trường hợp dự phòng nếu việc kiểm tra thất bại và vẫn bị từ chối
            // Bạn nên ghi log lỗi ở đây.
        }
    }
}