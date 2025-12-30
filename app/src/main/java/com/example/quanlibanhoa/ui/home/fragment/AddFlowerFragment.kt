package com.example.quanlibanhoa.ui.home.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.quanlibanhoa.R
import com.example.quanlibanhoa.databinding.FragmentAddFlowerBinding
import com.example.quanlibanhoa.ui.home.viewmodel.FlowerViewModel
import com.example.quanlibanhoa.ui.home.viewmodel.FlowerViewModelFactory
import com.example.quanlibanhoa.ui.home.viewmodel.StateFlower
import com.example.quanlibanhoa.utils.loadImageFromUri


class AddFlowerFragment : Fragment() {

    private var _binding: FragmentAddFlowerBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var imagePath: String? = null
    val flowerViewModel: FlowerViewModel by activityViewModels {
        FlowerViewModelFactory(
            requireContext()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddFlowerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addEvent()
        observerData()
    }

    private fun observerData() {
        // observer
        flowerViewModel.addFlowerState.observe(viewLifecycleOwner) { result ->
            if (result == StateFlower.IDLE) return@observe
            binding.btnSave.isEnabled = true
            binding.btnSave.alpha = 1f
            when (result) {
                StateFlower.ADD_FLOWER_SUCCESS -> {
                    Toast.makeText(
                        requireContext(),
                        "Thêm hoa thành công.",
                        Toast.LENGTH_SHORT
                    ).show()
                    imagePath = null
                    clearForm()
                    flowerViewModel.resetAddState()
                }

                StateFlower.ADD_FLOWER_ERROR -> {
                    Toast.makeText(
                        requireContext(),
                        "Có lỗi khi thêm hoa, vui lòng thử lại!",
                        Toast.LENGTH_SHORT
                    ).show()
                    flowerViewModel.resetAddState()
                }

                StateFlower.ADD_FLOWER_INVALID_NAME -> {
                    Toast.makeText(
                        requireContext(),
                        "Tên hoa đã tồn tại, vui lòng đặt tên khác!",
                        Toast.LENGTH_SHORT
                    ).show()
                    flowerViewModel.resetAddState()
                }

                else -> {}

            }
        }
    }

    private fun addEvent() {
        val pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.imgHoa.loadImageFromUri(it)
            }
        }

        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
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

            flowerViewModel.addFlower(
                tenHoa = tenHoa,
                giaNhap = giaNhap,
                giaBan = giaBan,
                imageUri = selectedImageUri
            )
        }
    }

    fun clearForm() {
        binding.scrollViewAddFlower.scrollTo(0,0)
        binding.edtTenHoa.text.clear()
        binding.edtGiaNhap.text.clear()
        binding.edtGiaBan.text.clear()
        binding.imgHoa.setImageResource(R.drawable.ic_photo)
        selectedImageUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}