package com.steadyme.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.steadyme.app.databinding.ActivityEntrySavedBinding;

public class EntrySavedActivity extends AppCompatActivity {
    private ActivityEntrySavedBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEntrySavedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnDoneSaved.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
