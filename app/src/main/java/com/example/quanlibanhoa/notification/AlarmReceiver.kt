package com.example.quanlibanhoa.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val timeString = intent.getStringExtra("EXTRA_TIME_SLOT")
        if (timeString == null) return

        // 1. Kích hoạt Worker để xử lý database và tạo thông báo (chạy 1 lần)
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)

        // 2. Đặt lịch lại cho 24 giờ sau
        val scheduler = AlarmScheduler(context)
        scheduler.scheduleExactAlarm(timeString)
    }
}