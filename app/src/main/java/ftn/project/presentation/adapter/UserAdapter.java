package ftn.project.presentation.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button; // za MaterialButton koristi import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ftn.project.R;

import ftn.project.domain.entity.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
    public interface OnAddFriendClick {
        void onAdd(User user);
    }

    private final List<User> items = new ArrayList<>();
    private final OnAddFriendClick listener;

    public UserAdapter(OnAddFriendClick listener) {
        this.listener = listener;
    }

    public void setItems(List<User> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged(); // kasnije može DiffUtil
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        User u = items.get(position);
        h.tvName.setText(u.getUsername());
        h.btnAddFriend.setOnClickListener(v -> {
            if (listener != null) listener.onAdd(u);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName;
        final MaterialButton btnAddFriend;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
        }
    }
}
