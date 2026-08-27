package com.arrazyfathan.kbbi.core.appupdate.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ApkIdentityValidatorTest {
    private val installed = ApkIdentity("com.arrazyfathan.kbbi", 10L, setOf("release-signer"))

    @Test
    fun `newer apk with matching package and signer is valid`() {
        val candidate = ApkIdentity("com.arrazyfathan.kbbi", 11L, setOf("release-signer"))

        assertEquals(ApkValidationResult.VALID, ApkIdentityValidator.validate(installed, candidate))
    }

    @Test
    fun `apk for a different application is rejected`() {
        val candidate = ApkIdentity("com.arrazyfathan.kbbi.dev", 11L, setOf("release-signer"))

        assertEquals(ApkValidationResult.WRONG_PACKAGE, ApkIdentityValidator.validate(installed, candidate))
    }

    @Test
    fun `apk without a newer version code is rejected`() {
        val candidate = ApkIdentity("com.arrazyfathan.kbbi", 10L, setOf("release-signer"))

        assertEquals(ApkValidationResult.VERSION_NOT_NEWER, ApkIdentityValidator.validate(installed, candidate))
    }

    @Test
    fun `apk signed by a different signer is rejected`() {
        val candidate = ApkIdentity("com.arrazyfathan.kbbi", 11L, setOf("other-signer"))

        assertEquals(ApkValidationResult.SIGNER_MISMATCH, ApkIdentityValidator.validate(installed, candidate))
    }
}
