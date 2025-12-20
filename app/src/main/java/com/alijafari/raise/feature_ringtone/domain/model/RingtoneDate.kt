package com.alijafari.raise.feature_ringtone.domain.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RingtoneData(val name: String? = null, val uri: Uri? = null) : Parcelable