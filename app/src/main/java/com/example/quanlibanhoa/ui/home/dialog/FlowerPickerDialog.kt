package com.example.quanlibanhoa.ui.home.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlibanhoa.R
import com.example.quanlibanhoa.data.entity.Flower
import com.example.quanlibanhoa.utils.loadImageFromUri

class FlowerPickerDialog(
    private val flowers: LiveData<List<Flower>>,
    private val onFlowerSelected: (Flower) -> Unit
) : DialogFragment() {

    private lateinit var adapter: FlowerAdapter
    private var allFlowers: List<Flower> = emptyList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_flower_picker, null)
        val rv = view.findViewById<RecyclerView>(R.id.rvFlowerPicker)
        val edtSearch = view.findViewById<EditText>(R.id.edtSearchFlower)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarFlowerPicker)

        adapter = FlowerAdapter(mutableListOf()) {
            onFlowerSelected(it)
            dismiss()
        }

        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(context)
        val dividerItemDecoration =
            DividerItemDecoration(rv.context, LinearLayoutManager.VERTICAL)
        rv.addItemDecoration(dividerItemDecoration)
        // Quan sát dữ liệu hoa (LiveData)
        flowers.observe(this) { flowers ->
            progressBar.visibility = View.GONE

            if (flowers.isNullOrEmpty()) {
                // Nếu DB trống thật, hiển thị danh sách rỗng
                allFlowers = emptyList()
                adapter.updateData(emptyList())
            } else {
                allFlowers = flowers
                adapter.updateData(flowers)
            }
        }

        // tìm kiếm
        edtSearch.addTextChangedListener { text ->
            val filtered = allFlowers.filter {
                it.tenHoa.contains(text.toString(), ignoreCase = true)
            }
            adapter.updateData(filtered)
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Chọn hoa")
            .setNegativeButton("Đóng") { d, _ -> d.dismiss() }
            .create()
    }

    class FlowerAdapter(
        private val data: MutableList<Flower>,
        private val onClick: (Flower) -> Unit
    ) : RecyclerView.Adapter<FlowerAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val txtName: TextView = view.findViewById(R.id.txtNamePicker)
            val img: ImageView = view.findViewById(R.id.imgFlowerPicker)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(
                R.layout.item_flower_picker, parent, false
            )
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val flower = data[position]
            holder.txtName.text = flower.tenHoa
            holder.img.loadImageFromUri(flower.hinhAnh)
            holder.itemView.setOnClickListener { onClick(flower) }
        }

        override fun getItemCount() = data.size

        @SuppressLint("NotifyDataSetChanged")
        fun updateData(newData: List<Flower>) {
            data.clear()
            data.addAll(newData)
            notifyDataSetChanged()
        }
    }
}
