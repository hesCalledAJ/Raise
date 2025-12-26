package com.alijafari.raise.feature_challenge.data.model

import com.alijafari.raise.feature_challenge.domain.model.ChallengeProvider
object MathProvider : ChallengeProvider {
    override fun generate(difficulty: Int): Pair<String, String> {
        return when (difficulty) {
            0 -> {
                val a = r(2, 9); val b = r(2, 9)
                "$a + $b" to (a + b).toString()
            }

            1 -> {
                val a = r(10, 30); val b = r(10, 30); val c = r(2, 10)
                "$a + $b - $c" to (a + b - c).toString()
            }

            2 -> {
                val a = r(10, 20); val b = r(2, 6); val c = r(3, 7)
                "$a + $b × $c" to (a + (b * c)).toString()
            }

            3 -> {
                val a = r(3, 8); val b = r(15, 30); val c = r(2, 12)
                "$a × ($b - $c)" to (a * (b - c)).toString()
            }

            else -> {
                val a = r(3, 6); val b = r(4, 9); val c = r(10, 20); val d = r(2, 5)
                "($a × $b) + ($c × $d)" to ((a * b) + (c * d)).toString()
            }
        }
    }

    private fun r(start: Int, end: Int) = (start..end).random()
}