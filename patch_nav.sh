sed -i 's/enum class NavigationTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {/enum class NavigationTab(@androidx.annotation.StringRes val titleResId: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/DASHBOARD("Dashboard", Icons.Default.Dashboard),/DASHBOARD(R.string.nav_dashboard, Icons.Default.Dashboard),/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/PNL("P&L Engine", Icons.Default.TrendingUp),/PNL(R.string.nav_pnl, Icons.AutoMirrored.Filled.TrendingUp),/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/FINANCE("Finance Tracker", Icons.Default.AccountBalanceWallet),/FINANCE(R.string.nav_finance, Icons.Default.AccountBalanceWallet),/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/EXPENSES("Expenses", Icons.Default.Receipt),/EXPENSES(R.string.nav_expenses, Icons.Default.Receipt),/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/INBOUND("Gate Entry", Icons.Default.Scale),/INBOUND(R.string.nav_inbound, Icons.Default.Scale),/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/DISPATCH("Dispatch", Icons.Default.LocalShipping),/DISPATCH(R.string.nav_dispatch, Icons.Default.LocalShipping),/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/GODOWNS("Godowns", Icons.Default.Warehouse),/GODOWNS(R.string.nav_godowns, Icons.Default.Warehouse),/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/RECEIPTS("Receipts", Icons.Default.ReceiptLong),/RECEIPTS(R.string.nav_receipts, Icons.AutoMirrored.Filled.ReceiptLong),/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/LEDGER("Ledger", Icons.Default.ListAlt),/LEDGER(R.string.nav_ledger, Icons.Default.ListAlt),/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/AI_ADVISOR("AI Advisor", Icons.Default.AutoAwesome)/AI_ADVISOR(R.string.nav_ai_advisor, Icons.Default.AutoAwesome)/g' app/src/main/java/com/example/MainActivity.kt
