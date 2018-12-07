package xyz.shmeleva.eight.utilities

import android.os.SystemClock

class DoubleClickBlocker {
    private var lastClickTime: Long = 0

    fun isSingleClick() : Boolean {
        var clickTime = SystemClock.elapsedRealtime();
        if (clickTime - lastClickTime < 500) {
            return false;
        }
        lastClickTime = clickTime;
        return true
    }

    fun isDoubleClick(): Boolean {
        return !isSingleClick()
    }
}