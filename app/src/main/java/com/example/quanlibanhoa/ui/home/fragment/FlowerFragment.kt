package com.example.quanlibanhoa.ui.home.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlibanhoa.data.entity.Flower
import com.example.quanlibanhoa.databinding.FragmentFlowerBinding
import com.example.quanlibanhoa.ui.edit_flower.EditFlowerActivity
import com.example.quanlibanhoa.ui.home.HomeActivity
import com.example.quanlibanhoa.ui.home.adapter.FlowerListAdapter
import com.example.quanlibanhoa.ui.home.viewmodel.FlowerViewModel
import com.example.quanlibanhoa.ui.home.viewmodel.FlowerViewModelFactory
import com.example.quanlibanhoa.ui.home.viewmodel.StateFlower


class FlowerFragment : Fragment() {
    private var _binding: FragmentFlowerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FlowerListAdapter
    private var currentFlowers = listOf<Flower>()

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
        _binding = FragmentFlowerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // setup ryc view
        adapter = FlowerListAdapter(
            onEdit = { flower ->
                val intent = Intent(
                    context,
                    EditFlowerActivity::class.java
                ).apply {
                    putExtra("flower_id", flower.id.toString())
                    putExtra("flower_name", flower.tenHoa)
                    putExtra("flower_price_in", flower.giaNhap)
                    putExtra("flower_price", flower.giaBan)
                    putExtra("flower_image_path", flower.hinhAnh)
                }
                requireContext().startActivity(intent)
                (requireContext() as HomeActivity).slideNewActivity()
            },
            onDeleteSelected = { list -> showDeleteConfirmDialog(list) },
            onMultiSelectModeChanged = { isEnabled ->
                if (isEnabled) {
                    showDeleteToolbar() // Tự tạo hàm hiển thị Toolbar xóa
                } else {
                    hideDeleteToolbar() // Tự tạo hàm ẩn Toolbar xóa
                }
            },
            onSelectionCountChanged = { count ->
                updateDeleteToolbarText(count) // Cập nhật text "Xóa (N)"
            }
        )
        // Gán adapter cho RecyclerView
        binding.rvFlowerList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFlowerList.adapter = adapter

        observerData()
        setupSearchView()
    }

    private fun observerData() {
        flowerViewModel.flowerStateList.observe(viewLifecycleOwner) {
            it?.let {
                val sortedList = it.sortedByDescending { flower ->
                    flower.createdAt
                }
                currentFlowers = it
                adapter.setData(sortedList)
            }
        }
        flowerViewModel.deleteFlowerState.observe(viewLifecycleOwner) { result ->
            if (result == StateFlower.IDLE) return@observe

            when (result) {
                StateFlower.DELETE_FLOWER_SUCCESS -> {
                    Toast.makeText(
                        requireContext(),
                        "Xóa thành công.",
                        Toast.LENGTH_SHORT
                    ).show()
                    flowerViewModel.resetDeleteState()
                }

                StateFlower.DELETE_FLOWER_ERROR -> {
                    Toast.makeText(
                        requireContext(),
                        "Có lỗi khi xóa hoa, vui lòng thử lại!",
                        Toast.LENGTH_SHORT
                    ).show()
                    flowerViewModel.resetDeleteState()
                }

                else -> {}
            }
        }
    }

    private fun showDeleteConfirmDialog(flowersToDelete: List<Flower>) {
        if (flowersToDelete.isEmpty()) return

        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa ${flowersToDelete.size} mục đã chọn?")
            .setPositiveButton("Xóa") { _, _ ->
                // xóa list flower
                flowerViewModel.deleteFlowers(flowersToDelete)
                // Thoát chế độ chọn sau khi gửi lệnh xóa
                adapter.clearSelection()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showDeleteToolbar() {
        // Hiển thị thanh công cụ xóa
        binding.deleteToolbar.visibility = View.VISIBLE

        binding.btnSelectAll.setOnClickListener {
            adapter.selectAllFlowers()
        }

        // Đảm bảo nút "Bỏ chọn tất cả" gọi hàm clearSelection của Adapter
        binding.btnClearSelection.setOnClickListener {
            adapter.clearSelection()
        }

        // Đảm bảo nút "Xóa" gọi hàm hiển thị hộp thoại xác nhận xóa
        binding.btnConfirmDelete.setOnClickListener {
            showDeleteConfirmDialog(adapter.selectedFlowers.toList())
        }
    }

    // Hàm ẩn thanh công cụ xóa
    private fun hideDeleteToolbar() {

        // Ẩn thanh công cụ xóa
        binding.deleteToolbar.visibility = View.GONE

        // Đảm bảo không còn lựa chọn nào
        updateDeleteToolbarText(0)
    }

    @SuppressLint("SetTextI18n")
    private fun updateDeleteToolbarText(count: Int) {
        // Cập nhật Text của nút Xóa (ví dụ: "Xóa (3)")
        binding.btnConfirmDelete.text = "Xóa ($count)"

        // Đảm bảo nút Xóa bị vô hiệu hóa nếu không có mục nào được chọn
        binding.btnConfirmDelete.isEnabled = count > 0

        // *TÙY CHỌN*: Nếu count bằng 0, tự động thoát chế độ chọn
        // (Lưu ý: Logic này đã được xử lý trong Adapter, nhưng bạn có thể dùng nó để đảm bảo)
        if (count == 0 && adapter.isMultiSelectMode) {
            adapter.setMultiSelectMode(false)
        }
    }

    private fun setupSearchView() {
        // 1. Theo dõi thay đổi văn bản và xử lý nút Clear
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Hiển thị/Ẩn nút Clear
                binding.ivClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE

                // Lọc danh sách ngay khi văn bản thay đổi
                filterList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 2. Xử lý khi nhấn nút Clear (X)
        binding.ivClear.setOnClickListener {
            binding.etSearch.setText("") // Xóa text
            binding.etSearch.requestFocus() // Giữ focus và bàn phím để người dùng tiếp tục gõ
            // filterList sẽ được gọi trong TextWatcher
        }

        // 3. Xử lý khi người dùng nhấn "Search" trên bàn phím
        binding.etSearch.setOnEditorActionListener { v: TextView?, actionId: Int, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // Ẩn bàn phím và bỏ focus
                hideKeyboard(v)
                binding.etSearch.clearFocus()
                // filterList đã được gọi trong onTextChanged
                return@setOnEditorActionListener true
            }
            false
        }

        // 4. Khôi phục danh sách đầy đủ khi EditText mất focus
        binding.etSearch.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && binding.etSearch.text.isNullOrBlank()) {
                // Nếu EditText mất focus VÀ không có nội dung tìm kiếm, hiển thị lại list đầy đủ
                adapter.setData(currentFlowers)
            }
        }
    }

    // Hàm tiện ích để ẩn bàn phím
    private fun hideKeyboard(view: TextView?) {
        val imm =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun filterList(query: String?) {
        // Sử dụng currentFlowers đã được sắp xếp
        val listToSearch = currentFlowers

        val result = if (query.isNullOrBlank()) {
            // Khi query rỗng (ngay sau khi mở hoặc clear), hiển thị list đầy đủ
            listToSearch
        } else {
            listToSearch.filter {
                it.tenHoa.contains(query, ignoreCase = true)
            }
        }

        adapter.setData(result)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}