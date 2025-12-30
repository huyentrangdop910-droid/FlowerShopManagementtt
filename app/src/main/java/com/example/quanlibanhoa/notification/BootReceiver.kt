package com.example.quanlibanhoa.notification


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    // Danh sách các khung giờ cần đặt lại lịch
    @Suppress("PrivatePropertyName")
    private val EXACT_REMINDER_TIMES = listOf("20:00")

    override fun onReceive(context: Context, intent: Intent) {
        // Chỉ xử lý khi nhận đúng sự kiện khởi động
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            // 1. Tạo Scheduler
            val scheduler = AlarmScheduler(context)

            // 2. Đặt lại lịch cho tất cả các khung giờ
            EXACT_REMINDER_TIMES.forEach { time ->
                scheduler.scheduleExactAlarm(time)
            }

        }
    }
}