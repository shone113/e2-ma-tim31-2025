package ftn.project.presentation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import ftn.project.R;
import ftn.project.domain.entity.Equipment;
import ftn.project.presentation.util.ImageResId;

public class EquipmentAdapter extends ArrayAdapter<Equipment> {
    private ArrayList<Equipment> aEquipment;
    private final Context context;
    public EquipmentAdapter(Context context, ArrayList<Equipment> equipment){
        super(context, R.layout.equipment_item, equipment);
        this.context = context;
        aEquipment = equipment;
    }

    static class ViewHolder {
        ImageView ivEquipment;
        TextView tvEquipmentName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        ViewHolder vh;
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.equipment_item,
                    parent, false);
            vh = new ViewHolder();
            vh.ivEquipment = convertView.findViewById(R.id.ivEquipment);
            convertView.setTag(vh);
        }else{
            vh = (ViewHolder) convertView.getTag();
        }

        Equipment equipment = getItem(position);
        if(equipment != null){
            int resId = ImageResId.returnResId(vh.ivEquipment.getContext(), equipment.getImageName());
            vh.ivEquipment.setImageResource(resId != 0 ? resId : R.drawable.potion);
        }
        return convertView;
    }
}
