package com.example.quanlibanhoa.utils

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeToToggleCallback(
    private val onToggle: (position: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    companion object {
        // Chỉ dùng THRESHOLD để trigger onSwiped.
        private const val THRESHOLD_RATIO = 0.6f

        // Hệ số giảm độ nhạy
    }

    // KHÔNG cần isLocked/isToggled nữa

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition

        // Thực hiện hành động Toggle/Xử lý dữ liệu
        onToggle(position)

        // ItemTouchHelper sẽ tự động trượt item ra khỏi màn hình
        // vì chúng ta không chặn nó.
    }

    // Bỏ override convertToAbsoluteDirection (vì ta muốn item trượt hẳn ra ngoài)

    // *** THAY ĐỔI 1: Chỉ cần Threshold để trigger onSwiped ***
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        // Nếu vuốt quá 30% width, onSwiped sẽ được gọi.
        return THRESHOLD_RATIO
    }

    // Giữ nguyên hoặc bỏ qua các hàm liên quan đến velocity (không cần thiết cho logic này)
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        // Tăng giá trị này lên rất cao để gần như không thể vượt qua,
        // buộc ItemTouchHelper phải dùng ngưỡng 0.8f
        return defaultValue * 1000
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        // Có thể giữ nguyên, hoặc tăng cao để đồng bộ với Escape Velocity
        return defaultValue
    }

    // *** THAY ĐỔI 2: Loại bỏ logic khóa và giới hạn dX ***
    @Suppress("RedundantOverride")
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        // KHÔNG CLAMP dX NỮA. Cho phép item trượt hết cỡ.
        // ItemTouchHelper sẽ tự động xử lý animation trượt ra ngoài.
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getAnimationDuration(
        recyclerView: RecyclerView,
        animationType: Int,
        animateDx: Float,
        animateDy: Float
    ): Long {
        // Tăng tốc độ trượt ra ngoài một chút nếu cần
        if (animationType == ItemTouchHelper.ANIMATION_TYPE_SWIPE_SUCCESS) {
            return 250L // Thời gian trượt ra ngoài
        }
        return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
    }

    // Giữ nguyên clearView. Nó sẽ được gọi sau khi item trượt ra khỏi màn hình
    @Suppress("RedundantOverride")
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        // Dữ liệu trong Adapter phải được xóa sau khi onToggle(position) được gọi
        // và ItemTouchHelper hoàn thành animation.
    }
}