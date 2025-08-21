package ftn.project.presentation.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import ftn.project.R;
import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.User;
import ftn.project.presentation.ui.ProfileActivity;

public class ShopAdapter extends ArrayAdapter<Equipment> {
    public interface OnBuyItemClick {
        void onAdd(Equipment equipment);
    }

    private ArrayList<Equipment> aEquipment;
    private final OnBuyItemClick listener;


    public ShopAdapter(Context context, ArrayList<Equipment> users, OnBuyItemClick listener) {
        super(context, R.layout.shop_item_card, users);
        aEquipment = users;
        this.listener = listener;
    }
    @Override
    public int getCount(){
        return aEquipment.size();
    }

    @Nullable
    @Override
    public Equipment getItem(int position){
        return aEquipment.get(position);
    }

    @Override
    public long getItemId(int position){
        return  position;
    }

    static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvPrice;
        LinearLayout root;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        ViewHolder vh;
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.shop_item_card,
                    parent, false);
            vh = new ViewHolder();
            vh.root   = convertView.findViewById(R.id.shop_item_card); // promeni id u XML
            vh.ivIcon = convertView.findViewById(R.id.ivItemIcon);
            vh.tvName = convertView.findViewById(R.id.tvItemTitle);
            vh.tvPrice= convertView.findViewById(R.id.tvItemPrice);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        Equipment equipment = getItem(position);
        if(equipment != null){
            vh.tvName.setText(equipment.getName());
            vh.tvPrice.setText((equipment.getBattleCount() != null
                    ? equipment.getBattleCount() * 500 : 500) + " coins");

            int resId = getContext().getResources().getIdentifier(
                    equipment.getImageName() == null ? "" : equipment.getImageName(),
                    "drawable",
                    getContext().getPackageName()
            );
            if (resId != 0) {
                vh.ivIcon.setImageResource(resId);
            } else {
                //vh.ivIcon.setImageResource(R.drawable.ic_placeholder);
            }

            vh.root.setOnClickListener(v ->
                    Log.i("Shop", "Klik na: " + equipment.getName())
            );
        }
        return convertView;
    }
}
