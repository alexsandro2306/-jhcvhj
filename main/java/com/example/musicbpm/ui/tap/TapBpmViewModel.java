package com.example.musicbpm.ui.tap;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicbpm.utils.BpmCalculator;
import com.example.musicbpm.utils.PreferencesManager;

/**
 * ViewModel for the Tap BPM screen.
 * Manages BPM calculation state and business logic.
 */
public class TapBpmViewModel extends AndroidViewModel {

    private BpmCalculator bpmCalculator;
    private PreferencesManager preferencesManager;

    private MutableLiveData<Integer> currentBpm = new MutableLiveData<>(0);
    private MutableLiveData<Integer> tapCount = new MutableLiveData<>(0);
    private MutableLiveData<Boolean> isCalculating = new MutableLiveData<>(false);

    public TapBpmViewModel(@NonNull Application application) {
        super(application);
        preferencesManager = new PreferencesManager(application);
        initializeBpmCalculator();
    }

    private void initializeBpmCalculator() {
        bpmCalculator = new BpmCalculator();

        // Set timeout from preferences
        int timeout = preferencesManager.getResetTimeout();
        bpmCalculator.setResetTimeout(timeout);

        // Set reset listener
        bpmCalculator.setOnResetListener(() -> {
            currentBpm.postValue(0);
            tapCount.postValue(0);
            isCalculating.postValue(false);
        });
    }

    /**
     * Handle tap event
     */
    public void onTap() {
        int bpm = bpmCalculator.onTap();
        currentBpm.setValue(bpm);
        tapCount.setValue(bpmCalculator.getTapCount());
        isCalculating.setValue(bpmCalculator.hasEnoughTaps());
    }

    /**
     * Manually reset the calculator
     */
    public void reset() {
        bpmCalculator.reset();
        currentBpm.setValue(0);
        tapCount.setValue(0);
        isCalculating.setValue(false);
    }

    /**
     * Get current BPM value
     */
    public LiveData<Integer> getCurrentBpm() {
        return currentBpm;
    }

    /**
     * Get current tap count
     */
    public LiveData<Integer> getTapCount() {
        return tapCount;
    }

    /**
     * Get calculating state
     */
    public LiveData<Boolean> getIsCalculating() {
        return isCalculating;
    }

    /**
     * Get the last calculated BPM value (for passing to add music screen)
     */
    public int getLastBpm() {
        Integer bpm = currentBpm.getValue();
        return bpm != null ? bpm : 0;
    }
}