package com.example.musicbpm.ui.library;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicbpm.R;
import com.example.musicbpm.data.database.MusicTrack;
import com.example.musicbpm.data.repository.MusicRepository;

/**
 * Fragment for displaying the music library.
 * Shows list of saved tracks with search and sort functionality.
 */
public class LibraryFragment extends Fragment implements MusicAdapter.OnTrackInteractionListener {

    private LibraryViewModel viewModel;
    private MusicAdapter adapter;

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private SearchView searchView;

    private static final String TAG = "LibraryFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(this).get(LibraryViewModel.class);
        Log.d(TAG, "onCreate - Fragment criado");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        initializeViews(view);
        setupRecyclerView();
        observeViewModel();

        Log.d(TAG, "onCreateView - Views inicializadas");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Fragment ficou visível");

        // Re-observa o ViewModel quando o fragment volta a ser visível
        // Isto força o LiveData a notificar com os dados mais recentes
        observeViewModel();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_library);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new MusicAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        Log.d(TAG, "RecyclerView configurado");
    }

    private void observeViewModel() {
        Log.d(TAG, "observeViewModel - Configurando observer do LiveData");

        viewModel.getDisplayedTracks().observe(getViewLifecycleOwner(), tracks -> {
            Log.d(TAG, "========================================");
            Log.d(TAG, "LiveData ATUALIZADO!");

            if (tracks == null) {
                Log.e(TAG, "Lista de tracks é NULL");
                recyclerView.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
                return;
            }

            Log.d(TAG, "Número de tracks recebidas: " + tracks.size());

            if (tracks.isEmpty()) {
                Log.d(TAG, "Lista VAZIA - mostrando empty state");
                recyclerView.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "Lista tem " + tracks.size() + " tracks - atualizando RecyclerView");

                // Log de cada track
                for (int i = 0; i < tracks.size(); i++) {
                    MusicTrack track = tracks.get(i);
                    Log.d(TAG, "  [" + i + "] " + track.getTitle() + " - " + track.getBpm() + " BPM");
                }

                recyclerView.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
                adapter.submitList(tracks);

                Log.d(TAG, "Adapter.submitList() chamado com " + tracks.size() + " tracks");
            }

            Log.d(TAG, "========================================");
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.library_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort_date) {
            viewModel.setSortMode("date");
            return true;
        } else if (id == R.id.action_sort_title) {
            viewModel.setSortMode("title");
            return true;
        } else if (id == R.id.action_sort_bpm) {
            viewModel.setSortMode("bpm");
            return true;
        } else if (id == R.id.action_sort_artist) {
            viewModel.setSortMode("artist");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTrackClick(MusicTrack track) {
        Log.d(TAG, "Track clicada: " + track.getTitle());

        // Open link in browser
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(track.getLink()));
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "Cannot open link", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTrackDelete(MusicTrack track) {
        Log.d(TAG, "Pedido para deletar track: " + track.getTitle());

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Track")
                .setMessage("Are you sure you want to delete \"" + track.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteTrack(track, new MusicRepository.OnOperationCompleteListener() {
                        @Override
                        public void onSuccess(long id) {
                            Log.d(TAG, "Track deletada com sucesso: " + id);
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Track deleted", Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Erro ao deletar track: " + error);
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onTrackEdit(MusicTrack track) {
        // TODO: Navigate to edit screen (future implementation)
        Toast.makeText(requireContext(), "Edit feature coming soon", Toast.LENGTH_SHORT).show();
    }
}