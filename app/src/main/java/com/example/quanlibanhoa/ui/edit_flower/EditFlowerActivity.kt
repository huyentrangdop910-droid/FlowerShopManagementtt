package com.example.quanlibanhoa.ui.edit_flower

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quanlibanhoa.R
import com.example.quanlibanhoa.databinding.ActivityEditFlowerBinding
import com.example.quanlibanhoa.ui.home.viewmodel.FlowerViewModel
import com.example.quanlibanhoa.ui.home.viewmodel.FlowerViewModelFactory
import com.example.quanlibanhoa.ui.home.viewmodel.StateFlower
import com.example.quanlibanhoa.utils.loadImageFromUri

class EditFlowerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditFlowerBinding
    private var flowerId: String = ""
    private var selectedImageUri: Uri? = null
    private var oldImagePath: String? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    val flowerViewModel: FlowerViewModel by viewModels {
        FlowerViewModelFactory(
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditFlowerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Nhận dữ liệu từ Intent
        flowerId = intent.getStringExtra("flower_id").toString()
        val flowerName = intent.getStringExtra("flower_name")
        val flowerPriceIn = intent.getDoubleExtra("flower_price_in", 0.0)
        val flowerPriceOut = intent.getDoubleExtra("flower_price", 0.0)
        oldImagePath = intent.getStringExtra("flower_image_path")

        // Thiết lập dữ liệu vào các trường
        binding.edtTenHoa.setText(flowerName)
        binding.edtGiaNhap.setText(flowerPriceIn.toInt().toString())
        binding.edtGiaBan.setText(flowerPriceOut.toInt().toString())
        // Thiết lập hình ảnh nếu có
        binding.imgHoa.loadImageFromUri(oldImagePath)

        addEvent()
        observerData()
    }

    private fun observerData() {
        // observer
        flowerViewModel.editFlowerState.observe(this) { result ->
            if (result == StateFlower.IDLE) return@observe
            binding.btnSave.isEnabled = true
            binding.btnSave.alpha = 1f
            when (result) {
                StateFlower.EDIT_FLOWER_SUCCESS -> {
                    Toast.makeText(
                        applicationContext,
                        "Cập nhật hoa thành công.",
                        Toast.LENGTH_SHORT
                    ).show()
                    flowerViewModel.resetEditState()
                    finish()
                    slideOutActivity()
                }

                StateFlower.EDIT_FLOWER_ERROR -> {
                    Toast.makeText(
                        applicationContext,
                        "Có lỗi khi cập nhật hoa, vui lòng thử lại!",
                        Toast.LENGTH_SHORT
                    ).show()
                    flowerViewModel.resetEditState()
                }

                StateFlower.EDIT_FLOWER_INVALID_NAME -> {
                    Toast.makeText(
                        applicationContext,
                        "Tên hoa đã tồn tại, vui lòng đặt tên khác!",
                        Toast.LENGTH_SHORT
                    ).show()
                    flowerViewModel.resetEditState()
                }

                else -> {}

            }
        }
    }

    private fun addEvent() {
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    selectedImageUri = it
                    binding.imgHoa.loadImageFromUri(it)
                }
            }

        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnBack.setOnClickListener {
            finish()
            slideOutActivity()
        }

        binding.btnSave.setOnClickListener {
            val tenHoa = binding.edtTenHoa.text.toString().trim()
            val giaNhap = binding.edtGiaNhap.text.toString().toDoubleOrNull() ?: 0.0
            val giaBan = binding.edtGiaBan.text.toString().toDoubleOrNull() ?: 0.0

            if (tenHoa.isEmpty()) {
                binding.edtTenHoa.error = "Bạn chưa nhập tên hoa!"
                binding.edtTenHoa.requestFocus()
                return@setOnClickListener
            }
            if (giaNhap == 0.0) {
                binding.edtGiaNhap.error = "Bạn chưa nhập giá nhập!"
                binding.edtGiaNhap.requestFocus()
                return@setOnClickListener
            }
            if (giaBan == 0.0) {
                binding.edtGiaBan.error = "Bạn chưa nhập giá bán!"
                binding.edtGiaBan.requestFocus()
                return@setOnClickListener
            }
            if (giaBan < giaNhap) {
                binding.edtGiaBan.error = "Giá bán không thể nhỏ hơn giá nhập!"
                binding.edtGiaBan.requestFocus()
                return@setOnClickListener
            }
            binding.btnSave.isEnabled = false
            binding.btnSave.alpha = 0.8f

            flowerViewModel.editFlower(
                flowerId = flowerId.toInt(),
                tenHoa = tenHoa,
                giaNhap = giaNhap,
                giaBan = giaBan,
                newImageUri = selectedImageUri, // URI mới (có thể null)
                oldImagePath = oldImagePath, // Đường dẫn ảnh cũ (có thể null)
            )
        }
    }

    private fun slideOutActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14 trở lên
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                R.anim.slide_out_right,
                0
            )
        } else {
            // Android 13 trở xuống
            @Suppress("DEPRECATION")
            overridePendingTransition(
                R.anim.fade_in,
                R.anim.slide_out_right
            )
        }
    }

}