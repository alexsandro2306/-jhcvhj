package com.example.musicbpm.data.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity class representing a music track in the database.
 * Contains all information about a saved song including BPM, link, and metadata.
 */
@Entity(tableName = "music_tracks",
        indices = {@Index(value = "bpm"), @Index(value = "title")})
public class MusicTrack {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "artist")
    private String artist;

    @ColumnInfo(name = "bpm")
    private int bpm;

    @NonNull
    @ColumnInfo(name = "link")
    private String link;

    @ColumnInfo(name = "platform")
    private String platform;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "tags")
    private String tags;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Constructor
    public MusicTrack(@NonNull String title, String artist, int bpm,
                      @NonNull String link, String platform, String notes,
                      String tags, long createdAt) {
        this.title = title;
        this.artist = artist;
        this.bpm = bpm;
        this.link = link;
        this.platform = platform;
        this.notes = notes;
        this.tags = tags;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    @NonNull
    public String getLink() {
        return link;
    }

    public void setLink(@NonNull String link) {
        this.link = link;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}