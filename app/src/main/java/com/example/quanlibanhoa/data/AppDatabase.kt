package com.example.quanlibanhoa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quanlibanhoa.data.dao.FlowerDao
import com.example.quanlibanhoa.data.dao.InvoiceDao
import com.example.quanlibanhoa.data.dao.InvoiceDetailDao
import com.example.quanlibanhoa.data.dao.InvoiceWithDetailsDao
import com.example.quanlibanhoa.data.entity.Flower
import com.example.quanlibanhoa.data.entity.Invoice
import com.example.quanlibanhoa.data.entity.InvoiceDetail

@Database(entities = [Flower::class, Invoice::class, InvoiceDetail::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flowerDao(): FlowerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceDetailDao(): InvoiceDetailDao
    abstract fun invoiceWithDetailsDao(): InvoiceWithDetailsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quan_li_ban_hoa.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
