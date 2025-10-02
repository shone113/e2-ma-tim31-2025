package ftn.project.presentation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import ftn.project.R;
import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.LevelDTO;
import ftn.project.domain.entity.Level;
import ftn.project.domain.entity.User;
import ftn.project.presentation.util.ImageResId;

public class LevelAdvancementAdapter extends ArrayAdapter<LevelDTO> {

    private ArrayList<LevelDTO> aLevels;
    private final Context context;
    private int currentLevel;
    public LevelAdvancementAdapter(Context context, ArrayList<LevelDTO> levelDTOs, int currentLevel){
        super(context, R.layout.level_item, levelDTOs);
        this.context = context;
        aLevels = levelDTOs;
        this.currentLevel = currentLevel;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        LevelDTO levelDTO = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.level_item,
                    parent, false);
        }
        MaterialCardView card = convertView.findViewById(R.id.level_item_card);
        TextView tvLevelNumber = convertView.findViewById(R.id.tvLevelNumber);
        ImageView ivTitleIcon = convertView.findViewById(R.id.ivTitleIcon);
        TextView tvLevelXP = convertView.findViewById(R.id.tvLevelXP);
        TextView tvRemainingXP = convertView.findViewById(R.id.tvRemainsXP);

        int color = (levelDTO.levelNumber <= currentLevel)
                ? ContextCompat.getColor(context, R.color.light_blue)
                : ContextCompat.getColor(context, R.color.darker_blue);
        card.setCardBackgroundColor(color);

        if(levelDTO != null){
            int resId = ImageResId.returnResId(ivTitleIcon.getContext(), levelDTO.titleIconKey);

            tvLevelNumber.setText(String.valueOf(levelDTO.levelNumber));
            ivTitleIcon.setImageResource(resId);
            tvLevelXP.setText(String.valueOf(levelDTO.requiredXP));
            tvRemainingXP.setText("Remains: " + levelDTO.remainingXP);
        }
        return convertView;
    }
}
