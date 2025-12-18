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
                            oldItem.getBpm() == newItem.getBpm();
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
        private ImageView ivPlatformIcon;
        private View btnDelete;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_track_title);
            tvArtist = itemView.findViewById(R.id.tv_track_artist);
            tvBpm = itemView.findViewById(R.id.tv_track_bpm);
            tvPlatform = itemView.findViewById(R.id.tv_track_platform);
            ivPlatformIcon = itemView.findViewById(R.id.iv_platform_icon);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(MusicTrack track, OnTrackInteractionListener listener) {
            tvTitle.setText(track.getTitle());

            if (track.getArtist() != null && !track.getArtist().isEmpty()) {
                tvArtist.setText(track.getArtist());
                tvArtist.setVisibility(View.VISIBLE);
            } else {
                tvArtist.setVisibility(View.GONE);
            }

            tvBpm.setText(track.getBpm() + " BPM");

            String platformName = PlatformDetector.getPlatformDisplayName(track.getPlatform());
            tvPlatform.setText(platformName);

            // Set platform icon (you would need to add actual icons)
            setPlatformIcon(track.getPlatform(), ivPlatformIcon);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTrackClick(track);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTrackDelete(track);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTrackEdit(track);
                }
                return true;
            });
        }

        private void setPlatformIcon(String platform, ImageView imageView) {
            // Set appropriate icon based on platform
            // For now, using a placeholder approach
            int iconRes = R.drawable.ic_launcher_foreground; // Default

            switch (platform) {
                case PlatformDetector.PLATFORM_YOUTUBE:
                    // iconRes = R.drawable.ic_youtube;
                    break;
                case PlatformDetector.PLATFORM_SPOTIFY:
                    // iconRes = R.drawable.ic_spotify;
                    break;
                case PlatformDetector.PLATFORM_SOUNDCLOUD:
                    // iconRes = R.drawable.ic_soundcloud;
                    break;
            }

            imageView.setImageResource(iconRes);
        }
    }

    public interface OnTrackInteractionListener {
        void onTrackClick(MusicTrack track);
        void onTrackDelete(MusicTrack track);
        void onTrackEdit(MusicTrack track);
    }
}