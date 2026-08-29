sed -i 's/text = "Analytics Dashboard"/text = androidx.compose.ui.res.stringResource(com.example.R.string.dash_analytics_title)/g' app/src/main/java/com/example/ui/screens/BigDashboardScreen.kt
sed -i 's/text = "Live warehouse telemetry & logistics"/text = androidx.compose.ui.res.stringResource(com.example.R.string.dash_analytics_subtitle)/g' app/src/main/java/com/example/ui/screens/BigDashboardScreen.kt
sed -i 's/label = "Total Inbound"/label = androidx.compose.ui.res.stringResource(com.example.R.string.dash_kpi_inbound)/g' app/src/main/java/com/example/ui/screens/BigDashboardScreen.kt
sed -i 's/label = "Volume (MT)"/label = androidx.compose.ui.res.stringResource(com.example.R.string.dash_kpi_volume)/g' app/src/main/java/com/example/ui/screens/BigDashboardScreen.kt
sed -i 's/label = "Farmers"/label = androidx.compose.ui.res.stringResource(com.example.R.string.dash_kpi_farmers)/g' app/src/main/java/com/example/ui/screens/BigDashboardScreen.kt
sed -i 's/label = "Pending Payouts"/label = androidx.compose.ui.res.stringResource(com.example.R.string.dash_kpi_pending_pay)/g' app/src/main/java/com/example/ui/screens/BigDashboardScreen.kt
sed -i 's/text = "✨ AI Advisor"/text = androidx.compose.ui.res.stringResource(com.example.R.string.dash_btn_ai_advisor)/g' app/src/main/java/com/example/ui/screens/BigDashboardScreen.kt
sed -i 's/Text("New Gate Entry"/Text(androidx.compose.ui.res.stringResource(com.example.R.string.dash_btn_new_entry)/g' app/src/main/java/com/example/ui/screens/BigDashboardScreen.kt
