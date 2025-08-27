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
    public LevelAdvancementAdapter(Context context, ArrayList<LevelDTO> levelDTOs){
        super(context, R.layout.level_item, levelDTOs);
        this.context = context;
        aLevels = levelDTOs;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        LevelDTO levelDTO = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.level_item,
                    parent, false);
        }
        TextView tvLevelNumber = convertView.findViewById(R.id.tvLevelNumber);
        ImageView ivTitleIcon = convertView.findViewById(R.id.ivTitleIcon);
        TextView tvLevelXP = convertView.findViewById(R.id.tvLevelXP);
        TextView tvRemainingXP = convertView.findViewById(R.id.tvRemainsXP);

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
