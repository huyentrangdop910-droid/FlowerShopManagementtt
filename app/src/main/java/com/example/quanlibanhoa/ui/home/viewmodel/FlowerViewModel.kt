package com.example.quanlibanhoa.ui.home.viewmodel

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.example.quanlibanhoa.data.entity.Flower
import com.example.quanlibanhoa.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class FlowerViewModel(
    private val repository: AppRepository
) : ViewModel() {
    private val _addFlowerState = MutableLiveData<StateFlower>().apply {
        value = StateFlower.IDLE // init về IDLE
    }
    val addFlowerState: LiveData<StateFlower> = _addFlowerState.distinctUntilChanged()

    private val _editFlowerState = MutableLiveData<StateFlower>().apply {
        value = StateFlower.IDLE // init về IDLE
    }
    val editFlowerState: LiveData<StateFlower> = _editFlowerState.distinctUntilChanged()

    private val _deleteFlowerState = MutableLiveData<StateFlower>().apply {
        value = StateFlower.IDLE // init về IDLE
    }
    val deleteFlowerState: LiveData<StateFlower> = _deleteFlowerState.distinctUntilChanged()

    val flowerStateList: LiveData<List<Flower>> = repository.getAllFlowers().distinctUntilChanged()

    fun addFlower(
        tenHoa: String,
        giaNhap: Double,
        giaBan: Double,
        imageUri: Uri?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            var imagePath: String? = null
            try {
                // 1. LƯU ẢNH TRÊN LUỒNG IO (An toàn và đúng)
                imagePath = imageUri?.let { uri ->
                    repository.saveImageToInternalStorage(uri, tenHoa)
                }

                val flower = Flower(
                    tenHoa = tenHoa,
                    giaNhap = giaNhap,
                    giaBan = giaBan,
                    hinhAnh = imagePath
                )

                // 2. INSERT VÀO DB
                val result = repository.insertFlower(flower)
                if (result > 0L) {
                    _addFlowerState.postValue(StateFlower.ADD_FLOWER_SUCCESS)
                } else if (result == -1L) {
                    // Lỗi trùng tên -> Phải xóa ảnh vừa lưu
                    imagePath?.let { repository.deleteImageFile(it) }
                    _addFlowerState.postValue(StateFlower.ADD_FLOWER_INVALID_NAME)
                } else {
                    // Lỗi Database khác -> Phải xóa ảnh vừa lưu
                    imagePath?.let { repository.deleteImageFile(it) }
                    _addFlowerState.postValue(StateFlower.ADD_FLOWER_ERROR)
                }
            } catch (_: Exception) {
                // Lỗi chung (I/O hoặc Database) -> Phải xóa ảnh
                imagePath?.let { repository.deleteImageFile(it) }
                _addFlowerState.postValue(StateFlower.ADD_FLOWER_ERROR)
            }
        }
    }

    fun editFlower(
        flowerId: Int,
        tenHoa: String,
        giaNhap: Double,
        giaBan: Double,
        newImageUri: Uri?,
        oldImagePath: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            var newImagePath: String? = null
            val finalImagePath: String?
            try {
                // 1. Xử lý ảnh: Nếu có ảnh mới, lưu nó
                if (newImageUri != null) {
                    newImagePath = repository.saveImageToInternalStorage(newImageUri, tenHoa)
                    finalImagePath = newImagePath
                } else {
                    // Không có ảnh mới, giữ ảnh cũ
                    finalImagePath = oldImagePath
                }

                // 2. Tạo đối tượng Flower mới để cập nhật
                val updatedFlower = Flower(
                    id = flowerId,
                    tenHoa = tenHoa,
                    giaNhap = giaNhap,
                    giaBan = giaBan,
                    hinhAnh = finalImagePath,
                    createdAt = Date().time
                )

                // 3. UPDATE vào DB (Có thể ném SQLiteConstraintException)
                repository.updateFlower(updatedFlower)

                // 4. THÀNH CÔNG: Xóa ảnh cũ (chỉ xóa khi có ảnh mới và đã lưu thành công)
                if (newImagePath != null) {
                    oldImagePath?.let { repository.deleteImageFile(it) }
                }

                _editFlowerState.postValue(StateFlower.EDIT_FLOWER_SUCCESS)

            } catch (_: SQLiteConstraintException) {
                // LỖI TRÙNG TÊN: Xóa ảnh mới vừa lưu (nếu có)
                newImagePath?.let { repository.deleteImageFile(it) }
                _editFlowerState.postValue(StateFlower.EDIT_FLOWER_INVALID_NAME)

            } catch (_: Exception) {
                // LỖI KHÁC (DB hoặc I/O): Xóa ảnh mới vừa lưu (nếu có)
                newImagePath?.let { repository.deleteImageFile(it) }
                _editFlowerState.postValue(StateFlower.EDIT_FLOWER_ERROR)
            }
        }
    }

    fun deleteFlowers(flowers: List<Flower>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteMultiple(flowers)
                // Xóa ảnh từ bộ nhớ nội bộ
                flowers.forEach { flower ->
                    flower.hinhAnh?.let { imagePath ->
                        // Đảm bảo chỉ xóa nếu đường dẫn không null
                        repository.deleteImageFile(imagePath)
                    }
                }
                _deleteFlowerState.postValue(StateFlower.DELETE_FLOWER_SUCCESS)
            } catch (_: Exception) {
                _deleteFlowerState.postValue(StateFlower.DELETE_FLOWER_ERROR)
            }
        }
    }

    fun resetAddState() {
        _addFlowerState.value = StateFlower.IDLE
    }

    fun resetEditState() {
        _editFlowerState.value = StateFlower.IDLE
    }

    fun resetDeleteState() {
        _deleteFlowerState.value = StateFlower.IDLE
    }

}

enum class StateFlower {
    IDLE,
    ADD_FLOWER_SUCCESS,
    ADD_FLOWER_INVALID_NAME,
    ADD_FLOWER_ERROR,
    EDIT_FLOWER_SUCCESS,
    EDIT_FLOWER_INVALID_NAME,
    EDIT_FLOWER_ERROR,
    DELETE_FLOWER_SUCCESS,
    DELETE_FLOWER_ERROR
}

class FlowerViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlowerViewModel::class.java)) {
            // Create dependencies inside the factory
            val localRepo = AppRepository(context)
            @Suppress("UNCHECKED_CAST")
            return FlowerViewModel(
                localRepo
            ) as T
        }
        throw IllegalArgumentException("Unknown TransactionViewModel class")
    }
}