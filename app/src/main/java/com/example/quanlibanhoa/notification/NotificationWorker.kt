package com.example.quanlibanhoa.notification


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.quanlibanhoa.R // Đảm bảo đúng package của R
import com.example.quanlibanhoa.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val invoiceDao = AppDatabase.getDatabase(appContext).invoiceDao()

    @Suppress("PrivatePropertyName")
    private val CHANNEL_ID = "reminder_channel"
    @Suppress("PrivatePropertyName")
    private val NOTIFICATION_ID = 101

    /**
     * Tạo và hiển thị Local Notification với âm thanh tùy chỉnh.
     */
    private fun showNotification(context: Context, @Suppress("SameParameterValue") title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 0. Kiểm tra quyền POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Nếu không có quyền, Worker không thể hiển thị thông báo.
                return
            }
        }

        // --- Chuẩn bị Âm thanh Tùy chỉnh ---
        // Đổi custom_sound thành tên file mp3/wav của bạn trong res/raw
        val soundUri: Uri =
            ("android.resource://" + context.packageName + "/" + R.raw.sound_notification).toUri()

        // 1. Tạo Notification Channel (Bắt buộc từ Android O trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Nhắc nhở đơn hàng", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Thông báo về các đơn hàng chưa hoàn thành"
                setSound(soundUri, null) // Set âm thanh tùy chỉnh (trên Channel)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Xây dựng thông báo
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Thay thế bằng icon của bạn
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        // Trên các phiên bản Android cũ hơn (trước O), set Sound trực tiếp trên Builder
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(soundUri)
        }

        // 3. Hiển thị thông báo
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // --- Xử lý ngày tháng theo múi giờ Việt Nam (UTC+7) ---
                val vietnamTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = vietnamTimeZone
                }

                val todayDateString = dateFormat.format(Date())

                // 1. Lấy TẤT CẢ các hóa đơn chưa hoàn thành
                val pendingInvoices = invoiceDao.getAllPendingInvoices()

                // 2. Lọc danh sách
                var todayCount = 0
                var lateCount = 0

                for (invoice in pendingInvoices) {
                    val invoiceDateString = dateFormat.format(invoice.date)

                    if (invoiceDateString == todayDateString) {
                        todayCount++
                    } else if (invoiceDateString < todayDateString) {
                        lateCount++
                    }
                }

                // 3. Hiển thị thông báo nếu cần
                if (todayCount > 0 || lateCount > 0) {
                    val title = "CẬP NHẬT ĐƠN HÀNG CHƯA HOÀN THÀNH"

                    var message = ""
                    if (lateCount > 0) {
                        message += "⚠️ Có $lateCount đơn hàng bị quá hạn."
                    }
                    if (todayCount > 0) {
                        message += "Hôm nay còn $todayCount đơn chưa giao/xử lý."
                    }

                    showNotification(applicationContext, title, message.trim())
                }

                Result.success()
            } catch (_: Exception) {
                Result.failure()
            }
        }
    }
}