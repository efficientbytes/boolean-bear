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
-flattenpackagehierarchy 'com.pankratov.yevgeny'
-allowaccessmodification

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-ignorewarnings
-keep class * {
    public *;
}

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

-dontwarn org.xmlpull.v1.**
-dontnote org.xmlpull.v1.**
-keep class org.xmlpull.** { *; }

#retRofit
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**

#Image Cropper
-keep class androidx.appcompat.widget.** { *; }
-keepnames class com.parse.** { *; }

# Required for Parse
-keepattributes *Annotation*
-keepattributes Signature
-dontwarn com.squareup.**
-dontwarn okio.**

-keep class com.parse.*{ *; }
-dontwarn com.parse.**
-dontwarn com.squareup.picasso.**

-keepclasseswithmembernames class * {
    native <methods>;
}

#for room DB
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# all repositories
-keep class app.efficientbytes.booleanbear.repositories.AdsRepository {
    public *;
}

-keep class app.efficientbytes.booleanbear.repositories.AssetsRepository {
    public *;
}

-keep class app.efficientbytes.booleanbear.repositories.AuthenticationRepository {
    public *;
}

-keep class app.efficientbytes.booleanbear.repositories.FeedbackNSupportRepository{
    public *;
}

-keep class app.efficientbytes.booleanbear.repositories.StatisticsRepository {
    public *;
}

-keep class app.efficientbytes.booleanbear.repositories.UserProfileRepository {
    public *;
}

-keep class app.efficientbytes.booleanbear.repositories.UtilityDataRepository {
    public *;
}

-keep class app.efficientbytes.booleanbear.repositories.VerificationRepository{
    public *;
}

# all services
-keep class app.efficientbytes.booleanbear.services.AdFreeSessionService{
    public *;
}
-keep class app.efficientbytes.booleanbear.services.PushNotificationService{
    public *;
}

# all viewmodel
-keep class app.efficientbytes.booleanbear.viewmodels.* {
    public *;
}

#adapters
-keep class app.efficientbytes.booleanbear.ui.adapters.* {
    public *;
}

#koin DI
-keep class app.efficientbytes.booleanbear.di.* {
    public *;
}

#utils
-keep class app.efficientbytes.booleanbear.utils.* {
    public *;
}

#all models
-keep class app.efficientbytes.booleanbear.models.** { *; }
-keep class app.efficientbytes.booleanbear.services.models.** { *; }
-keep class app.efficientbytes.booleanbear.database.models.** { *; }
-keep class app.efficientbytes.booleanbear.repositories.models.** { *; }
-keep class app.efficientbytes.booleanbear.ui.models.** { *; }
