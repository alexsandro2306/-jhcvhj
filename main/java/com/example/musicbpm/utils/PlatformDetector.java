package com.example.musicbpm.utils;

/**
 * Utility class to detect music platform from URL.
 * Identifies YouTube, Spotify, SoundCloud, and other platforms.
 */
public class PlatformDetector {

    public static final String PLATFORM_YOUTUBE = "youtube";
    public static final String PLATFORM_SPOTIFY = "spotify";
    public static final String PLATFORM_SOUNDCLOUD = "soundcloud";
    public static final String PLATFORM_APPLE_MUSIC = "apple_music";
    public static final String PLATFORM_BANDCAMP = "bandcamp";
    public static final String PLATFORM_TIDAL = "tidal";
    public static final String PLATFORM_DEEZER = "deezer";
    public static final String PLATFORM_OTHER = "other";

    /**
     * Detects the platform from a given URL
     * @param url The music link URL
     * @return Platform identifier string
     */
    public static String detectPlatform(String url) {
        if (url == null || url.trim().isEmpty()) {
            return PLATFORM_OTHER;
        }

        String lowerUrl = url.toLowerCase();

        if (lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be")) {
            return PLATFORM_YOUTUBE;
        } else if (lowerUrl.contains("spotify.com")) {
            return PLATFORM_SPOTIFY;
        } else if (lowerUrl.contains("soundcloud.com")) {
            return PLATFORM_SOUNDCLOUD;
        } else if (lowerUrl.contains("music.apple.com") || lowerUrl.contains("itunes.apple.com")) {
            return PLATFORM_APPLE_MUSIC;
        } else if (lowerUrl.contains("bandcamp.com")) {
            return PLATFORM_BANDCAMP;
        } else if (lowerUrl.contains("tidal.com")) {
            return PLATFORM_TIDAL;
        } else if (lowerUrl.contains("deezer.com")) {
            return PLATFORM_DEEZER;
        }

        return PLATFORM_OTHER;
    }

    /**
     * Gets a display name for the platform
     * @param platform Platform identifier
     * @return Human-readable platform name
     */
    public static String getPlatformDisplayName(String platform) {
        if (platform == null) {
            return "Unknown";
        }

        switch (platform) {
            case PLATFORM_YOUTUBE:
                return "YouTube";
            case PLATFORM_SPOTIFY:
                return "Spotify";
            case PLATFORM_SOUNDCLOUD:
                return "SoundCloud";
            case PLATFORM_APPLE_MUSIC:
                return "Apple Music";
            case PLATFORM_BANDCAMP:
                return "Bandcamp";
            case PLATFORM_TIDAL:
                return "TIDAL";
            case PLATFORM_DEEZER:
                return "Deezer";
            default:
                return "Other";
        }
    }
}