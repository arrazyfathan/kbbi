package com.arrazyfathan.kbbi.core.presentation.ui

import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.domain.model.DataError

fun DataError.asUiText(): UiText =
    when (this) {
        DataError.EmptyQuery -> UiText.StringResource(R.string.error_empty_query)
        DataError.NoInternet -> UiText.StringResource(R.string.error_no_internet)
        DataError.BadRequest -> UiText.StringResource(R.string.error_bad_request)
        DataError.RequestTimeout -> UiText.StringResource(R.string.error_request_timeout)
        DataError.Unauthorized -> UiText.StringResource(R.string.error_unauthorized)
        DataError.Forbidden -> UiText.StringResource(R.string.error_forbidden)
        DataError.NotFound -> UiText.StringResource(R.string.error_not_found)
        DataError.Conflict -> UiText.StringResource(R.string.error_conflict)
        DataError.TooManyRequests -> UiText.StringResource(R.string.error_too_many_requests)
        DataError.PayloadTooLarge -> UiText.StringResource(R.string.error_payload_too_large)
        DataError.ServerError -> UiText.StringResource(R.string.error_server_error)
        DataError.ServiceUnavailable -> UiText.StringResource(R.string.error_service_unavailable)
        DataError.EmptyBody -> UiText.StringResource(R.string.error_empty_body)
        DataError.Serialization -> UiText.StringResource(R.string.error_serialization)
        is DataError.Remote -> UiText.DynamicString(message)
        DataError.Unknown -> UiText.StringResource(R.string.error_unknown)
    }
