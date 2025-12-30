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
import com.example.quanlibanhoa.databinding.FragmentReportBinding
import com.example.quanlibanhoa.ui.home.fragment.report_invoice.MonthlyReportFragment
import com.example.quanlibanhoa.ui.home.fragment.report_invoice.TodayReportFragment
import com.example.quanlibanhoa.ui.home.fragment.report_invoice.WeeklyReportFragment
import com.example.quanlibanhoa.ui.home.fragment.report_invoice.YearlyReportFragment
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.reflect.Field


class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentReportBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabLayoutExceptedInvoice()
    }

    private fun setupTabLayoutExceptedInvoice() {
        if (binding.viewPagerReport.adapter == null) {
            val adapter = ViewPagerExceptedInvoiceAdapter(requireActivity())
            binding.viewPagerReport.adapter = adapter
            binding.viewPagerReport.isUserInputEnabled = false
            // nối tab layout với viewpager2
            TabLayoutMediator(
                binding.tabLayoutReportInvoice,
                binding.viewPagerReport
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> "Hôm nay"
                    1 -> "Tuần này"
                    2 -> "Tháng này"
                    3 -> "Năm này"
                    else -> null
                }
            }.attach()
            binding.viewPagerReport.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            reduceSwipeSensitivity(binding.viewPagerReport, factor = 4)
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

    class ViewPagerExceptedInvoiceAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TodayReportFragment()
                1 -> WeeklyReportFragment()
                2 -> MonthlyReportFragment()
                3 -> YearlyReportFragment()
                else -> throw IllegalArgumentException("(ExceptedInvoiceFragment) Invalid position")
            }
        }

        override fun getItemCount(): Int = 4
    }

}