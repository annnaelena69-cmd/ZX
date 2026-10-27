package com.nexusx6.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF080C14)
private val Panel = Color(0xFF121D2B)
private val Cyan = Color(0xFF62F5E5)
private val Muted = Color(0xFFA4B4C8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("nexus", MODE_PRIVATE)
        setContent {
            var profile by remember { mutableStateOf(prefs.getString("profile", "Smooth Feel") ?: "Smooth Feel") }
            var shape by remember { mutableStateOf(prefs.getString("shape", "Plus") ?: "Plus") }
            var crossColor by remember { mutableStateOf(prefs.getString("color", "Cyan") ?: "Cyan") }
            var overlay by remember { mutableStateOf(false) }
            var boost by remember { mutableStateOf(prefs.getBoolean("boost", true)) }
            var impact by remember { mutableStateOf(prefs.getBoolean("impact", true)) }
            var status by remember { mutableStateOf("Siap bermain • POCO X6") }
            MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, background = Bg, surface = Panel, onPrimary = Bg, onBackground = Color.White, onSurface = Color.White)) {
                Column(Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("NEXUS X6 / GAMING LAUNCHER", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Main lebih\nresponsif.", fontSize = 36.sp, lineHeight = 39.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Gaming companion native Android untuk sesi 120 Hz.", color = Muted)
                    CardBlock("⚡ PERFORMANCE PRESET") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Balanced", "Smooth Feel", "Focus").forEach { item ->
                                FilterChip(selected = profile == item, onClick = { profile = item; prefs.edit().putString("profile", item).apply(); status = when(item) { "Smooth Feel" -> "Tips: tutup aplikasi latar dan jaga suhu."; "Focus" -> "Mode fokus dipilih."; else -> "Profil seimbang dipilih." } }, label = { Text(item, fontSize = 11.sp) })
                            }
                        }
                        Text("Preset memberi rekomendasi saja; tidak mengubah FPS atau batas refresh layar.", color = Muted, fontSize = 12.sp)
                    }
                    CardBlock("🎮 QUICK LAUNCH") {
                        Text("Buka game yang sudah terpasang.", color = Muted, fontSize = 12.sp)
                        GameButton("PUBG Mobile", "com.tencent.ig") { status = it }
                        GameButton("Mobile Legends", "com.mobile.legends") { status = it }
                        GameButton("Free Fire", "com.dts.freefireth") { status = it }
                        GameButton("Free Fire MAX", "com.dts.freefiremax") { status = it }
                    }
                    CardBlock("🎯 CROSSHAIR OVERLAY") {
                        Text("Overlay sederhana di atas aplikasi lain. Sebagian game mungkin melarang overlay; matikan jika mengganggu atau tidak diizinkan.", color = Muted, fontSize = 12.sp)
                        Text("Bentuk", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Plus", "Dot", "Circle", "X", "T").forEach { item ->
                                FilterChip(selected = shape == item, onClick = { shape = item; prefs.edit().putString("shape", item).apply() }, label = { Text(item, fontSize = 11.sp) })
                            }
                        }
                        Text("Warna", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Cyan", "Lime", "Pink", "Red", "White").forEach { item ->
                                FilterChip(selected = crossColor == item, onClick = { crossColor = item; prefs.edit().putString("color", item).apply() }, label = { Text(item, fontSize = 11.sp) })
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Aktifkan crosshair", color = Color.White)
                            Switch(checked = overlay, onCheckedChange = { enabled ->
                                if (enabled && !Settings.canDrawOverlays(this@MainActivity)) {
                                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                                    status = "Izinkan tampil di atas aplikasi, lalu kembali dan aktifkan lagi."
                                } else {
                                    overlay = enabled
                                    if (enabled) startCrosshair(shape, crossColor) else stopService(Intent(this@MainActivity, CrosshairService::class.java))
                                }
                            })
                        }
                        Button(onClick = {
                            if (!Settings.canDrawOverlays(this@MainActivity)) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                            else { startCrosshair(shape, crossColor); overlay = true }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Izinkan / perbarui overlay") }
                    }
                    CardBlock("👁️ VISUAL & COMFORT") {
                        SettingSwitch("Color boost (preferensi)", "Simpan pilihan untuk tuning manual di dalam game.", boost) { boost = it; prefs.edit().putBoolean("boost", it).apply() }
                        SettingSwitch("Reduced impact (preferensi)", "Gunakan pengaturan audio game/headset untuk efek nyata.", impact) { impact = it; prefs.edit().putBoolean("impact", it).apply() }
                        Button(onClick = { startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) }, modifier = Modifier.fillMaxWidth()) { Text("Buka pengaturan layar Android") }
                        Button(onClick = { startActivity(Intent(Settings.ACTION_SOUND_SETTINGS)) }, modifier = Modifier.fillMaxWidth()) { Text("Buka pengaturan suara Android") }
                    }
                    CardBlock("🎙️ ANIME VOICE STUDIO") {
                        Text("Voice changer wanita anime real-time belum diaktifkan dalam build ini. Android umumnya tidak mengizinkan aplikasi mengubah mikrofon aplikasi/game lain secara universal.", color = Muted, fontSize = 12.sp)
                        Button(onClick = { status = "Voice studio: perlu modul audio native khusus dan uji kompatibilitas." }, modifier = Modifier.fillMaxWidth()) { Text("Cek status voice studio") }
                    }
                    Text(status, color = Cyan, fontSize = 12.sp)
                    Text("NEXUS X6 • Aplikasi pendamping. Refresh rate maksimal mengikuti hardware dan pengaturan sistem.", color = Muted, fontSize = 11.sp)
                }
            }
        }
    }

    private fun startCrosshair(shape: String, color: String) {
        val i = Intent(this, CrosshairService::class.java).putExtra("shape", shape).putExtra("color", color)
        startService(i)
    }

    @Composable private fun GameButton(label: String, pkg: String, report: (String) -> Unit) {
        OutlinedButton(onClick = {
            val launch = packageManager.getLaunchIntentForPackage(pkg)
            if (launch != null) { startActivity(launch); report("Membuka $label…") }
            else report("$label belum terpasang atau ID paket berbeda.")
        }, modifier = Modifier.fillMaxWidth()) { Text("▶  $label") }
    }

    @Composable private fun CardBlock(title: String, content: @Composable ColumnScope.() -> Unit) {
        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).border(1.dp, Color(0xFF29394D), RoundedCornerShape(18.dp)).padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            content()
        }
    }

    @Composable private fun SettingSwitch(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontWeight = FontWeight.SemiBold); Text(description, color = Muted, fontSize = 11.sp) }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}
