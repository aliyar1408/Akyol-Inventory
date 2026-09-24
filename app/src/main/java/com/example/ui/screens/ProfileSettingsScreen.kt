package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel
import com.example.util.ExportUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val currentRole by viewModel.currentRole.collectAsState()
    val assets by viewModel.allAssets.collectAsState()

    var showRoleDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
            Text(
                text = "Profil & Sistem Ayarları",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = NavyDark
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // User Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(TurquoisePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AA",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = NavyDark
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Aliyar Akyol",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "aliyar.akyol@gmail.com",
                                    fontSize = 12.sp,
                                    color = TurquoiseLight
                                )
                                Text(
                                    text = "AKYOL INVENTORY Yönetici Portalı",
                                    fontSize = 11.sp,
                                    color = TextMutedDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = NavySurface)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Mevcut Kullanıcı Rolü", fontSize = 11.sp, color = TextMutedDark)
                                Text(currentRole.labelTr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrightBlue)
                            }

                            Button(
                                onClick = { showRoleDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Rolü Değiştir", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Notification / Alerts Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sistem Bildirimleri ve Uyarılar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        AlertRowItem(
                            icon = Icons.Default.Warning,
                            iconColor = WarningAmber,
                            title = "Garanti Bitiş Yaklaşan",
                            desc = "3 demirbaşın garantisi önümüzdeki 60 gün içinde sona erecek."
                        )
                        AlertRowItem(
                            icon = Icons.Default.Build,
                            iconColor = StatusMaintenance,
                            title = "Planlı Bakım Vakti",
                            desc = "Daikin Klima ve HP Lazer Yazıcı periyodik bakım bekliyor."
                        )
                        AlertRowItem(
                            icon = Icons.Default.AssignmentLate,
                            iconColor = StatusFaulty,
                            title = "Geciken Zimmet İadeleri",
                            desc = "Tüm personeller güncel iade takvimine uygun durumdadır."
                        )
                    }
                }
            }

            // Module Navigation Shortcuts
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Yönetim Modülleri", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        SettingMenuItem(
                            icon = Icons.Default.People,
                            title = "Personel (Zimmet Dağılımı)",
                            desc = "Çalışan listesi ve zimmetli demirbaşları yönet",
                            onClick = { viewModel.navigateTo(AppScreen.EMPLOYEES) }
                        )

                        SettingMenuItem(
                            icon = Icons.Default.BarChart,
                            title = "Raporlama & Analitik",
                            desc = "10 farklı kurumsal rapor türü ve veri analizi",
                            onClick = { viewModel.navigateTo(AppScreen.REPORTS) }
                        )

                        SettingMenuItem(
                            icon = Icons.Default.FactCheck,
                            title = "Sayım & Saha Denetimi",
                            desc = "2026 Yıllık sayım oturumu ve QR kontrolü",
                            onClick = { viewModel.navigateTo(AppScreen.AUDIT) }
                        )

                        SettingMenuItem(
                            icon = Icons.Default.CloudUpload,
                            title = "Excel'den Toplu İçe Aktar",
                            desc = "Şablon ile toplu demirbaş yükleme ve doğrulama",
                            onClick = { showImportDialog = true }
                        )

                        SettingMenuItem(
                            icon = Icons.Default.Download,
                            title = "Tüm Veriyi Dışa Aktar (CSV / Excel)",
                            desc = "Tüm aktif demirbaş verisini indir",
                            onClick = {
                                val csv = ExportUtil.generateAssetCsv(assets)
                                ExportUtil.shareText(context, "AKYOL_Tum_Demirbaslar.csv", csv)
                            }
                        )
                    }
                }
            }

            // System Information Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Sistem Bilgileri", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                        Text("Uygulama: AKYOL INVENTORY v1.0 Enterprise", fontSize = 11.sp, color = TextSecondaryLight)
                        Text("Mimari: Room Database Local Persistence + Gemini Multimodal AI", fontSize = 11.sp, color = TextSecondaryLight)
                        Text("Kayıtlı Demirbaş Sayısı: ${assets.size}", fontSize = 11.sp, color = TextSecondaryLight)
                        Text("Barkod & Karekod Standardı: ISO/IEC 18004 ZXing 2D Matrix", fontSize = 11.sp, color = TextSecondaryLight)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Role Switcher Dialog
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Kullanıcı Rolü Seçin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserRole.entries.forEach { role ->
                        Surface(
                            color = if (currentRole == role) TurquoiseDark.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (currentRole == role) TurquoiseDark else NeutralCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setRole(role)
                                    showRoleDialog = false
                                }
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(role.labelTr, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (currentRole == role) TurquoiseDark else Color.Black)
                                Text(role.description, fontSize = 11.sp, color = TextSecondaryLight)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRoleDialog = false }) { Text("Kapat") }
            }
        )
    }

    // Excel Import Dialog (Validating Başarılı, Uyarılı, Hatalı)
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Excel'den İçe Aktarım") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Excel şablonu incelendi ve doğrulandı:")
                    Surface(color = StatusAvailable.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        Text("✔ Başarılı Kayıt: 2 Adet (Hatasız ve eksiksiz)", color = StatusAvailable, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp))
                    }
                    Surface(color = WarningAmber.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        Text("⚠ Uyarılı Kayıt: 0 Adet", color = WarningAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp))
                    }
                    Surface(color = StatusFaulty.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        Text("✖ Hatalı Kayıt: 0 Adet", color = StatusFaulty, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(6.dp))
                    }
                    Text("İçe aktarmak istediğiniz 2 yeni demirbaş sisteme eklensin mi?", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importDemoBulkAssets()
                        showImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark)
                ) {
                    Text("Verileri İçe Aktar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Vazgeç") }
            }
        )
    }
}

@Composable
fun AlertRowItem(icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, title: String, desc: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(iconColor.copy(alpha = 0.08f))
            .padding(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = iconColor)
            Text(desc, fontSize = 11.sp, color = TextSecondaryLight)
        }
    }
}

@Composable
fun SettingMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TurquoiseDark.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TurquoiseDark, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(desc, fontSize = 11.sp, color = TextSecondaryLight)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMutedLight, modifier = Modifier.size(18.dp))
    }
    HorizontalDivider(color = NeutralCardBorder.copy(alpha = 0.5f))
}
