# Room
-keep class androidx.room.** { *; }
# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
# Keep entity classes (Room reflection on field names not needed, but safe)
-keepclassmembers class com.jinatra.finatra.data.local.entity.** { *; }
