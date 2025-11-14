package com.alijafari.raise.feature_ringtone.domain.infrastructure

interface SystemVolumeManager {
    fun setVolumeForType(volume : Int, type : Int,showUi : Boolean = false)
    fun setMaxVolumeForType(type : Int,showUi : Boolean = false)
    fun getMaxVolumeForType(type : Int) : Int
    fun getVolumeForType(type : Int) : Int
}