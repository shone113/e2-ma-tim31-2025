package ftn.project.presentation.util;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class QrGenerator {
    public static Bitmap generate(String content, int sizePx) throws WriterException {
        BarcodeEncoder encoder = new BarcodeEncoder();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.MARGIN, 1); // tanak border
        return encoder.encodeBitmap(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints);
    }
}
