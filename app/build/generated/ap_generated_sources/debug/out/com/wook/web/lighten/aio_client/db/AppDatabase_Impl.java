package com.wook.web.lighten.aio_client.db;

import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ReportDao _reportDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `Report` (`report_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `report_name` TEXT, `report_end_time` TEXT, `report_interval_sec` TEXT, `report_cycle` TEXT, `report_depth_correct` TEXT, `report_up_depth` TEXT, `report_down_depth` TEXT, `report_bpm` TEXT, `report_angle` TEXT, `report_depth_list` TEXT, `report_presstimeList` TEXT, `report_breathtime` TEXT, `report_breathval` TEXT, `report_ventil_volume` TEXT, `to_day` TEXT, `min` TEXT, `max` TEXT, `depth_num` TEXT, `depth_correct` TEXT, `position_num` TEXT, `position_correct` TEXT, `lung_num` TEXT, `lung_correct` TEXT, `stop_time_list` TEXT)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'c227a63f40d15911de7eede3986b9d5d')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `Report`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      protected RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsReport = new HashMap<String, TableInfo.Column>(25);
        _columnsReport.put("report_id", new TableInfo.Column("report_id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_name", new TableInfo.Column("report_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_end_time", new TableInfo.Column("report_end_time", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_interval_sec", new TableInfo.Column("report_interval_sec", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_cycle", new TableInfo.Column("report_cycle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_depth_correct", new TableInfo.Column("report_depth_correct", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_up_depth", new TableInfo.Column("report_up_depth", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_down_depth", new TableInfo.Column("report_down_depth", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_bpm", new TableInfo.Column("report_bpm", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_angle", new TableInfo.Column("report_angle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_depth_list", new TableInfo.Column("report_depth_list", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_presstimeList", new TableInfo.Column("report_presstimeList", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_breathtime", new TableInfo.Column("report_breathtime", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_breathval", new TableInfo.Column("report_breathval", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("report_ventil_volume", new TableInfo.Column("report_ventil_volume", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("to_day", new TableInfo.Column("to_day", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("min", new TableInfo.Column("min", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("max", new TableInfo.Column("max", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("depth_num", new TableInfo.Column("depth_num", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("depth_correct", new TableInfo.Column("depth_correct", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("position_num", new TableInfo.Column("position_num", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("position_correct", new TableInfo.Column("position_correct", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("lung_num", new TableInfo.Column("lung_num", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("lung_correct", new TableInfo.Column("lung_correct", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReport.put("stop_time_list", new TableInfo.Column("stop_time_list", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReport = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReport = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReport = new TableInfo("Report", _columnsReport, _foreignKeysReport, _indicesReport);
        final TableInfo _existingReport = TableInfo.read(_db, "Report");
        if (! _infoReport.equals(_existingReport)) {
          return new RoomOpenHelper.ValidationResult(false, "Report(com.wook.web.lighten.aio_client.db.Report).\n"
                  + " Expected:\n" + _infoReport + "\n"
                  + " Found:\n" + _existingReport);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "c227a63f40d15911de7eede3986b9d5d", "20baa92e4b5794adae3fa58716b17777");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "Report");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `Report`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ReportDao.class, ReportDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public ReportDao reportDao() {
    if (_reportDao != null) {
      return _reportDao;
    } else {
      synchronized(this) {
        if(_reportDao == null) {
          _reportDao = new ReportDao_Impl(this);
        }
        return _reportDao;
      }
    }
  }
}
