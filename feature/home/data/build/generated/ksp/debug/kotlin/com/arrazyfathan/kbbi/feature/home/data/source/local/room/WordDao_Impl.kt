package com.arrazyfathan.kbbi.feature.home.`data`.source.local.room

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.arrazyfathan.kbbi.feature.home.`data`.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.`data`.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.`data`.source.local.entity.WordEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WordDao_Impl(
  __db: RoomDatabase,
) : WordDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfListWordEntity: EntityUpsertAdapter<ListWordEntity>

  private val __upsertAdapterOfHistoryEntity: EntityUpsertAdapter<HistoryEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfListWordEntity = EntityUpsertAdapter<ListWordEntity>(object : EntityInsertAdapter<ListWordEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `word_table` (`word`,`listWords`,`isSaved`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ListWordEntity) {
        statement.bindText(1, entity.word)
        val _tmp: String? = Converters.fromListWord(entity.listWords)
        if (_tmp == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmp)
        }
        val _tmp_1: Int = if (entity.isSaved) 1 else 0
        statement.bindLong(3, _tmp_1.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<ListWordEntity>() {
      protected override fun createQuery(): String = "UPDATE `word_table` SET `word` = ?,`listWords` = ?,`isSaved` = ? WHERE `word` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ListWordEntity) {
        statement.bindText(1, entity.word)
        val _tmp: String? = Converters.fromListWord(entity.listWords)
        if (_tmp == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmp)
        }
        val _tmp_1: Int = if (entity.isSaved) 1 else 0
        statement.bindLong(3, _tmp_1.toLong())
        statement.bindText(4, entity.word)
      }
    })
    this.__upsertAdapterOfHistoryEntity = EntityUpsertAdapter<HistoryEntity>(object : EntityInsertAdapter<HistoryEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `history_table` (`word`,`searchedAt`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: HistoryEntity) {
        statement.bindText(1, entity.word)
        statement.bindLong(2, entity.searchedAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<HistoryEntity>() {
      protected override fun createQuery(): String = "UPDATE `history_table` SET `word` = ?,`searchedAt` = ? WHERE `word` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: HistoryEntity) {
        statement.bindText(1, entity.word)
        statement.bindLong(2, entity.searchedAt)
        statement.bindText(3, entity.word)
      }
    })
  }

  public override suspend fun insertHistoryAndTrim(historyEntity: HistoryEntity, limit: Int): Unit = performInTransactionSuspending(__db) {
    super@WordDao_Impl.insertHistoryAndTrim(historyEntity, limit)
  }

  public override suspend fun insertWord(listWordEntity: ListWordEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __upsertAdapterOfListWordEntity.upsertAndReturnId(_connection, listWordEntity)
    _result
  }

  public override suspend fun insertHistory(historyEntity: HistoryEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfHistoryEntity.upsert(_connection, historyEntity)
  }

  public override fun getAllWords(): Flow<List<ListWordEntity>> {
    val _sql: String = "SELECT * FROM word_table"
    return createFlow(__db, false, arrayOf("word_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfWord: Int = getColumnIndexOrThrow(_stmt, "word")
        val _columnIndexOfListWords: Int = getColumnIndexOrThrow(_stmt, "listWords")
        val _columnIndexOfIsSaved: Int = getColumnIndexOrThrow(_stmt, "isSaved")
        val _result: MutableList<ListWordEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ListWordEntity
          val _tmpWord: String
          _tmpWord = _stmt.getText(_columnIndexOfWord)
          val _tmpListWords: List<WordEntity>
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfListWords)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfListWords)
          }
          val _tmp_1: List<WordEntity>? = Converters.toListWord(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'kotlin.collections.List<com.arrazyfathan.kbbi.feature.home.`data`.source.local.entity.WordEntity>', but it was NULL.")
          } else {
            _tmpListWords = _tmp_1
          }
          val _tmpIsSaved: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSaved).toInt()
          _tmpIsSaved = _tmp_2 != 0
          _item = ListWordEntity(_tmpWord,_tmpListWords,_tmpIsSaved)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun checkWordIsExist(word: String): Flow<Boolean> {
    val _sql: String = "SELECT EXISTS (SELECT * FROM word_table WHERE word = ?)"
    return createFlow(__db, false, arrayOf("word_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, word)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getListHistory(): Flow<List<HistoryEntity>> {
    val _sql: String = "SELECT * FROM history_table ORDER BY searchedAt DESC, word DESC"
    return createFlow(__db, false, arrayOf("history_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfWord: Int = getColumnIndexOrThrow(_stmt, "word")
        val _columnIndexOfSearchedAt: Int = getColumnIndexOrThrow(_stmt, "searchedAt")
        val _result: MutableList<HistoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: HistoryEntity
          val _tmpWord: String
          _tmpWord = _stmt.getText(_columnIndexOfWord)
          val _tmpSearchedAt: Long
          _tmpSearchedAt = _stmt.getLong(_columnIndexOfSearchedAt)
          _item = HistoryEntity(_tmpWord,_tmpSearchedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteWord(word: String) {
    val _sql: String = "DELETE FROM word_table WHERE word = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, word)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun trimHistories(limit: Int) {
    val _sql: String = "DELETE FROM history_table WHERE word NOT IN (SELECT word FROM history_table ORDER BY searchedAt DESC, word DESC LIMIT ?)"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
