package com.steadyme.app.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.steadyme.app.databinding.ItemMoodLogBinding;
import com.steadyme.app.model.MoodLog;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class MoodLogAdapter extends RecyclerView.Adapter<MoodLogAdapter.Holder> {
    private final List<MoodLog> items = new ArrayList<>();

    public void submit(List<MoodLog> logs) {
        items.clear();
        items.addAll(logs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemMoodLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemMoodLogBinding binding;

        Holder(ItemMoodLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MoodLog moodLog) {
            binding.tvEmotion.setText(moodLog.getEmotion());
            binding.tvMoodEmoji.setText(emoji(moodLog.getEmotion()));
            binding.tvNotes.setText(moodLog.getNotes() == null || moodLog.getNotes().isEmpty() ? "No notes added" : moodLog.getNotes());
            binding.tvDate.setText(moodLog.getCreatedAt() == null ? "Saving…" : DateFormat.getDateInstance(DateFormat.MEDIUM).format(moodLog.getCreatedAt().toDate()));
        }

        private String emoji(String emotion) {
            if (emotion == null) {
                return "🙂";
            }
            switch (emotion) {
                case "Happy":
                    return "😊";
                case "Calm":
                    return "😌";
                case "Elated":
                    return "🤩";
                case "Anxious":
                    return "😰";
                case "Sad":
                    return "😢";
                case "Tired":
                    return "😴";
                case "Angry":
                    return "😠";
                default:
                    return "😣";
            }
        }
    }
}
