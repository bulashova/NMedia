package ru.netology.nmedia.activity

import kotlin.math.truncate

object Count {
    fun formatCount(count: Int): String =
        when {
            count in 1_000..9_999 -> if (count % 1000 >= 100) "${truncate((count / 100).toDouble()) / 10.0}K" else "${count / 1_000}K"
            count in 10_000..999_999 -> "${count / 1_000}K"
            count >= 1_000_000 -> if (count % 1_000_000 >= 100_000) "${truncate((count / 100_000).toDouble()) / 10.0}M" else "${count / 1_000_000}M"
            else -> count.toString()
        }
}