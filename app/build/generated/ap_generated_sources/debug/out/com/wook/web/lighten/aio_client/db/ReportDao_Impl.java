package com.wook.web.lighten.aio_client.db;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class ReportDao_Impl implements ReportDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Report> __insertionAdapterOfReport;

  private final EntityDeletionOrUpdateAdapter<Report> __deletionAdapterOfReport;

  public ReportDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReport = new EntityInsertionAdapter<Report>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `Report` (`report_id`,`report_name`,`report_end_time`,`report_interval_sec`,`report_cycle`,`report_depth_correct`,`report_up_depth`,`report_down_depth`,`report_bpm`,`report_angle`,`report_depth_list`,`report_presstimeList`,`report_breathtime`,`report_breathval`,`report_ventil_volume`,`to_day`,`min`,`max`,`depth_num`,`depth_correct`,`position_num`,`position_correct`,`lung_num`,`lung_correct`,`stop_time_list`,`report_bluetoothtime`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Report value) {
        stmt.bindLong(1, value.report_id);
        if (value.report_name == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.report_name);
        }
        if (value.report_end_time == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.report_end_time);
        }
        if (value.report_interval_sec == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.report_interval_sec);
        }
        if (value.report_cycle == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.report_cycle);
        }
        if (value.report_depth_correct == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.report_depth_correct);
        }
        if (value.report_up_depth == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.report_up_depth);
        }
        if (value.report_down_depth == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.report_down_depth);
        }
        if (value.report_bpm == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.report_bpm);
        }
        if (value.report_angle == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, value.report_angle);
        }
        if (value.report_depth_list == null) {
          stmt.bindNull(11);
        } else {
          stmt.bindString(11, value.report_depth_list);
        }
        if (value.report_presstimeList == null) {
          stmt.bindNull(12);
        } else {
          stmt.bindString(12, value.report_presstimeList);
        }
        if (value.report_breathtime == null) {
          stmt.bindNull(13);
        } else {
          stmt.bindString(13, value.report_breathtime);
        }
        if (value.report_breathval == null) {
          stmt.bindNull(14);
        } else {
          stmt.bindString(14, value.report_breathval);
        }
        if (value.report_ventil_volume == null) {
          stmt.bindNull(15);
        } else {
          stmt.bindString(15, value.report_ventil_volume);
        }
        if (value.to_day == null) {
          stmt.bindNull(16);
        } else {
          stmt.bindString(16, value.to_day);
        }
        if (value.min == null) {
          stmt.bindNull(17);
        } else {
          stmt.bindString(17, value.min);
        }
        if (value.max == null) {
          stmt.bindNull(18);
        } else {
          stmt.bindString(18, value.max);
        }
        if (value.depth_num == null) {
          stmt.bindNull(19);
        } else {
          stmt.bindString(19, value.depth_num);
        }
        if (value.depth_correct == null) {
          stmt.bindNull(20);
        } else {
          stmt.bindString(20, value.depth_correct);
        }
        if (value.position_num == null) {
          stmt.bindNull(21);
        } else {
          stmt.bindString(21, value.position_num);
        }
        if (value.position_correct == null) {
          stmt.bindNull(22);
        } else {
          stmt.bindString(22, value.position_correct);
        }
        if (value.lung_num == null) {
          stmt.bindNull(23);
        } else {
          stmt.bindString(23, value.lung_num);
        }
        if (value.lung_correct == null) {
          stmt.bindNull(24);
        } else {
          stmt.bindString(24, value.lung_correct);
        }
        if (value.stop_time_list == null) {
          stmt.bindNull(25);
        } else {
          stmt.bindString(25, value.stop_time_list);
        }
        if (value.report_bluetoothtime == null) {
          stmt.bindNull(26);
        } else {
          stmt.bindString(26, value.report_bluetoothtime);
        }
      }
    };
    this.__deletionAdapterOfReport = new EntityDeletionOrUpdateAdapter<Report>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `Report` WHERE `report_id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Report value) {
        stmt.bindLong(1, value.report_id);
      }
    };
  }

  @Override
  public void insert(final Report... devices) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfReport.insert(devices);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final Report device) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfReport.handle(device);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Report> getAllDevice() {
    final String _sql = "SELECT * FROM Report";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfReportId = CursorUtil.getColumnIndexOrThrow(_cursor, "report_id");
      final int _cursorIndexOfReportName = CursorUtil.getColumnIndexOrThrow(_cursor, "report_name");
      final int _cursorIndexOfReportEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "report_end_time");
      final int _cursorIndexOfReportIntervalSec = CursorUtil.getColumnIndexOrThrow(_cursor, "report_interval_sec");
      final int _cursorIndexOfReportCycle = CursorUtil.getColumnIndexOrThrow(_cursor, "report_cycle");
      final int _cursorIndexOfReportDepthCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "report_depth_correct");
      final int _cursorIndexOfReportUpDepth = CursorUtil.getColumnIndexOrThrow(_cursor, "report_up_depth");
      final int _cursorIndexOfReportDownDepth = CursorUtil.getColumnIndexOrThrow(_cursor, "report_down_depth");
      final int _cursorIndexOfReportBpm = CursorUtil.getColumnIndexOrThrow(_cursor, "report_bpm");
      final int _cursorIndexOfReportAngle = CursorUtil.getColumnIndexOrThrow(_cursor, "report_angle");
      final int _cursorIndexOfReportDepthList = CursorUtil.getColumnIndexOrThrow(_cursor, "report_depth_list");
      final int _cursorIndexOfReportPresstimeList = CursorUtil.getColumnIndexOrThrow(_cursor, "report_presstimeList");
      final int _cursorIndexOfReportBreathtime = CursorUtil.getColumnIndexOrThrow(_cursor, "report_breathtime");
      final int _cursorIndexOfReportBreathval = CursorUtil.getColumnIndexOrThrow(_cursor, "report_breathval");
      final int _cursorIndexOfReportVentilVolume = CursorUtil.getColumnIndexOrThrow(_cursor, "report_ventil_volume");
      final int _cursorIndexOfToDay = CursorUtil.getColumnIndexOrThrow(_cursor, "to_day");
      final int _cursorIndexOfMin = CursorUtil.getColumnIndexOrThrow(_cursor, "min");
      final int _cursorIndexOfMax = CursorUtil.getColumnIndexOrThrow(_cursor, "max");
      final int _cursorIndexOfDepthNum = CursorUtil.getColumnIndexOrThrow(_cursor, "depth_num");
      final int _cursorIndexOfDepthCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "depth_correct");
      final int _cursorIndexOfPositionNum = CursorUtil.getColumnIndexOrThrow(_cursor, "position_num");
      final int _cursorIndexOfPositionCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "position_correct");
      final int _cursorIndexOfLungNum = CursorUtil.getColumnIndexOrThrow(_cursor, "lung_num");
      final int _cursorIndexOfLungCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "lung_correct");
      final int _cursorIndexOfStopTimeList = CursorUtil.getColumnIndexOrThrow(_cursor, "stop_time_list");
      final int _cursorIndexOfReportBluetoothtime = CursorUtil.getColumnIndexOrThrow(_cursor, "report_bluetoothtime");
      final List<Report> _result = new ArrayList<Report>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final Report _item;
        _item = new Report();
        _item.report_id = _cursor.getInt(_cursorIndexOfReportId);
        if (_cursor.isNull(_cursorIndexOfReportName)) {
          _item.report_name = null;
        } else {
          _item.report_name = _cursor.getString(_cursorIndexOfReportName);
        }
        if (_cursor.isNull(_cursorIndexOfReportEndTime)) {
          _item.report_end_time = null;
        } else {
          _item.report_end_time = _cursor.getString(_cursorIndexOfReportEndTime);
        }
        if (_cursor.isNull(_cursorIndexOfReportIntervalSec)) {
          _item.report_interval_sec = null;
        } else {
          _item.report_interval_sec = _cursor.getString(_cursorIndexOfReportIntervalSec);
        }
        if (_cursor.isNull(_cursorIndexOfReportCycle)) {
          _item.report_cycle = null;
        } else {
          _item.report_cycle = _cursor.getString(_cursorIndexOfReportCycle);
        }
        if (_cursor.isNull(_cursorIndexOfReportDepthCorrect)) {
          _item.report_depth_correct = null;
        } else {
          _item.report_depth_correct = _cursor.getString(_cursorIndexOfReportDepthCorrect);
        }
        if (_cursor.isNull(_cursorIndexOfReportUpDepth)) {
          _item.report_up_depth = null;
        } else {
          _item.report_up_depth = _cursor.getString(_cursorIndexOfReportUpDepth);
        }
        if (_cursor.isNull(_cursorIndexOfReportDownDepth)) {
          _item.report_down_depth = null;
        } else {
          _item.report_down_depth = _cursor.getString(_cursorIndexOfReportDownDepth);
        }
        if (_cursor.isNull(_cursorIndexOfReportBpm)) {
          _item.report_bpm = null;
        } else {
          _item.report_bpm = _cursor.getString(_cursorIndexOfReportBpm);
        }
        if (_cursor.isNull(_cursorIndexOfReportAngle)) {
          _item.report_angle = null;
        } else {
          _item.report_angle = _cursor.getString(_cursorIndexOfReportAngle);
        }
        if (_cursor.isNull(_cursorIndexOfReportDepthList)) {
          _item.report_depth_list = null;
        } else {
          _item.report_depth_list = _cursor.getString(_cursorIndexOfReportDepthList);
        }
        if (_cursor.isNull(_cursorIndexOfReportPresstimeList)) {
          _item.report_presstimeList = null;
        } else {
          _item.report_presstimeList = _cursor.getString(_cursorIndexOfReportPresstimeList);
        }
        if (_cursor.isNull(_cursorIndexOfReportBreathtime)) {
          _item.report_breathtime = null;
        } else {
          _item.report_breathtime = _cursor.getString(_cursorIndexOfReportBreathtime);
        }
        if (_cursor.isNull(_cursorIndexOfReportBreathval)) {
          _item.report_breathval = null;
        } else {
          _item.report_breathval = _cursor.getString(_cursorIndexOfReportBreathval);
        }
        if (_cursor.isNull(_cursorIndexOfReportVentilVolume)) {
          _item.report_ventil_volume = null;
        } else {
          _item.report_ventil_volume = _cursor.getString(_cursorIndexOfReportVentilVolume);
        }
        if (_cursor.isNull(_cursorIndexOfToDay)) {
          _item.to_day = null;
        } else {
          _item.to_day = _cursor.getString(_cursorIndexOfToDay);
        }
        if (_cursor.isNull(_cursorIndexOfMin)) {
          _item.min = null;
        } else {
          _item.min = _cursor.getString(_cursorIndexOfMin);
        }
        if (_cursor.isNull(_cursorIndexOfMax)) {
          _item.max = null;
        } else {
          _item.max = _cursor.getString(_cursorIndexOfMax);
        }
        if (_cursor.isNull(_cursorIndexOfDepthNum)) {
          _item.depth_num = null;
        } else {
          _item.depth_num = _cursor.getString(_cursorIndexOfDepthNum);
        }
        if (_cursor.isNull(_cursorIndexOfDepthCorrect)) {
          _item.depth_correct = null;
        } else {
          _item.depth_correct = _cursor.getString(_cursorIndexOfDepthCorrect);
        }
        if (_cursor.isNull(_cursorIndexOfPositionNum)) {
          _item.position_num = null;
        } else {
          _item.position_num = _cursor.getString(_cursorIndexOfPositionNum);
        }
        if (_cursor.isNull(_cursorIndexOfPositionCorrect)) {
          _item.position_correct = null;
        } else {
          _item.position_correct = _cursor.getString(_cursorIndexOfPositionCorrect);
        }
        if (_cursor.isNull(_cursorIndexOfLungNum)) {
          _item.lung_num = null;
        } else {
          _item.lung_num = _cursor.getString(_cursorIndexOfLungNum);
        }
        if (_cursor.isNull(_cursorIndexOfLungCorrect)) {
          _item.lung_correct = null;
        } else {
          _item.lung_correct = _cursor.getString(_cursorIndexOfLungCorrect);
        }
        if (_cursor.isNull(_cursorIndexOfStopTimeList)) {
          _item.stop_time_list = null;
        } else {
          _item.stop_time_list = _cursor.getString(_cursorIndexOfStopTimeList);
        }
        if (_cursor.isNull(_cursorIndexOfReportBluetoothtime)) {
          _item.report_bluetoothtime = null;
        } else {
          _item.report_bluetoothtime = _cursor.getString(_cursorIndexOfReportBluetoothtime);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
