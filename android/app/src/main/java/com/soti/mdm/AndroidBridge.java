package com.soti.mdm;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
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
        // 1. Try Build.getSerial() (Android 8+)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String s = Build.getSerial();
                if (isValidSerial(s)) {
                    return s.toUpperCase(Locale.US);
                }
            }
        } catch (Throwable ignored) {}

        // 2. Try SystemProperties reflection (works on Pixel, Samsung, Xiaomi, etc.)
        String[] propKeys = {"ro.serialno", "ro.boot.serialno", "gsm.sn1", "ril.serialnumber", "sys.serialnumber"};
        for (String key : propKeys) {
            try {
                Class<?> sp = Class.forName("android.os.SystemProperties");
                Method get = sp.getMethod("get", String.class);
                String val = (String) get.invoke(null, key);
                if (isValidSerial(val)) {
                    return val.toUpperCase(Locale.US);
                }
            } catch (Throwable ignored) {}
        }

        // 3. Try Build.SERIAL
        try {
            if (isValidSerial(Build.SERIAL)) {
                return Build.SERIAL.toUpperCase(Locale.US);
            }
        } catch (Throwable ignored) {}

        // 4. Secure Android ID fallback
        try {
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (androidId != null && !androidId.trim().isEmpty() && !androidId.equalsIgnoreCase("unknown")) {
                return androidId.toUpperCase(Locale.US);
            }
        } catch (Throwable ignored) {}

        return "";
    }

    private boolean isValidSerial(String s) {
        return s != null && !s.trim().isEmpty() && !s.equalsIgnoreCase("unknown") && !s.equalsIgnoreCase("none");
    }

    @JavascriptInterface
    public boolean isWifiEnabled() {
        try {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wm != null && wm.isWifiEnabled()) return true;
        } catch (Throwable ignored) {}
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                for (Network net : cm.getAllNetworks()) {
                    NetworkCapabilities caps = cm.getNetworkCapabilities(net);
                    if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    @JavascriptInterface
    public boolean isWifiConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                for (Network net : cm.getAllNetworks()) {
                    NetworkCapabilities caps = cm.getNetworkCapabilities(net);
                    if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    @JavascriptInterface
    public String getWifiIpAddress() {
        if (!isWifiEnabled() && !isWifiConnected()) {
            return "Wi-Fi is turned off";
        }

        // 1. ConnectivityManager LinkProperties (most accurate on modern Android)
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                for (Network net : cm.getAllNetworks()) {
                    NetworkCapabilities caps = cm.getNetworkCapabilities(net);
                    if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        android.net.LinkProperties lp = cm.getLinkProperties(net);
                        if (lp != null) {
                            for (android.net.LinkAddress la : lp.getLinkAddresses()) {
                                InetAddress addr = la.getAddress();
                                if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                                    return addr.getHostAddress();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 2. Iterate network interfaces (wlan, wifi, etc.)
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                String name = intf.getName().toLowerCase(Locale.US);
                if (name.contains("wlan") || name.contains("wifi") || name.contains("eth")) {
                    for (InetAddress addr : Collections.list(intf.getInetAddresses())) {
                        if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                            return addr.getHostAddress();
                        }
                    }
                }
            }
            // General active non-loopback interface
            for (NetworkInterface intf : interfaces) {
                if (!intf.isLoopback() && intf.isUp()) {
                    for (InetAddress addr : Collections.list(intf.getInetAddresses())) {
                        if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                            return addr.getHostAddress();
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 3. Fallback to WifiManager
        try {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wm != null && wm.getConnectionInfo() != null) {
                int ip = wm.getConnectionInfo().getIpAddress();
                if (ip != 0) {
                    return String.format(Locale.US, "%d.%d.%d.%d",
                            (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
                }
            }
        } catch (Throwable ignored) {}

        return "";
    }

    @JavascriptInterface
    public String getWifiMacAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (intf.getName().equalsIgnoreCase("wlan0")) {
                    byte[] mac = intf.getHardwareAddress();
                    if (mac != null) {
                        StringBuilder buf = new StringBuilder();
                        for (byte aMac : mac) {
                            buf.append(String.format("%02X:", aMac));
                        }
                        if (buf.length() > 0) {
                            buf.deleteCharAt(buf.length() - 1);
                        }
                        return buf.toString();
                    }
                }
            }
        } catch (Throwable ignored) {}
        return "02:00:00:00:00:00";
    }

    @JavascriptInterface
    public String getWifiDisplayInfo() {
        if (!isWifiEnabled() || !isWifiConnected()) {
            return "Wi-Fi is turned off";
        }
        return getWifiIpAddress();
    }

    @JavascriptInterface
    public String getCellularCarrier() {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                String opName = tm.getNetworkOperatorName();
                if (opName != null && !opName.trim().isEmpty()) {
                    return opName.toUpperCase(Locale.US);
                }
                String simName = tm.getSimOperatorName();
                if (simName != null && !simName.trim().isEmpty()) {
                    return simName.toUpperCase(Locale.US);
                }
            }
        } catch (Throwable ignored) {}
        return "JIO";
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
        } catch (Throwable ignored) {}
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
        } catch (Throwable ignored) {}
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
            obj.put("isWifiEnabled", isWifiEnabled());
            obj.put("isWifiConnected", isWifiConnected());
            obj.put("wifiIp", getWifiIpAddress());
            obj.put("wifiMac", getWifiMacAddress());
            obj.put("carrier", getCellularCarrier());
            obj.put("totalRam", getTotalRam());
            obj.put("totalStorage", getTotalStorage());
            obj.put("isKnox", isKnoxSupported());
        } catch (Throwable ignored) {}
        return obj.toString();
    }

    @JavascriptInterface
    public void showToast(final String message) {
        if (message == null || message.trim().isEmpty()) return;
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
