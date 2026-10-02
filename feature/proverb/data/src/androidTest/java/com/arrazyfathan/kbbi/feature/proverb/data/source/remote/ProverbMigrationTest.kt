package com.arrazyfathan.kbbi.feature.proverb.data.source.remote

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arrazyfathan.kbbi.feature.proverb.data.di.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProverbMigrationTest {
    @Test
    fun migrationPreservesExistingDetailsAndAddsDefaultAttribution() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper =
            FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name(null)
                    .callback(
                        object : SupportSQLiteOpenHelper.Callback(1) {
                            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                                db.execSQL(
                                    "CREATE TABLE cached_proverb_detail_table (slug TEXT NOT NULL PRIMARY KEY, " +
                                        "text TEXT NOT NULL, letter TEXT NOT NULL, sourceUrl TEXT, meaning TEXT)",
                                )
                            }

                            override fun onUpgrade(
                                db: androidx.sqlite.db.SupportSQLiteDatabase,
                                oldVersion: Int,
                                newVersion: Int,
                            ) = Unit
                        },
                    ).build(),
            )
        try {
            val db = helper.writableDatabase
            db.execSQL(
                "INSERT INTO cached_proverb_detail_table VALUES " +
                    "('air', 'Air beriak tanda tak dalam', 'A', 'https://example.com/air', 'Orang banyak bicara')",
            )

            MIGRATION_1_2.migrate(db)

            db.query("SELECT text, meaning, aiGenerated, notice FROM cached_proverb_detail_table WHERE slug = 'air'").use { cursor ->
                assertEquals(1, cursor.count)
                cursor.moveToFirst()
                assertEquals("Air beriak tanda tak dalam", cursor.getString(0))
                assertEquals("Orang banyak bicara", cursor.getString(1))
                assertEquals(0, cursor.getInt(2))
                assertEquals(null, cursor.getString(3))
            }
        } finally {
            helper.close()
        }
    }
}
