package com.example.musicbpm.ui.library;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicbpm.data.database.MusicTrack;
import com.example.musicbpm.data.repository.MusicRepository;

import java.util.List;

public class LibraryViewModel extends AndroidViewModel {

    private static final String TAG = "LibraryViewModel";

    private MusicRepository repository;
    private MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private MutableLiveData<String> sortMode = new MutableLiveData<>();

    private MediatorLiveData<List<MusicTrack>> displayedTracks = new MediatorLiveData<>();

    public LibraryViewModel(@NonNull Application application) {
        super(application);
        repository = new MusicRepository(application);

        // Fontes de dados para o MediatorLiveData
        LiveData<List<MusicTrack>> allTracksByDate = repository.getAllTracks();
        LiveData<List<MusicTrack>> allTracksByTitle = repository.getTracksSortedByTitle();
        LiveData<List<MusicTrack>> allTracksByBpm = repository.getTracksSortedByBpm();
        LiveData<List<MusicTrack>> allTracksByArtist = repository.getTracksSortedByArtist();

        // Adicionar fontes ao Mediator
        displayedTracks.addSource(allTracksByDate, tracks -> {
            if ("date".equals(sortMode.getValue())) {
                applySearchFilter(tracks);
            }
        });
        displayedTracks.addSource(allTracksByTitle, tracks -> {
            if ("title".equals(sortMode.getValue())) {
                applySearchFilter(tracks);
            }
        });
        displayedTracks.addSource(allTracksByBpm, tracks -> {
            if ("bpm".equals(sortMode.getValue())) {
                applySearchFilter(tracks);
            }
        });
        displayedTracks.addSource(allTracksByArtist, tracks -> {
            if ("artist".equals(sortMode.getValue())) {
                applySearchFilter(tracks);
            }
        });

        // Observers para as mudanças de query e sort
        displayedTracks.addSource(searchQuery, query -> reevaluateTracks());
        displayedTracks.addSource(sortMode, mode -> reevaluateTracks());

        // Forçar o trigger inicial!
        searchQuery.setValue("");
        sortMode.setValue("date");
    }

    private void reevaluateTracks() {
        String mode = sortMode.getValue();
        if (mode == null) return;

        switch (mode) {
            case "title":
                applySearchFilter(repository.getTracksSortedByTitle().getValue());
                break;
            case "bpm":
                applySearchFilter(repository.getTracksSortedByBpm().getValue());
                break;
            case "artist":
                applySearchFilter(repository.getTracksSortedByArtist().getValue());
                break;
            default:
                applySearchFilter(repository.getAllTracks().getValue());
                break;
        }
    }

    private void applySearchFilter(List<MusicTrack> tracks) {
        String query = searchQuery.getValue();
        if (query == null || query.trim().isEmpty()) {
            displayedTracks.setValue(tracks);
        } else {
            repository.searchTracks(query).observeForever(filteredTracks -> {
                displayedTracks.setValue(filteredTracks);
            });
        }
    }

    public LiveData<List<MusicTrack>> getDisplayedTracks() {
        return displayedTracks;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setSortMode(String mode) {
        sortMode.setValue(mode);
    }

    public void deleteTrack(MusicTrack track, MusicRepository.OnOperationCompleteListener listener) {
        repository.delete(track, listener);
    }
}
