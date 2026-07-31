package com.steadyme.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.steadyme.app.LoginActivity;
import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.FragmentHomeBinding;
import com.steadyme.app.model.MoodLog;

import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private ListenerRegistration registration;
    private final FirebaseRepository repo = new FirebaseRepository();
    private final MoodLogAdapter adapter = new MoodLogAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecent.setAdapter(adapter);

        String email = FirebaseAuth.getInstance().getCurrentUser() == null ? "there" : FirebaseAuth.getInstance().getCurrentUser().getEmail();
        binding.tvGreetingName.setText(email == null ? "there 👋" : email.split("@")[0] + " 👋");
        binding.tvLogout.setOnClickListener(x -> logout());

        registration = repo.observeMoodLogs(new FirebaseRepository.LogsCallback() {
            @Override
            public void onLoaded(List<MoodLog> logs) {
                adapter.submit(logs);
                binding.tvStreakValue.setText(String.valueOf(calculateStreak(logs)));
                binding.progressStreak.setProgress(Math.min(7, calculateStreak(logs)));
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(binding.getRoot(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private int calculateStreak(List<MoodLog> logs) {
        return logs.size();
    }

    @Override
    public void onDestroyView() {
        if (registration != null) {
            registration.remove();
        }
        binding = null;
        super.onDestroyView();
    }
}
