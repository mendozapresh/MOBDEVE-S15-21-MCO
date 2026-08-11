package com.steadyme.app.data;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.steadyme.app.model.AppNotification;
import com.steadyme.app.model.MoodLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralizes authenticated Firestore access; UI never supplies a uid.
 */
public class FirebaseRepository {

    public interface LogsCallback {
        void onLoaded(List<MoodLog> logs);
        void onError(Exception error);
    }

    public interface NotificationsCallback {
        void onLoaded(List<AppNotification> notifications);
        void onError(Exception error);
    }

    public interface SaveCallback {
        void onSaved();
        void onError(Exception error);
    }

    public interface ProfileCallback {
        void onLoaded(Map<String, Object> data);
        void onError(Exception error);
    }

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference logs() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Please sign in first.");
        }
        return db.collection("users").document(user.getUid()).collection("moodLogs");
    }

    private CollectionReference notifications() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Please sign in first.");
        }
        return db.collection("users").document(user.getUid()).collection("notifications");
    }

    public void saveMood(@NonNull MoodLog log, @NonNull SaveCallback callback) {
        try {
            logs().add(log)
                    .addOnSuccessListener(ref -> callback.onSaved())
                    .addOnFailureListener(callback::onError);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    public ListenerRegistration observeMoodLogs(@NonNull LogsCallback callback) {
        try {
            return logs().orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener((snap, e) -> {
                        if (e != null) {
                            callback.onError(e);
                            return;
                        }
                        List<MoodLog> result = new ArrayList<>();
                        if (snap != null) {
                            for (DocumentSnapshot d : snap.getDocuments()) {
                                MoodLog log = d.toObject(MoodLog.class);
                                if (log != null) {
                                    log.setId(d.getId());
                                    result.add(log);
                                }
                            }
                        }
                        callback.onLoaded(result);
                    });
        } catch (Exception e) {
            callback.onError(e);
            return null;
        }
    }

    public void createProfile(String name) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).set(new HashMap<String, Object>() {{
                put("name", name);
                put("email", user.getEmail());
                put("createdAt", FieldValue.serverTimestamp());
            }}, SetOptions.merge());
        }
    }

    public void getProfile(@NonNull ProfileCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError(new IllegalStateException("User not signed in"));
            return;
        }
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(snap -> {
                    if (snap.exists()) {
                        callback.onLoaded(snap.getData());
                    } else {
                        callback.onError(new Exception("Profile not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void saveNotification(String title, String message) {
        try {
            notifications().add(new AppNotification(title, message));
        } catch (Exception ignored) {}
    }

    public ListenerRegistration observeNotifications(@NonNull NotificationsCallback callback) {
        try {
            return notifications().orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener((snap, e) -> {
                        if (e != null) {
                            callback.onError(e);
                            return;
                        }
                        List<AppNotification> result = new ArrayList<>();
                        if (snap != null) {
                            for (DocumentSnapshot d : snap.getDocuments()) {
                                AppNotification n = d.toObject(AppNotification.class);
                                if (n != null) {
                                    n.setId(d.getId());
                                    result.add(n);
                                }
                            }
                        }
                        callback.onLoaded(result);
                    });
        } catch (Exception e) {
            callback.onError(e);
            return null;
        }
    }
}
