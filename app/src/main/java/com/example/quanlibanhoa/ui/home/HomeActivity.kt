
package com.example.quanlibanhoa.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.quanlibanhoa.R
import com.example.quanlibanhoa.databinding.ActivityHomeBinding
import com.example.quanlibanhoa.notification.AlarmScheduler
import com.example.quanlibanhoa.ui.home.fragment.AddFlowerFragment
import com.example.quanlibanhoa.ui.home.fragment.AddInvoiceFragment
import com.example.quanlibanhoa.ui.home.fragment.ExceptedInvoiceFragment
import com.example.quanlibanhoa.ui.home.fragment.FlowerFragment
import com.example.quanlibanhoa.ui.home.fragment.InvoiceFragment
import com.example.quanlibanhoa.ui.home.fragment.ReportFragment
import com.example.quanlibanhoa.ui.home.viewmodel.FlowerViewModel
import com.example.quanlibanhoa.ui.home.viewmodel.FlowerViewModelFactory
import com.example.quanlibanhoa.ui.home.viewmodel.InvoiceViewModel
import com.example.quanlibanhoa.ui.home.viewmodel.InvoiceViewModelFactory
import kotlin.getValue
import androidx.core.content.edit
import androidx.core.net.toUri

class HomeActivity : AppCompatActivity() {
    // Các khung giờ chính xác bạn muốn kiểm tra (8h)
    @Suppress("PrivatePropertyName")
    private val EXACT_REMINDER_TIMES = listOf("20:00")
    @Suppress("PrivatePropertyName")
    private val REQUEST_SCHEDULE_EXACT_ALARM = 123
    // Khởi tạo Activity Result Launcher cho quyền POST_NOTIFICATIONS
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Quyền thông báo đã được cấp, tiến hành đặt lịch
            scheduleAllDailyExactReminders()
        } else {
            Toast.makeText(
                this,
                "Không có quyền thông báo, các nhắc nhở sẽ không xuất hiện.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private var activeFragment: Fragment? = null

    private lateinit var binding: ActivityHomeBinding

    @Suppress("unused")
    val flowerViewModel: FlowerViewModel by viewModels {
        FlowerViewModelFactory(
            this
        )
    }

    @Suppress("unused")
    val invoiceViewModel: InvoiceViewModel by viewModels {
        InvoiceViewModelFactory(
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupDefaultFragment(savedInstanceState)
        setupBottomNavApp()

        // BẮT ĐẦU quy trình kiểm tra quyền
        handleNotificationPermissions()

        // BẮT ĐẦU: Kiểm tra và Yêu cầu quyền báo thức chính xác
        checkAndRequestExactAlarmPermission()

        // Đặt lịch cho tất cả 4 khung giờ
//        scheduleAllDailyExactReminders()
    }

    override fun onResume() {
        super.onResume()
        // Kích hoạt việc đặt lịch nếu nó chưa từng được đặt và quyền đã có.
        scheduleAllDailyExactReminders()
    }

    // Thêm vào HomeActivity
    @SuppressLint("BatteryLife")
    private fun checkAndRequestIgnoreBatteryOptimization() {
        // 1. Lấy PowerManager
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        // 2. Kiểm tra xem ứng dụng có đang bị tối ưu hóa pin không
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {

            // Hiển thị giải thích trước khi chuyển hướng (tùy chọn)
            Toast.makeText(
                this,
                "Để đảm bảo nhắc nhở hằng ngày hoạt động chính xác khi ứng dụng đóng, vui lòng cho phép ứng dụng 'Không bị hạn chế' pin.",
                Toast.LENGTH_LONG
            ).show()

            // 3. Chuyển người dùng đến màn hình cấp quyền
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            ).apply {
                data = "package:$packageName".toUri()
            }
            startActivity(intent)
        }
    }

    private fun handleNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            // 1. Kiểm tra và yêu cầu quyền POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Nếu đã có quyền POST_NOTIFICATIONS, chuyển sang kiểm tra quyền EXACT_ALARM
                checkAndRequestExactAlarmPermission()
                checkAndRequestIgnoreBatteryOptimization()
            }
        } else {
            // Dưới API 33, quyền thông báo mặc định có. Chỉ cần kiểm tra EXACT_ALARM
            checkAndRequestExactAlarmPermission()
            checkAndRequestIgnoreBatteryOptimization()
        }
    }

    // Hàm mới: Kiểm tra và yêu cầu quyền
    private fun checkAndRequestExactAlarmPermission() {
        val scheduler = AlarmScheduler(applicationContext)

        // Chỉ thực hiện kiểm tra này trên Android 12 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!scheduler.canScheduleExactAlarms()) {
                // Hiển thị thông báo giải thích lý do cần quyền
                Toast.makeText(
                    this,
                    "Ứng dụng cần quyền Đặt Báo Thức Chính Xác để gửi nhắc nhở đúng giờ.",
                    Toast.LENGTH_LONG
                ).show()

                // Chuyển người dùng đến màn hình cấp quyền
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                @Suppress("DEPRECATION")
                startActivityForResult(intent, REQUEST_SCHEDULE_EXACT_ALARM)
            }
        }
    }

    // Xử lý kết quả trả về từ màn hình Cài đặt (tùy chọn)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SCHEDULE_EXACT_ALARM) {
            val scheduler = AlarmScheduler(applicationContext)
            if (scheduler.canScheduleExactAlarms()) {
                // Nếu người dùng vừa cấp quyền, hãy đảm bảo đặt lịch
                scheduleAllDailyExactReminders()
            } else {
                Toast.makeText(
                    this,
                    "Không thể đảm bảo nhắc nhở chính xác vì thiếu quyền.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Hàm đặt lịch báo thức (chạy sau khi quyền đã được xử lý)
    private fun scheduleAllDailyExactReminders() {
        // Chỉ đặt lịch 1 lần duy nhất khi app chạy
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isScheduled = prefs.getBoolean("is_daily_alarm_set", false)
        val scheduler = AlarmScheduler(applicationContext)


        if (!isScheduled) {
            // 2. Phải có quyền Đặt Báo Thức Chính Xác (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!scheduler.canScheduleExactAlarms()) {
                    // Chưa có quyền, không đặt lịch lần đầu.
                    // Logic checkAndRequestExactAlarmPermission sẽ lo việc này.
                    return
                }
            }

            // Đặt lịch
            EXACT_REMINDER_TIMES.forEach { time ->
                scheduler.scheduleExactAlarm(time)
            }

            prefs.edit { putBoolean("is_daily_alarm_set", true) }
            Toast.makeText(
                this,
                "Đã đặt lịch nhắc nhở chính xác hằng ngày.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupDefaultFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val dashboardFragment = FlowerFragment()
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frame_layout_home,
                    dashboardFragment,
                    FlowerFragment::class.java.simpleName
                )
                .commit()
            activeFragment = dashboardFragment
        } else {
            // lấy lại fragment đang active sau khi xoay
            activeFragment = supportFragmentManager.fragments.find { !it.isHidden }
        }
    }

    private fun setupBottomNavApp() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavApp) { view, insets ->
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, 0)
            insets
        }

        binding.bottomNavApp.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.flower -> switchFragment(FlowerFragment())
                R.id.add_flower -> switchFragment(AddFlowerFragment())
                R.id.add_invoice -> switchFragment(AddInvoiceFragment())
                R.id.excepted_invoice -> switchFragment(ExceptedInvoiceFragment())
                R.id.invoice -> switchFragment(InvoiceFragment())
                R.id.report -> switchFragment(ReportFragment())
                else -> {}
            }
            true
        }
    }

    private fun switchFragment(newFragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        // ẩn fragment hiện tại nếu có
        activeFragment?.let { fragmentTransaction.hide(it) }

        // kiểm tra fragment mới đã tồn tại chưa
        var fragment =
            fragmentManager.findFragmentByTag(newFragment::class.java.simpleName)

        // newFragment chưa có
        if (fragment == null) {
            fragment = newFragment
            fragmentTransaction.add(
                R.id.frame_layout_home,
                fragment,
                fragment::class.java.simpleName
            )
        } else {
            // đã ồn tại fragment
            fragmentTransaction.show(fragment)
        }

        fragmentTransaction.commit()
        activeFragment = fragment
    }
    fun slideNewActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14 trở lên
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                R.anim.slide_in_right,
                0
            )
        } else {
            // Android 13 trở xuống
            @Suppress("DEPRECATION")
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.fade_out
            )
        }
    }
}