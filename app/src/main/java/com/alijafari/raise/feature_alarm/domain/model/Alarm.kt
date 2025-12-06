package com.alijafari.raise.feature_alarm.domain.model




import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData
import java.util.Calendar
import kotlin.random.Random


data class Alarm(
    val id: Int = 0,
    var hour: Int = 7,
    var minute: Int = 0,
    val snoozeCount : Int = 0,
    val snoozeMinutes : Int = 5,
    val vibrate : Boolean = true,
    val isEnabled: Boolean = true,
    val ringtoneVolume : Float = 0.9f,
    val label: String = "Good Morning !",
    val ringtoneData : RingtoneData? = null,
    val repeatDays : List<Int> = emptyList(),
    val smartOffsetData : OffsetData = OffsetData(),
    val timeBombData : TimeBombData = TimeBombData(),
){
    fun getNextActualTriggerAtMillis(lastNominal: Long? = null): Long {
        val base = Calendar.getInstance()

        // if actual alarm time was shifted cause of Smart Offset , start counting forward from it, not from NOW
        if (lastNominal != null && lastNominal > 0) {
            base.timeInMillis = lastNominal
        }

        val alarmCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = Calendar.getInstance()
        if (repeatDays.isEmpty()) {
            if (alarmCal.before(now)) alarmCal.add(Calendar.DAY_OF_YEAR, 1)
        } else {
            val today = now.get(Calendar.DAY_OF_WEEK)
            val sorted = repeatDays.sorted()
            val nextDay = sorted.firstOrNull { it > today } ?: sorted.first()
            var daysUntil = (nextDay - today + 7) % 7
            if (daysUntil == 0 && alarmCal.before(now)) daysUntil = 7
            alarmCal.add(Calendar.DAY_OF_YEAR, daysUntil)
        }

        return alarmCal.timeInMillis
    }
    
    fun getRandomSmartOffsetMillis() = if (smartOffsetData.enabled) Random.nextInt(smartOffsetData.range.first,smartOffsetData.range.last) * 60 * 1000 else 0
}
