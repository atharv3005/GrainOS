sed -i 's/.padding(16.dp),/.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),/g' app/src/main/java/com/example/ui/screens/OutboundDispatchScreen.kt

cat << 'INNER_EOF' > /tmp/imports.txt
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.widget.Toast
INNER_EOF
sed -i '/import androidx.compose.ui.unit.sp/r /tmp/imports.txt' app/src/main/java/com/example/ui/screens/OutboundDispatchScreen.kt
