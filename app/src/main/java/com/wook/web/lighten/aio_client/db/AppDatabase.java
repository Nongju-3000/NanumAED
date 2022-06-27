package com.wook.web.lighten.aio_client.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Report.class}, version = 1)

public abstract class AppDatabase extends RoomDatabase {

    public abstract ReportDao reportDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDbInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "Report")
                    .allowMainThreadQueries()
                    .build();
        }
        return  INSTANCE;
    }
}
