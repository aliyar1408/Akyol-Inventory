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
            .background(BackgroundDark)
    ) {
        // App Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        viewModel.clearAiSuggestion()
                        viewModel.navigateTo(AppScreen.ASSET_LIST)
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Vazgeç", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Yapay Zeka Önerisi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Gemini İnceleme ve Onay Ekranı",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                color = CardSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Güven: %$confidencePercent",
                        color = AccentTealLight,
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentTeal),
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
                                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Gemini Analizi Tamamlandı",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AccentTealLight
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Nesne: ${aiSuggestion?.objectType ?: "Elektronik Cihaz"}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                if (!aiSuggestion?.conditionDescription.isNullOrBlank()) {
                                    Text(
                                        text = "Durum: ${aiSuggestion?.conditionDescription}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Security & Approval Notice
                        Surface(
                            color = SurfaceDark,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = StatusMaintenance, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Yapay zeka sonuçları otomatik kaydedilmez. Lütfen bilgileri kontrol edip düzenleyin ve alttaki butondan onaylayın.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
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
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Düzenlenebilir Demirbaş Alanları",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )

                        // Asset Code (Generated systematically, not from Gemini)
                        Column {
                            Text("Demirbaş Kodu (Sistem Üretimi)", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = assetCode,
                                onValueChange = { assetCode = it },
                                textStyle = CodeTextStyle,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = outlinedColors(),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            assetCode = viewModel.generateNextAssetCode(category)
                                        }
                                    }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Kodu Yenile", tint = AccentTeal)
                                    }
                                }
                            )
                        }

                        // Asset Name
                        Column {
                            Text("Demirbaş Adı *", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = assetName,
                                onValueChange = { assetName = it },
                                colors = outlinedColors(),
                                trailingIcon = {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Alanı", tint = AccentTeal, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Category Selector
                        val categoryList = listOf(
                            "Dizüstü Bilgisayar", "Masaüstü Bilgisayar", "Monitör",
                            "Yazıcı", "Telefon", "Tablet", "Mobilya", "Klima", "Televizyon", "Ağ Ekipmanı", "Diğer"
                        )
                        var catExpanded by remember { mutableStateOf(false) }
                        Column {
                            Text("Kategori (AI Önerisi)", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    colors = outlinedColors(),
                                    trailingIcon = {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                    },
                                    modifier = Modifier.fillMaxWidth().clickable { catExpanded = true }
                                )
                                DropdownMenu(
                                    expanded = catExpanded,
                                    onDismissRequest = { catExpanded = false },
                                    modifier = Modifier.background(CardSurfaceElevated)
                                ) {
                                    categoryList.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c, color = TextPrimary) },
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
                        }

                        // Brand & Model
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Marka", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = brand,
                                    onValueChange = { brand = it },
                                    colors = outlinedColors(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Model", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = model,
                                    onValueChange = { model = it },
                                    colors = outlinedColors(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Serial Number & Color
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Seri Numarası", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = serialNumber,
                                    onValueChange = { serialNumber = it },
                                    textStyle = SmallCodeTextStyle,
                                    colors = outlinedColors(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Renk / Görünüm", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = colorFeature,
                                    onValueChange = { colorFeature = it },
                                    colors = outlinedColors(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Department
                        val deptList = listOf("Bilgi İşlem", "Muhasebe", "İnsan Kaynakları", "Satın Alma", "İdari İşler")
                        var deptExpanded by remember { mutableStateOf(false) }
                        Column {
                            Text("Departman", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                OutlinedTextField(
                                    value = department,
                                    onValueChange = {},
                                    readOnly = true,
                                    colors = outlinedColors(),
                                    trailingIcon = {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                    },
                                    modifier = Modifier.fillMaxWidth().clickable { deptExpanded = true }
                                )
                                DropdownMenu(
                                    expanded = deptExpanded,
                                    onDismissRequest = { deptExpanded = false },
                                    modifier = Modifier.background(CardSurfaceElevated)
                                ) {
                                    deptList.forEach { d ->
                                        DropdownMenuItem(
                                            text = { Text(d, color = TextPrimary) },
                                            onClick = {
                                                department = d
                                                deptExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Location: Building, Floor, Room
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bina", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(value = building, onValueChange = { building = it }, colors = outlinedColors(), modifier = Modifier.fillMaxWidth())
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Kat", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(value = floor, onValueChange = { floor = it }, colors = outlinedColors(), modifier = Modifier.fillMaxWidth())
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Oda", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(value = room, onValueChange = { room = it }, colors = outlinedColors(), modifier = Modifier.fillMaxWidth())
                            }
                        }

                        // Price & Date
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Satın Alma Bedeli (TL)", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(value = purchasePriceStr, onValueChange = { purchasePriceStr = it }, colors = outlinedColors(), modifier = Modifier.fillMaxWidth())
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Satın Alma Tarihi", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(value = purchaseDate, onValueChange = { purchaseDate = it }, colors = outlinedColors(), modifier = Modifier.fillMaxWidth())
                            }
                        }

                        // Description
                        Column {
                            Text("Açıklama (AI Tarafından Oluşturuldu)", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                colors = outlinedColors(),
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }
                    }
                }
            }

            // Bottom Confirmation Actions
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { reTakePhotoLauncher.launch(null) },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Yeniden Çek", fontSize = 13.sp, color = TextPrimary)
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
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BackgroundDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bilgileri Onayla ve Kaydet", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentTeal,
    unfocusedBorderColor = BorderDark,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = SurfaceDark,
    unfocusedContainerColor = SurfaceDark
)
