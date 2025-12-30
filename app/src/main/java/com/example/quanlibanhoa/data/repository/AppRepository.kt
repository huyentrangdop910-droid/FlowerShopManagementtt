package com.example.quanlibanhoa.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.quanlibanhoa.data.AppDatabase
import com.example.quanlibanhoa.data.entity.Flower
import com.example.quanlibanhoa.data.entity.Invoice
import com.example.quanlibanhoa.data.entity.InvoiceDetail
import com.example.quanlibanhoa.data.entity.InvoiceWithDetails
import java.io.File
import java.io.FileOutputStream

class AppRepository(private val context: Context) {
    private val db = AppDatabase.Companion.getDatabase(context)

    fun saveImageToInternalStorage(uri: Uri, flowerName: String): String? {
        // CHỈ LÀM VIỆC NẶNG Ở ĐÂY
        val inputStream = context.contentResolver.openInputStream(uri)?: return null
        var fileName = flowerName.replace(" ", "_")
        var file = File(context.filesDir, "images/$fileName.jpg")
        var count = 1

        while (file.exists()) {
            fileName = "${flowerName}_$count".replace(" ", "_")
            file = File(context.filesDir, "images/$fileName.jpg")
            count++
        }

        file.parentFile?.mkdirs()
        val outputStream = FileOutputStream(file)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    // Giữ hàm này cho trường hợp cần xóa ảnh từ Repository
    fun deleteImageFile(imagePath: String) {
        val file = File(imagePath)
        if (file.exists()) {
            file.delete()
        }
    }

    suspend fun insertFlower(flower: Flower) = db.flowerDao().insert(flower)
    suspend fun updateFlower(flower: Flower) = db.flowerDao().update(flower)
    suspend fun deleteMultiple(flowers: List<Flower>) = db.flowerDao().deleteMultiple(flowers)
    fun getAllFlowers(): LiveData<List<Flower>>{
        return db.flowerDao().getAllFlowers()
    }
    suspend fun insertInvoiceWithDetails(invoice: Invoice, details: List<InvoiceDetail>) {
        val invoiceId = db.invoiceWithDetailsDao().insertInvoice(invoice)
        details.forEach { detail ->
            db.invoiceWithDetailsDao().insertInvoiceDetail(detail.copy(invoiceId = invoiceId.toInt()))
        }
    }

    fun getAllInvoicesWithDetails(): LiveData<List<InvoiceWithDetails>> =
        db.invoiceWithDetailsDao().getAllInvoicesWithDetails()

    suspend fun deleteInvoicesByIds(invoiceIds: List<Int>) {
        db.invoiceDao().deleteInvoicesByIds(invoiceIds)
    }

    suspend fun updateInvoiceWithDetail(invoice: Invoice, details: List<InvoiceDetail>){
        db.invoiceWithDetailsDao().updateInvoiceWithDetail(invoice, details)
    }

    suspend fun updateInvoiceCompleted(id: Int, isCompleted: Boolean){
        db.invoiceDao().updateIsCompleted(id, isCompleted)
    }

}
