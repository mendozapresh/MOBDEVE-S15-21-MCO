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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.steadyme.app.LoginActivity;
import com.steadyme.app.databinding.FragmentProfileBinding;

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

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(user.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    String fullName = document.getString("name");
                                    if (fullName != null && !fullName.isEmpty()) {
                                        binding.tvUserName.setText(fullName);
                                    } else {
                                        binding.tvUserName.setText("SteadyMe User");
                                    }
                                } else {
                                    Log.d(TAG, "No user document found in Firestore.");
                                    binding.tvUserName.setText("SteadyMe User");
                                }
                            } else {
                                Log.e(TAG, "Error fetching user data: ", task.getException());
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
