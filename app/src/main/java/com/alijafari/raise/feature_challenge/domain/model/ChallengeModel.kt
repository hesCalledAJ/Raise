package com.alijafari.raise.feature_challenge.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ChallengeType : Parcelable {
    MATH, MEMORY, SHAKE // Example types
}

@Parcelize
data class ChallengeModel(
    val type: ChallengeType,
    val difficulty: Int,
    val repeats: Int,
    val data: String,
) : Parcelable