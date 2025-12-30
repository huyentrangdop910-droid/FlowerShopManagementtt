package com.example.quanlibanhoa.ui.home.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlibanhoa.R
import com.example.quanlibanhoa.data.entity.TopFlower
import com.example.quanlibanhoa.utils.loadImageFromUri
import com.example.quanlibanhoa.utils.toVNOnlyK

class TopFlowerAdapter(
    private var items: List<TopFlower>
) : RecyclerView.Adapter<TopFlowerAdapter.TopFlowerViewHolder>() {

    inner class TopFlowerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFlower: ImageView = itemView.findViewById(R.id.imgFlowerTop)
        val tvFlowerName: TextView = itemView.findViewById(R.id.tvFlowerNameTop)
        val tvToTalTop: TextView = itemView.findViewById(R.id.tvToTalTop)
        val tvStt: TextView = itemView.findViewById(R.id.tvFlowerRankNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopFlowerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_product, parent, false)
        return TopFlowerViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TopFlowerViewHolder, position: Int) {
        val item = items[position]

        holder.tvStt.text = "${position + 1}"
        holder.tvFlowerName.text = item.name
        holder.tvToTalTop.text = "Đã bán: ${item.totalSold} bông " +
                "| Doanh thu: ${item.totalRevenue.toInt().toVNOnlyK()}"
        holder.imgFlower.loadImageFromUri(item.imageUrl)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<TopFlower>) {
        items = newItems
        notifyDataSetChanged()
    }
}
