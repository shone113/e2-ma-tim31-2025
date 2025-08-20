package ftn.project.presentation.adapter;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ftn.project.R;

import ftn.project.domain.entity.User;

public class UserAdapter extends ArrayAdapter<User> {
    public interface OnAddFriendClick {
        void onAdd(User user);
    }

    private ArrayList<User> aUsers;
    private final OnAddFriendClick listener;

    public UserAdapter(Context context, ArrayList<User> users, OnAddFriendClick listener) {
        super(context, R.layout.user_card, users);
        aUsers = users;
        this.listener = listener;
    }

    @Override
    public int getCount(){
        return aUsers.size();
    }

    @Nullable
    @Override
    public User getItem(int position){
        return aUsers.get(position);
    }

    @Override
    public long getItemId(int position){
        return  position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        User user = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_card,
                    parent, false);
        }
        LinearLayout userCard = convertView.findViewById(R.id.user_card_item);
        TextView tvName = convertView.findViewById(R.id.tvName);

        if(user != null){
            tvName.setText(user.getUsername());
            userCard.setOnClickListener(v -> {
                Log.i("All users", "Clicked: " + user.getUsername());
            });
        }
    return convertView;
    }
}
