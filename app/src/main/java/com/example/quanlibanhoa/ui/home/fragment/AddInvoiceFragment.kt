package com.example.quanlibanhoa.ui.home.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlibanhoa.R
import com.example.quanlibanhoa.data.entity.Flower
import com.example.quanlibanhoa.data.entity.Invoice
import com.example.quanlibanhoa.data.entity.InvoiceDetail
import com.example.quanlibanhoa.databinding.FragmentAddInvoiceBinding
import com.example.quanlibanhoa.ui.home.adapter.InvoiceFlowerAdapter
import com.example.quanlibanhoa.ui.home.dialog.FlowerPickerDialog
import com.example.quanlibanhoa.ui.home.viewmodel.FlowerViewModel
import com.example.quanlibanhoa.ui.home.viewmodel.FlowerViewModelFactory
import com.example.quanlibanhoa.ui.home.viewmodel.InvoiceViewModel
import com.example.quanlibanhoa.ui.home.viewmodel.InvoiceViewModelFactory
import com.example.quanlibanhoa.ui.home.viewmodel.StateInvoice
import com.example.quanlibanhoa.utils.toVNOnlyK
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class AddInvoiceFragment : Fragment() {

    private var _binding: FragmentAddInvoiceBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: InvoiceFlowerAdapter
    private val selectedFlowers = mutableListOf<Flower>()

    private var availableFlowers: List<Flower> = emptyList()
    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate = LocalDate.now()

    // các biến lưu state add hiện tại
    private var totalQty = 0
    private var totalIncome = 0.0
    private var totalProfit = 0.0

    val invoiceViewModel: InvoiceViewModel by activityViewModels {
        InvoiceViewModelFactory(
            requireContext()
        )
    }

    val flowerViewModel: FlowerViewModel by activityViewModels {
        FlowerViewModelFactory(
            requireContext()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddInvoiceBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup adapter
        adapter = InvoiceFlowerAdapter(
            selectedFlowers,
            onDelete = { flower ->
                adapter.removeFlower(flower)
                updateSummary()
            },
            onEdit = { flower ->
                showEditPriceDialog(flower)
            },
            onQuantityChanged = {
                updateSummary()
            }
        )

        binding.rvInvoiceFlowers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInvoiceFlowers.adapter = adapter

        addEvent()
        observerData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private fun observerData() {
        // lấy ds hoa
        flowerViewModel.flowerStateList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                availableFlowers = it
            }
        }
        invoiceViewModel.addInvoiceState.observe(viewLifecycleOwner) { state ->
            if (state == StateInvoice.IDLE) return@observe
            binding.btnSaveInvoice.isEnabled = true
            binding.btnSaveInvoice.alpha = 1f
            when (state) {
                StateInvoice.ADD_INVOICE_SUCCESS -> {
                    Toast.makeText(
                        requireContext(),
                        "Hóa đơn đã lưu!",
                        Toast.LENGTH_SHORT
                    ).show()
                    selectedFlowers.clear()
                    adapter.notifyDataSetChanged()
                    clearForm()
                    invoiceViewModel.resetAddState()
                }

                StateInvoice.ADD_INVOICE_ERROR -> {
                    Toast.makeText(
                        requireContext(),
                        "Lưu hóa đơn thất bại, vui lòng thử lại!",
                        Toast.LENGTH_SHORT
                    ).show()
                    invoiceViewModel.resetAddState()
                }

                else -> {}
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addEvent() {
        val myFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        binding.edtDateInvoice.setText(selectedDate.format(myFormat))


        // chọn ngày
        binding.edtDateInvoice.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    binding.edtDateInvoice.setText(selectedDate.format(myFormat))
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            )
            datePicker.show()
        }

        // Chọn hoa
        binding.btnAddFlower.setOnClickListener {
            val dialog = FlowerPickerDialog(
                flowerViewModel.flowerStateList
            ) { flower ->
                // Kiểm tra xem Flower đã có trong danh sách selectedFlowers chưa
                val existingFlower = selectedFlowers.find { it.tenHoa == flower.tenHoa }

                if (existingFlower != null) {
                    // Nếu hoa đã tồn tại, thông báo cho người dùng
                    Toast.makeText(
                        requireContext(),
                        "Hoa ${flower.tenHoa} đã có trong hóa đơn!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Nếu chưa tồn tại, thêm hoa vào danh sách
                    adapter.addFlower(flower.copy(soluong = 1)) // mặc định số lượng 1
                    updateSummary()
                }
            }
            dialog.show(parentFragmentManager, "flower_picker")
        }

        binding.edtSale.addTextChangedListener {
            updateSummary()
        }

        // Lưu hóa đơn
        binding.btnSaveInvoice.setOnClickListener {
            val customerName = binding.edtCustomerName.text.toString().trim()
            val phone = binding.edtCustomerPhone.text.toString().trim()
            val address = binding.edtCustomerAddress.text.toString().trim()
            val sale = binding.edtSale.text.toString().toIntOrNull() ?: 0
            val note = binding.edtNote.text.toString().trim()
            val paymentMethod = when (binding.rgPaymentMethod.checkedRadioButtonId) {
                binding.rbTransfer.id -> binding.rbTransfer.text.toString()
                binding.rbCash.id -> binding.rbCash.text.toString()
                else -> getString(R.string.action_crash)
            }

            val isComplete = when (binding.rgOrderStatus.checkedRadioButtonId) {
                binding.rbNotYet.id -> false
                binding.rbComplete.id -> true
                else -> {false}
            }

            if (customerName.isBlank()) {
                binding.edtCustomerName.error = "Tên khách hàng không được để trống!"
                binding.edtCustomerName.requestFocus()
                return@setOnClickListener
            }
            if(selectedFlowers.isEmpty()){
                Toast.makeText(
                    requireContext(),
                    "Bạn chưa thêm hoa vào hóa đơn!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            binding.btnSaveInvoice.isEnabled = false
            binding.btnSaveInvoice.alpha = 0.8f
            // ⚡ Tạo đối tượng Date chỉ chứa ngày/tháng/năm (giờ = 0)
            val date =
                Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())

            //tạo hóa đơn
            val invoice = Invoice(
                tenKhach = customerName,
                sdt = phone,
                diaChi = address,
                giamGia = sale,
                tongSoLuong = totalQty,
                tongTienThu = totalIncome,
                tongLoiNhuan = totalProfit,
                ghiChu = note,
                loaiGiaoDich = paymentMethod,
                isCompleted = isComplete,
                date = date
            )
            // tạo danh sách chi tiết hóa đơn
            val details = selectedFlowers.map {
                InvoiceDetail(
                    invoiceId = 0,
                    hinhAnh = it.hinhAnh,
                    tenHoa = it.tenHoa,
                    soLuong = it.soluong,
                    giaNhap = it.giaNhap,
                    giaBan = it.giaBan
                )
            }
            invoiceViewModel.addInvoiceWithDetail(invoice, details)
        }
    }

    // popup sửa giá
    @SuppressLint("NotifyDataSetChanged")
    private fun showEditPriceDialog(flower: Flower) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_price, null)
        val edtPriceIn = dialogView.findViewById<android.widget.EditText>(R.id.edtGiaNhap)
        val edtPriceOut = dialogView.findViewById<android.widget.EditText>(R.id.edtGiaBan)

        edtPriceIn.setText(flower.giaNhap.toInt().toString())
        edtPriceOut.setText(flower.giaBan.toInt().toString())

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Sửa giá ${flower.tenHoa}")
            .setView(dialogView)
            .setPositiveButton("Lưu") { d, _ ->
                flower.giaNhap = edtPriceIn.text.toString().toDoubleOrNull() ?: flower.giaNhap
                flower.giaBan = edtPriceOut.text.toString().toDoubleOrNull() ?: flower.giaBan
                adapter.notifyDataSetChanged()
                updateSummary()
                d.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // tính tổng
    @SuppressLint("SetTextI18n")
    private fun updateSummary() {
        val sale = binding.edtSale.text.toString().toIntOrNull() ?: 0

        totalQty = selectedFlowers.sumOf { it.soluong }
        totalIncome = selectedFlowers.sumOf { it.soluong * it.giaBan } - sale
        totalProfit = selectedFlowers.sumOf { it.soluong * (it.giaBan - it.giaNhap) } - sale

        binding.txtCount.text = "\uD83C\uDF38 Số lượng: $totalQty bó"
        binding.txtTotalIn.text = "\uD83D\uDCB0 Tổng thu: ${totalIncome.toInt().toVNOnlyK()}"
        binding.txtTotalProfit.text = "\uD83D\uDCB0 Tổng lợi nhuận: ${totalProfit.toInt().toVNOnlyK()}"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun clearForm() {
        binding.scrollViewAddInvoice.scrollTo(0,0)
        binding.edtCustomerName.setText("")
        binding.edtCustomerPhone.setText("")
        binding.edtCustomerAddress.setText("")
        binding.edtSale.setText("")
        // set default date
        val myFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        selectedDate = LocalDate.now()

        binding.edtDateInvoice.setText(selectedDate.format(myFormat))
        // reset summaries
        totalQty = 0
        totalIncome = 0.0
        totalProfit = 0.0
        binding.txtCount.text = "Tổng: 0 bó"
        binding.txtTotalIn.text = "Tổng thu: 0k"
        binding.txtTotalProfit.text = "Tổng lợi nhuận: 0k"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
