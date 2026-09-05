# Keep JavascriptInterface methods so Proguard doesn't obfuscate or strip them
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.soti.mdm.AndroidBridge { *; }
