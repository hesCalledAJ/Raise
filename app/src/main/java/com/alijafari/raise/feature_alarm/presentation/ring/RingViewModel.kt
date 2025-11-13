package com.alijafari.raise.feature_alarm.presentation.ring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alijafari.raise.feature_alarm.data.service.AlarmService
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RingViewModel @Inject constructor() : ViewModel() {


    private val _isSnoozed = MutableStateFlow(false)
    val isSnoozed: StateFlow<Boolean> = _isSnoozed.asStateFlow()

    private val _alarm : MutableStateFlow<Alarm?> = MutableStateFlow(null)
    val alarm : StateFlow<Alarm?> = _alarm.asStateFlow()

    private var serviceRef: AlarmService? = null
    private var serviceJob: Job? = null

    fun attachService(service: AlarmService) {
        serviceRef = service

        _alarm.value = service.alarm

        serviceJob?.cancel()
        serviceJob = viewModelScope.launch {
            launch {
                service.isSnoozed.collect { _isSnoozed.value = it }
            }
        }
    }

    fun detachService() {
        serviceJob?.cancel()
        serviceJob = null
        serviceRef = null
    }

    fun onSnooze() {
        serviceRef?.handleSnooze()
    }

    fun onSkipSnooze() {
        serviceRef?.handleSkipSnooze()
    }

    fun onDismiss() {
        serviceRef?.handleDismiss()
    }
}
