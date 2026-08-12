package com.steadyme.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.steadyme.app.LoginActivity;
import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.FragmentProfileBinding;

import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String TAG = "PROFILE_FRAGMENT";
    private static final String PREFS_NAME = "SteadyMePrefs";
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserData();

        // --- Logout Button Click Listener ---
        binding.btnLogout.setOnClickListener(v -> performLogout());
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            binding.tvUserEmail.setText(user.getEmail());

            new FirebaseRepository().getProfile(new FirebaseRepository.ProfileCallback() {
                @Override
                public void onLoaded(Map<String, Object> data) {
                    if (data != null && binding != null) {
                        String fullName = (String) data.get("name");
                        if (fullName != null && !fullName.isEmpty()) {
                            binding.tvUserName.setText(fullName);
                        } else {
                            binding.tvUserName.setText("SteadyMe User");
                        }
                    }
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "Error fetching user data: ", error);
                    if (binding != null) {
                        binding.tvUserName.setText("SteadyMe User");
                    }
                }
            });
        } else {
            performLogout();
        }
    }

    private void performLogout() {
        Log.d(TAG, "User initiated logout.");
        FirebaseAuth.getInstance().signOut();

        if (getActivity() != null) {
            getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
