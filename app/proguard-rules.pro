# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep DataStore
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite { *; }

# Keep Keystore
-keep class android.security.keystore.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep our app models
-keep class com.apphider.data.model.** { *; }
-keep class com.apphider.domain.model.** { *; }
-keep class com.apphider.data.local.db.** { *; }