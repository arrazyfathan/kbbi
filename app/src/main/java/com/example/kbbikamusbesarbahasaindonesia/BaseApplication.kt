package com.example.kbbikamusbesarbahasaindonesia

import android.app.Application
import com.example.kbbikamusbesarbahasaindonesia.database.KataDatabase
import com.example.kbbikamusbesarbahasaindonesia.repository.KataRepository

class BaseApplication : Application() {
    val databasee by lazy { KataDatabase.getInstance(this) }
    val repository by lazy { KataRepository(databasee.kataDao()) }
}
