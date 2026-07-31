package com.steadyme.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.steadyme.app.databinding.ActivityUserProfileBinding;


public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "USER_PROFILE_ACT";
    private static final String PREFS_NAME = "SteadyMePrefs";
    private ActivityUserProfileBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        viewBinding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        loadUserData();

        // --- Back Button Click Listener ---
        viewBinding.btnBack.setOnClickListener(v -> finish());

        // --- Logout Button Click Listener ---
        viewBinding.btnLogout.setOnClickListener(v -> performLogout());
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Email is safely stored natively in Firebase Auth
            viewBinding.tvUserEmail.setText(user.getEmail());

            // Fetch the Name from your Firestore "Users" collection
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // We use the Auth UID as the document ID to find the correct user
            db.collection("Users").document(user.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {

                                    // NOTE: Ensure "name" matches the exact key used during registration!
                                    String fullName = document.getString("name");

                                    if (fullName != null && !fullName.isEmpty()) {
                                        viewBinding.tvUserName.setText(fullName);
                                    } else {
                                        viewBinding.tvUserName.setText("SteadyMe User"); // Safe fallback
                                    }

                                } else {
                                    Log.d(TAG, "No user document found in Firestore.");
                                    viewBinding.tvUserName.setText("SteadyMe User");
                                }
                            } else {
                                // Standard error logging convention
                                Log.e(TAG, "Error fetching user data: ", task.getException());
                            }
                        }
                    });
        } else {
            // If the user is somehow null, they shouldn't be here. Log them out.
            performLogout();
        }
    }

    private void performLogout() {
        Log.d(TAG, "User initiated logout.");

        // 1. Sign out of Firebase Auth
        FirebaseAuth.getInstance().signOut();

        // 2. Clear local SharedPreferences
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // 3. Redirect to Login Activity & Clear stack
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // 4. Destroy this activity
        finish();
    }
}
