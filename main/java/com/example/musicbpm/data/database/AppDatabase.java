package com.example.musicbpm.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Main database class for the app.
 * Singleton pattern to ensure only one instance of the database exists.
 */
@Database(entities = {MusicTrack.class}, version = 2, exportSchema = false)  // ← MUDADO PARA VERSION 2
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "music_bpm_database";

    public abstract MusicTrackDao musicTrackDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()  // ← ADICIONADO - Recria DB quando muda schema
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}