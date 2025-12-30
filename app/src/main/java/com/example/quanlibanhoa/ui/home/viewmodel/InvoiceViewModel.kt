package com.example.quanlibanhoa.ui.home.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.example.quanlibanhoa.data.entity.Invoice
import com.example.quanlibanhoa.data.entity.InvoiceDetail
import com.example.quanlibanhoa.data.entity.InvoiceWithDetails
import com.example.quanlibanhoa.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InvoiceViewModel(
    private val repository: AppRepository
) : ViewModel() {
    private val _addInvoiceState = MutableLiveData<StateInvoice>().apply {
        value = StateInvoice.IDLE // init về IDLE
    }
    val addInvoiceState: LiveData<StateInvoice> = _addInvoiceState.distinctUntilChanged()

    private val _editInvoiceState = MutableLiveData<StateInvoice>().apply {
        value = StateInvoice.IDLE // init về IDLE
    }
    val editInvoiceState: LiveData<StateInvoice> = _editInvoiceState.distinctUntilChanged()

    private val _editInvoiceState1 = MutableLiveData<StateInvoice>().apply {
        value = StateInvoice.IDLE // init về IDLE
    }
    val editInvoiceState1: LiveData<StateInvoice> = _editInvoiceState1.distinctUntilChanged()

    private val _editInvoiceState2 = MutableLiveData<StateInvoice>().apply {
        value = StateInvoice.IDLE // init về IDLE
    }
    val editInvoiceState2: LiveData<StateInvoice> = _editInvoiceState2.distinctUntilChanged()

    private val _deleteInvoiceState1 = MutableLiveData<StateInvoice>().apply {
        value = StateInvoice.IDLE // init về IDLE
    }
    val deleteInvoiceState1: LiveData<StateInvoice> = _deleteInvoiceState1.distinctUntilChanged()

    private val _deleteInvoiceState2 = MutableLiveData<StateInvoice>().apply {
        value = StateInvoice.IDLE // init về IDLE
    }
    val deleteInvoiceState2: LiveData<StateInvoice> = _deleteInvoiceState2.distinctUntilChanged()

    private val _deleteInvoiceState3 = MutableLiveData<StateInvoice>().apply {
        value = StateInvoice.IDLE // init về IDLE
    }
    val deleteInvoiceState3: LiveData<StateInvoice> = _deleteInvoiceState3.distinctUntilChanged()

    private val _deleteInvoiceState4 = MutableLiveData<StateInvoice>().apply {
        value = StateInvoice.IDLE // init về IDLE
    }
    val deleteInvoiceState4: LiveData<StateInvoice> = _deleteInvoiceState4.distinctUntilChanged()

    private val _deleteInvoiceState5 = MutableLiveData<StateInvoice>().apply {
        value = StateInvoice.IDLE // init về IDLE
    }
    val deleteInvoiceState5: LiveData<StateInvoice> = _deleteInvoiceState5.distinctUntilChanged()

    private val _deleteInvoiceState6 = MutableLiveData<StateInvoice>().apply {
        value = StateInvoice.IDLE // init về IDLE
    }
    val deleteInvoiceState6: LiveData<StateInvoice> = _deleteInvoiceState6.distinctUntilChanged()

    private val _deleteInvoiceState7 = MutableLiveData<StateInvoice>().apply {
        value = StateInvoice.IDLE // init về IDLE
    }
    val deleteInvoiceState7: LiveData<StateInvoice> = _deleteInvoiceState7.distinctUntilChanged()

    val invoiceWithDetailsStateList: LiveData<List<InvoiceWithDetails>> =
        repository.getAllInvoicesWithDetails().distinctUntilChanged()

    fun addInvoiceWithDetail(invoice: Invoice, details: List<InvoiceDetail>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertInvoiceWithDetails(invoice, details)
                _addInvoiceState.postValue(StateInvoice.ADD_INVOICE_SUCCESS)
            } catch (_: Exception) {
                _addInvoiceState.postValue(StateInvoice.ADD_INVOICE_ERROR)
            }
        }
    }

    fun resetAddState() {
        _addInvoiceState.value = StateInvoice.IDLE
    }

    fun resetEditState(id: Int) {
        when(id){
            0 -> {_editInvoiceState.value = StateInvoice.IDLE}
            1 -> {_editInvoiceState1.value = StateInvoice.IDLE}
            2 -> {_editInvoiceState2.value = StateInvoice.IDLE}
        }
    }

    fun resetDeleteState(id: Int) {
        when (id) {
            1 -> {
                _deleteInvoiceState1.value = StateInvoice.IDLE
            }

            2 -> {
                _deleteInvoiceState2.value = StateInvoice.IDLE
            }

            3 -> {
                _deleteInvoiceState3.value = StateInvoice.IDLE
            }

            4 -> {
                _deleteInvoiceState4.value = StateInvoice.IDLE
            }

            5 -> {
                _deleteInvoiceState5.value = StateInvoice.IDLE
            }
            6 -> {
                _deleteInvoiceState6.value = StateInvoice.IDLE
            }
            7 -> {
                _deleteInvoiceState7.value = StateInvoice.IDLE
            }
        }
    }

    fun deleteInvoicesByIds(invoiceIds: List<Int>, id: Int) {
        viewModelScope.launch {
            try {
                // Gọi hàm DAO đã thiết lập onDelete CASCADE
                repository.deleteInvoicesByIds(invoiceIds)
                when (id) {
                    1 -> {
                        _deleteInvoiceState1.postValue(StateInvoice.DELETE_INVOICE_SUCCESS)
                    }

                    2 -> {
                        _deleteInvoiceState2.postValue(StateInvoice.DELETE_INVOICE_SUCCESS)
                    }

                    3 -> {
                        _deleteInvoiceState3.postValue(StateInvoice.DELETE_INVOICE_SUCCESS)
                    }

                    4 -> {
                        _deleteInvoiceState4.postValue(StateInvoice.DELETE_INVOICE_SUCCESS)
                    }

                    5 -> {
                        _deleteInvoiceState5.postValue(StateInvoice.DELETE_INVOICE_SUCCESS)
                    }
                    6 -> {
                        _deleteInvoiceState5.postValue(StateInvoice.DELETE_INVOICE_SUCCESS)
                    }
                    7 -> {
                        _deleteInvoiceState5.postValue(StateInvoice.DELETE_INVOICE_SUCCESS)
                    }
                }
            } catch (_: Exception) {
                when (id) {
                    1 -> {
                        _deleteInvoiceState1.postValue(StateInvoice.DELETE_INVOICE_ERROR)
                    }

                    2 -> {
                        _deleteInvoiceState2.postValue(StateInvoice.DELETE_INVOICE_ERROR)
                    }

                    3 -> {
                        _deleteInvoiceState3.postValue(StateInvoice.DELETE_INVOICE_ERROR)
                    }

                    4 -> {
                        _deleteInvoiceState4.postValue(StateInvoice.DELETE_INVOICE_ERROR)
                    }

                    5 -> {
                        _deleteInvoiceState5.postValue(StateInvoice.DELETE_INVOICE_ERROR)
                    }
                    6 -> {
                        _deleteInvoiceState5.postValue(StateInvoice.DELETE_INVOICE_ERROR)
                    }
                    7 -> {
                        _deleteInvoiceState5.postValue(StateInvoice.DELETE_INVOICE_ERROR)
                    }
                }
            }
        }
    }

    fun updateInvoiceWithDetail(invoice: Invoice, details: List<InvoiceDetail>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateInvoiceWithDetail(invoice, details)
                _editInvoiceState.postValue(StateInvoice.EDIT_INVOICE_SUCCESS)
            } catch (_: Exception) {
                _editInvoiceState.postValue(StateInvoice.EDIT_INVOICE_ERROR)
            }
        }
    }

    fun toggleIsCompleted(invoiceId: Int, currentState: Boolean, id: Int) {
        viewModelScope.launch {
            try {
                delay(300L)  // Delay 300ms để snap back xong (có thể chỉnh)
                repository.updateInvoiceCompleted(invoiceId, !currentState)
                when(id){
                    0 -> {_editInvoiceState.postValue(StateInvoice.EDIT_INVOICE_SUCCESS)}
                    1 -> {_editInvoiceState.postValue(StateInvoice.EDIT_INVOICE_SUCCESS)}
                    2 -> {_editInvoiceState.postValue(StateInvoice.EDIT_INVOICE_SUCCESS)}
                }
            } catch (_: Exception) {
                when(id){
                    0 -> {_editInvoiceState.postValue(StateInvoice.EDIT_INVOICE_ERROR)}
                    1 -> {_editInvoiceState.postValue(StateInvoice.EDIT_INVOICE_ERROR)}
                    2 -> {_editInvoiceState.postValue(StateInvoice.EDIT_INVOICE_ERROR)}
                }
            }
        }
    }

}

enum class StateInvoice {
    IDLE,
    ADD_INVOICE_SUCCESS,
    ADD_INVOICE_ERROR,
    EDIT_INVOICE_SUCCESS,
    EDIT_INVOICE_ERROR,
    DELETE_INVOICE_SUCCESS,
    DELETE_INVOICE_ERROR
}

class InvoiceViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            // Create dependencies inside the factory
            val localRepo = AppRepository(context)
            @Suppress("UNCHECKED_CAST")
            return InvoiceViewModel(
                localRepo
            ) as T
        }
        throw IllegalArgumentException("Unknown TransactionViewModel class")
    }
}