package com.example.quanlibanhoa.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "flowers",
    // Dùng indices để đặt ràng buộc UNIQUE cho cột tenHoa
    indices = [Index(value = ["tenHoa"], unique = true)]
)
data class Flower(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tenHoa: String,
    val hinhAnh: String?,
    var giaNhap: Double,
    var giaBan: Double,
    var soluong: Int = 1,
    val createdAt: Long = Date().time
)

