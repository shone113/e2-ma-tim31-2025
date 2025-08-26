package ftn.project.presentation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ftn.project.R;
import ftn.project.domain.entity.Badge;
import ftn.project.presentation.util.ImageResId;

public class BadgeAdapter extends ArrayAdapter<Badge> {
    private ArrayList<Badge> aBadges;

    private final Context context;
    public BadgeAdapter(Context context, ArrayList<Badge> badges){
        super(context, R.layout.badge_item, badges);
        this.context = context;
        aBadges = badges;
    }
    static class ViewHolder {
        ImageView ivBadge;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        ViewHolder vh;
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.badge_item,
                    parent, false);
            vh = new ViewHolder();
            vh.ivBadge = convertView.findViewById(R.id.ivBadge);
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }

        Badge badge = getItem(position);
        if(badge != null){
            int resId = ImageResId.returnResId(vh.ivBadge.getContext(), badge.getImageName());
            vh.ivBadge.setImageResource(resId != 0 ? resId : R.drawable.potion);
        }
        return convertView;
    }
}
