package ftn.project.presentation.util;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import ftn.project.R;

public final class ImageResId {


    public static int returnResId(Context ctx, String imageName){
        if (imageName == null || imageName.isEmpty()) return 0;
        int dot = imageName.lastIndexOf('.');
        if (dot != -1) imageName = imageName.substring(0, dot);

        return ctx.getResources().getIdentifier(imageName, "drawable", ctx.getPackageName());
    }
}
