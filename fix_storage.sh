cat << 'INNER_EOF' > /tmp/storage.patch
--- app/src/main/java/com/example/security/FirmEncryptedPreferencesHelper.kt
+++ app/src/main/java/com/example/security/FirmEncryptedPreferencesHelper.kt
@@ -107,4 +107,12 @@
         return CropType.valueOf(cropStr)
     }
+
+    fun saveLong(key: String, value: Long) {
+        sharedPreferences.edit().putLong(key, value).apply()
+    }
+
+    fun getLong(key: String, defValue: Long): Long {
+        return sharedPreferences.getLong(key, defValue)
+    }
 }
--- app/src/main/java/com/example/security/SecureStorageManager.kt
+++ app/src/main/java/com/example/security/SecureStorageManager.kt
@@ -32,4 +32,12 @@
     fun getEncryptedPreferencesHelper(): FirmEncryptedPreferencesHelper {
         return encryptedPrefsHelper
     }
+
+    fun saveLong(key: String, value: Long) {
+        encryptedPrefsHelper.saveLong(key, value)
+    }
+
+    fun getLong(key: String, defValue: Long): Long {
+        return encryptedPrefsHelper.getLong(key, defValue)
+    }
 }
INNER_EOF
patch -p0 < /tmp/storage.patch
