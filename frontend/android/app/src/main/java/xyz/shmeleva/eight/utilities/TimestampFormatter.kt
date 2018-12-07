package xyz.shmeleva.eight.utilities

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

class TimestampFormatter {
    companion object {
        fun format(milliseconds: Long): String {
            val timestamp = Date(milliseconds)

            val dateString = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(timestamp)
            val timeString = SimpleDateFormat("HH:mm").format(timestamp)

            if (DateUtils.isToday(milliseconds)) {
                return timeString
            }
            return "$dateString $timeString"
        }

        fun formatDate(milliseconds: Long): String {
            val timestamp = Date(milliseconds)
            val dateString = SimpleDateFormat("yyyy MMMM d", Locale.getDefault()).format(timestamp)
            return dateString
        }
    }
}