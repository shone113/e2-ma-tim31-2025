package ftn.project.presentation.adapter;

import android.content.Context;
import android.content.Intent;
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

import ftn.project.data.dto.UserFriendDTO;
import ftn.project.domain.entity.User;
import ftn.project.presentation.ui.ProfileActivity;

public class UserAdapter extends ArrayAdapter<UserFriendDTO> {
    public interface OnAddFriendClick {
        void onAdd(User user);
    }

    private ArrayList<UserFriendDTO> aFriends;
    private final OnAddFriendClick listener;

    public UserAdapter(Context context, ArrayList<UserFriendDTO> friendDTOs, OnAddFriendClick listener) {
        super(context, R.layout.user_card, friendDTOs);
        aFriends = friendDTOs;
        this.listener = listener;
    }
    public void submitList(List<UserFriendDTO> newItems) {
        aFriends.clear();
        if (newItems != null) aFriends.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        return aFriends.size();
    }

    @Nullable
    @Override
    public UserFriendDTO getItem(int position){
        return aFriends.get(position);
    }

    @Nullable
    public void addItems(List<UserFriendDTO> newItems) {
        int start = aFriends.size();
        aFriends.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position){
        return  position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        UserFriendDTO user = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_card,
                    parent, false);
        }
        LinearLayout userCard = convertView.findViewById(R.id.user_card_item);
        TextView tvName = convertView.findViewById(R.id.tvName);

        if(user != null){
            tvName.setText(user.username);
            userCard.setOnClickListener(v -> {
                Context ctx = v.getContext();
                Intent i = new Intent(ctx, ProfileActivity.class);
                i.putExtra(ProfileActivity.EXTRA_USER_ID, user.userId);
                ctx.startActivity(i);
                Log.i("All users", "Clicked: " + user.username);
            });
        }
    return convertView;
    }
}
