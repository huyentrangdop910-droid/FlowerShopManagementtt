package com.example.quanlibanhoa.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.quanlibanhoa.databinding.FragmentExceptedInvoiceBinding
import com.example.quanlibanhoa.ui.home.fragment.expected_invoice.DueTodayFragment
import com.example.quanlibanhoa.ui.home.fragment.expected_invoice.NotDueYetFragment
import com.example.quanlibanhoa.ui.home.fragment.expected_invoice.OverdueFragment
import com.google.android.material.tabs.TabLayoutMediator

class ExceptedInvoiceFragment : Fragment() {

    private var _binding: FragmentExceptedInvoiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentExceptedInvoiceBinding.inflate(
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
        if (binding.viewPagerExcepted.adapter == null) {
            val adapter = ViewPagerExceptedInvoiceAdapter(requireActivity())
            binding.viewPagerExcepted.adapter = adapter
            binding.viewPagerExcepted.isUserInputEnabled = false
            // nối tab layout với viewpager2
            TabLayoutMediator(
                binding.tabLayoutExceptedInvoice,
                binding.viewPagerExcepted
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> "Chưa đến ngày"
                    1 -> "Đến ngày giao"
                    2 -> "Quá hạn"
                    else -> null
                }
            }.attach()
            binding.viewPagerExcepted.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ViewPagerExceptedInvoiceAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> NotDueYetFragment()
                1 -> DueTodayFragment()
                2 -> OverdueFragment()
                else -> throw IllegalArgumentException("(ExceptedInvoiceFragment) Invalid position")
            }
        }

        override fun getItemCount(): Int = 3
    }
}