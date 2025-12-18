package com.example.musicbpm.data.database;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Type converters for Room database.
 * Converts complex types (like List<String>) to types that Room can store.
 */
public class Converters {

    @TypeConverter
    public static String fromList(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }

    @TypeConverter
    public static List<String> toList(String tagsString) {
        if (tagsString == null || tagsString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(tagsString.split(","));
    }
}