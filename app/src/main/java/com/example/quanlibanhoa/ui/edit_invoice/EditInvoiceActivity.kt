package com.example.quanlibanhoa.ui.edit_invoice

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quanlibanhoa.R
import com.example.quanlibanhoa.data.entity.Flower
import com.example.quanlibanhoa.data.entity.Invoice
import com.example.quanlibanhoa.data.entity.InvoiceDetail
import com.example.quanlibanhoa.data.entity.InvoiceWithDetails
import com.example.quanlibanhoa.databinding.ActivityEditInvoiceBinding
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

class EditInvoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditInvoiceBinding
    private lateinit var adapter: InvoiceFlowerAdapter
    private val selectedFlowers = mutableListOf<Flower>()
    private var invoiceWithDetails: InvoiceWithDetails? = null

    private var availableFlowers: List<Flower> = emptyList()

    private var totalQty = 0
    private var totalIncome = 0.0
    private var totalProfit = 0.0
    val flowerViewModel: FlowerViewModel by viewModels {
        FlowerViewModelFactory(
            this
        )
    }

    val invoiceViewModel: InvoiceViewModel by viewModels {
        InvoiceViewModelFactory(
            this
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        setupDataToEdit()
        addEvent()
        observerData()
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private fun setupDataToEdit() {
        invoiceWithDetails =
            intent.getSerializableExtra("invoice_data") as? InvoiceWithDetails
        if(invoiceWithDetails == null){
            Toast.makeText(applicationContext,
                "Không tìm thấy hóa đơn, vui lòng thử lại!",
                Toast.LENGTH_LONG
            ).show()
            finish()
            slideOutActivity()
        }
        invoiceWithDetails?.let {
            // hiển thị dữ liệu hóa đơn lên UI
            val invoice = it.invoice
            binding.edtCustomerName.setText(invoice.tenKhach)
            binding.edtCustomerPhone.setText(invoice.sdt ?: "")
            binding.edtCustomerAddress.setText(invoice.diaChi ?: "")
            binding.edtSale.setText(invoice.giamGia.toString())
            binding.edtNote.setText(invoice.ghiChu ?: "")
            // ngày giờ
            val localDateTime = invoice.date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            binding.edtDateInvoice.setText(localDateTime.toLocalDate().format(dateFormatter))
            // loại giao dịch
            when (invoice.loaiGiaoDich) {
                getString(R.string.action_transfer) -> binding.rbTransfer.isChecked = true
                getString(R.string.action_crash) -> binding.rbCash.isChecked = true
            }
            // status đơn hàng
            if (invoice.isCompleted) {
                binding.rbComplete.isChecked = true
            } else {
                binding.rbNotYet.isChecked = true
            }

            // đổ danh sách hoa
            selectedFlowers.clear()
            selectedFlowers.addAll(it.details.map { detail ->
                Flower(
                    tenHoa = detail.tenHoa,
                    soluong = detail.soLuong,
                    giaNhap = detail.giaNhap,
                    giaBan = detail.giaBan,
                    hinhAnh = detail.hinhAnh ?: ""
                )
            })
            adapter.notifyDataSetChanged()
            updateSummary()
        }
    }

    private fun setupRecyclerView() {
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
        binding.rvInvoiceFlowers.layoutManager = LinearLayoutManager(this)
        binding.rvInvoiceFlowers.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observerData() {
        flowerViewModel.flowerStateList.observe(this) {
            if (!it.isNullOrEmpty()) {
                availableFlowers = it
            }
        }

        invoiceViewModel.editInvoiceState.observe(this) { state ->
            if (state == StateInvoice.IDLE) return@observe
            binding.btnSaveInvoice.isEnabled = true
            binding.btnSaveInvoice.alpha = 1f
            when (state) {
                StateInvoice.EDIT_INVOICE_SUCCESS -> {
                    Toast.makeText(
                        this,
                        "Hóa đơn đã cập nhật!",
                        Toast.LENGTH_SHORT
                    ).show()
                    invoiceViewModel.resetEditState(0)
                    finish()
                    slideOutActivity()
                }

                StateInvoice.EDIT_INVOICE_ERROR -> {
                    Toast.makeText(
                        this,
                        "Cập nhật hóa đơn thất bại, vui lòng thử lại!",
                        Toast.LENGTH_SHORT
                    ).show()
                    invoiceViewModel.resetEditState(0)
                }

                else -> {}
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addEvent() {
        val myFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val invoiceDate = invoiceWithDetails?.invoice?.date ?: Date()
        var selectedDate = invoiceDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()


        // Chọn ngày
        binding.edtDateInvoice.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
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
            val dialog = FlowerPickerDialog(flowerViewModel.flowerStateList) { flower ->
                val existingFlower = selectedFlowers.find { it.tenHoa == flower.tenHoa }
                if (existingFlower != null) {
                    Toast.makeText(this, "Hoa ${flower.tenHoa} đã có trong hóa đơn!", Toast.LENGTH_SHORT).show()
                } else {
                    adapter.addFlower(flower.copy(soluong = 1))
                    updateSummary()
                }
            }
            dialog.show(supportFragmentManager, "flower_picker")
        }

        binding.edtSale.addTextChangedListener {
            updateSummary()
        }

        // Lưu hóa đơn (hoặc cập nhật)
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
                else -> false
            }

            if (customerName.isBlank()) {
                binding.edtCustomerName.error = "Tên khách hàng không được để trống!"
                binding.edtCustomerName.requestFocus()
                return@setOnClickListener
            }
            if (selectedFlowers.isEmpty()) {
                Toast.makeText(this, "Bạn chưa thêm hoa vào hóa đơn!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnSaveInvoice.isEnabled = false
            binding.btnSaveInvoice.alpha = 0.8f

            val date =
                Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())

            val oldId = invoiceWithDetails?.invoice?.id ?: 0

            val invoice = Invoice(
                id = oldId,
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
                date = date,
                createdAt = Date().time
            )

            val details = selectedFlowers.map {
                InvoiceDetail(
                    id = 0,
                    invoiceId = oldId,
                    hinhAnh = it.hinhAnh,
                    tenHoa = it.tenHoa,
                    soLuong = it.soluong,
                    giaNhap = it.giaNhap,
                    giaBan = it.giaBan
                )
            }

            invoiceViewModel.updateInvoiceWithDetail(invoice, details)

        }
        // thoát activity
        binding.btnBack.setOnClickListener {
            finish()
            slideOutActivity()
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

        android.app.AlertDialog.Builder(this)
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
