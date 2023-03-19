package com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.room

import androidx.room.TypeConverter
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.ListWordEntity
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.MeaningEntity
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.WordEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    companion object {

        @JvmStatic
        @TypeConverter
        fun toWordList(string: String?): List<ListWordEntity>? {
            val listType = object : TypeToken<List<ListWordEntity>>() {}.type
            return if (string != null) {
                Gson().fromJson<List<ListWordEntity>>(string, listType)
            } else {
                null
            }
        }

        @JvmStatic
        @TypeConverter
        fun toListWord(string: String?): List<WordEntity>? {
            val listType = object : TypeToken<List<WordEntity>>() {}.type
            return if (string != null) {
                Gson().fromJson<List<WordEntity>>(string, listType)
            } else {
                null
            }
        }

        @JvmStatic
        @TypeConverter
        fun fromListWord(list: List<WordEntity>?): String? {
            val type = object : TypeToken<List<WordEntity>>() {}.type
            return if (list != null) {
                Gson().toJson(list, type)
            } else {
                null
            }
        }

        @JvmStatic
        @TypeConverter
        fun fromWordList(list: List<ListWordEntity>?): String? {
            val type = object : TypeToken<List<ListWordEntity>>() {}.type
            return if (list != null) {
                Gson().toJson(list, type)
            } else {
                null
            }
        }

        @JvmStatic
        @TypeConverter
        fun toListMeanings(string: String?): List<MeaningEntity>? {
            val listType = object : TypeToken<List<MeaningEntity>>() {}.type
            return if (string != null) {
                Gson().fromJson<List<MeaningEntity>>(string, listType)
            } else {
                null
            }
        }

        @JvmStatic
        @TypeConverter
        fun fromListMeanings(list: List<MeaningEntity>?): String? {
            val type = object : TypeToken<List<MeaningEntity>>() {}.type
            return if (list != null) {
                Gson().toJson(list, type)
            } else {
                null
            }
        }
    }
}
