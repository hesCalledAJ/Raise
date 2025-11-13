package com.alijafari.raise.feature_ringtone.domain.usecases

import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData
import com.alijafari.raise.feature_ringtone.domain.repository.RingtoneRepository

data class RingtoneUseCases(
    val getDeviceRingtones : GetDeviceRingtonesUseCase,
    val getDeviceDefaultRingtone: GetDeviceDefaultRingtoneUseCase
)

class GetDeviceRingtonesUseCase(private val repo: RingtoneRepository) {
    suspend operator fun invoke(): List<RingtoneData> = repo.getDeviceRingtones()
}
class GetDeviceDefaultRingtoneUseCase(private val repo: RingtoneRepository) {
    operator fun invoke(): RingtoneData = repo.getDeviceDefaultRingtone()
}