package com.steadyme.app.model;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Firestore document stored at users/{uid}/moodLogs/{logId}.
 */
public class MoodLog {
    private String id;
    private String emotion;
    private String notes;
    private String source;
    private int score;
    private List<String> reasons;
    private Timestamp createdAt;

    public MoodLog() {
    }

    public MoodLog(String emotion, String notes, String source, int score) {
        this(emotion, notes, source, score, new ArrayList<>());
    }

    public MoodLog(String emotion, String notes, String source, int score, List<String> reasons) {
        this.emotion = emotion;
        this.notes = notes;
        this.source = source;
        this.score = score;
        this.reasons = reasons;
        this.createdAt = Timestamp.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
