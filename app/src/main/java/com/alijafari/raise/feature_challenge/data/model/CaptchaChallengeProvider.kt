package com.alijafari.raise.feature_challenge.data.model

import com.alijafari.raise.feature_challenge.domain.model.ChallengeProvider

object CaptchaProvider : ChallengeProvider {
    private val charPool = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    private val complexPool = charPool + "!@#$%^&*()_+-=[]{}|;:,.<>?"

    override fun generate(difficulty: Int): Pair<String,Int> {
        val length = when (difficulty) {
            0 -> 4
            1 -> 6
            2 -> 8
            3 -> 10
            else -> 12
        }
        val pool = if (difficulty > 2) complexPool else charPool
        return (1..length)
            .map { pool.random() }
            .joinToString("") to 0
    }
}