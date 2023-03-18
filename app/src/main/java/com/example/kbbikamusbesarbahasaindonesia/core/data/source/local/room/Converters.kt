package com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.room

import androidx.room.TypeConverter
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.MeaningEntity
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.WordEntity
import com.example.kbbikamusbesarbahasaindonesia.model.Arti
import com.example.kbbikamusbesarbahasaindonesia.model.Data
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    companion object {

        @JvmStatic
        @TypeConverter
        fun toListData(string: String?): List<Data>? {
            val listType = object : TypeToken<List<Data>>() {}.type
            return if (string != null) {
                Gson().fromJson<List<Data>>(string, listType)
            } else {
                null
            }
        }

        @JvmStatic
        @TypeConverter
        fun toListWord(string: String?): List<WordEntity>? {
            val listType = object : TypeToken<List<Data>>() {}.type
            return if (string != null) {
                Gson().fromJson<List<WordEntity>>(string, listType)
            } else {
                null
            }
        }

        @JvmStatic
        @TypeConverter
        fun fromListData(list: List<Data>?): String? {
            val type = object : TypeToken<List<Data>>() {}.type
            return if (list != null) {
                Gson().toJson(list, type)
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
        fun toListArti(string: String?): List<Arti>? {
            val listType = object : TypeToken<List<Arti>>() {}.type
            return if (string != null) {
                Gson().fromJson<List<Arti>>(string, listType)
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
        fun fromListArti(list: List<Arti>?): String? {
            val type = object : TypeToken<List<Arti>>() {}.type
            return if (list != null) {
                Gson().toJson(list, type)
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
