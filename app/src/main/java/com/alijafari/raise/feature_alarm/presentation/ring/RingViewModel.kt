package com.alijafari.raise.feature_alarm.presentation.ring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alijafari.raise.core.utils.tickerFlow
import com.alijafari.raise.feature_alarm.data.service.AlarmService
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RingViewModel @Inject constructor() : ViewModel() {


    private val _isSnoozed = MutableStateFlow(false)
    val isSnoozed: StateFlow<Boolean> = _isSnoozed.asStateFlow()

    private val _snoozeUntil = MutableStateFlow<Long?>(null)
    val snoozeUntil = _snoozeUntil.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val snoozeRemaining = snoozeUntil
        .filterNotNull()
        .flatMapLatest { target ->
            tickerFlow(1000).map { target - System.currentTimeMillis() }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )


    private val _alarm : MutableStateFlow<Alarm?> = MutableStateFlow(null)
    val alarm : StateFlow<Alarm?> = _alarm.asStateFlow()

    private var serviceRef: AlarmService? = null
    private var serviceJob: Job? = null

    fun attachService(service: AlarmService) {
        serviceRef = service

        _alarm.value = service.alarm

        serviceJob?.cancel()
        serviceJob = viewModelScope.launch {
            service.snoozeUntil.let { _snoozeUntil.value = it }
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
    fun hideHeadsUpNotification(){
        serviceRef?.hideHeadsUpNotification()
    }
}
