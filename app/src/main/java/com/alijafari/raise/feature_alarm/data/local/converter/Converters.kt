package com.alijafari.raise.feature_alarm.data.local.converter

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter
import com.alijafari.raise.feature_alarm.domain.model.OffsetData
import com.alijafari.raise.feature_alarm.domain.model.TimeBombData
import com.alijafari.raise.feature_challenge.domain.model.ChallengeModel
import com.alijafari.raise.feature_challenge.domain.model.ChallengeType
import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData

class Converters {

    private companion object {
        const val SEPARATOR_COMPLEX = "@@@&&"
        const val SEPARATOR_SIMPLE = ","
        const val SEPARATOR_LIST = "|||"
    }

    @TypeConverter
    fun challengeToString(challenge: ChallengeModel): String {
        return listOf(
            challenge.type.name,
            challenge.difficulty.toString(),
            challenge.repeats.toString(),
            challenge.data
        ).joinToString(SEPARATOR_COMPLEX)
    }

    @TypeConverter
    fun stringToChallenge(value: String): ChallengeModel? {
        if (value.isBlank()) return null
        val parts = value.split(SEPARATOR_COMPLEX)
        return try {
            ChallengeModel(
                type = ChallengeType.valueOf(parts[0]),
                difficulty = parts[1].toInt(),
                repeats = parts[2].toInt(),
                data = parts.getOrNull(3).orEmpty()
            )
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun challengeListToString(list: ArrayList<ChallengeModel>): String =
        list.joinToString(SEPARATOR_LIST) { challengeToString(it) }

    @TypeConverter
    fun stringToChallengeList(value: String): ArrayList<ChallengeModel> {
        if (value.isBlank()) return arrayListOf()
        return ArrayList(value.split(SEPARATOR_LIST).mapNotNull { stringToChallenge(it) })
    }

    @TypeConverter
    fun intListToString(list: List<Int>): String =
        list.joinToString(SEPARATOR_SIMPLE)

    @TypeConverter
    fun stringToIntList(value: String): List<Int> =
        if (value.isBlank()) emptyList()
        else value.split(SEPARATOR_SIMPLE).mapNotNull { it.toIntOrNull() }

    @TypeConverter
    fun ringtoneDataToString(data: RingtoneData): String =
        "${data.name}$SEPARATOR_COMPLEX${data.uri}"

    @TypeConverter
    fun stringToRingtoneData(value: String): RingtoneData =
        if (value.isBlank()) RingtoneData()
        else {
            val parts = value.split(SEPARATOR_COMPLEX)
            RingtoneData(
                name = parts.getOrNull(0).orEmpty(),
                uri = parts.getOrNull(1)?.toUri() ?: Uri.EMPTY
            )
        }

    @TypeConverter
    fun offsetDataToString(data: OffsetData): String =
        "${data.enabled}$SEPARATOR_COMPLEX${data.range.first}$SEPARATOR_COMPLEX${data.range.last}"

    @TypeConverter
    fun stringToOffsetData(value: String): OffsetData =
        if (value.isBlank()) OffsetData()
        else {
            val parts = value.split(SEPARATOR_COMPLEX)
            val enabled = parts.getOrNull(0)?.toBooleanStrictOrNull() ?: false
            val start = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val end = parts.getOrNull(2)?.toIntOrNull() ?: 0
            OffsetData(enabled = enabled, range = start..end)
        }

    @TypeConverter
    fun timeBombDataToString(data: TimeBombData): String =
        "${data.enabled}$SEPARATOR_COMPLEX${data.delaySeconds}"

    @TypeConverter
    fun stringToTimeBombData(value: String): TimeBombData =
        if (value.isBlank()) TimeBombData()
        else {
            val parts = value.split(SEPARATOR_COMPLEX)
            val enabled = parts.getOrNull(0)?.toBooleanStrictOrNull() ?: false
            val delay = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(30, 120) ?: 60
            TimeBombData(enabled = enabled, delaySeconds = delay)
        }
}