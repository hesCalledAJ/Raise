package com.alijafari.raise.feature_challenge.domain.model

interface ChallengeProvider {
    fun generate(difficulty: Int): Pair<String, String>
}