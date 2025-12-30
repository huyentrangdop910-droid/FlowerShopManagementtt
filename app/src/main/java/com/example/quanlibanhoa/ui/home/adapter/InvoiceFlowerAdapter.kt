package com.example.quanlibanhoa.ui.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlibanhoa.data.entity.Flower
import com.example.quanlibanhoa.databinding.ItemInvoiceFlowerBinding
import com.example.quanlibanhoa.utils.loadImageFromUri
import com.example.quanlibanhoa.utils.toVNOnlyK

class InvoiceFlowerAdapter(
    private val flowers: MutableList<Flower>,
    private val onDelete: (Flower) -> Unit,
    private val onEdit: (Flower) -> Unit,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<InvoiceFlowerAdapter.ViewHolder>() {

    @SuppressLint("SetTextI18n")
    inner class ViewHolder(val binding: ItemInvoiceFlowerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnDelete.setOnClickListener {
                val flower = flowers[bindingAdapterPosition]
                onDelete(flower)
            }
            // long click để edit
            binding.root.setOnLongClickListener {
                val flower = flowers[bindingAdapterPosition]
                onEdit(flower)
                true
            }
            binding.btnMinus.setOnClickListener {
                val qty = binding.edtQuantity.text.toString().toIntOrNull() ?: 1
                if (qty > 1) {
                    binding.edtQuantity.setText((qty - 1).toString())
                    val flower = flowers[bindingAdapterPosition]
                    flower.soluong = qty - 1
                    onQuantityChanged()
                }
            }
            binding.btnPlus.setOnClickListener {
                val qty = binding.edtQuantity.text.toString().toIntOrNull() ?: 0
                binding.edtQuantity.setText((qty + 1).toString())
                val flower = flowers[bindingAdapterPosition]
                flower.soluong = qty + 1
                onQuantityChanged()
            }
            binding.edtQuantity.addTextChangedListener { text ->
                val qty = text.toString().toIntOrNull() ?: 1
                flowers[bindingAdapterPosition].soluong = qty
                onQuantityChanged()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInvoiceFlowerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val flower = flowers[position]
        holder.binding.txtName.text = flower.tenHoa
        holder.binding.imgFlower.loadImageFromUri(flower.hinhAnh)
        holder.binding.txtPriceIn.text = "Giá nhập: ${flower.giaNhap.toInt().toVNOnlyK()}/bó"
        holder.binding.txtPriceOut.text = "Giá bán: ${flower.giaBan.toInt().toVNOnlyK()}/bó"
        holder.binding.edtQuantity.setText(flower.soluong.toString())
    }

    override fun getItemCount() = flowers.size

    fun addFlower(flower: Flower) {
        flowers.add(flower)
        notifyItemInserted(flowers.size - 1)
    }

    fun removeFlower(flower: Flower) {
        val index = flowers.indexOf(flower)
        if (index >= 0) {
            flowers.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
