package com.example.musicbpm.ui.add;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.musicbpm.R;
import com.example.musicbpm.data.repository.MusicRepository;
import com.example.musicbpm.data.spotify.SpotifyService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Track;

public class AddMusicFragment extends Fragment {

    private static final String TAG = "AddMusicFragment";

    private AddMusicViewModel viewModel;
    private TextInputLayout tilLink;
    private TextInputEditText etLink;
    private Button btnFindMusic;
    private SpotifyService spotifyService;

    // Track preview card
    private CardView cardTrackPreview;
    private ImageView ivAlbumArt;
    private TextView tvTrackTitle;
    private TextView tvTrackArtist;
    private TextView tvBpmDisplay;
    private Button btnTapTempo;
    private Button btnResetTaps;
    private TextInputLayout tilBpm;
    private TextInputEditText etBpm;
    private ImageView ivSpotifyLogo;
    private Button btnOpenSpotify;
    private Button btnAddToLibrary;

    // Store current track data
    private Track currentTrack;
    private String currentSpotifyUrl;
    private Integer manualBpm = null;

    // Tap tempo variables
    private List<Long> tapTimes = new ArrayList<>();
    private static final int MAX_TAP_INTERVAL = 2000;
    private static final int MIN_TAPS = 4;
    private Handler tapResetHandler = new Handler(Looper.getMainLooper());
    private Runnable tapResetRunnable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddMusicViewModel.class);
        spotifyService = new SpotifyService();
        Log.d(TAG, "onCreate - Fragment criado");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_music, container, false);

        initializeViews(view);
        setupListeners();
        checkSpotifyCredentials();

        return view;
    }

    private void initializeViews(View view) {
        tilLink = view.findViewById(R.id.til_link);
        etLink = view.findViewById(R.id.et_link);
        btnFindMusic = view.findViewById(R.id.btn_find_music);

        cardTrackPreview = view.findViewById(R.id.card_track_preview);
        ivAlbumArt = view.findViewById(R.id.iv_album_art);
        tvTrackTitle = view.findViewById(R.id.tv_track_title);
        tvTrackArtist = view.findViewById(R.id.tv_track_artist);
        tvBpmDisplay = view.findViewById(R.id.tv_bpm_display);
        btnTapTempo = view.findViewById(R.id.btn_tap_tempo);
        btnResetTaps = view.findViewById(R.id.btn_reset_taps);
        tilBpm = view.findViewById(R.id.til_bpm);
        etBpm = view.findViewById(R.id.et_bpm);
        ivSpotifyLogo = view.findViewById(R.id.iv_spotify_logo);
        btnOpenSpotify = view.findViewById(R.id.btn_open_spotify);
        btnAddToLibrary = view.findViewById(R.id.btn_add_to_library);
    }

    private void setupListeners() {
        btnFindMusic.setOnClickListener(v -> {
            String link = etLink.getText().toString().trim();
            if (link.isEmpty()) {
                tilLink.setError("Por favor insira um link");
                return;
            }
            tilLink.setError(null);
            findMusicFromLink(link);
        });

        btnTapTempo.setOnClickListener(v -> handleTap());

        btnResetTaps.setOnClickListener(v -> resetTaps());

        btnOpenSpotify.setOnClickListener(v -> {
            if (currentSpotifyUrl != null && !currentSpotifyUrl.isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentSpotifyUrl));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Não foi possível abrir o Spotify", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnAddToLibrary.setOnClickListener(v -> {
            if (currentTrack != null) {
                saveTrackToLibrary();
            }
        });

        etBpm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String bpmText = s.toString().trim();
                if (!bpmText.isEmpty()) {
                    try {
                        int bpm = Integer.parseInt(bpmText);
                        if (bpm < 20 || bpm > 300) {
                            tilBpm.setError("BPM deve estar entre 20 e 300");
                            manualBpm = null;
                        } else {
                            tilBpm.setError(null);
                            manualBpm = bpm;
                            updateBpmDisplay();
                        }
                    } catch (NumberFormatException e) {
                        tilBpm.setError("BPM inválido");
                        manualBpm = null;
                    }
                } else {
                    manualBpm = null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void handleTap() {
        long currentTime = System.currentTimeMillis();

        if (tapResetRunnable != null) {
            tapResetHandler.removeCallbacks(tapResetRunnable);
        }

        if (!tapTimes.isEmpty() && (currentTime - tapTimes.get(tapTimes.size() - 1)) > MAX_TAP_INTERVAL) {
            tapTimes.clear();
        }

        tapTimes.add(currentTime);

        if (tapTimes.size() >= MIN_TAPS) {
            calculateBpm();
        } else {
            int remaining = MIN_TAPS - tapTimes.size();
            tvBpmDisplay.setText(String.format("Toque mais %d vez%s", remaining, remaining == 1 ? "" : "es"));
        }

        tapResetRunnable = () -> {
            if (!tapTimes.isEmpty()) {
                tapTimes.clear();
                tvBpmDisplay.setText("---");
                btnTapTempo.setText("TAP TEMPO");
            }
        };
        tapResetHandler.postDelayed(tapResetRunnable, MAX_TAP_INTERVAL);

        btnTapTempo.setText(String.format("Tap %d", tapTimes.size()));
    }

    private void calculateBpm() {
        if (tapTimes.size() < 2) return;

        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < tapTimes.size(); i++) {
            intervals.add(tapTimes.get(i) - tapTimes.get(i - 1));
        }

        long sum = 0;
        for (long interval : intervals) {
            sum += interval;
        }
        double avgInterval = (double) sum / intervals.size();

        int bpm = (int) Math.round(60000.0 / avgInterval);

        if (bpm >= 20 && bpm <= 300) {
            manualBpm = bpm;
            etBpm.setText(String.valueOf(bpm));
            updateBpmDisplay();
            tilBpm.setError(null);
            btnTapTempo.setText("✓ " + bpm + " BPM");
            Log.d(TAG, "BPM calculado via Tap Tempo: " + bpm);
        } else {
            tvBpmDisplay.setText("BPM fora do intervalo");
            resetTaps();
        }
    }

    private void resetTaps() {
        tapTimes.clear();
        tvBpmDisplay.setText(manualBpm != null ? manualBpm + " BPM" : "---");
        btnTapTempo.setText("TAP TEMPO");

        if (tapResetRunnable != null) {
            tapResetHandler.removeCallbacks(tapResetRunnable);
        }
    }

    private void updateBpmDisplay() {
        if (manualBpm != null) {
            tvBpmDisplay.setText(manualBpm + " BPM");
        } else {
            tvBpmDisplay.setText("---");
        }
    }

    private void checkSpotifyCredentials() {
        if (!SpotifyService.areCredentialsConfigured()) {
            Toast.makeText(getContext(),
                    "⚠️ Spotify não configurado!\n\n" +
                            "Por favor configura as credenciais no SpotifyService.java",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void findMusicFromLink(String link) {
        if (link.contains("spotify.com")) {
            handleSpotifyLink(link);
        } else if (link.contains("youtube.com") || link.contains("youtu.be")) {
            handleYouTubeLink(link);
        } else {
            Toast.makeText(getContext(), "Por favor use um link do Spotify ou YouTube.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSpotifyLink(String url) {
        if (!SpotifyService.areCredentialsConfigured()) {
            Toast.makeText(getContext(),
                    "❌ Spotify não configurado!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String trackId = extractSpotifyTrackId(url);
        if (trackId != null) {
            btnFindMusic.setEnabled(false);
            btnFindMusic.setText("A procurar...");
            cardTrackPreview.setVisibility(View.GONE);

            CompletableFuture<Track> trackFuture = spotifyService.getTrack(trackId);
            CompletableFuture<AudioFeatures> audioFeaturesFuture = spotifyService.getAudioFeatures(trackId);

            trackFuture.thenCombine(audioFeaturesFuture, (track, audioFeatures) -> {
                if (track != null) {
                    requireActivity().runOnUiThread(() -> {
                        updateUiWithTrackDetails(track, audioFeatures, url);
                        btnFindMusic.setEnabled(true);
                        btnFindMusic.setText("Procurar Música");
                    });
                }
                return null;
            }).exceptionally(throwable -> {
                trackFuture.thenAccept(track -> {
                    if (track != null) {
                        requireActivity().runOnUiThread(() -> {
                            updateUiWithTrackDetails(track, null, url);
                            btnFindMusic.setEnabled(true);
                            btnFindMusic.setText("Procurar Música");
                        });
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            btnFindMusic.setEnabled(true);
                            btnFindMusic.setText("Procurar Música");
                            Toast.makeText(getContext(), "❌ Erro ao buscar música.", Toast.LENGTH_LONG).show();
                        });
                    }
                });
                return null;
            });
        } else {
            Toast.makeText(getContext(), "Link do Spotify inválido", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleYouTubeLink(String url) {
        Toast.makeText(getContext(), "Suporte para YouTube em breve!", Toast.LENGTH_SHORT).show();
    }

    private void updateUiWithTrackDetails(Track track, AudioFeatures audioFeatures, String spotifyUrl) {
        hideKeyboard();
        currentTrack = track;
        currentSpotifyUrl = spotifyUrl;

        resetTaps();
        cardTrackPreview.setVisibility(View.VISIBLE);

        tvTrackTitle.setText(track.getName());
        Log.d(TAG, "Track encontrada: " + track.getName());

        if (track.getArtists() != null && track.getArtists().length > 0) {
            StringBuilder artists = new StringBuilder();
            for (int i = 0; i < track.getArtists().length; i++) {
                artists.append(track.getArtists()[i].getName());
                if (i < track.getArtists().length - 1) {
                    artists.append(", ");
                }
            }
            tvTrackArtist.setText(artists.toString());
        }

        if (audioFeatures != null) {
            int bpm = (int) Math.round(audioFeatures.getTempo());
            etBpm.setText(String.valueOf(bpm));
            manualBpm = bpm;
            updateBpmDisplay();
            tilBpm.setHelperText("✓ BPM do Spotify (pode ajustar com Tap Tempo)");
            Toast.makeText(getContext(), "✓ Música encontrada com BPM!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "BPM do Spotify: " + bpm);
        } else {
            etBpm.setText("");
            manualBpm = null;
            tvBpmDisplay.setText("---");
            tilBpm.setHelperText("🎵 Use Tap Tempo para encontrar o BPM");
            Toast.makeText(getContext(), "✓ Música encontrada! Use Tap Tempo.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "BPM não disponível - usar Tap Tempo");
        }

        if (track.getAlbum().getImages().length > 0) {
            String imageUrl = track.getAlbum().getImages()[0].getUrl();
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(ivAlbumArt);
        }

        etLink.setText("");
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
            
            if (imm != null && getView() != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao fechar teclado: " + e.getMessage());
        }
    }

    private void saveTrackToLibrary() {
        Log.d(TAG, "=== INICIANDO SAVE ===");

        if (currentTrack == null) {
            Toast.makeText(getContext(), "Nenhuma música selecionada", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ERRO: currentTrack é null");
            return;
        }

        if (manualBpm == null) {
            tilBpm.setError("Use Tap Tempo ou insira o BPM");
            Toast.makeText(getContext(), "❌ BPM é obrigatório!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ERRO: BPM é null");
            return;
        }

        String title = currentTrack.getName();
        String artist = currentTrack.getArtists()[0].getName();
        int bpm = manualBpm;
        String link = currentSpotifyUrl;

        Log.d(TAG, "Dados a guardar:");
        Log.d(TAG, "  Title: " + title);
        Log.d(TAG, "  Artist: " + artist);
        Log.d(TAG, "  BPM: " + bpm);
        Log.d(TAG, "  Link: " + link);

        viewModel.setTitle(title);
        viewModel.setArtist(artist);
        viewModel.setBpm(bpm);
        viewModel.setLink(link);

        btnAddToLibrary.setEnabled(false);
        btnAddToLibrary.setText("A guardar...");

        Log.d(TAG, "Chamando viewModel.saveTrack()...");

        viewModel.saveTrack(new MusicRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess(long id) {
                Log.d(TAG, "✓✓✓ SUCESSO! Música guardada com ID: " + id + " ✓✓✓");
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "✓ Música guardada com BPM " + bpm + "! ID: " + id, Toast.LENGTH_LONG).show();
                    btnAddToLibrary.setEnabled(true);
                    btnAddToLibrary.setText("Adicionar à Biblioteca");

                    cardTrackPreview.setVisibility(View.GONE);
                    currentTrack = null;
                    currentSpotifyUrl = null;
                    manualBpm = null;
                    resetTaps();
                    etBpm.setText("");
                    tilBpm.setError(null);
                    tilBpm.setHelperText(null);

                    viewModel.clearForm();

                    Log.d(TAG, "UI limpa após save com sucesso");
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌❌❌ ERRO AO GUARDAR: " + error + " ❌❌❌");
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "❌ Erro ao guardar: " + error, Toast.LENGTH_LONG).show();
                    btnAddToLibrary.setEnabled(true);
                    btnAddToLibrary.setText("Adicionar à Biblioteca");
                });
            }
        });
    }

    private String extractSpotifyTrackId(String url) {
        Pattern pattern = Pattern.compile("track/([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tapResetHandler != null && tapResetRunnable != null) {
            tapResetHandler.removeCallbacks(tapResetRunnable);
        }
        Log.d(TAG, "onDestroyView");
    }
}