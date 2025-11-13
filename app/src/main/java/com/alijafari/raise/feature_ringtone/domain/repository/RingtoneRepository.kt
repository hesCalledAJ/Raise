package com.alijafari.raise.feature_ringtone.domain.repository

import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData

interface RingtoneRepository {
    suspend fun getDeviceRingtones() : List<RingtoneData>
    fun getDeviceDefaultRingtone() : RingtoneData
}