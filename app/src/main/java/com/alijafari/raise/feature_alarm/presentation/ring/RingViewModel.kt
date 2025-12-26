package com.alijafari.raise.feature_alarm.presentation.ring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alijafari.raise.feature_alarm.data.service.ActiveChallenge
import com.alijafari.raise.feature_alarm.data.service.AlarmService
import com.alijafari.raise.feature_alarm.data.service.AlarmStatus
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

@HiltViewModel
class RingViewModel @Inject constructor() : ViewModel() {

    private val _status = MutableStateFlow<AlarmStatus>(AlarmStatus.Ringing)
    val status: StateFlow<AlarmStatus> = _status.asStateFlow()

    private val _alarm: MutableStateFlow<Alarm?> = MutableStateFlow(null)
    val alarm: StateFlow<Alarm?> = _alarm.asStateFlow()

    private val _serviceRef = MutableStateFlow<AlarmService?>(null)
    val serviceRef: AlarmService? get() = _serviceRef.value

    private var serviceJob: Job? = null

    fun verifyAnswer(answer: String): Boolean {
        val state = serviceRef?.state ?: return false
        val isCorrect = state.verifyChallenge(answer)

        if (isCorrect && !state.hasBlockingChallenges()) {
            onDismiss()
        }
        return isCorrect
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeChallenges: StateFlow<List<ActiveChallenge>> = _serviceRef
        .flatMapLatest { service ->
            service?.state?.activeChallenges ?: MutableStateFlow(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun hasChallenges(): Boolean {
        return serviceRef?.state?.hasBlockingChallenges() == true
    }
    fun attachService(service: AlarmService) {
        _serviceRef.value = service
        _alarm.value = service.alarm

        serviceJob?.cancel()
        serviceJob = viewModelScope.launch {
            service.state.status.collect {
                _status.value = it
            }
        }
    }
    fun detachService() {
        _serviceRef.value = null
        serviceJob?.cancel()
    }
    fun onUserInteraction(){
        serviceRef?.state?.cancelTimeBomb()
    }
    fun onSnooze() = serviceRef?.handleSnooze()
    fun onSkipSnooze() = serviceRef?.handleSkipSnooze()
    fun onDismiss() = serviceRef?.handleDismiss()
    fun hideHeadsUpNotification(){
        serviceRef?.hideHeadsUpNotification()
    }
}
