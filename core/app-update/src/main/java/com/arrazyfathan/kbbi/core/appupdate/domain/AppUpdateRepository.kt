package com.arrazyfathan.kbbi.core.appupdate.domain

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError

interface AppUpdateRepository {
    suspend fun checkForUpdate(
        currentVersion: String,
        force: Boolean = false,
    ): AppResult<AppUpdate?, DataError>
}
