package com.example.musicbpm.ui.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.musicbpm.R;
import com.example.musicbpm.data.repository.MusicRepository;
import com.example.musicbpm.utils.PreferencesManager;

/**
 * Fragment for app settings and preferences.
 * Allows users to configure app behavior.
 */
public class SettingsFragment extends Fragment {

    private PreferencesManager preferencesManager;
    private MusicRepository repository;

    private SwitchCompat switchVibration;
    private TextView tvResetTimeout;
    private View btnDeleteAll;
    private TextView tvAbout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesManager = new PreferencesManager(requireContext());
        repository = new MusicRepository(requireActivity().getApplication());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeViews(view);
        setupListeners();
        loadCurrentSettings();

        return view;
    }

    private void initializeViews(View view) {
        switchVibration = view.findViewById(R.id.switch_vibration);
        tvResetTimeout = view.findViewById(R.id.tv_reset_timeout);
        btnDeleteAll = view.findViewById(R.id.btn_delete_all);
        tvAbout = view.findViewById(R.id.tv_about);
    }

    private void setupListeners() {
        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setVibrationEnabled(isChecked);
        });

        tvResetTimeout.setOnClickListener(v -> showResetTimeoutDialog());

        btnDeleteAll.setOnClickListener(v -> showDeleteAllDialog());

        tvAbout.setOnClickListener(v -> showAboutDialog());
    }

    private void loadCurrentSettings() {
        switchVibration.setChecked(preferencesManager.isVibrationEnabled());
        int timeout = preferencesManager.getResetTimeout();
        tvResetTimeout.setText(String.format("Auto-reset after %d seconds", timeout / 1000));
    }

    private void showResetTimeoutDialog() {
        String[] options = {"2 seconds", "3 seconds", "4 seconds", "5 seconds"};
        int[] values = {2000, 3000, 4000, 5000};

        int currentTimeout = preferencesManager.getResetTimeout();
        int selectedIndex = 1; // default 3 seconds
        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentTimeout) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Auto-reset Timeout")
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    preferencesManager.setResetTimeout(values[which]);
                    tvResetTimeout.setText(String.format("Auto-reset after %d seconds", values[which] / 1000));
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAllDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete All Tracks")
                .setMessage("Are you sure you want to delete ALL music tracks? This action cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    showConfirmDeleteDialog();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showConfirmDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("This is your last chance. Really delete everything?")
                .setPositiveButton("Yes, Delete Everything", (dialog, which) -> {
                    repository.deleteAllTracks(new MusicRepository.OnOperationCompleteListener() {
                        @Override
                        public void onSuccess(long id) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "All tracks deleted", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("About Music BPM App")
                .setMessage("Version 1.0.0\n\n" +
                        "A professional BPM calculator and music library manager.\n\n" +
                        "Features:\n" +
                        "• Tap Tempo BPM calculator\n" +
                        "• Music link organizer\n" +
                        "• Search and filter tracks\n" +
                        "• Multiple platform support\n\n" +
                        "© 2024")
                .setPositiveButton("OK", null)
                .show();
    }
}