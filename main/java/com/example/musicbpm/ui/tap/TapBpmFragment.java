package com.example.musicbpm.ui.tap;

import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.musicbpm.R;
import com.example.musicbpm.utils.PreferencesManager;

/**
 * Fragment for the Tap BPM screen.
 * Allows users to tap to calculate BPM in real-time.
 */
public class TapBpmFragment extends Fragment {

    private TapBpmViewModel viewModel;
    private PreferencesManager preferencesManager;

    private Button btnTap;
    private Button btnReset;
    private TextView tvBpm;
    private TextView tvTapCount;
    private TextView tvInstruction;

    private Vibrator vibrator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TapBpmViewModel.class);
        preferencesManager = new PreferencesManager(requireContext());
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tap_bpm, container, false);

        initializeViews(view);
        setupListeners();
        observeViewModel();

        return view;
    }

    private void initializeViews(View view) {
        btnTap = view.findViewById(R.id.btn_tap);
        btnReset = view.findViewById(R.id.btn_reset);
        tvBpm = view.findViewById(R.id.tv_bpm);
        tvTapCount = view.findViewById(R.id.tv_tap_count);
        tvInstruction = view.findViewById(R.id.tv_instruction);
    }

    private void setupListeners() {
        btnTap.setOnClickListener(v -> {
            viewModel.onTap();

            // Vibrate if enabled
            if (preferencesManager.isVibrationEnabled() && vibrator != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(50);
                }
            }
        });

        btnReset.setOnClickListener(v -> viewModel.reset());
    }

    private void observeViewModel() {
        viewModel.getCurrentBpm().observe(getViewLifecycleOwner(), bpm -> {
            if (bpm > 0) {
                tvBpm.setText(String.valueOf(bpm));
                tvBpm.setTextSize(72);
            } else {
                tvBpm.setText("--");
                tvBpm.setTextSize(72);
            }
        });

        viewModel.getTapCount().observe(getViewLifecycleOwner(), count -> {
            if (count == 0) {
                tvTapCount.setText("Ready");
                tvInstruction.setText("Tap the button to the beat");
            } else if (count == 1) {
                tvTapCount.setText("1 tap");
                tvInstruction.setText("Keep tapping...");
            } else {
                tvTapCount.setText(count + " taps");
                tvInstruction.setText("Keep tapping for more accuracy");
            }
        });

        viewModel.getIsCalculating().observe(getViewLifecycleOwner(), isCalculating -> {
            // Could animate the button or change color
            if (isCalculating) {
                btnTap.setAlpha(1.0f);
            } else {
                btnTap.setAlpha(0.8f);
            }
        });
    }
}