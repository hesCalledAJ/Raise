package com.alijafari.raise.feature_challenge.data.model

import com.alijafari.raise.feature_challenge.domain.model.ChallengeProvider
import com.alijafari.raise.feature_challenge.domain.model.ChallengeType

object ChallengeFactory {
    fun getProvider(type: ChallengeType): ChallengeProvider {
        return when (type) {
            ChallengeType.MATH -> MathProvider
            ChallengeType.CAPTCHA -> CaptchaProvider
        }
    }

    fun generateChallengeData(type: ChallengeType, difficulty: Int): Pair<String, String> {
        return getProvider(type).generate(difficulty)
    }
}