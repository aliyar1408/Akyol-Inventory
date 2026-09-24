package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetAddEditScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isAnalyzing by viewModel.isAnalyzingAi.collectAsState()
    val aiSuggestion by viewModel.aiSuggestion.collectAsState()
    val capturedBitmap by viewModel.capturedBitmap.collectAsState()

    // Form fields
    var assetCode by remember { mutableStateOf("") }
    var assetName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Dizüstü Bilgisayar") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Bilgi İşlem") }
    var building by remember { mutableStateOf("A Blok") }
    var floor by remember { mutableStateOf("Kat 3") }
    var room by remember { mutableStateOf("No: 304") }
    var purchasePriceStr by remember { mutableStateOf("35000") }
    var purchaseDate by remember { mutableStateOf("2024-06-01") }
    var supplier by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf(AssetCondition.GOOD.name) }
    var status by remember { mutableStateOf(AssetStatus.AVAILABLE.name) }

    // Auto generate code on launch
    LaunchedEffect(category) {
        if (assetCode.isBlank()) {
            assetCode = viewModel.generateNextAssetCode(category)
        }
    }

    // Populate from AI if suggestion arrives
    LaunchedEffect(aiSuggestion) {
        aiSuggestion?.let { ai ->
            if (!ai.assetName.isNullOrBlank()) assetName = ai.assetName
            if (!ai.category.isNullOrBlank()) category = ai.category
            if (!ai.brand.isNullOrBlank()) brand = ai.brand
            if (!ai.model.isNullOrBlank()) model = ai.model
            if (!ai.visibleSerialNumber.isNullOrBlank()) serialNumber = ai.visibleSerialNumber
            if (!ai.suggestedDescription.isNullOrBlank()) description = ai.suggestedDescription
            assetCode = viewModel.generateNextAssetCode(ai.category)
        }
    }

    // Photo pickers
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.analyzeAssetPhoto(bitmap)
        }
    }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        viewModel.analyzeAssetPhoto(bmp)
                    }
                }
            } catch (e: Exception) {
                viewModel.showMessage("Fotoğraf açılamadı: ${e.message}")
            }
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
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo(AppScreen.ASSET_LIST) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
            Text(
                text = "Yeni Demirbaş Ekle",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isAnalyzing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TurquoisePrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Gemini Yapay Zeka Demirbaşı İnceliyor...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nesne tipi, marka, model ve seri no taranıyor",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                }
            }
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Mobile-First Action Methods
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TurquoiseLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Yapay Zeka Destekli Otomatik Tanıma",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "Demirbaşın fotoğrafını çekin veya yükleyin. Gemini nesneyi, markayı ve teknik özellikleri otomatik çıkarsın.",
                            fontSize = 12.sp,
                            color = NeutralCardBorder,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { takePhotoLauncher.launch(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = TurquoisePrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fotoğraf Çek", color = NavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { pickPhotoLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Galeriden Seç", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // If AI Analyzed image is present, show preview badge
            if (aiSuggestion != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = TurquoiseLight.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TurquoisePrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            capturedBitmap?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Analiz Edilen Fotoğraf",
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Yapay Zeka Önerisi Yüklendi",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TurquoiseDark
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = TurquoiseDark,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "%${((aiSuggestion?.confidenceScore ?: 0.9f) * 100).toInt()} Güven",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Bilgiler form alanlarına dolduruldu. Lütfen kontrol edip eksikleri tamamlayın.",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight
                                )
                            }
                        }
                    }
                }
            }

            // Form Inputs Card
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
                            text = "Temel Demirbaş Bilgileri",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyDark
                        )

                        OutlinedTextField(
                            value = assetCode,
                            onValueChange = { assetCode = it },
                            label = { Text("Demirbaş Kodu (Benzersiz)") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        assetCode = viewModel.generateNextAssetCode(category)
                                    }
                                }) {
                                    Icon(Icons.Default.Autorenew, contentDescription = "Yeniden Kod Üret")
                                }
                            }
                        )

                        OutlinedTextField(
                            value = assetName,
                            onValueChange = { assetName = it },
                            label = { Text("Demirbaş Adı *") },
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
                                label = { Text("Kategori") },
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

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = brand,
                                onValueChange = { brand = it },
                                label = { Text("Marka") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = model,
                                onValueChange = { model = it },
                                label = { Text("Model") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = serialNumber,
                                onValueChange = { serialNumber = it },
                                label = { Text("Seri Numarası") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = barcode,
                                onValueChange = { barcode = it },
                                label = { Text("Barkod") },
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
                                label = { Text("Bölüm / Departman") },
                                trailingIcon = {
                                    IconButton(onClick = { deptExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = deptExpanded, onDismissRequest = { deptExpanded = false }) {
                                deptList.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d) },
                                        onClick = {
                                            department = d
                                            deptExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Location
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = building,
                                onValueChange = { building = it },
                                label = { Text("Bina") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = floor,
                                onValueChange = { floor = it },
                                label = { Text("Kat") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = room,
                                onValueChange = { room = it },
                                label = { Text("Oda") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = purchasePriceStr,
                                onValueChange = { purchasePriceStr = it },
                                label = { Text("Fiyat (TL)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = purchaseDate,
                                onValueChange = { purchaseDate = it },
                                label = { Text("Satın Alma Tarihi") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Açıklama / Notlar") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }

            // Save Confirmation Button
            item {
                Button(
                    onClick = {
                        if (assetName.isBlank()) {
                            viewModel.showMessage("Lütfen demirbaş adını giriniz.")
                            return@Button
                        }
                        val price = purchasePriceStr.toDoubleOrNull() ?: 0.0
                        val newAsset = Asset(
                            assetCode = assetCode.ifBlank { "AKY-2026-999999" },
                            assetName = assetName,
                            category = category,
                            brand = brand,
                            model = model,
                            serialNumber = serialNumber,
                            barcode = barcode,
                            department = department,
                            building = building,
                            floor = floor,
                            room = room,
                            purchasePrice = price,
                            purchaseDate = purchaseDate,
                            supplier = supplier,
                            description = description,
                            status = status,
                            condition = condition
                        )
                        viewModel.saveAsset(newAsset, isNew = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TurquoiseDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bilgileri Onayla ve Kaydet", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
