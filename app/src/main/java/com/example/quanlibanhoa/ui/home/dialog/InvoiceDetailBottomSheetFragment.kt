package com.example.quanlibanhoa.ui.home.dialog

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlibanhoa.R
import com.example.quanlibanhoa.data.entity.InvoiceWithDetails
import com.example.quanlibanhoa.ui.home.adapter.InvoiceDetailAdapter
import com.example.quanlibanhoa.utils.toFormattedString
import com.example.quanlibanhoa.utils.toVNOnlyK
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class InvoiceDetailBottomSheetFragment(
    private val fullInvoice: InvoiceWithDetails
) : BottomSheetDialogFragment() {
    private lateinit var detailAdapter: InvoiceDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Sử dụng layout chi tiết hóa đơn bạn đã tạo
        return inflater.inflate(
            R.layout.layout_invoice_detail,
            container,
            false
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        displayInvoiceData(view, fullInvoice)
    }

    private fun setupRecyclerView(view: View) {
        detailAdapter = InvoiceDetailAdapter()
        val rvDetails =
            view.findViewById<RecyclerView>(R.id.rv_invoice_details) // Dùng findViewById

        rvDetails.layoutManager = LinearLayoutManager(requireContext())
        rvDetails.adapter = detailAdapter
    }

    // Hàm hiển thị dữ liệu lên layout
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun displayInvoiceData(rootView: View, data: InvoiceWithDetails) {
        val invoice = data.invoice

        // 1. HEADER & THÔNG TIN KHÁCH HÀNG
        rootView.findViewById<TextView>(R.id.tv_invoice_id).text = "HÓA ĐƠN"
        rootView.findViewById<TextView>(R.id.tv_invoice_date).text =
            invoice.date.toFormattedString()

        rootView.findViewById<TextView>(R.id.tv_customer_name).text =
            invoice.tenKhach

        val phoneTextView = rootView.findViewById<TextView>(R.id.tv_customer_phone)
        if (invoice.sdt.isNullOrBlank()) {
            phoneTextView.text = "SDT: Không có"
        } else {
            phoneTextView.text = "SĐT: ${invoice.sdt}"
        }

        val addressTextView = rootView.findViewById<TextView>(R.id.tv_customer_address)
        if (invoice.diaChi.isNullOrBlank()) {
            addressTextView.text = "Địa chỉ: Không có"
        } else {
            addressTextView.text = "Địa chỉ: ${invoice.diaChi}"
        }

        // 2. CHI TIẾT SẢN PHẨM (RecyclerView)
        detailAdapter.submitList(data.details)

        // Gán dữ liệu vào TextView
        rootView.findViewById<TextView>(R.id.tv_total_quantity).text =
            invoice.tongSoLuong.toString() + " bó"
        rootView.findViewById<TextView>(R.id.tv_discount_value).text =
            invoice.giamGia.toVNOnlyK()
        rootView.findViewById<TextView>(R.id.tv_total_revenue).text =
            invoice.tongTienThu.toInt().toVNOnlyK()
        rootView.findViewById<TextView>(R.id.tv_seller_note).text =
            "Ghi chú: " + if (!invoice.ghiChu.isNullOrBlank())
                invoice.ghiChu else "Không có"
        rootView.findViewById<TextView>(R.id.tv_payment_method).text =
            invoice.loaiGiaoDich
        rootView.findViewById<TextView>(R.id.tv_total_profit).text =
            invoice.tongLoiNhuan.toInt().toVNOnlyK()

        val tvStatus = rootView.findViewById<TextView>(R.id.tv_order_status)

        val currentDate = LocalDate.now()
        val invoiceDate = convertToLocalDate(invoice.date)

        when (invoice.isCompleted) {
            true -> {
                tvStatus.text = requireContext()
                    .getString(R.string.action_complete_ship)
                tvStatus.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green_600
                    )
                )
            }
            false -> {
                when {
                    invoiceDate.isAfter(currentDate) -> { // Chưa đến ngày
                        tvStatus.text = requireContext()
                            .getString(R.string.status_not_due_yet)
                        tvStatus.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.yellow_600
                            )
                        )
                    }
                    invoiceDate.isEqual(currentDate) -> { // Đến ngày
                        tvStatus.text = requireContext()
                            .getString(R.string.status_due_today)
                        tvStatus.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.orange
                            )
                        )
                    }
                    invoiceDate.isBefore(currentDate) -> { // Quá hạn
                        tvStatus.text = requireContext()
                            .getString(R.string.status_over_due)
                        tvStatus.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.red_700
                            )
                        )
                    }
                }
            }
        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToLocalDate(date: Date): LocalDate {
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}