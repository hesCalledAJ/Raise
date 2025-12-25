package com.alijafari.raise.core.utils

import com.alijafari.raise.feature_challenge.domain.model.ChallengeType
import com.alijafari.raise.R

class ChallengeUtils {
    companion object {
        fun difficultyLevel(difficulty: Int) = when (difficulty) {
            0 -> "Lite"
            1 -> "Easy"
            2 -> "Medium"
            3 -> "Hard"
            else -> "Insane"
        }
        fun nameStringResource(type : ChallengeType) = when (type) {
            ChallengeType.MATH -> R.string.challenge_math
            ChallengeType.CAPTCHA -> R.string.challenge_captcha
        }
    }
}