sed -i 's/onSaveProfile: (FirmProfile) -> Unit,/onSaveProfile: (FirmProfile) -> Unit,\n    onSaveFacilities: (List<Pair<String, Double>>) -> Unit,/g' app/src/main/java/com/example/ui/components/FirmLoginDialog.kt

cat << 'INNER_EOF' > /tmp/imports_insert.txt
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
INNER_EOF
sed -i '/import androidx.compose.runtime.setValue/r /tmp/imports_insert.txt' app/src/main/java/com/example/ui/components/FirmLoginDialog.kt

cat << 'INNER_EOF' > /tmp/facilities_insert.txt
    val facilities = remember {
        mutableStateListOf(
            DynamicFacilityItem(1L, "Godown 1 (Main Silo)", "2500")
        )
    }
INNER_EOF
sed -i '/var contactNumber by remember { mutableStateOf(currentProfile.contactNumber) }/r /tmp/facilities_insert.txt' app/src/main/java/com/example/ui/components/FirmLoginDialog.kt

