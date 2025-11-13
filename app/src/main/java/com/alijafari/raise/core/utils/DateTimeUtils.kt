package com.alijafari.raise.core.utils

import android.content.Context
import com.alijafari.raise.R
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import java.util.Calendar
import java.util.Locale

fun getTimeString(hour : Int , minute : Int) = String.format("%02d:%02d", hour, minute)
fun Alarm.getTimeString() = getTimeString(hour,minute)
fun Context.getRelativeNextRingText(nextTriggerMillis: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = nextTriggerMillis - now
    if (diffMillis <= 0) return getString(R.string.alarm_ringing_now)

    val minutes = diffMillis / 60000
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> resources.getQuantityString(R.plurals.next_ring_days, days.toInt(), days)
        hours > 0 -> resources.getQuantityString(R.plurals.next_ring_hours, hours.toInt(), hours)
        minutes > 0 -> resources.getQuantityString(R.plurals.next_ring_minutes, minutes.toInt(), minutes)
        else -> getString(R.string.next_ring_less_than_minute)
    }
}

object WeekdayUtils {

    fun getLocalizedWeekdays(locale: Locale = Locale.getDefault(), style : Int = Calendar.SHORT): List<Pair<Int, String>> {
        val cal = Calendar.getInstance(locale)
        val firstDay = cal.firstDayOfWeek
        val days = (0 until 7).map { ((firstDay + it - 1) % 7) + 1 }

        return days.map { day ->
            cal.set(Calendar.DAY_OF_WEEK, day)
            val name = cal.getDisplayName(Calendar.DAY_OF_WEEK, style, locale) ?: day.toString()
            day to name
        }
    }
    fun formatSelectedDays(days: List<Int>, locale: Locale = Locale.getDefault()): String {
        if (days.isEmpty()) return "No repeat"

        val shortDayNames = getLocalizedWeekdays(locale)
        val longDayNames = getLocalizedWeekdays(locale, Calendar.LONG)
        val ordered = shortDayNames.map { it.first }
        val sorted = days.sortedBy { ordered.indexOf(it) }

        val indices = sorted.map { ordered.indexOf(it) }
        val consecutive = indices.zipWithNext().all { (a, b) -> b == a + 1 }

        return when {
            sorted.size == 7 -> "Every day"
            sorted.size == 6 -> {
                val missing = ordered.first { it !in sorted }
                val missingShort = longDayNames.first { it.first == missing }.second
                "Except $missingShort"
            }
            sorted.size == 1 -> longDayNames.first { it.first == sorted.first() }.second
            sorted.size == 2 -> sorted.joinToString(" & ") { d ->
                longDayNames.first { it.first == d }.second
            }
            consecutive -> {
                val start = longDayNames.first { it.first == sorted.first() }.second
                val end = longDayNames.first { it.first == sorted.last() }.second
                "$start-$end"
            }

            else -> sorted.joinToString(", ") { d ->
                longDayNames.first { it.first == d }.second
            }
        }
    }

}
