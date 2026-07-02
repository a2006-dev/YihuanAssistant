# 基础优化
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# 保持 Kotlin 元数据
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }

# 保持序列化
-keepattributes Signature
-keepattributes Exceptions

# 保持 Retrofit 接口
-keep interface com.yh.assistant.data.api.** { *; }

# 保持数据模型
-keep class com.yh.assistant.data.model.** { *; }
-keepclassmembers class com.yh.assistant.data.model.** { *; }

-keep class com.yh.assistant.util.PreferenceUtil { *; }
-keepclassmembers class com.yh.assistant.util.PreferenceUtil$* { *; }
-keep class com.yh.assistant.util.CacheManager { *; }
-keepclassmembers class com.yh.assistant.util.CacheManager$* { *; }
-keep class com.yh.assistant.util.** { *; }

# 不混淆加密相关类
-keep class com.yh.assistant.crypto.** { *; }
-keepclassmembers class com.yh.assistant.crypto.** { *; }

# 保持 Gson 序列化
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*

# 混淆所有其他类
-repackageclasses 'a'
-allowaccessmodification
-flattenpackagehierarchy
-overloadaggressively