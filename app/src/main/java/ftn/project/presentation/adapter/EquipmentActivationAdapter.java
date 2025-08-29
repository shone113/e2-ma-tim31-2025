package ftn.project.presentation.adapter;

import android.content.Context;
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

import java.util.ArrayList;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.UserEquipmentDTO;
import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.User;
import ftn.project.domain.entity.UserEquipment;
import ftn.project.presentation.util.ImageResId;

public class EquipmentActivationAdapter extends ArrayAdapter<UserEquipmentDTO> {

    public interface OnActivateItemClick {
        void onActivateItemClick(int userEquipmentId);
    }
    private ArrayList<UserEquipmentDTO> aUserEquipmentDTOs;
    private final OnActivateItemClick listener;
    private final Context context;
    private String loggedFirebaseUid;
    public EquipmentActivationAdapter(Context context, ArrayList<UserEquipmentDTO> userEquipmentDTOs, String firebaseUid, OnActivateItemClick  listener) {
        super(context, R.layout.equipment_item_card, userEquipmentDTOs);
        this.context = context;
        aUserEquipmentDTOs = userEquipmentDTOs;
        this.loggedFirebaseUid = firebaseUid;
        this.listener = listener;
    }
    @Override
    public int getCount(){
        return aUserEquipmentDTOs.size();
    }

    @Nullable
    @Override
    public UserEquipmentDTO getItem(int position){
        return aUserEquipmentDTOs.get(position);
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
        MaterialButton btnActivate;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        EquipmentActivationAdapter.ViewHolder vh;
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.equipment_item_card,
                    parent, false);
            vh = new EquipmentActivationAdapter.ViewHolder();
            vh.root   = convertView.findViewById(R.id.equipment_item_card);
            vh.ivIcon = convertView.findViewById(R.id.ivItemIcon);
            vh.tvReward = convertView.findViewById(R.id.tvReward);
            vh.tvUsageCount = convertView.findViewById(R.id.tvUsageCount);
            vh.btnActivate = convertView.findViewById(R.id.btnActivate);
            convertView.setTag(vh);
        } else {
            vh = (EquipmentActivationAdapter.ViewHolder) convertView.getTag();
        }

        UserEquipmentDTO userEquipmentDTO = getItem(position);
        if(userEquipmentDTO != null){
            String effectType;
            switch (userEquipmentDTO.effectType){
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
            vh.tvReward.setText("+ " + Math.round(userEquipmentDTO.bonusPercentage) + "% " + effectType);

            String useLabel;
            switch (userEquipmentDTO.activeType) {
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

            if(userEquipmentDTO.active){
                vh.btnActivate.setText("Active");
                vh.btnActivate.setEnabled(false);
            }else{
                vh.btnActivate.setText("Activate");
                vh.btnActivate.setEnabled(true);
            }


            vh.btnActivate.setOnClickListener(v -> {
                vh.btnActivate.setEnabled(false);
                vh.btnActivate.setText("Activated");
                if (listener != null) listener.onActivateItemClick(userEquipmentDTO.userEquipmentId);
            });

            int resId = ImageResId.returnResId(vh.ivIcon.getContext(), userEquipmentDTO.imageName);
            vh.ivIcon.setImageResource(resId != 0 ? resId : R.drawable.potion);

            vh.root.setOnClickListener(v ->
                    Log.i("ACTIVATE EQUIPMENT ID: ", "CLICK na: " + userEquipmentDTO.userEquipmentId)
            );

        }
        return convertView;
    }
}
