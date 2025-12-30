# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Quy tắc giữ lại các class WorkManager (BẮT BUỘC)
-dontwarn androidx.work.**
-keep public class * extends androidx.work.ListenableWorker { *; }
-keep public class * extends androidx.work.Worker { *; }

# Giữ lại các class liên quan đến Notification/WorkManager Data
-keep class androidx.work.impl.workers.ConstraintTrackingWorker { *; }
-keep class androidx.work.impl.workers.DiagnosticsWorker { *; }
-keep public class androidx.work.Data { *; }
