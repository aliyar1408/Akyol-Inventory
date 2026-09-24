package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Asset
import com.example.data.model.AssetCondition
import com.example.data.model.AssetStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConfirmationScreen(viewModel: InventoryViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val aiSuggestion by viewModel.aiSuggestion.collectAsState()
    val capturedBitmap by viewModel.capturedBitmap.collectAsState()

    val confidencePercent = remember(aiSuggestion) {
        ((aiSuggestion?.confidenceScore ?: 0.94f) * 100).toInt()
    }

    // Editable state fields initialized from AI suggestions
    var assetCode by remember { mutableStateOf("") }
    var assetName by remember { mutableStateOf(aiSuggestion?.assetName ?: aiSuggestion?.objectType ?: "") }
    var category by remember { mutableStateOf(aiSuggestion?.category ?: "Dizüstü Bilgisayar") }
    var brand by remember { mutableStateOf(aiSuggestion?.brand ?: "") }
    var model by remember { mutableStateOf(aiSuggestion?.model ?: "") }
    var serialNumber by remember { mutableStateOf(aiSuggestion?.visibleSerialNumber ?: "") }
    var colorFeature by remember { mutableStateOf(aiSuggestion?.color ?: "") }
    var department by remember { mutableStateOf("Bilgi İşlem") }
    var building by remember { mutableStateOf("A Blok") }
    var floor by remember { mutableStateOf("Kat 3") }
    var room by remember { mutableStateOf("No: 304") }
    var purchasePriceStr by remember { mutableStateOf("32500") }
    var purchaseDate by remember { mutableStateOf("2024-06-15") }
    var description by remember { mutableStateOf(aiSuggestion?.suggestedDescription ?: "") }
    var condition by remember { mutableStateOf(AssetCondition.GOOD.name) }
    var status by remember { mutableStateOf(AssetStatus.AVAILABLE.name) }

    // Automatically generate unique asset code based on category
    LaunchedEffect(category) {
        if (assetCode.isBlank()) {
            assetCode = viewModel.generateNextAssetCode(category)
        }
    }

    // Re-take photo launcher
    val reTakePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            viewModel.analyzeAssetPhoto(bmp)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    viewModel.clearAiSuggestion()
                    viewModel.navigateTo(AppScreen.ASSET_LIST)
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Vazgeç")
                }
                Column {
                    Text(
                        text = "Yapay Zeka Önerisi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NavyDark
                    )
                    Text(
                        text = "Gemini İnceleme & Onay Ekranı",
                        fontSize = 11.sp,
                        color = TextSecondaryLight
                    )
                }
            }

            Surface(
                color = TurquoisePrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TurquoiseDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Güven: %$confidencePercent",
                        color = TurquoiseDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Photo & AI Insight Callout Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TurquoisePrimary.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            capturedBitmap?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Fotoğraf",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, NeutralCardBorder, RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = TurquoiseDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Gemini Analizi Tamamlandı",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TurquoiseDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Nesne: ${aiSuggestion?.objectType ?: "Elektronik Cihaz"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!aiSuggestion?.conditionDescription.isNullOrBlank()) {
                                    Text(
                                        text = "Durum: ${aiSuggestion?.conditionDescription}",
                                        fontSize = 11.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Security & Approval Notice
                        Surface(
                            color = WarningAmber.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Yapay zeka sonuçları otomatik kaydedilmez. Lütfen bilgileri kontrol edip düzenleyin ve alttaki butondan onaylayın.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Editable Asset Information Form
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Düzenlenebilir Demirbaş Alanları",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyDark
                        )

                        // Asset Code (Generated systematically, not from Gemini)
                        OutlinedTextField(
                            value = assetCode,
                            onValueChange = { assetCode = it },
                            label = { Text("Demirbaş Kodu (Sistem Tarafından Üretildi)") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        assetCode = viewModel.generateNextAssetCode(category)
                                    }
                                }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Kodu Yenile")
                                }
                            }
                        )

                        // Asset Name
                        OutlinedTextField(
                            value = assetName,
                            onValueChange = { assetName = it },
                            label = { Text("Demirbaş Adı *") },
                            trailingIcon = {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Alanı", tint = TurquoiseDark, modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Category Selector
                        val categoryList = listOf(
                            "Dizüstü Bilgisayar", "Masaüstü Bilgisayar", "Monitör",
                            "Yazıcı", "Telefon", "Tablet", "Mobilya", "Klima", "Televizyon", "Ağ Ekipmanı", "Diğer"
                        )
                        var catExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kategori (AI Önerisi)") },
                                trailingIcon = {
                                    IconButton(onClick = { catExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { catExpanded = true }
                            )
                            DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                                categoryList.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c) },
                                        onClick = {
                                            category = c
                                            catExpanded = false
                                            coroutineScope.launch {
                                                assetCode = viewModel.generateNextAssetCode(c)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Brand & Model
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = brand,
                                onValueChange = { brand = it },
                                label = { Text("Marka") },
                                trailingIcon = {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TurquoiseDark, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = model,
                                onValueChange = { model = it },
                                label = { Text("Model") },
                                trailingIcon = {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TurquoiseDark, modifier = Modifier.size(16.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Serial Number & Color
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = serialNumber,
                                onValueChange = { serialNumber = it },
                                label = { Text("Seri Numarası") },
                                placeholder = { Text(aiSuggestion?.visibleSerialNumber ?: "Tespit Edilemedi") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = colorFeature,
                                onValueChange = { colorFeature = it },
                                label = { Text("Renk / Dış Görünüm") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Department
                        val deptList = listOf("Bilgi İşlem", "Muhasebe", "İnsan Kaynakları", "Satın Alma", "İdari İşler")
                        var deptExpanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedTextField(
                                value = department,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Departman") },
                                trailingIcon = {
                                    IconButton(onClick = { deptExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = deptExpanded, onDismissRequest = { deptExpanded = false }) {
                                deptList.forEach { d ->
                                    DropdownMenuItem(text = { Text(d) }, onClick = {
                                        department = d
                                        deptExpanded = false
                                    })
                                }
                            }
                        }

                        // Location
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(value = building, onValueChange = { building = it }, label = { Text("Bina") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = floor, onValueChange = { floor = it }, label = { Text("Kat") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Oda") }, modifier = Modifier.weight(1f))
                        }

                        // Price & Date
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(value = purchasePriceStr, onValueChange = { purchasePriceStr = it }, label = { Text("Satın Alma (TL)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = purchaseDate, onValueChange = { purchaseDate = it }, label = { Text("Tarih") }, modifier = Modifier.weight(1f))
                        }

                        // AI Suggested Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Demirbaş Açıklaması (AI Önerisi)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { reTakePhotoLauncher.launch(null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Yeniden Çek", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (assetName.isBlank()) {
                                viewModel.showMessage("Lütfen demirbaş adını kontrol ediniz.")
                                return@Button
                            }
                            val price = purchasePriceStr.toDoubleOrNull() ?: 0.0
                            val assetToSave = Asset(
                                assetCode = assetCode.ifBlank { "AKY-2026-999999" },
                                assetName = assetName,
                                category = category,
                                brand = brand,
                                model = model,
                                serialNumber = serialNumber,
                                barcode = "869" + (System.currentTimeMillis() % 1000000000L).toString(),
                                department = department,
                                building = building,
                                floor = floor,
                                room = room,
                                purchasePrice = price,
                                purchaseDate = purchaseDate,
                                description = description,
                                status = status,
                                condition = condition
                            )
                            viewModel.saveAsset(assetToSave, isNew = true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bilgileri Onayla ve Kaydet", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
