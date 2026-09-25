package com.arrazyfathan.kbbi.feature.home.data.di

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WordMigrationTest {
    @Test
    fun migrationMarksExistingSavedAndCachedWordsAsOrdinary() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(null)
                .callback(object : SupportSQLiteOpenHelper.Callback(10) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE word_table (word TEXT NOT NULL PRIMARY KEY, " +
                                "listWords TEXT NOT NULL, visitorCount INTEGER, isSaved INTEGER NOT NULL)",
                        )
                    }

                    override fun onUpgrade(
                        db: androidx.sqlite.db.SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) = Unit
                }).build(),
        )
        try {
            val db = helper.writableDatabase
            db.execSQL("INSERT INTO word_table VALUES ('saved', '[]', 1, 1)")
            db.execSQL("INSERT INTO word_table VALUES ('cached', '[]', 2, 0)")

            MIGRATION_10_11.migrate(db)

            db.query("SELECT word, isSaved, aiGenerated FROM word_table ORDER BY word").use { cursor ->
                assertEquals(2, cursor.count)
                cursor.moveToFirst()
                assertEquals("cached", cursor.getString(0))
                assertFalse(cursor.getInt(1) != 0)
                assertFalse(cursor.getInt(2) != 0)
                cursor.moveToNext()
                assertEquals("saved", cursor.getString(0))
                assertEquals(1, cursor.getInt(1))
                assertEquals(0, cursor.getInt(2))
            }
        } finally {
            helper.close()
        }
    }
}
