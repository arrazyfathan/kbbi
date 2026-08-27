package com.arrazyfathan.kbbi.core.appupdate.domain

data class ApkIdentity(
    val packageName: String,
    val versionCode: Long,
    val signerDigests: Set<String>,
)

enum class ApkValidationResult {
    VALID,
    WRONG_PACKAGE,
    VERSION_NOT_NEWER,
    SIGNER_MISMATCH,
}

object ApkIdentityValidator {
    fun validate(
        installed: ApkIdentity,
        candidate: ApkIdentity,
    ): ApkValidationResult =
        when {
            candidate.packageName != installed.packageName -> {
                ApkValidationResult.WRONG_PACKAGE
            }

            candidate.versionCode <= installed.versionCode -> {
                ApkValidationResult.VERSION_NOT_NEWER
            }

            installed.signerDigests.isEmpty() || candidate.signerDigests != installed.signerDigests -> {
                ApkValidationResult.SIGNER_MISMATCH
            }

            else -> {
                ApkValidationResult.VALID
            }
        }
}
