package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.util.ExportUtil
import com.example.util.QrCodeGenerator

@Composable
fun StatusBadge(status: AssetStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        AssetStatus.AVAILABLE -> StatusAvailable.copy(alpha = 0.15f) to StatusAvailable
        AssetStatus.ASSIGNED -> StatusAssigned.copy(alpha = 0.15f) to StatusAssigned
        AssetStatus.MAINTENANCE -> StatusMaintenance.copy(alpha = 0.15f) to StatusMaintenance
        AssetStatus.FAULTY -> StatusFaulty.copy(alpha = 0.15f) to StatusFaulty
        AssetStatus.LOST -> StatusLost.copy(alpha = 0.15f) to StatusLost
        AssetStatus.RETIRED -> StatusRetired.copy(alpha = 0.15f) to StatusRetired
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.labelTr,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ConditionBadge(condition: AssetCondition, modifier: Modifier = Modifier) {
    val color = when (condition) {
        AssetCondition.EXCELLENT -> Color(0xFF10B981)
        AssetCondition.GOOD -> Color(0xFF38BDF8)
        AssetCondition.FAIR -> Color(0xFFF59E0B)
        AssetCondition.POOR -> Color(0xFFEF4444)
        AssetCondition.DAMAGED -> Color(0xFFDC2626)
    }

    Text(
        text = condition.labelTr,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun QrCodeDialog(
    asset: Asset,
    onDismiss: () -> Unit,
    onPrintLabel: () -> Unit
) {
    val context = LocalContext.current
    val qrBitmap: Bitmap = remember(asset.assetCode) {
        QrCodeGenerator.generateQrBitmap("AKYOL:${asset.assetCode}", 512)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Demirbaş QR Kodu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = asset.assetCode,
                    style = CodeTextStyle,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = asset.assetName,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            ExportUtil.shareText(
                                context,
                                "${asset.assetCode} - Demirbaş Bilgisi",
                                "AKYOL INVENTORY\nDemirbaş Kodu: ${asset.assetCode}\nAdı: ${asset.assetName}\nBölüm: ${asset.department}\nDurum: ${asset.getStatusEnum().labelTr}"
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paylaş", fontSize = 13.sp, color = TextPrimary)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onPrintLabel()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Etiket", fontSize = 13.sp, color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PrintLabelDialog(asset: Asset, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val qrBitmap = remember(asset.assetCode) {
        QrCodeGenerator.generateQrBitmap("AKYOL:${asset.assetCode}", 300)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Demirbaş Barkod / Etiket Önizleme",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Realistic physical sticker simulation
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Label QR",
                            modifier = Modifier.size(72.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AKYOL INVENTORY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF101923),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = asset.assetCode,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = asset.assetName,
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "${asset.department} | ${asset.room}",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Kapat", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            ExportUtil.shareText(
                                context,
                                "Etiket Yazdırma: ${asset.assetCode}",
                                "AKYOL INVENTORY DEMİRBAŞ ETİKETİ\n-------------------------------\nKOD: ${asset.assetCode}\nAD: ${asset.assetName}\nDEPARTMAN: ${asset.department}\nKONUM: ${asset.room}\n-------------------------------"
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Yazdır", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ZimmetleDialog(
    asset: Asset,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onConfirm: (Employee, String, String) -> Unit
) {
    var selectedEmp by remember { mutableStateOf(employees.firstOrNull()) }
    var returnDate by remember { mutableStateOf("31.12.2026") }
    var notes by remember { mutableStateOf("Standart personel tahsisi yapıldı.") }
    var signatureConfirmed by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Demirbaş Zimmetle",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${asset.assetCode} - ${asset.assetName}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Zimmetlenecek Personel Seçin:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))

                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceDark)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedEmp?.let { "${it.fullName} (${it.department})" } ?: "Personel Seçin",
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(CardSurfaceElevated)
                    ) {
                        employees.forEach { emp ->
                            DropdownMenuItem(
                                text = { Text("${emp.fullName} - ${emp.department} (${emp.title})", color = TextPrimary) },
                                onClick = {
                                    selectedEmp = emp
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = returnDate,
                    onValueChange = { returnDate = it },
                    label = { Text("Tahmini İade Tarihi", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Zimmet Notu", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = signatureConfirmed,
                        onCheckedChange = { signatureConfirmed = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentTeal)
                    )
                    Text("Dijital Zimmet Tutanağı ve Personel Onayı Alındı", fontSize = 12.sp, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("İptal", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedEmp?.let { onConfirm(it, returnDate, notes) }
                        },
                        enabled = selectedEmp != null && signatureConfirmed,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusAssigned),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Zimmetle", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ZimmettenDusDialog(
    asset: Asset,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var returnNotes by remember { mutableStateOf("Eksiksiz ve çalışır durumda teslim alındı.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zimmetten Düş", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "${asset.assetCode} kodlu demirbaş ${asset.assignedUserName} üzerinden iade alınıp BOŞTA durumuna getirilecektir.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = returnNotes,
                    onValueChange = { returnNotes = it },
                    label = { Text("İade / Kontrol Notu", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(returnNotes) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusAvailable)
            ) {
                Text("Onayla ve Boşa Çıkar", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç", color = TextSecondary) }
        },
        containerColor = CardSurfaceDark,
        shape = RoundedCornerShape(16.dp)
    )
}
