package com.example.musicbpm.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for calculating BPM based on tap tempo.
 * Handles tap timestamps, calculates average intervals, and manages auto-reset.
 */
public class BpmCalculator {

    private static final int MAX_TAPS = 10;
    private static final long DEFAULT_RESET_TIMEOUT = 3000; // 3 seconds
    private static final int MIN_BPM = 40;
    private static final int MAX_BPM = 220;

    private List<Long> tapTimestamps;
    private Handler resetHandler;
    private Runnable resetRunnable;
    private long resetTimeout;
    private OnResetListener resetListener;

    public BpmCalculator() {
        this.tapTimestamps = new ArrayList<>();
        this.resetHandler = new Handler(Looper.getMainLooper());
        this.resetTimeout = DEFAULT_RESET_TIMEOUT;
    }

    /**
     * Records a tap and returns the calculated BPM
     * @return Current BPM or 0 if not enough taps
     */
    public int onTap() {
        long currentTime = System.currentTimeMillis();
        tapTimestamps.add(currentTime);

        // Limit number of stored taps
        if (tapTimestamps.size() > MAX_TAPS) {
            tapTimestamps.remove(0);
        }

        // Reset the auto-reset timer
        resetHandler.removeCallbacks(resetRunnable);
        if (resetRunnable != null) {
            resetHandler.postDelayed(resetRunnable, resetTimeout);
        }

        return calculateBpm();
    }

    /**
     * Calculates BPM based on stored tap timestamps
     * @return BPM value or 0 if less than 2 taps
     */
    private int calculateBpm() {
        if (tapTimestamps.size() < 2) {
            return 0;
        }

        // Calculate intervals between taps
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < tapTimestamps.size(); i++) {
            intervals.add(tapTimestamps.get(i) - tapTimestamps.get(i - 1));
        }

        // Calculate average interval
        long sum = 0;
        for (long interval : intervals) {
            sum += interval;
        }
        double averageInterval = (double) sum / intervals.size();

        // Convert to BPM: 60000 ms per minute / average interval in ms
        int bpm = (int) Math.round(60000.0 / averageInterval);

        // Clamp to realistic BPM range
        return Math.max(MIN_BPM, Math.min(MAX_BPM, bpm));
    }

    /**
     * Manually reset all taps
     */
    public void reset() {
        tapTimestamps.clear();
        resetHandler.removeCallbacks(resetRunnable);
    }

    /**
     * Get number of taps recorded
     */
    public int getTapCount() {
        return tapTimestamps.size();
    }

    /**
     * Check if enough taps have been recorded to calculate BPM
     */
    public boolean hasEnoughTaps() {
        return tapTimestamps.size() >= 2;
    }

    /**
     * Set the timeout for auto-reset
     * @param timeoutMs Timeout in milliseconds
     */
    public void setResetTimeout(long timeoutMs) {
        this.resetTimeout = timeoutMs;
    }

    /**
     * Set callback for auto-reset events
     */
    public void setOnResetListener(OnResetListener listener) {
        this.resetListener = listener;
        this.resetRunnable = () -> {
            reset();
            if (resetListener != null) {
                resetListener.onReset();
            }
        };
    }

    /**
     * Interface for reset callbacks
     */
    public interface OnResetListener {
        void onReset();
    }
}