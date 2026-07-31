<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true"
    android:background="#FFFFFF">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="20dp">

        <TextView
            android:id="@+id/tvInsightsTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Insights"
            android:textColor="#000000"
            android:textSize="28sp"
            android:textStyle="bold"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintStart_toStartOf="parent" />

        <TextView
            android:id="@+id/tvInsightSummary"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="Your emotional patterns at a glance"
            android:textColor="#666666"
            app:layout_constraintTop_toBottomOf="@id/tvInsightsTitle"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />

        <!-- 1. Top 3 Counters -->
        <LinearLayout
            android:id="@+id/llTopCounters"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp"
            android:orientation="horizontal"
            android:weightSum="3"
            app:layout_constraintTop_toBottomOf="@id/tvInsightSummary">

            <!-- Total Logs Box -->
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:gravity="center"
                android:background="#F5F5F5"
                android:padding="12dp"
                android:layout_marginEnd="8dp">
                <TextView
                    android:id="@+id/tvTotalLogs"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    tools:text="14"
                    android:textColor="#000000"
                    android:textSize="22sp"
                    android:textStyle="bold" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="TOTAL LOGS"
                    android:textColor="#666666"
                    android:textSize="10sp"
                    android:layout_marginTop="4dp"/>
            </LinearLayout>

            <!-- Day Streak Box -->
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:gravity="center"
                android:background="#F5F5F5"
                android:padding="12dp"
                android:layout_marginEnd="8dp">
                <TextView
                    android:id="@+id/tvDayStreak"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    tools:text="14d"
                    android:textColor="#000000"
                    android:textSize="22sp"
                    android:textStyle="bold" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="DAY STREAK"
                    android:textColor="#666666"
                    android:textSize="10sp"
                    android:layout_marginTop="4dp"/>
            </LinearLayout>

            <!-- This Week Box -->
            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical"
                android:gravity="center"
                android:background="#F5F5F5"
                android:padding="12dp">
                <TextView
                    android:id="@+id/tvThisWeek"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    tools:text="7/7"
                    android:textColor="#000000"
                    android:textSize="22sp"
                    android:textStyle="bold" />
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="THIS WEEK"
                    android:textColor="#666666"
                    android:textSize="10sp"
                    android:layout_marginTop="4dp"/>
            </LinearLayout>
        </LinearLayout>

        <!-- 2. Dynamic Description Card -->
        <androidx.constraintlayout.widget.ConstraintLayout
            android:id="@+id/clDynamicDesc"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp"
            android:background="#F5F5F5"
            android:padding="16dp"
            app:layout_constraintTop_toBottomOf="@id/llTopCounters">

            <TextView
                android:id="@+id/tvDynamicTitle"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                tools:text="Significant Mood Swing"
                android:textStyle="bold"
                android:textColor="#FF9800"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintTop_toTopOf="parent"
                app:layout_constraintEnd_toEndOf="parent" />

            <TextView
                android:id="@+id/tvDynamicDesc"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:textColor="#333333"
                tools:text="A large mood shift was detected in recent entries."
                app:layout_constraintTop_toBottomOf="@id/tvDynamicTitle"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintEnd_toEndOf="parent" />
        </androidx.constraintlayout.widget.ConstraintLayout>

        <!-- 3. Existing Mood Trend LineChart -->
        <LinearLayout
            android:id="@+id/llTrendContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="#F5F5F5"
            android:padding="16dp"
            android:layout_marginTop="20dp"
            app:layout_constraintTop_toBottomOf="@id/clDynamicDesc">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Mood Trend"
                android:textColor="#000000"
                android:textStyle="bold"
                android:textSize="18sp" />

            <com.github.mikephil.charting.charts.LineChart
                android:id="@+id/chartMood"
                android:layout_width="match_parent"
                android:layout_height="200dp"
                android:layout_marginTop="12dp" />
        </LinearLayout>

        <!-- 4. Mood Distribution PieChart -->
        <LinearLayout
            android:id="@+id/llDistributionContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="#F5F5F5"
            android:padding="16dp"
            android:layout_marginTop="20dp"
            app:layout_constraintTop_toBottomOf="@id/llTrendContainer">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Mood Distribution"
                android:textColor="#000000"
                android:textStyle="bold"
                android:textSize="18sp" />

            <com.github.mikephil.charting.charts.PieChart
                android:id="@+id/chartMoodDistribution"
                android:layout_width="match_parent"
                android:layout_height="250dp"
                android:layout_marginTop="12dp" />
        </LinearLayout>

        <!-- 5. Mood Consistency -->
        <androidx.constraintlayout.widget.ConstraintLayout
            android:id="@+id/clConsistency"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp"
            android:background="#F5F5F5"
            android:padding="16dp"
            app:layout_constraintTop_toBottomOf="@id/llDistributionContainer">

            <TextView
                android:id="@+id/tvConsistencyHeader"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Mood Consistency"
                android:textColor="#000000"
                android:textStyle="bold"
                android:textSize="18sp"
                app:layout_constraintTop_toTopOf="parent"
                app:layout_constraintStart_toStartOf="parent"/>

            <TextView
                android:id="@+id/tvAvgThisWeek"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:textColor="#666666"
                tools:text="AVG THIS WEEK\nN/A"
                android:textAlignment="center"
                app:layout_constraintTop_toBottomOf="@id/tvConsistencyHeader"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintEnd_toStartOf="@id/tvStability" />

            <TextView
                android:id="@+id/tvStability"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:textColor="#4CAF50"
                android:textStyle="bold"
                tools:text="STABILITY\nHighly Stable"
                android:textAlignment="center"
                app:layout_constraintTop_toTopOf="@id/tvAvgThisWeek"
                app:layout_constraintStart_toEndOf="@id/tvAvgThisWeek"
                app:layout_constraintEnd_toEndOf="parent" />

            <!-- Consistency Score Progress Bar -->
            <TextView
                android:id="@+id/tvScoreLabel"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:text="Consistency Score"
                android:textColor="#666666"
                app:layout_constraintTop_toBottomOf="@id/tvAvgThisWeek"
                app:layout_constraintStart_toStartOf="parent" />

            <TextView
                android:id="@+id/tvScorePercent"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                tools:text="85%"
                android:textColor="#4CAF50"
                android:textStyle="bold"
                app:layout_constraintTop_toTopOf="@id/tvScoreLabel"
                app:layout_constraintEnd_toEndOf="parent" />

            <ProgressBar
                android:id="@+id/pbConsistency"
                style="?android:attr/progressBarStyleHorizontal"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:max="100"
                android:progressTint="#4CAF50"
                app:layout_constraintTop_toBottomOf="@id/tvScoreLabel" />

        </androidx.constraintlayout.widget.ConstraintLayout>

    </androidx.constraintlayout.widget.ConstraintLayout>
</androidx.core.widget.NestedScrollView>
