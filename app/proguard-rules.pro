# ===== 通用规则 =====
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-renamesourcefileattribute SourceFile

# ===== 混淆字典（使用 I1lO0 等易混淆字符） =====
-classobfuscationdictionary proguard-dict.txt
-obfuscationdictionary proguard-dict.txt
-packageobfuscationdictionary proguard-dict.txt

# ===== LSParanoid 字符串混淆 =====
-keep class com.lsparanoid.** { *; }
-dontwarn com.lsparanoid.**

# ===== Android 四大组件 =====
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ===== JNI Native 方法 =====
-keep class com.example.demo.crypto.native_.NativeCryptoManager {
    *;
}
-keep class com.example.demo.network.http.NativeHttpManager {
    *;
}

# ===== ViewBinding =====
-keep class * implements androidx.viewbinding.ViewBinding {
    *;
}

# ===== Bouncy Castle（反射加载算法） =====
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# ===== Protobuf / gRPC =====
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-keep class io.grpc.** { *; }
-dontwarn io.grpc.**

# ===== Gson / Retrofit =====
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# ===== OkHttp =====
-dontwarn okhttp3.**
-dontwarn okio.**

# ===== Conscrypt =====
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**

# ===== Tink =====
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# ===== Java-WebSocket =====
-keep class org.java_websocket.** { *; }
-dontwarn org.java_websocket.**

# ===== 枚举 =====
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===== Parcelable =====
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
