package com.steadyme.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.ListenerRegistration;
import com.steadyme.app.R;
import com.steadyme.app.data.FirebaseRepository;
import com.steadyme.app.databinding.FragmentNotificationsBinding;
import com.steadyme.app.databinding.ItemNotificationBinding;
import com.steadyme.app.model.AppNotification;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private ListenerRegistration registration;
    private final NotificationAdapter adapter = new NotificationAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotifications.setAdapter(adapter);

        binding.btnBackNotifs.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        registration = new FirebaseRepository().observeNotifications(new FirebaseRepository.NotificationsCallback() {
            @Override
            public void onLoaded(List<AppNotification> notifications) {
                adapter.submit(notifications);
                binding.tvEmptyNotifs.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(binding.getRoot(), "Error loading notifications", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (registration != null) {
            registration.remove();
        }
        binding = null;
        super.onDestroyView();
    }

    static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Holder> {
        private final List<AppNotification> items = new ArrayList<>();

        void submit(List<AppNotification> notifications) {
            items.clear();
            items.addAll(notifications);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
            private final ItemNotificationBinding binding;

            Holder(ItemNotificationBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            void bind(AppNotification n) {
                binding.tvNotifTitle.setText(n.getTitle());
                binding.tvNotifMessage.setText(n.getMessage());
                binding.tvNotifDate.setText(n.getCreatedAt() == null ? "" : DateFormat.getDateInstance(DateFormat.SHORT).format(n.getCreatedAt().toDate()));
            }
        }
    }
}
