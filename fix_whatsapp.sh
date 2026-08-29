sed -i 's/val firmName = currentProfile.firmName.ifEmpty { "GrainOS Enterprise" }/val firmName = "GrainOS Enterprise"/' app/src/main/java/com/example/ui/screens/OutboundDispatchScreen.kt
cat << 'INNER_EOF' > /tmp/context.txt
    val context = LocalContext.current
INNER_EOF
sed -i '/var editingDispatch by remember/r /tmp/context.txt' app/src/main/java/com/example/ui/screens/OutboundDispatchScreen.kt
