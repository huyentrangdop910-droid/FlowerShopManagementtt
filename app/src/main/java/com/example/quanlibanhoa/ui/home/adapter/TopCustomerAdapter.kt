package com.example.quanlibanhoa.ui.home.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlibanhoa.R
import com.example.quanlibanhoa.data.entity.TopCustomer
import com.example.quanlibanhoa.utils.toVNOnlyK

class TopCustomerAdapter(
    private var customers: List<TopCustomer>
) : RecyclerView.Adapter<TopCustomerAdapter.TopCustomerViewHolder>() {

    inner class TopCustomerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvTotalTop: TextView = view.findViewById(R.id.tvTotalTop)
        val tvStt: TextView = view.findViewById(R.id.tvRankNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopCustomerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_customer, parent, false)
        return TopCustomerViewHolder(view)
    }

    override fun getItemCount(): Int = customers.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TopCustomerViewHolder, position: Int) {
        val item = customers[position]

        holder.tvStt.text = "${position + 1}"
        holder.tvCustomerName.text = item.name
        holder.tvTotalTop.text = "Tổng chi: ${item.totalSpent.toInt().toVNOnlyK()} " +
                "| Đã mua: ${item.totalFlowers} bó"
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newCustomers: List<TopCustomer>) {
        customers = newCustomers
        notifyDataSetChanged()
    }
}
