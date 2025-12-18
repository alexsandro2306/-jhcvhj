package com.example.musicbpm.ui.add;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicbpm.data.database.MusicTrack;
import com.example.musicbpm.data.repository.MusicRepository;
import com.example.musicbpm.utils.PlatformDetector;

/**
 * ViewModel for the Add Music screen.
 * Manages form state, validation, and saving music tracks.
 */
public class AddMusicViewModel extends AndroidViewModel {

    private MusicRepository repository;

    private MutableLiveData<String> title = new MutableLiveData<>("");
    private MutableLiveData<String> artist = new MutableLiveData<>("");
    private MutableLiveData<Integer> bpm = new MutableLiveData<>(0);
    private MutableLiveData<String> link = new MutableLiveData<>("");
    private MutableLiveData<String> notes = new MutableLiveData<>("");
    private MutableLiveData<String> tags = new MutableLiveData<>("");

    private MutableLiveData<String> titleError = new MutableLiveData<>();
    private MutableLiveData<String> linkError = new MutableLiveData<>();
    private MutableLiveData<String> bpmError = new MutableLiveData<>();

    public AddMusicViewModel(@NonNull Application application) {
        super(application);
        repository = new MusicRepository(application);
    }

    public void setTitle(String title) {
        this.title.setValue(title);
        validateTitle();
    }

    public void setArtist(String artist) {
        this.artist.setValue(artist);
    }

    public void setBpm(int bpm) {
        this.bpm.setValue(bpm);
        validateBpm();
    }

    public void setLink(String link) {
        this.link.setValue(link);
        validateLink();
    }

    public void setNotes(String notes) {
        this.notes.setValue(notes);
    }

    public void setTags(String tags) {
        this.tags.setValue(tags);
    }

    private boolean validateTitle() {
        String titleValue = title.getValue();
        if (titleValue == null || titleValue.trim().isEmpty()) {
            titleError.setValue("Title is required");
            return false;
        }
        titleError.setValue(null);
        return true;
    }

    private boolean validateLink() {
        String linkValue = link.getValue();
        if (linkValue == null || linkValue.trim().isEmpty()) {
            linkError.setValue("Link is required");
            return false;
        }
        if (!Patterns.WEB_URL.matcher(linkValue).matches()) {
            linkError.setValue("Invalid URL format");
            return false;
        }
        linkError.setValue(null);
        return true;
    }

    private boolean validateBpm() {
        Integer bpmValue = bpm.getValue();
        if (bpmValue == null || bpmValue < 40 || bpmValue > 220) {
            bpmError.setValue("BPM must be between 40 and 220");
            return false;
        }
        bpmError.setValue(null);
        return true;
    }

    public boolean validateAll() {
        boolean isTitleValid = validateTitle();
        boolean isLinkValid = validateLink();
        boolean isBpmValid = validateBpm();
        return isTitleValid && isLinkValid && isBpmValid;
    }

    public void saveTrack(MusicRepository.OnOperationCompleteListener listener) {
        if (!validateAll()) {
            listener.onError("Please fix validation errors");
            return;
        }

        String platform = PlatformDetector.detectPlatform(link.getValue());

        MusicTrack track = new MusicTrack(
                title.getValue().trim(),
                artist.getValue() != null ? artist.getValue().trim() : null,
                bpm.getValue(),
                link.getValue().trim(),
                platform,
                notes.getValue() != null ? notes.getValue().trim() : null,
                tags.getValue() != null ? tags.getValue().trim() : null,
                System.currentTimeMillis()
        );

        repository.insert(track, listener);
    }

    public void clearForm() {
        title.setValue("");
        artist.setValue("");
        bpm.setValue(0);
        link.setValue("");
        notes.setValue("");
        tags.setValue("");
        titleError.setValue(null);
        linkError.setValue(null);
        bpmError.setValue(null);
    }

    // Getters for LiveData
    public LiveData<String> getTitle() {
        return title;
    }

    public LiveData<String> getArtist() {
        return artist;
    }

    public LiveData<Integer> getBpm() {
        return bpm;
    }

    public LiveData<String> getLink() {
        return link;
    }

    public LiveData<String> getNotes() {
        return notes;
    }

    public LiveData<String> getTags() {
        return tags;
    }

    public LiveData<String> getTitleError() {
        return titleError;
    }

    public LiveData<String> getLinkError() {
        return linkError;
    }

    public LiveData<String> getBpmError() {
        return bpmError;
    }
}