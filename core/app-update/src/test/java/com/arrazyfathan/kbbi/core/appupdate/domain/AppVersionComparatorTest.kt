package com.arrazyfathan.kbbi.core.appupdate.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionComparatorTest {
    @Test
    fun `v1 2 0 is newer than 1 1 9`() {
        assertTrue(AppVersionComparator.isNewer("v1.2.0", "1.1.9"))
    }

    @Test
    fun `v1 10 0 is newer than 1 9 9`() {
        assertTrue(AppVersionComparator.isNewer("v1.10.0", "1.9.9"))
    }

    @Test
    fun `v1 0 0 is not newer than 1 0 0`() {
        assertFalse(AppVersionComparator.isNewer("v1.0.0", "1.0.0"))
    }

    @Test
    fun `malformed version is not newer`() {
        assertFalse(AppVersionComparator.isNewer("release-latest", "1.0.0"))
    }

    @Test
    fun `dev suffix does not crash parser`() {
        assertFalse(AppVersionComparator.isNewer("v1.2.0", "1.2.0-dev.4"))
    }

    @Test
    fun `higher major version is a major update`() {
        assertTrue(AppVersionComparator.isMajorUpdate("6.0", "5.24.3"))
    }

    @Test
    fun `higher minor version is not a major update`() {
        assertFalse(AppVersionComparator.isMajorUpdate("5.25", "5.24"))
    }

    @Test
    fun `equivalent versions with omitted trailing zero are the same`() {
        assertTrue(AppVersionComparator.isSameVersion("v5.24", "5.24.0"))
    }
}
