package com.example.musicbpm.ui.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicbpm.R;
import com.example.musicbpm.data.database.MusicTrack;
import com.example.musicbpm.utils.PlatformDetector;

/**
 * RecyclerView Adapter for displaying music tracks in the library.
 */
public class MusicAdapter extends ListAdapter<MusicTrack, MusicAdapter.TrackViewHolder> {

    private OnTrackInteractionListener listener;

    public MusicAdapter(OnTrackInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<MusicTrack> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MusicTrack>() {
                @Override
                public boolean areItemsTheSame(@NonNull MusicTrack oldItem, @NonNull MusicTrack newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull MusicTrack oldItem, @NonNull MusicTrack newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle()) &&
                            oldItem.getBpm() == newItem.getBpm() &&
                            (oldItem.getArtist() != null ? oldItem.getArtist().equals(newItem.getArtist()) : newItem.getArtist() == null);
                }
            };

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_music_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        MusicTrack track = getItem(position);
        holder.bind(track, listener);
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvArtist;
        private TextView tvBpm;
        private TextView tvPlatform;
        private ImageView ivAlbumArt;
        private View btnDelete;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            // IDs corrigidos para corresponder ao item_music_track.xml
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvArtist = itemView.findViewById(R.id.tv_artist);
            tvBpm = itemView.findViewById(R.id.tv_bpm);
            tvPlatform = itemView.findViewById(R.id.tv_platform);
            ivAlbumArt = itemView.findViewById(R.id.iv_album_art);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(MusicTrack track, OnTrackInteractionListener listener) {
            // Define o título
            tvTitle.setText(track.getTitle());

            // Define o artista (ou esconde se não existir)
            if (track.getArtist() != null && !track.getArtist().isEmpty()) {
                tvArtist.setText(track.getArtist());
                tvArtist.setVisibility(View.VISIBLE);
            } else {
                tvArtist.setVisibility(View.GONE);
            }

            // Define o BPM
            tvBpm.setText(track.getBpm() + " BPM");

            // Define a plataforma
            String platformName = PlatformDetector.getPlatformDisplayName(track.getPlatform());
            tvPlatform.setText(platformName);

            // Mostra a badge da plataforma apenas se não for "Other"
            if (track.getPlatform() != null && !track.getPlatform().equals(PlatformDetector.PLATFORM_OTHER)) {
                tvPlatform.setVisibility(View.VISIBLE);
            } else {
                tvPlatform.setVisibility(View.GONE);
            }

            // Album art usa o placeholder definido no layout
            // No futuro, podes carregar imagens reais aqui com Glide/Picasso

            // Click listener - abre o link
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTrackClick(track);
                }
            });

            // Delete button listener
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTrackDelete(track);
                }
            });

            // Long click listener - editar (futuro)
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTrackEdit(track);
                }
                return true;
            });
        }
    }

    /**
     * Interface para callbacks de interação com tracks
     */
    public interface OnTrackInteractionListener {
        void onTrackClick(MusicTrack track);
        void onTrackDelete(MusicTrack track);
        void onTrackEdit(MusicTrack track);
    }
}