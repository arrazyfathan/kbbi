package com.arrazyfathan.kbbi.core.appupdate.domain

object AppVersionComparator {
    fun isNewer(
        latestVersion: String,
        currentVersion: String,
    ): Boolean =
        compare(
            latest = latestVersion.toComparableVersion(),
            current = currentVersion.toComparableVersion(),
        ) > 0

    fun normalize(version: String): String? {
        val normalized =
            version
                .trim()
                .removePrefix("v")
                .removePrefix("V")
                .substringBefore("-")
                .substringBefore("+")

        return normalized.takeIf { it.matches(Regex("""\d+(\.\d+)*""")) }
    }

    private fun String.toComparableVersion(): List<Int>? =
        normalize(this)
            ?.split(".")
            ?.map { part -> part.toIntOrNull() ?: return null }

    private fun compare(
        latest: List<Int>?,
        current: List<Int>?,
    ): Int {
        if (latest == null || current == null) return 0

        val maxSize = maxOf(latest.size, current.size)
        val firstDifferentIndex =
            (0 until maxSize).firstOrNull { index ->
                latest.getOrElse(index) { 0 } != current.getOrElse(index) { 0 }
            }

        return firstDifferentIndex?.let { index ->
            latest.getOrElse(index) { 0 }.compareTo(current.getOrElse(index) { 0 })
        } ?: 0
    }
}
