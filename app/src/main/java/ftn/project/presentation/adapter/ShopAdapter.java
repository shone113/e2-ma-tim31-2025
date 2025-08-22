package ftn.project.presentation.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.io.InputStream;
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
        TextView tvReward;
        TextView tvUsageCount;
        LinearLayout root;
        TextView tvCost;
        MaterialButton btnBuy;
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
            vh.tvReward = convertView.findViewById(R.id.tvReward);
            vh.tvUsageCount = convertView.findViewById(R.id.tvUsageCount);
            vh.tvCost = convertView.findViewById(R.id.tvCost);
            vh.btnBuy = convertView.findViewById(R.id.btnBuy);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        Equipment equipment = getItem(position);
        if(equipment != null){
            String effectType;
            switch (equipment.getEffectType()){
                case STRENGTH:
                    effectType = "PP";
                    break;
                case ATTACK_CHANCE:
                    effectType = "         Attack success";
                    break;
                case EXTRA_ATTACK:
                    effectType = "         Attack chance";
                    break;
                default:
                    effectType = "";
            }
            vh.tvReward.setText("+ " + equipment.getBonusPercentage() + "% " + effectType);

            String useLabel;
            switch (equipment.getActiveType()) {
                case ONE_USE:
                    useLabel = "1 use";
                    break;
                case TWO_USES:
                    useLabel = "2 uses";
                    break;
                case PERMANENT:
                    useLabel = "permanent";
                    break;
                default:
                    useLabel = "";
            }
            vh.tvUsageCount.setText(useLabel);
            Double cost = equipment.getCostPercentageOfReward();
            vh.tvCost.setText("Cost: " + cost + "% of reward");
            vh.btnBuy.setText("Buy");

            String img = equipment.getImageName(); // npr. "potion"
            int resId = 0;
            if (img != null) {
                // ako u bazi nekad stoji "potion.png" – skini ekstenziju
                int dot = img.lastIndexOf('.');
                if (dot != -1) img = img.substring(0, dot);
                resId = getContext().getResources()
                        .getIdentifier(img, "drawable", getContext().getPackageName());
            }
            if (resId != 0) {
                vh.ivIcon.setImageResource(resId);
            } else {
                vh.ivIcon.setImageResource(R.drawable.potion);
            }

            vh.root.setOnClickListener(v ->
                    Log.i("Shop", "Klik na: " + equipment.getName())
            );
            //vh.btnBuy.setOnClickListener(v -> listener.onBuy(eq));

        }
        return convertView;
    }
}
