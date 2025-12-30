package com.example.quanlibanhoa.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.quanlibanhoa.databinding.FragmentInvoiceBinding
import com.example.quanlibanhoa.ui.home.fragment.history_invoice.DailyInvoiceFragment
import com.example.quanlibanhoa.ui.home.fragment.history_invoice.MonthlyInvoiceFragment
import com.example.quanlibanhoa.ui.home.fragment.history_invoice.WeeklyInvoiceFragment
import com.example.quanlibanhoa.ui.home.fragment.history_invoice.YearlyInvoiceFragment
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.reflect.Field


class InvoiceFragment : Fragment() {

    private var _binding: FragmentInvoiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInvoiceBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabLayoutHistoryInvoice()
    }

    private fun setupTabLayoutHistoryInvoice() {
        if (binding.viewPagerHistory.adapter == null) {
            val adapter = ViewPagerHistoryInvoiceAdapter(requireActivity())
            binding.viewPagerHistory.adapter = adapter
            binding.viewPagerHistory.isUserInputEnabled = false
            // nối tab layout với viewpager2
            TabLayoutMediator(
                binding.tabLayoutHistoryInvoice,
                binding.viewPagerHistory
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> "Hôm nay"
                    1 -> "Tuần này"
                    2 -> "Tháng này"
                    3 -> "Năm này"
                    else -> null
                }
            }.attach()
            binding.viewPagerHistory.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            reduceSwipeSensitivity(binding.viewPagerHistory, factor = 4)
        }
    }

    fun reduceSwipeSensitivity(viewPager2: ViewPager2, factor: Int = 3) {
        try {
            val recyclerViewField: Field = ViewPager2::class.java.getDeclaredField("mRecyclerView")
            recyclerViewField.isAccessible = true
            val recyclerView = recyclerViewField.get(viewPager2) as RecyclerView

            val touchSlopField: Field = RecyclerView::class.java.getDeclaredField("mTouchSlop")
            touchSlopField.isAccessible = true
            val touchSlop = touchSlopField.get(recyclerView) as Int

            // Giảm độ nhạy bằng cách nhân hệ số
            touchSlopField.set(recyclerView, touchSlop * factor)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ViewPagerHistoryInvoiceAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DailyInvoiceFragment()
                1 -> WeeklyInvoiceFragment()
                2 -> MonthlyInvoiceFragment()
                3 -> YearlyInvoiceFragment()
                else -> throw IllegalArgumentException("(HistoryInvoiceFragment) Invalid position")
            }
        }

        override fun getItemCount(): Int = 4
    }

}