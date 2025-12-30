package com.example.quanlibanhoa.utils

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.quanlibanhoa.R
import java.io.File

fun ImageView.loadImageFromUri(uriString: String?) {
    // Kiểm tra xem uriString có phải là URL không
    if (!uriString.isNullOrEmpty()
        && (uriString.startsWith("http://") || uriString.startsWith("https://"))) {
        // Nếu uriString là URL, tải hình ảnh từ URL
        Glide.with(context)
            .load(uriString)
            .placeholder(R.drawable.ic_photo)
            .error(R.drawable.ic_photo)
            .into(this)
    } else {
        // 1. Tạo đối tượng File từ đường dẫn tuyệt đối
        val file = uriString?.let { File(it) }

        // 2. Kiểm tra File có tồn tại không
        if (file != null && file.exists()) {
            // 3. Sử dụng Glide để tải hình ảnh từ đối tượng File
            Glide.with(context)
                .load(file) // <<< SỬA ĐỔI Ở ĐÂY: Load đối tượng File
                .placeholder(R.drawable.ic_photo)
                .error(R.drawable.ic_photo)
                .into(this)
        } else {
            // 4. Nếu không có đường dẫn hoặc file không tồn tại
            Glide.with(context)
                .load(R.drawable.ic_photo)
                .into(this)
        }
    }
}

fun ImageView.loadImageFromUri(uri: Uri?) {
    if (uri != null) {
        // 3. Sử dụng Glide để tải hình ảnh từ URI
        Glide.with(context)
            .load(uri)
            .placeholder(R.drawable.ic_photo)
            .error(R.drawable.ic_photo)
            .into(this)
    } else {
        // 4. Nếu không có URI, tải hình ảnh mặc định
        Glide.with(context)
            .load(R.drawable.ic_photo)
            .into(this)
    }
}