package ftn.project.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ftn.project.R;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarVH> {

    public interface OnAvatarClick {
        void onClick(int drawableId, int position);
    }

    private final ArrayList<Integer> avatars; // npr. R.drawable.a1, a2...

    public AvatarAdapter(ArrayList<Integer> avatars) {
        this.avatars = avatars;
    }
    @Override
    public int getItemCount() {
        return avatars.size();
    }

    public Integer getItem(int position) {
        return avatars.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public AvatarVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.avatar_item, parent, false);
        return new AvatarVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarVH holder, int position) {
        Integer resId = getItem(position);
        holder.iv.setImageResource(resId);

        holder.itemView.setOnClickListener(v -> {
          //  if (listener != null) listener.onClick(resId, position);
        });
    }

    static class AvatarVH extends RecyclerView.ViewHolder {
        ImageView iv;
        AvatarVH(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.ivAvatar);
        }
    }

}
