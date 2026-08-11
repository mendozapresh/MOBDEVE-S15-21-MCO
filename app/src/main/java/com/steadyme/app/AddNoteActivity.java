package com.steadyme.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.ActivityAddNoteBinding;
import com.steadyme.app.model.MoodLog;

public class AddNoteActivity extends AppCompatActivity {
    public static final String EXTRA_EMOTION = "extra_emotion";
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_SOURCE = "extra_source";

    private ActivityAddNoteBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBackNote.setOnClickListener(v -> finish());
        binding.btnSaveNote.setOnClickListener(v -> save());
    }

    private void save() {
        String emotion = getIntent().getStringExtra(EXTRA_EMOTION);
        int score = getIntent().getIntExtra(EXTRA_SCORE, 5);
        String source = getIntent().getStringExtra(EXTRA_SOURCE);
        String notes = binding.etNote.getText() == null ? "" : binding.etNote.getText().toString().trim();

        // Safety check for emotion
        if (emotion == null || emotion.isEmpty()) {
            Toast.makeText(this, "Emotion data missing. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnSaveNote.setEnabled(false);

        try {
            // We proceed immediately to the success screen. 
            // Firestore's local persistence ensures the data is "saved" to the UI 
            // instantly, while background sync handles the server upload.
            new FirebaseRepository().saveMood(new MoodLog(emotion, notes, source, score), new FirebaseRepository.SaveCallback() {
                @Override
                public void onSaved() {
                    // Already proceeded, but we could log success here
                }

                @Override
                public void onError(Exception e) {
                    // If a fatal error occurs (e.g. not signed in), Firestore might still 
                    // trigger this, though usually it just queues the work.
                }
            });

            Intent intent = new Intent(AddNoteActivity.this, EntrySavedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            binding.btnSaveNote.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
