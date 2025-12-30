package com.example.quanlibanhoa.ui.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlibanhoa.data.entity.InvoiceDetail
import com.example.quanlibanhoa.databinding.ItemInvoiceFlowerDetailBinding
import com.example.quanlibanhoa.utils.loadImageFromUri
import com.example.quanlibanhoa.utils.toVNOnlyK

class InvoiceDetailAdapter :
    ListAdapter<InvoiceDetail, InvoiceDetailAdapter.InvoiceDetailViewHolder>(InvoiceDetailDiffCallback()) {

    // Đã bỏ private val currencyFormatter = DecimalFormat("#,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceDetailViewHolder {
        val binding = ItemInvoiceFlowerDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InvoiceDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceDetailViewHolder, position: Int) {
        val item = getItem(position)
        // Đã bỏ đối số currencyFormatter
        holder.bind(item)
    }

    // ViewHolder chịu trách nhiệm gán dữ liệu vào các View
    class InvoiceDetailViewHolder(private val binding: ItemInvoiceFlowerDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(detail: InvoiceDetail) {

            // 1. Tên Hoa
            binding.txtName.text = detail.tenHoa

            // 2. Giá Bán / Đơn vị (Chỉ hiển thị số)
            binding.txtPrice.text = "Giá bán: ${detail.giaBan.toInt().toVNOnlyK()}/bó" +
                    " | Giá nhập: ${detail.giaNhap.toInt().toVNOnlyK()}/bó"

            // 3. Số Lượng
            binding.txtQuantity.text = "x${detail.soLuong}"

            binding.imgFlower.loadImageFromUri(detail.hinhAnh)
        }
    }

    // DiffCallback giữ nguyên
    class InvoiceDetailDiffCallback : DiffUtil.ItemCallback<InvoiceDetail>() {
        override fun areItemsTheSame(oldItem: InvoiceDetail, newItem: InvoiceDetail): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: InvoiceDetail, newItem: InvoiceDetail): Boolean {
            // So sánh tất cả các trường dữ liệu để đảm bảo item có thay đổi hay không
            return oldItem == newItem
        }
    }
}