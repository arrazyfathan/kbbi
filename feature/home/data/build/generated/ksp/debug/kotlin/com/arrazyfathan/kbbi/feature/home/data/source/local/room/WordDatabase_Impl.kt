package com.arrazyfathan.kbbi.feature.home.`data`.source.local.room

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WordDatabase_Impl : WordDatabase() {
  private val _wordDao: Lazy<WordDao> = lazy {
    WordDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(8, "15c5e8eab68c39723270f5b866e159af", "6c2c6f8dd39e6cf036841f8a6175f51c") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `word_table` (`word` TEXT NOT NULL, `listWords` TEXT NOT NULL, `isSaved` INTEGER NOT NULL, PRIMARY KEY(`word`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `history_table` (`word` TEXT NOT NULL, `searchedAt` INTEGER NOT NULL, PRIMARY KEY(`word`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '15c5e8eab68c39723270f5b866e159af')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `word_table`")
        connection.execSQL("DROP TABLE IF EXISTS `history_table`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsWordTable: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWordTable.put("word", TableInfo.Column("word", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWordTable.put("listWords", TableInfo.Column("listWords", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWordTable.put("isSaved", TableInfo.Column("isSaved", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWordTable: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWordTable: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoWordTable: TableInfo = TableInfo("word_table", _columnsWordTable, _foreignKeysWordTable, _indicesWordTable)
        val _existingWordTable: TableInfo = read(connection, "word_table")
        if (!_infoWordTable.equals(_existingWordTable)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |word_table(com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity).
              | Expected:
              |""".trimMargin() + _infoWordTable + """
              |
              | Found:
              |""".trimMargin() + _existingWordTable)
        }
        val _columnsHistoryTable: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsHistoryTable.put("word", TableInfo.Column("word", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsHistoryTable.put("searchedAt", TableInfo.Column("searchedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysHistoryTable: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesHistoryTable: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoHistoryTable: TableInfo = TableInfo("history_table", _columnsHistoryTable, _foreignKeysHistoryTable, _indicesHistoryTable)
        val _existingHistoryTable: TableInfo = read(connection, "history_table")
        if (!_infoHistoryTable.equals(_existingHistoryTable)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |history_table(com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity).
              | Expected:
              |""".trimMargin() + _infoHistoryTable + """
              |
              | Found:
              |""".trimMargin() + _existingHistoryTable)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "word_table", "history_table")
  }

  public override fun clearAllTables() {
    super.performClear(false, "word_table", "history_table")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(WordDao::class, WordDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun wordDao(): WordDao = _wordDao.value
}
