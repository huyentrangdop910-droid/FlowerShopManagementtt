package com.example.quanlibanhoa.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlibanhoa.data.entity.Invoice
import com.example.quanlibanhoa.data.entity.InvoiceDetail
import com.example.quanlibanhoa.data.entity.InvoiceWithDetails

@Dao
interface InvoiceWithDetailsDao {

    // Invoice
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Transaction
    @Update
    suspend fun updateInvoice(invoice: Invoice)

    // InvoiceDetail
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceDetail(detail: InvoiceDetail): Long

    @Query("SELECT * FROM invoice_details WHERE invoiceId = :invoiceId")
    suspend fun getDetailsByInvoiceId(invoiceId: Int): List<InvoiceDetail>

    // ✅ Lấy hóa đơn kèm chi tiết
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoicesWithDetails(): LiveData<List<InvoiceWithDetails>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceWithDetailsById(id: Int): InvoiceWithDetails?

    @Transaction
    @Query("DELETE FROM invoice_details WHERE invoiceId = :invoiceId")
    suspend fun deleteInvoiceDetailsByInvoiceId(invoiceId: Int)

    // ✅ UPDATE INVOICE + DETAILS
    // -----------------------------
    @Transaction
    suspend fun updateInvoiceWithDetail(invoice: Invoice, details: List<InvoiceDetail>) {
        // Cập nhật hóa đơn chính
        updateInvoice(invoice)

        // Xóa chi tiết cũ
        deleteInvoiceDetailsByInvoiceId(invoice.id)

        // Thêm lại danh sách chi tiết mới
        for (detail in details) {
            insertInvoiceDetail(detail.copy(invoiceId = invoice.id))
        }
    }

}
