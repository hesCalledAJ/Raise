package com.alijafari.raise.feature_alarm.presentation

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_alarm.domain.usecases.AlarmUseCases
import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData
import com.alijafari.raise.feature_ringtone.domain.usecases.RingtoneUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ScheduleExactAlarm")
@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val useCases: AlarmUseCases,
    private val ringtoneUseCases: RingtoneUseCases
) : ViewModel() {

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms = _alarms
        .map { list -> list.sortedBy { it.getNextTriggerAtMillis() }.sortedByDescending { it.isEnabled } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _editingAlarm = MutableStateFlow<Alarm?>(null)
    val editingAlarm: StateFlow<Alarm?> = _editingAlarm.asStateFlow()

    init {
        viewModelScope.launch {
            useCases.getAll.invoke().collect { list ->
                _alarms.value = list
                if (list.isNotEmpty()) {
                    list.filter { it.isEnabled }.forEach { alarm ->
                        useCases.cancel(alarm)
                        useCases.schedule(alarm)
                    }
                }
            }
        }
    }


    fun openEditor(alarm: Alarm?) {
        loadRingtonesIfNeeded()
        _editingAlarm.value = alarm ?: Alarm()
    }

    fun hideEditor() {
        _editingAlarm.value = null
    }

    fun setEditingAlarmEdit(newInstance : Alarm){
        _editingAlarm.value = newInstance
    }

    fun toggleAlarm(alarm: Alarm) {
        if (alarm.isEnabled) useCases.cancel(alarm)
        saveAlarm(alarm.copy(isEnabled = alarm.isEnabled.not()))
    }

    fun saveEditingAlarm() {
        saveAlarm(editingAlarm.value?.copy(isEnabled = true) ?: return)
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                useCases.delete(alarm)
                hideEditor()
            }
        }
    }

    private val _deviceRingtones = MutableStateFlow<List<RingtoneData>>(emptyList())
    val deviceRingtones: StateFlow<List<RingtoneData>> = _deviceRingtones
    val deviceDefaultRingtones: RingtoneData = ringtoneUseCases.getDeviceDefaultRingtone()

    private var deviceRingtonesLoaded = false

    fun loadRingtonesIfNeeded() {
        if (deviceRingtonesLoaded) return
        viewModelScope.launch {
            _deviceRingtones.value = ringtoneUseCases.getDeviceRingtones()
            deviceRingtonesLoaded = true
        }
    }
    private fun saveAlarm(alarm: Alarm) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                useCases.upsert(alarm)
                hideEditor()
            }
        }
    }
}

