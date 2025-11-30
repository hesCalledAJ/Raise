package com.alijafari.raise.feature_ringtone.data.repository

import android.content.Context
import android.media.RingtoneManager
import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData
import com.alijafari.raise.feature_ringtone.domain.repository.RingtoneRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import java.lang.Exception

class RingtoneRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ringtoneManager: RingtoneManager
) : RingtoneRepository {
    override suspend fun getDeviceRingtones(): List<RingtoneData> {
        try {
            ringtoneManager.setType(RingtoneManager.TYPE_ALARM)
        }catch (_: Exception){

        }
        val cursor = ringtoneManager.cursor
        val ringtones = mutableListOf<RingtoneData>()

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = ringtoneManager.getRingtoneUri(cursor.position)
            ringtones.add(RingtoneData(name = title, uri = uri))
        }

        // Adding default if its missing
        val defaultUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        if (defaultUri != null && ringtones.none { it.uri == defaultUri }) {
            ringtones.add(0, getDeviceDefaultRingtone())
        }

        return ringtones
    }


    override fun getDeviceDefaultRingtone(): RingtoneData {
        val defaultUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(context, defaultUri)
        val title = ringtone?.getTitle(context)
        return RingtoneData(name = title, uri = defaultUri)
    }
}