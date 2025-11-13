package com.alijafari.raise.feature_alarm.data.local.converter

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter
import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData

class Converters {
    @TypeConverter
    fun fromIntList(days: List<Int>): String =
        days.joinToString(",") { it.toString() }

    @TypeConverter
    fun toIntList(value: String): List<Int> =
        if (value.isEmpty()) emptyList()
        else value.split(",").map { it.toInt() }

    @TypeConverter
    fun fromRingtoneData(ringtoneData: RingtoneData): String =
        "${ringtoneData.name}@@@&&${ringtoneData.uri}"

    @TypeConverter
    fun toRingtoneData(value: String): RingtoneData {
        val parts = value.split("@@@&&")
        return RingtoneData(
            name = parts.getOrNull(0).orEmpty(),
            uri = parts.getOrNull(1)?.toUri() ?: Uri.EMPTY
        )
    }

}