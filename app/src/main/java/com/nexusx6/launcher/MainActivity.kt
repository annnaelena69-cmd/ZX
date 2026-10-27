package com.nexusx6.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private val Bg = Color(0xFF080C14)
private val Panel = Color(0xFF121D2B)
private val Cyan = Color(0xFF62F5E5)
private val Muted = Color(0xFFA4B4C8)

private data class GameEntry(val label: String, val packageName: String)

class MainActivity : ComponentActivity() {
    private val voiceEngine = VoiceChangerEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("nexus", MODE_PRIVATE)
        setContent {
            var profile by remember { mutableStateOf(prefs.getString("profile", "Smooth Feel") ?: "Smooth Feel") }
            var shape by remember { mutableStateOf(prefs.getString("shape", "Plus") ?: "Plus") }
            var crossColor by remember { mutableStateOf(prefs.getString("color", "Cyan") ?: "Cyan") }
            var overlay by remember { mutableStateOf(false) }
            var overlayAuto by remember { mutableStateOf(prefs.getBoolean("overlay_auto", true)) }
            var boost by remember { mutableStateOf(prefs.getBoolean("boost", true)) }
            var impact by remember { mutableStateOf(prefs.getBoolean("impact", true)) }
            var voiceEnabled by remember { mutableStateOf(false) }
            var pitchRate by remember { mutableFloatStateOf(1.5f) }
            var status by remember { mutableStateOf("Siap bermain • POCO X6") }
            var selectedGames by remember { mutableStateOf(loadSavedGames(prefs)) }
            var showGamePicker by remember { mutableStateOf(false) }
            var availableGames by remember { mutableStateOf(emptyList<GameEntry>()) }
            var customPhoto by remember { mutableStateOf(prefs.getString("custom_photo", null)) }
            var clarityMode by remember { mutableStateOf(prefs.getString("clarity", "Balanced") ?: "Balanced") }
            val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
                    customPhoto = uri.toString()
                    prefs.edit().putString("custom_photo", customPhoto).apply()
                    status = "Foto kustom dipasang di launcher."
                }
            }
            val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    try {
                        voiceEngine.start(pitchRate)
                        voiceEnabled = true
                        status = "Live voice aktif • pakai earphone untuk menghindari feedback."
                    } catch (e: Exception) {
                        voiceEnabled = false
                        status = "Audio gagal dimulai: periksa izin mikrofon atau perangkat audio."
                    }
                } else status = "Izin mikrofon ditolak; voice studio tidak aktif."
            }
            DisposableEffect(Unit) { onDispose { voiceEngine.stop() } }

            MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, background = Bg, surface = Panel, onPrimary = Bg, onBackground = Color.White, onSurface = Color.White)) {
                Column(Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("◈ NEXUS X6 / BLACK LEGION", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Main lebih\nresponsif.", fontSize = 36.sp, lineHeight = 39.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Gaming companion • Android 15 / 16 • dark tactical edition", color = Muted)
                    CardBlock("🖼️ CUSTOM LAUNCHER ART") {
                        if (customPhoto != null) {
                            AsyncImage(model = customPhoto, contentDescription = "Foto kustom launcher", modifier = Modifier.fillMaxWidth().height(170.dp), contentScale = ContentScale.Crop)
                            OutlinedButton(onClick = { customPhoto = null; prefs.edit().remove("custom_photo").apply(); status = "Foto kustom dihapus." }, modifier = Modifier.fillMaxWidth()) { Text("Hapus foto") }
                        } else {
                            Box(Modifier.fillMaxWidth().height(92.dp).background(Color(0xFF080A0E), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Text("N X 6  •  BLACK LEGION", color = Cyan, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                            }
                        }
                        Button(onClick = { photoPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Pilih foto dari galeri") }
                        Text("Foto dipakai sebagai artwork di launcher; tidak diunggah ke server.", color = Muted, fontSize = 11.sp)
                    }
                    CardBlock("⚡ PERFORMANCE PRESET") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Balanced", "Smooth Feel", "Ultra Smooth", "Focus").forEach { item ->
                                FilterChip(selected = profile == item, onClick = { profile = item; prefs.edit().putString("profile", item).apply(); status = when(item) { "Smooth Feel" -> "Tips: refresh rate tertinggi yang tersedia, grafik stabil, suhu terjaga."; "Ultra Smooth" -> "Profil tips performa dipilih; tidak mengunci FPS atau mengubah refresh rate."; "Focus" -> "Mode fokus dipilih."; else -> "Profil seimbang dipilih." } }, label = { Text(item, fontSize = 11.sp) })
                            }
                        }
                        Text("Preset memberi rekomendasi saja; tidak mengubah FPS atau batas refresh layar.", color = Muted, fontSize = 12.sp)
                    }
                    CardBlock("🎮 MY GAME LIBRARY") {
                        Text("Pilih game yang terpasang di HP. Game pilihan akan tampil di daftar cepat.", color = Muted, fontSize = 12.sp)
                        SettingSwitch("Tampilkan tombol N saat game dibuka", "Overlay otomatis dijalankan sebelum game jika izin overlay sudah aktif.", overlayAuto) { overlayAuto = it; prefs.edit().putBoolean("overlay_auto", it).apply() }
                        Button(onClick = { availableGames = discoverGames(); showGamePicker = true }, modifier = Modifier.fillMaxWidth()) { Text("＋ Pilih / tambah game dari HP") }
                        if (selectedGames.isEmpty()) Text("Belum ada game dipilih. Tekan tombol di atas.", color = Muted, fontSize = 12.sp)
                        selectedGames.forEach { game ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(Modifier.weight(1f)) { GameButton(game.label, game.packageName, overlayAuto, shape, crossColor) { status = it } }
                                OutlinedButton(onClick = { selectedGames = selectedGames.filterNot { it.packageName == game.packageName }; saveGames(prefs, selectedGames); status = "${game.label} dihapus dari daftar cepat." }) { Text("−") }
                            }
                        }
                    }
                    if (showGamePicker) AlertDialog(
                        onDismissRequest = { showGamePicker = false },
                        title = { Text("Pilih game dari perangkat") },
                        text = {
                            if (availableGames.isEmpty()) Text("Tidak ada aplikasi launcher yang ditemukan.")
                            else LazyColumn(Modifier.heightIn(max = 420.dp)) {
                                items(availableGames, key = { it.packageName }) { game ->
                                    val alreadyAdded = selectedGames.any { it.packageName == game.packageName }
                                    TextButton(onClick = {
                                        if (!alreadyAdded) { selectedGames = (selectedGames + game).sortedBy { it.label.lowercase() }; saveGames(prefs, selectedGames); status = "${game.label} ditambahkan." }
                                        showGamePicker = false
                                    }, modifier = Modifier.fillMaxWidth()) { Text((if (alreadyAdded) "✓  " else "＋  ") + game.label, modifier = Modifier.fillMaxWidth()) }
                                }
                            }
                        },
                        confirmButton = { TextButton(onClick = { showGamePicker = false }) { Text("Selesai") } }
                    )
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
                    CardBlock("👁️ CLARITY / RED MAGIC-STYLE PRESET") {
                        Text("Pilih preset visibilitas. Android tidak mengizinkan aplikasi biasa mempertajam piksel game lain secara langsung; preset ini memberi panduan tuning dan tidak menandai atau melacak musuh.", color = Muted, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Balanced", "Clarity", "Vivid", "Eye Care").forEach { item ->
                                FilterChip(selected = clarityMode == item, onClick = { clarityMode = item; prefs.edit().putString("clarity", item).apply(); status = when(item) { "Clarity" -> "Clarity dipilih: naikkan sharpness di pengaturan game jika tersedia."; "Vivid" -> "Vivid dipilih: coba saturasi layar secara moderat."; "Eye Care" -> "Eye Care dipilih."; else -> "Visual seimbang dipilih." } }, label = { Text(item, fontSize = 11.sp) })
                            }
                        }
                        Text(when (clarityMode) { "Clarity" -> "Saran: gunakan resolusi tertinggi yang stabil, anti-aliasing sesuai FPS, dan sharpness game jika tersedia."; "Vivid" -> "Saran: gunakan mode warna layar yang lebih hidup jika tersedia di pengaturan sistem."; "Eye Care" -> "Saran: kurangi kecerahan di ruangan gelap dan aktifkan kenyamanan mata saat jeda."; else -> "Saran: prioritaskan frame rate stabil dan suhu perangkat." }, color = Cyan, fontSize = 11.sp)
                    }
                    CardBlock("👁️ VISUAL & COMFORT") {
                        SettingSwitch("Color boost (preferensi)", "Simpan pilihan untuk tuning manual di dalam game.", boost) { boost = it; prefs.edit().putBoolean("boost", it).apply() }
                        SettingSwitch("Reduced impact (preferensi)", "Gunakan pengaturan audio game/headset untuk efek nyata.", impact) { impact = it; prefs.edit().putBoolean("impact", it).apply() }
                        Button(onClick = { startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) }, modifier = Modifier.fillMaxWidth()) { Text("Buka pengaturan layar Android") }
                        Button(onClick = { startActivity(Intent(Settings.ACTION_SOUND_SETTINGS)) }, modifier = Modifier.fillMaxWidth()) { Text("Buka pengaturan suara Android") }
                    }
                    CardBlock("🎙️ ANIME VOICE STUDIO • LIVE MIC") {
                        Text("Ubah pitch suara mikrofon secara langsung dan dengarkan hasilnya. Pilih pitch lebih tinggi untuk efek suara anime; gunakan earphone agar suara speaker tidak masuk kembali ke mikrofon.", color = Muted, fontSize = 12.sp)
                        Text("Pitch / gaya", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Natural" to 1.0f, "Anime" to 1.35f, "Anime +" to 1.6f).forEach { (label, rate) ->
                                FilterChip(selected = pitchRate == rate, onClick = {
                                    pitchRate = rate
                                    if (voiceEnabled) {
                                        try { voiceEngine.start(rate); status = "Pitch $label aktif secara live." }
                                        catch (_: Exception) { voiceEnabled = false; status = "Audio gagal dimulai; coba lagi setelah cek izin mikrofon." }
                                    }
                                }, label = { Text(label, fontSize = 11.sp) })
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                Text(if (voiceEnabled) "Live microphone aktif" else "Aktifkan live voice", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("Izin mikrofon hanya dipakai saat fitur ini dinyalakan.", color = Muted, fontSize = 11.sp)
                            }
                            Switch(checked = voiceEnabled, onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        try { voiceEngine.start(pitchRate); voiceEnabled = true; status = "Live voice aktif • gunakan earphone." }
                                        catch (_: Exception) { voiceEnabled = false; status = "Audio gagal dimulai; periksa izin dan perangkat audio." }
                                    } else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    voiceEngine.stop()
                                    voiceEnabled = false
                                    status = "Live voice dimatikan."
                                }
                            })
                        }
                        Text("Batas Android: ini memproses mikrofon dan memonitor suara di aplikasi ini. Tidak bisa mengganti input mic PUBG/ML/Free Fire dari aplikasi lain tanpa dukungan sistem/game.", color = Muted, fontSize = 11.sp)
                    }
                    Text(status, color = Cyan, fontSize = 12.sp)
                    Text("NEXUS X6 • Aplikasi pendamping. Refresh rate maksimal mengikuti hardware dan pengaturan sistem.", color = Muted, fontSize = 11.sp)
                }
            }
        }
    }

    override fun onDestroy() {
        voiceEngine.stop()
        super.onDestroy()
    }

    private fun startCrosshair(shape: String, color: String) {
        val i = Intent(this, CrosshairService::class.java).putExtra("shape", shape).putExtra("color", color)
        startService(i)
    }

    private fun discoverGames(): List<GameEntry> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return try { packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL).mapNotNull { info ->
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == packageName) return@mapNotNull null
            val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty()
            if (label.isBlank()) null else GameEntry(label, pkg)
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() } } catch (_: Exception) { emptyList() }
    }

    private fun loadSavedGames(prefs: android.content.SharedPreferences): List<GameEntry> {
        val saved = prefs.getStringSet("selected_games", null)
        if (saved != null) return saved.mapNotNull { value -> val parts = value.split("|", limit = 2); if (parts.size == 2) GameEntry(parts[1], parts[0]) else null }.sortedBy { it.label.lowercase() }
        return listOf(GameEntry("PUBG Mobile", "com.tencent.ig"), GameEntry("Mobile Legends", "com.mobile.legends"), GameEntry("Free Fire", "com.dts.freefireth"), GameEntry("Free Fire MAX", "com.dts.freefiremax"))
    }

    private fun saveGames(prefs: android.content.SharedPreferences, games: List<GameEntry>) {
        prefs.edit().putStringSet("selected_games", games.map { "${it.packageName}|${it.label}" }.toSet()).apply()
    }

    @Composable private fun GameButton(label: String, pkg: String, autoOverlay: Boolean, overlayShape: String, overlayColor: String, report: (String) -> Unit) {
        OutlinedButton(onClick = {
            val launch = packageManager.getLaunchIntentForPackage(pkg)
            if (launch != null) {
                if (autoOverlay && Settings.canDrawOverlays(this@MainActivity)) startCrosshair(overlayShape, overlayColor)
                startActivity(launch)
                report(if (autoOverlay && !Settings.canDrawOverlays(this@MainActivity)) "Membuka $label. Izinkan overlay agar tombol N tampil di dalam game." else "Membuka $label…")
            }
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
