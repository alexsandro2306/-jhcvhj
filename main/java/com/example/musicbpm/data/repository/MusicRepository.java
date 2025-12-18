package com.example.musicbpm.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.musicbpm.data.database.AppDatabase;
import com.example.musicbpm.data.database.MusicTrack;
import com.example.musicbpm.data.database.MusicTrackDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class that abstracts access to the data layer.
 * Handles all database operations on background threads.
 */
public class MusicRepository {

    private MusicTrackDao musicTrackDao;
    private LiveData<List<MusicTrack>> allTracks;
    private ExecutorService executorService;

    public MusicRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        musicTrackDao = database.musicTrackDao();
        allTracks = musicTrackDao.getAllTracks();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<MusicTrack>> getAllTracks() {
        return allTracks;
    }

    public LiveData<MusicTrack> getTrackById(int id) {
        return musicTrackDao.getTrackById(id);
    }

    public LiveData<List<MusicTrack>> searchTracks(String query) {
        return musicTrackDao.searchTracks(query);
    }

    public LiveData<List<MusicTrack>> getTracksByBpmRange(int minBpm, int maxBpm) {
        return musicTrackDao.getTracksByBpmRange(minBpm, maxBpm);
    }

    public LiveData<List<MusicTrack>> getTracksByPlatform(String platform) {
        return musicTrackDao.getTracksByPlatform(platform);
    }

    public LiveData<List<MusicTrack>> getTracksSortedByTitle() {
        return musicTrackDao.getTracksSortedByTitle();
    }

    public LiveData<List<MusicTrack>> getTracksSortedByBpm() {
        return musicTrackDao.getTracksSortedByBpm();
    }

    public LiveData<List<MusicTrack>> getTracksSortedByArtist() {
        return musicTrackDao.getTracksSortedByArtist();
    }

    public LiveData<Integer> getTrackCount() {
        return musicTrackDao.getTrackCount();
    }

    public void insert(MusicTrack track, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                long id = musicTrackDao.insert(track);
                if (listener != null) {
                    listener.onSuccess(id);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            }
        });
    }

    public void update(MusicTrack track, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                musicTrackDao.update(track);
                if (listener != null) {
                    listener.onSuccess(-1);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            }
        });
    }

    public void delete(MusicTrack track, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                musicTrackDao.delete(track);
                if (listener != null) {
                    listener.onSuccess(-1);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            }
        });
    }

    public void deleteAllTracks(OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                musicTrackDao.deleteAllTracks();
                if (listener != null) {
                    listener.onSuccess(-1);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            }
        });
    }

    /**
     * Callback interface for async operations
     */
    public interface OnOperationCompleteListener {
        void onSuccess(long id);
        void onError(String error);
    }
}