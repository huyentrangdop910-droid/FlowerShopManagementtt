package com.example.quanlibanhoa.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tenKhach: String,
    val sdt: String?,
    val diaChi: String?,
    val tongSoLuong: Int,
    val giamGia: Int,
    val tongTienThu: Double,
    val tongLoiNhuan: Double,
    val ghiChu: String?,
    val loaiGiaoDich: String,
    val date: Date,
    val isCompleted: Boolean,
    val createdAt: Long = Date().time
): Serializable