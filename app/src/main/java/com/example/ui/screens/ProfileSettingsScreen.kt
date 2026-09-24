package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // App Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Profil ve Sistem Ayarları",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Kullanıcı rolü ve uygulama konfigürasyonu",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
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
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(CardSurfaceElevated)
                                    .border(1.dp, BorderLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = AccentTeal
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Aliyar Akyol",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "aliyar.akyol@gmail.com",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = SurfaceDark,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                                ) {
                                    Text(
                                        text = currentRole.labelTr,
                                        color = AccentTealLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = BorderDark)
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentRole.description,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showRoleDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kullanıcı Rolünü Değiştir (RBAC)", color = TextPrimary)
                        }
                    }
                }
            }

            // Fast Navigation Shortcuts
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Yönetim Modülleri", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

                        SettingsMenuRow(
                            title = "Personel Listesi ve Zimmetler",
                            subtitle = "Tüm çalışanları ve üzerlerindeki demirbaşları görüntüleyin",
                            icon = Icons.Default.People,
                            onClick = { viewModel.navigateTo(AppScreen.EMPLOYEES) }
                        )

                        SettingsMenuRow(
                            title = "Kurumsal Raporlar & Dışa Aktarım",
                            subtitle = "Excel (.xlsx) ve PDF formatlarında envanter raporları alın",
                            icon = Icons.Default.Assessment,
                            onClick = { viewModel.navigateTo(AppScreen.REPORTS) }
                        )

                        SettingsMenuRow(
                            title = "Yıllık Sayım ve Denetim Modu",
                            subtitle = "Depo ve oda bazlı hızlı QR sayımı gerçekleştirin",
                            icon = Icons.Default.FactCheck,
                            onClick = { viewModel.navigateTo(AppScreen.AUDIT) }
                        )
                    }
                }
            }

            // System Information Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Sistem Bilgileri", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Uygulama: AKYOL INVENTORY v1.0.1 Enterprise", fontSize = 12.sp, color = TextSecondary)
                        Text("Mimari: Room Database Local Persistence + Gemini Multimodal AI", fontSize = 12.sp, color = TextSecondary)
                        Text("Kayıtlı Demirbaş Sayısı: ${assets.size} adet", fontSize = 12.sp, color = TextSecondary)
                        Text("Rapor Motoru: Pure-Kotlin ECMA-376 OpenXML (.xlsx) + Android Native PDF", fontSize = 12.sp, color = TextSecondary)
                        Text("Barkod & Karekod Standardı: ISO/IEC 18004 ZXing 2D Matrix", fontSize = 12.sp, color = TextSecondary)
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
            title = { Text("Kullanıcı Rolü Seçin (RBAC)", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    UserRole.entries.forEach { role ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setRole(role)
                                    showRoleDialog = false
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = currentRole == role,
                                onClick = {
                                    viewModel.setRole(role)
                                    showRoleDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentTeal)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(role.labelTr, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(role.description, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Kapat", color = AccentTeal)
                }
            },
            containerColor = CardSurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SettingsMenuRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                Text(subtitle, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}
