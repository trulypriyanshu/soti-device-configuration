package com.soti.mdm;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

public class AndroidBridge {

    private final Context context;

    public AndroidBridge(Context context) {
        this.context = context.getApplicationContext();
    }

    @JavascriptInterface
    public String getBrand() {
        return Build.BRAND != null ? Build.BRAND.toUpperCase(Locale.US) : "UNKNOWN";
    }

    @JavascriptInterface
    public String getManufacturer() {
        return Build.MANUFACTURER != null ? Build.MANUFACTURER.toUpperCase(Locale.US) : "UNKNOWN";
    }

    @JavascriptInterface
    public String getModel() {
        return Build.MODEL != null ? Build.MODEL : "Android Device";
    }

    @JavascriptInterface
    public String getDevice() {
        return Build.DEVICE != null ? Build.DEVICE : "";
    }

    @JavascriptInterface
    public String getProduct() {
        return Build.PRODUCT != null ? Build.PRODUCT : "";
    }

    @JavascriptInterface
    public String getAndroidVersion() {
        return "Android " + Build.VERSION.RELEASE;
    }

    @JavascriptInterface
    public int getSdkInt() {
        return Build.VERSION.SDK_INT;
    }

    @JavascriptInterface
    public String getSerialNumber() {
        // Try Build.getSerial() (works if permission granted / MDM device owner)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String serial = Build.getSerial();
                if (serial != null && !serial.equalsIgnoreCase("unknown") && !serial.trim().isEmpty()) {
                    return serial.toUpperCase(Locale.US);
                }
            }
        } catch (SecurityException ignored) {
            // Android 10+ restricts this for regular apps
        }

        // Try Build.SERIAL (older Android)
        try {
            if (Build.SERIAL != null && !Build.SERIAL.equalsIgnoreCase("unknown") && !Build.SERIAL.trim().isEmpty()) {
                return Build.SERIAL.toUpperCase(Locale.US);
            }
        } catch (Exception ignored) {}

        // Fallback: derive stable serial from Android ID
        try {
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (androidId != null && !androidId.trim().isEmpty()) {
                return androidId.toUpperCase(Locale.US);
            }
        } catch (Exception ignored) {}

        return "";
    }

    @JavascriptInterface
    public String getTotalRam() {
        try {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            if (actManager != null) {
                actManager.getMemoryInfo(memInfo);
                double gb = (double) memInfo.totalMem / (1024.0 * 1024.0 * 1024.0);
                return String.format(Locale.US, "%.1f GB", gb);
            }
        } catch (Exception ignored) {}
        return "8 GB";
    }

    @JavascriptInterface
    public String getTotalStorage() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            double gb = (double) (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0);
            return String.format(Locale.US, "%.0f GB", gb);
        } catch (Exception ignored) {}
        return "128 GB";
    }

    @JavascriptInterface
    public boolean isKnoxSupported() {
        String mfg = getManufacturer();
        String brand = getBrand();
        return mfg.contains("SAMSUNG") || brand.contains("SAMSUNG");
    }

    @JavascriptInterface
    public String getDeviceInfoJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("brand", getBrand());
            obj.put("manufacturer", getManufacturer());
            obj.put("model", getModel());
            obj.put("device", getDevice());
            obj.put("product", getProduct());
            obj.put("androidVersion", getAndroidVersion());
            obj.put("sdkInt", getSdkInt());
            obj.put("serial", getSerialNumber());
            obj.put("totalRam", getTotalRam());
            obj.put("totalStorage", getTotalStorage());
            obj.put("isKnox", isKnoxSupported());
        } catch (Exception ignored) {}
        return obj.toString();
    }

    @JavascriptInterface
    public void showToast(final String message) {
        if (message == null || message.trim().isEmpty()) return;
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
