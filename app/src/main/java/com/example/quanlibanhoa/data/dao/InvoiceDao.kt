package com.example.quanlibanhoa.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlibanhoa.data.entity.Invoice

@Dao
interface InvoiceDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: Invoice): Long

    @Transaction
    @Update
    suspend fun update(invoice: Invoice)

    @Transaction
    @Delete
    suspend fun delete(invoice: Invoice)

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): LiveData<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Int): Invoice?

    @Transaction
    @Query("DELETE FROM invoices WHERE id IN (:invoiceIds)")
    suspend fun deleteInvoicesByIds(invoiceIds: List<Int>)

    @Transaction
    @Query("UPDATE invoices SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateIsCompleted(id: Int, isCompleted: Boolean)

    @Transaction
    @Query("SELECT * FROM invoices WHERE  isCompleted = 0")
    suspend fun getAllPendingInvoices(): List<Invoice>
}
