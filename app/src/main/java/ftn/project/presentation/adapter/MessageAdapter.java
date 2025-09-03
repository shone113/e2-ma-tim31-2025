package ftn.project.presentation.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ftn.project.R;
import ftn.project.data.dto.UserFriendDTO;
import ftn.project.domain.entity.AllianceMessage;

public class MessageAdapter extends ArrayAdapter<AllianceMessage> {
    private ArrayList<AllianceMessage> aMessages;
    private int loggedUserId;

    static class VH {
        TextView txtUsername, txtContent, txtTime;
    }
    public MessageAdapter(Context context, ArrayList<AllianceMessage> messages, int loggedUserId) {
        super(context, R.layout.message_item, messages);
        this.aMessages = messages;
        this.loggedUserId = loggedUserId;
    }
    public void replaceAll(List<AllianceMessage> newItems) {
        aMessages.clear();
        aMessages.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        return aMessages.size();
    }

    @Nullable
    @Override
    public AllianceMessage getItem(int position){
        return aMessages.get(position);
    }

    @Override
    public long getItemId(int position){
        return  position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        AllianceMessage message = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item,
                    parent, false);
        }
        if(message == null) return convertView;

        LinearLayout messageItem = convertView.findViewById(R.id.message_card_item);
        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        TextView tvContent = convertView.findViewById(R.id.tvContent);
        TextView tvTime = convertView.findViewById(R.id.tvTime);
        TextView tvDate = convertView.findViewById(R.id.tvDate);

        tvUsername.setText(message.getCreatorUsername());
        tvContent.setText(message.getContent());

        long sentAt = message.getSentAt();
        Date creationDate = new Date(sentAt);

        SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        String formattedTime = time.format(creationDate);
        String formattedDate = date.format(creationDate);

        tvTime.setText(formattedTime);
        tvDate.setText(formattedDate);

        boolean isMine = message.getCreatorUserId() == loggedUserId ? true : false;
        LinearLayout root = convertView.findViewById(R.id.root);
        if (isMine) {
            root.setGravity(Gravity.END);
        } else {
            root.setGravity(Gravity.START);
        }

        return convertView;
    }
}
