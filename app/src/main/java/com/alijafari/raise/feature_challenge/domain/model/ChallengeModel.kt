package com.alijafari.raise.feature_challenge.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ChallengeType : Parcelable {
    MATH, CAPTCHA
}

@Parcelize
data class ChallengeModel(
    val type: ChallengeType,
    val difficulty: Int = 2,
    val repeats: Int = 1,
    val data: String = "",
) : Parcelable