package ftn.project.presentation.adapter;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ftn.project.R;
import ftn.project.data.dto.UserFriendDTO;
import ftn.project.domain.entity.InvitationStatus;
import ftn.project.presentation.ui.ProfileActivity;

public class FriendAdapter extends ArrayAdapter<UserFriendDTO> {
    public interface OnInviteFriendClick {
        void onInvite(UserFriendDTO dto);
    }
    private ArrayList<UserFriendDTO> aFriends;
    private final OnInviteFriendClick listener;
    private final boolean leaderUser;

    public FriendAdapter(Context context, ArrayList<UserFriendDTO> friendDTOs, boolean leaderUser, OnInviteFriendClick listener) {
        super(context, R.layout.user_card, new ArrayList<>());
        this.aFriends = new ArrayList<>(friendDTOs);
        this.leaderUser = leaderUser;
        this.listener = listener;
    }
    public void replaceAll(List<UserFriendDTO> newItems) {
        aFriends.clear();
        aFriends.addAll(newItems);
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

    @Override
    public long getItemId(int position){
        return  position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        UserFriendDTO userFriendDTO = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_card,
                    parent, false);
        }
        if (userFriendDTO == null) return convertView;

        LinearLayout userCard = convertView.findViewById(R.id.user_card_item);
        TextView tvName = convertView.findViewById(R.id.tvName);
        Button btnAddFriend = convertView.findViewById(R.id.btnAddFriend);

        Log.w("UU123", "" + userFriendDTO.friend);

        if(leaderUser){
            if(userFriendDTO.invitationStatus == InvitationStatus.ACCEPTED){
                btnAddFriend.setEnabled(false);
                btnAddFriend.setText("Member");
            }else if(userFriendDTO.invitationStatus == InvitationStatus.PENDING){
                btnAddFriend.setEnabled(false);
                btnAddFriend.setText("Pending");
            }else{
                btnAddFriend.setEnabled(true);
                btnAddFriend.setText("Invite friend");
            }
        }else{
            btnAddFriend.setVisibility(View.GONE);
        }

        if(userFriendDTO != null){
            tvName.setText(userFriendDTO.username);
            userCard.setOnClickListener(v -> {
                Context ctx = v.getContext();
                Intent i = new Intent(ctx, ProfileActivity.class);
                i.putExtra(ProfileActivity.EXTRA_USER_ID, userFriendDTO.userId);
                ctx.startActivity(i);
                Log.i("All users", "Clicked: " + userFriendDTO.username);
            });
        }

        btnAddFriend.setOnClickListener(v -> {
            btnAddFriend.setEnabled(false);
            btnAddFriend.setText("Pending");
            listener.onInvite(userFriendDTO);
        });

        return convertView;
    }

    public void updateStatus(String inviteId, String status) {

    }

}
