package com.example.musicbpm.data.spotify;

import android.util.Log;

import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

public class SpotifyService {
    private static final String TAG = "SpotifyService";

    // ⚠️ IMPORTANTE: Substitui estas credenciais pelas tuas próprias!
    // Obtém em: https://developer.spotify.com/dashboard/applications
    private static final String CLIENT_ID = "c5d2a66ea01d495d94bc4a6b2428916c";
    private static final String CLIENT_SECRET = "e7bca760ced24c0ebcfbf2260f55b68e";

    private SpotifyApi spotifyApi;
    private String accessToken;
    private long tokenExpirationTime = 0;
    private boolean isInitializing = false;
    private CompletableFuture<Void> initializationFuture;

    public SpotifyService() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .build();
    }

    /**
     * Renova o access token do Spotify (válido por 1 hora)
     * ✅ Agora é assíncrono para não bloquear a UI thread
     */
    private synchronized CompletableFuture<Void> refreshAccessToken() {
        // Se já está a inicializar, retorna o future existente
        if (isInitializing && initializationFuture != null) {
            return initializationFuture;
        }

        isInitializing = true;

        initializationFuture = CompletableFuture.runAsync(() -> {
            try {
                Log.d(TAG, "A obter access token do Spotify...");

                ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
                ClientCredentials clientCredentials = clientCredentialsRequest.execute();

                accessToken = clientCredentials.getAccessToken();
                // Token expira em 3600 segundos (1 hora)
                tokenExpirationTime = System.currentTimeMillis() + (clientCredentials.getExpiresIn() * 1000);

                // Atualiza o token na API
                spotifyApi.setAccessToken(accessToken);

                Log.d(TAG, "✓ Access token renovado com sucesso");
                Log.d(TAG, "Token expira em: " + clientCredentials.getExpiresIn() + " segundos");

                isInitializing = false;

            } catch (IOException | SpotifyWebApiException | ParseException e) {
                Log.e(TAG, "❌ Erro ao obter access token", e);
                Log.e(TAG, "Verifica se CLIENT_ID e CLIENT_SECRET estão corretos!");
                isInitializing = false;
                throw new RuntimeException("Erro ao obter token do Spotify", e);
            }
        });

        return initializationFuture;
    }

    /**
     * Verifica se o token está válido, e renova se necessário
     */
    private CompletableFuture<Void> ensureValidToken() {
        // Se nunca obteve token ou está expirado, renova
        if (accessToken == null || System.currentTimeMillis() >= (tokenExpirationTime - 300000)) {
            Log.d(TAG, "Token inválido ou expirado. A renovar...");
            return refreshAccessToken();
        }

        // Token ainda válido
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Obtém informações de uma track do Spotify
     */
    public CompletableFuture<Track> getTrack(String trackId) {
        return ensureValidToken()
                .thenCompose(v -> {
                    GetTrackRequest getTrackRequest = spotifyApi.getTrack(trackId).build();
                    return getTrackRequest.executeAsync();
                })
                .thenApply(track -> {
                    Log.d(TAG, "✓ Track obtida: " + track.getName());
                    return track;
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "❌ Erro ao obter track: " + trackId, throwable);

                    // Se for erro de autenticação, força renovação
                    if (throwable.getMessage() != null &&
                            (throwable.getMessage().contains("401") ||
                                    throwable.getMessage().contains("Unauthorized") ||
                                    throwable.getMessage().contains("Forbidden"))) {
                        Log.d(TAG, "Erro de autenticação. Forçando renovação de token...");
                        accessToken = null; // Força renovação no próximo pedido
                    }

                    return null;
                });
    }

    /**
     * Obtém audio features (incluindo BPM) de uma track
     */
    public CompletableFuture<AudioFeatures> getAudioFeatures(String trackId) {
        return ensureValidToken()
                .thenCompose(v -> {
                    GetAudioFeaturesForTrackRequest getAudioFeaturesRequest =
                            spotifyApi.getAudioFeaturesForTrack(trackId).build();
                    return getAudioFeaturesRequest.executeAsync();
                })
                .thenApply(audioFeatures -> {
                    Log.d(TAG, "✓ Audio Features obtidas - BPM: " + audioFeatures.getTempo());
                    return audioFeatures;
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "❌ Erro ao obter audio features: " + trackId, throwable);

                    // Se for erro de autenticação, força renovação
                    if (throwable.getMessage() != null &&
                            (throwable.getMessage().contains("401") ||
                                    throwable.getMessage().contains("Unauthorized") ||
                                    throwable.getMessage().contains("Forbidden"))) {
                        Log.d(TAG, "Erro de autenticação. Forçando renovação de token...");
                        accessToken = null; // Força renovação no próximo pedido
                    }

                    return null;
                });
    }

    /**
     * Verifica se as credenciais estão configuradas
     */
    public static boolean areCredentialsConfigured() {
        return !CLIENT_ID.equals("SEU_CLIENT_ID_AQUI") &&
                !CLIENT_SECRET.equals("SEU_CLIENT_SECRET_AQUI");
    }
}