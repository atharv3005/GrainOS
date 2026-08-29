cat << 'INNER_EOF' > /tmp/whatsapp_logic.txt
                        val firmName = currentProfile.firmName.ifEmpty { "GrainOS Enterprise" }
                        val netKg = (gross - tare).coerceAtLeast(0.0)
                        val msg = """*GATE PASS*
$firmName
-----------------------
Destination: $destination
Truck No: $vehicleNumber
Gross: ${gross.toInt()} kg
Tare: ${tare.toInt()} kg
Net: ${netKg.toInt()} kg
""".trimIndent()
                        val intent = Intent(Intent.ACTION_VIEW)
                        val numStr = driverMobile.filter { it.isDigit() }
                        if (numStr.isNotEmpty()) {
                            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=91$numStr&text=${Uri.encode(msg)}")
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp not found.", Toast.LENGTH_SHORT).show()
                            }
                        }
INNER_EOF
sed -i '/ratePerQuintal = ""/r /tmp/whatsapp_logic.txt' app/src/main/java/com/example/ui/screens/OutboundDispatchScreen.kt
