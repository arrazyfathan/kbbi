package com.arrazyfathan.kbbi.feature.home.data.source.remote

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

interface VisitorIdProvider {
    fun getVisitorId(): String
}

private const val VISITOR_PREFS_NAME = "kbbi_visitor"
private const val VISITOR_ID_KEY = "visitor_id"

class SharedPreferencesVisitorIdProvider(
    context: Context,
) : VisitorIdProvider {
    private val preferences =
        context.applicationContext.getSharedPreferences(VISITOR_PREFS_NAME, Context.MODE_PRIVATE)

    override fun getVisitorId(): String {
        preferences.getString(VISITOR_ID_KEY, null)?.let { return it }

        val visitorId = UUID.randomUUID().toString()
        preferences
            .edit {
                putString(VISITOR_ID_KEY, visitorId)
            }
        return visitorId
    }
}
