package com.example.quanlibanhoa.ui.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlibanhoa.R
import com.example.quanlibanhoa.data.entity.Flower
import com.example.quanlibanhoa.utils.loadImageFromUri
import com.example.quanlibanhoa.utils.toVNOnlyK

class FlowerListAdapter(
    private val onEdit: (Flower) -> Unit,
    @Suppress("unused") private val onDeleteSelected: (List<Flower>) -> Unit, // Hàm xóa mới
    private val onMultiSelectModeChanged: (Boolean) -> Unit, // Báo cho Fragment biết chế độ đã thay đổi
    private val onSelectionCountChanged: (Int) -> Unit // Báo số lượng item đã chọn
) : RecyclerView.Adapter<FlowerListAdapter.ViewHolder>() {

    private val flowers = mutableListOf<Flower>()
    val selectedFlowers = mutableSetOf<Flower>() // Dùng Set để quản lý các mục đã chọn
    var isMultiSelectMode = false // Trạng thái chế độ chọn

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Flower>) {
        flowers.clear()
        flowers.addAll(data)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAllFlowers() {
        if (isMultiSelectMode) {
            selectedFlowers.clear()
            selectedFlowers.addAll(flowers)
            onSelectionCountChanged(selectedFlowers.size)
            notifyDataSetChanged()
        }
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbSelect: android.widget.CheckBox = view.findViewById(R.id.cbSelect)
        val imgFlower: ImageView = view.findViewById(R.id.imgFlower)
        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtPrice: TextView = view.findViewById(R.id.txtPrice)
        val txtPriceIn: TextView = view.findViewById(R.id.txtPriceIn)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flower, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = flowers.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val flower = flowers[position]
        holder.txtName.text = flower.tenHoa
        holder.txtPriceIn.text = "\uD83D\uDCB0 Giá nhập: ${flower.giaNhap.toInt().toVNOnlyK()}/bó"
        holder.txtPrice.text = "\uD83D\uDCB0 Giá bán: ${flower.giaBan.toInt().toVNOnlyK()}/bó"
        holder.imgFlower.loadImageFromUri(flower.hinhAnh)

        // 2. Trạng thái giao diện
        if (isMultiSelectMode) {
            holder.cbSelect.visibility = View.VISIBLE
            holder.btnEdit.visibility = View.GONE
            holder.cbSelect.isChecked = selectedFlowers.contains(flower)
        } else {
            holder.cbSelect.visibility = View.GONE
            holder.btnEdit.visibility = View.VISIBLE
        }

        // 3. Sự kiện
        holder.btnEdit.setOnClickListener { onEdit(flower) } // CHUYỂN longClick thành click

        // Long Click: Bật chế độ chọn
        holder.itemView.setOnLongClickListener {
            if (!isMultiSelectMode) {
                setMultiSelectMode(true) // Bật chế độ chọn
                toggleSelection(flower)   // Tự động chọn item này
                true
            } else {
                false // Cho phép click thường tiếp tục (xử lý ở dưới)
            }
        }
        // Click thường: Chọn/bỏ chọn nếu đang ở chế độ chọn
        holder.itemView.setOnClickListener {
            if (isMultiSelectMode) {
                toggleSelection(flower)
            } else {
                // Nếu không ở chế độ chọn, có thể thêm hành động click thường nếu cần
            }
        }

        // Click CheckBox
        holder.cbSelect.setOnClickListener {
            toggleSelection(flower)
        }
    }

    // Hàm Quản lý Chế độ chọn
    @SuppressLint("NotifyDataSetChanged")
    @JvmName("setMultiSelectModeFromFunction")
    fun setMultiSelectMode(enabled: Boolean) {
        if (isMultiSelectMode != enabled) {
            isMultiSelectMode = enabled
            if (!enabled) {
                selectedFlowers.clear() // Xóa lựa chọn khi thoát chế độ
            }
            onMultiSelectModeChanged(enabled) // Báo cho Fragment biết để hiển thị Toolbar
            notifyDataSetChanged()
        }
    }

    // Hàm Chọn/Bỏ chọn một mục
    private fun toggleSelection(flower: Flower) {
        if (selectedFlowers.contains(flower)) {
            selectedFlowers.remove(flower)
        } else {
            selectedFlowers.add(flower)
        }
        onSelectionCountChanged(selectedFlowers.size) // Báo số lượng
        // Chỉ cập nhật item bị ảnh hưởng để tối ưu hiệu suất
        val index = flowers.indexOf(flower)
        if (index != -1) notifyItemChanged(index)
    }

    // Hàm hủy chọn tất cả (Dùng cho Fragment)
    fun clearSelection() {
        selectedFlowers.clear()
        setMultiSelectMode(false)
    }
}
