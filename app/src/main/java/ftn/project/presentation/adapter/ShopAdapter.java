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
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.User;
import ftn.project.domain.usecase.ComputeLevelStats;
import ftn.project.presentation.ui.ProfileActivity;
import ftn.project.presentation.util.ImageResId;

public class ShopAdapter extends ArrayAdapter<Equipment> {
    public interface OnBuyItemClick {
        void onBuyItemClick(User user, double price, Equipment equipment);
    }

    private ArrayList<Equipment> aEquipment;
    private final OnBuyItemClick listener;
    private final Context context;
    private String loggedFirebaseUid;
    public ShopAdapter(Context context, ArrayList<Equipment> users, String firebaseUid, OnBuyItemClick listener) {
        super(context, R.layout.shop_item_card, users);
        this.context = context;
        aEquipment = users;
        this.loggedFirebaseUid = firebaseUid;
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
            vh.tvReward.setText("+ " + Math.round(equipment.getBonusPercentage()) + "% " + effectType);

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
            AppDatabase db = AppDatabase.getInstance(context);
            User user = db.userRepository().getByFirebaseUid(loggedFirebaseUid);

            int coinsPreviousReward = ComputeLevelStats.coinsPreviousReward(user.getLevel());
            double price = (coinsPreviousReward / 100) * equipment.getCostPercentageOfReward();
            vh.btnBuy.setText("" + Math.round(price));
            vh.btnBuy.setIconResource(R.drawable.coin);
            vh.btnBuy.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            vh.btnBuy.setIconTint(null);

            if(user.getCoins() < price){
                vh.btnBuy.setEnabled(false);
            }

            vh.btnBuy.setOnClickListener(v -> {
                if (listener != null) listener.onBuyItemClick(user, price, equipment);
            });

            int resId = ImageResId.returnResId(vh.ivIcon.getContext(), equipment.getImageName());
            vh.ivIcon.setImageResource(resId != 0 ? resId : R.drawable.potion);

            vh.root.setOnClickListener(v ->
                    Log.i("Shop", "Klik na: " + equipment.getName())
            );
            //vh.btnBuy.setOnClickListener(v -> listener.onBuy(eq));

        }
        return convertView;
    }
}
