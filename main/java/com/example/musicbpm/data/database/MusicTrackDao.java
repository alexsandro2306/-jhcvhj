package com.example.musicbpm.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for MusicTrack entity.
 * Defines all database operations for music tracks.
 */
@Dao
public interface MusicTrackDao {

    @Insert
    long insert(MusicTrack track);

    @Update
    void update(MusicTrack track);

    @Delete
    void delete(MusicTrack track);

    @Query("SELECT * FROM music_tracks ORDER BY created_at DESC")
    LiveData<List<MusicTrack>> getAllTracks();

    @Query("SELECT * FROM music_tracks WHERE id = :id")
    LiveData<MusicTrack> getTrackById(int id);

    @Query("SELECT * FROM music_tracks WHERE " +
            "title LIKE '%' || :query || '%' OR " +
            "artist LIKE '%' || :query || '%' OR " +
            "tags LIKE '%' || :query || '%' " +
            "ORDER BY created_at DESC")
    LiveData<List<MusicTrack>> searchTracks(String query);

    @Query("SELECT * FROM music_tracks WHERE bpm BETWEEN :minBpm AND :maxBpm ORDER BY bpm ASC")
    LiveData<List<MusicTrack>> getTracksByBpmRange(int minBpm, int maxBpm);

    @Query("SELECT * FROM music_tracks WHERE platform = :platform ORDER BY created_at DESC")
    LiveData<List<MusicTrack>> getTracksByPlatform(String platform);

    @Query("SELECT * FROM music_tracks ORDER BY title ASC")
    LiveData<List<MusicTrack>> getTracksSortedByTitle();

    @Query("SELECT * FROM music_tracks ORDER BY bpm ASC")
    LiveData<List<MusicTrack>> getTracksSortedByBpm();

    @Query("SELECT * FROM music_tracks ORDER BY artist ASC")
    LiveData<List<MusicTrack>> getTracksSortedByArtist();

    @Query("DELETE FROM music_tracks")
    void deleteAllTracks();

    @Query("SELECT COUNT(*) FROM music_tracks")
    LiveData<Integer> getTrackCount();
}