package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.core.content.ContextCompat
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch
import java.io.InputStream

private const val TAG = "AssetAddEditScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetAddEditScreen(viewModel: InventoryViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isAnalyzing by viewModel.isAnalyzingAi.collectAsState()
    val aiSuggestion by viewModel.aiSuggestion.collectAsState()

    // Staged Photo & Preview State (User reviews photo before AI analysis)
    var stagedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCameraErrorDialog by remember { mutableStateOf(false) }
    var cameraErrorMessage by remember { mutableStateOf("") }

    // Form fields
    var assetCode by remember { mutableStateOf("") }
    var assetName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Dizüstü Bilgisayar") }
    var subcategory by remember { mutableStateOf("") }
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
    var invoiceNumber by remember { mutableStateOf("") }
    var warrantyEndDate by remember { mutableStateOf("2027-06-01") }
    var description by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf(AssetCondition.GOOD.name) }
    var status by remember { mutableStateOf(AssetStatus.AVAILABLE.name) }

    // Validation state
    var assetNameError by remember { mutableStateOf<String?>(null) }

    // Auto generate code on category change
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
            coroutineScope.launch {
                assetCode = viewModel.generateNextAssetCode(ai.category)
            }
        }
    }

    // Camera photo capture launcher with safe error handling
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            // Downsample gracefully to avoid memory spikes
            stagedBitmap = scaleBitmapDown(bitmap, 1024)
        }
    }

    // Camera runtime permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                takePhotoLauncher.launch(null)
            } catch (e: Exception) {
                Log.e(TAG, "Kamera başlatılamadı", e)
                cameraErrorMessage = "Kamera başlatılamadı: ${e.localizedMessage ?: "Bilinmeyen hata"}"
                showCameraErrorDialog = true
            }
        } else {
            cameraErrorMessage = "Kamera izni verilmedi. Kamera iznini kontrol edin veya galeriden fotoğraf seçin."
            showCameraErrorDialog = true
        }
    }

    fun startCameraCapture() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                takePhotoLauncher.launch(null)
            } catch (e: Exception) {
                Log.e(TAG, "Kamera başlatma hatası", e)
                cameraErrorMessage = "Kamera başlatılamadı. Cihazınızda kamera uygulaması bulunamayabilir."
                showCameraErrorDialog = true
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gallery photo picker launcher with safe stream decoding and compression
    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val sampled = decodeSampledBitmapFromStream(stream, 1024, 1024)
                    if (sampled != null) {
                        stagedBitmap = sampled
                    } else {
                        viewModel.showMessage("Görsel çözümlenemedi. Lütfen başka bir dosya seçin.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Galeri görsel okuma hatası", e)
                viewModel.showMessage("Galeriden fotoğraf seçilemedi: ${e.localizedMessage}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // App Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.ASSET_LIST) },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Yeni Demirbaş Ekle",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Envantere yeni demirbaş kaydı oluşturun",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Analyzing State Banner
        if (isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(color = AccentTeal)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Gemini Yapay Zeka Demirbaşı İnceliyor...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Görseldeki nesne türü, marka, model ve seri no çıkarılıyor.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // STEP 1: Photo Capture & Gallery Selection Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Demirbaş Fotoğrafı & AI Analizi",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Fotoğraf çekin veya galeriden seçin. Çekilen fotoğrafı önizleyip onayladıktan sonra Gemini AI ile otomatik olarak form alanlarını doldurabilirsiniz.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Photo Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { startCameraCapture() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fotoğraf Çek", color = BackgroundDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { pickPhotoLauncher.launch("image/*") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Galeriden Seç", color = TextPrimary, fontSize = 13.sp)
                            }
                        }

                        // Photo Preview & Review Before AI Call (Requirement 7)
                        stagedBitmap?.let { bmp ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Çekilen Fotoğraf Önizleme",
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Fotoğraf Hazır",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "Fotoğrafı inceleyin. İsterseniz tekrar çekebilir veya AI analizini başlatabilirsiniz.",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action buttons for staged photo
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedButton(
                                            onClick = { startCameraCapture() },
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Tekrar Çek", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.analyzeAssetPhoto(bmp)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                            modifier = Modifier.weight(1.5f).height(40.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Bu Fotoğrafı Kullan", color = BackgroundDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // STEP 2: Form Inputs Card
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
                            text = "Temel Demirbaş Bilgileri",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )

                        // Demirbaş Kodu Input
                        Column {
                            FormLabel(title = "Demirbaş Kodu", required = true)
                            OutlinedTextField(
                                value = assetCode,
                                onValueChange = { assetCode = it },
                                placeholder = { Text("Örn: AKY-PC-000124", color = TextMuted) },
                                textStyle = CodeTextStyle,
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            assetCode = viewModel.generateNextAssetCode(category)
                                        }
                                    }) {
                                        Icon(Icons.Default.Autorenew, contentDescription = "Yeni Kod Üret", tint = AccentTeal)
                                    }
                                },
                                colors = outlinedColors(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Demirbaş Adı Input
                        Column {
                            FormLabel(title = "Demirbaş Adı", required = true)
                            OutlinedTextField(
                                value = assetName,
                                onValueChange = {
                                    assetName = it
                                    if (it.isNotBlank()) assetNameError = null
                                },
                                placeholder = { Text("Örn: Dell Latitude 5540 Laptop", color = TextMuted) },
                                isError = assetNameError != null,
                                singleLine = true,
                                colors = outlinedColors(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            assetNameError?.let { err ->
                                Text(err, color = StatusFaulty, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
                            }
                        }

                        // Kategori Selector
                        val categoryList = listOf(
                            "Dizüstü Bilgisayar", "Masaüstü Bilgisayar", "Monitör",
                            "Yazıcı", "Telefon", "Tablet", "Mobilya", "Klima", "Televizyon", "Ağ Ekipmanı", "Diğer"
                        )
                        var catExpanded by remember { mutableStateOf(false) }
                        Column {
                            FormLabel(title = "Kategori", required = true)
                            Box {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                    },
                                    colors = outlinedColors(),
                                    shape = RoundedCornerShape(8.dp),
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

                        // Marka & Model
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(title = "Marka", required = false)
                                OutlinedTextField(
                                    value = brand,
                                    onValueChange = { brand = it },
                                    placeholder = { Text("Dell, HP, Apple...", color = TextMuted) },
                                    singleLine = true,
                                    colors = outlinedColors(),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(title = "Model", required = false)
                                OutlinedTextField(
                                    value = model,
                                    onValueChange = { model = it },
                                    placeholder = { Text("Latitude 5540...", color = TextMuted) },
                                    singleLine = true,
                                    colors = outlinedColors(),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Seri Numarası & Barkod
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(title = "Seri Numarası", required = false)
                                OutlinedTextField(
                                    value = serialNumber,
                                    onValueChange = { serialNumber = it },
                                    placeholder = { Text("SN-982341", color = TextMuted) },
                                    textStyle = SmallCodeTextStyle,
                                    singleLine = true,
                                    colors = outlinedColors(),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(title = "Barkod", required = false)
                                OutlinedTextField(
                                    value = barcode,
                                    onValueChange = { barcode = it },
                                    placeholder = { Text("869000123", color = TextMuted) },
                                    textStyle = SmallCodeTextStyle,
                                    singleLine = true,
                                    colors = outlinedColors(),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Departman Selector
                        val deptList = listOf("Bilgi İşlem", "Muhasebe", "İnsan Kaynakları", "Satın Alma", "İdari İşler", "Satış & Pazarlama")
                        var deptExpanded by remember { mutableStateOf(false) }
                        Column {
                            FormLabel(title = "Departman / Bölüm", required = true)
                            Box {
                                OutlinedTextField(
                                    value = department,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                    },
                                    colors = outlinedColors(),
                                    shape = RoundedCornerShape(8.dp),
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

                        // Konum (Bina, Kat, Oda)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(title = "Bina", required = false)
                                OutlinedTextField(
                                    value = building,
                                    onValueChange = { building = it },
                                    placeholder = { Text("A Blok", color = TextMuted) },
                                    singleLine = true,
                                    colors = outlinedColors(),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(title = "Kat", required = false)
                                OutlinedTextField(
                                    value = floor,
                                    onValueChange = { floor = it },
                                    placeholder = { Text("Kat 3", color = TextMuted) },
                                    singleLine = true,
                                    colors = outlinedColors(),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(title = "Oda", required = false)
                                OutlinedTextField(
                                    value = room,
                                    onValueChange = { room = it },
                                    placeholder = { Text("304", color = TextMuted) },
                                    singleLine = true,
                                    colors = outlinedColors(),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Satın Alma Bedeli & Tarihi
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(title = "Satın Alma Bedeli (TL)", required = false)
                                OutlinedTextField(
                                    value = purchasePriceStr,
                                    onValueChange = { purchasePriceStr = it },
                                    placeholder = { Text("35000", color = TextMuted) },
                                    singleLine = true,
                                    colors = outlinedColors(),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel(title = "Satın Alma Tarihi", required = false)
                                OutlinedTextField(
                                    value = purchaseDate,
                                    onValueChange = { purchaseDate = it },
                                    placeholder = { Text("YYYY-AA-GG", color = TextMuted) },
                                    singleLine = true,
                                    colors = outlinedColors(),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Açıklama
                        Column {
                            FormLabel(title = "Açıklama & Notlar", required = false)
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = { Text("Demirbaş hakkında ek teknik veya idari notlar...", color = TextMuted) },
                                colors = outlinedColors(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }
                    }
                }
            }

            // Save Confirmation Button
            item {
                Button(
                    onClick = {
                        if (assetName.isBlank()) {
                            assetNameError = "Demirbaş adı zorunludur."
                            viewModel.showMessage("Lütfen demirbaş adını giriniz.")
                            return@Button
                        }
                        val price = purchasePriceStr.toDoubleOrNull() ?: 0.0
                        val newAsset = Asset(
                            assetCode = assetCode.ifBlank { "AKY-2026-999999" },
                            assetName = assetName,
                            category = category,
                            subcategory = subcategory,
                            brand = brand,
                            model = model,
                            serialNumber = serialNumber,
                            barcode = barcode.ifBlank { "869" + (System.currentTimeMillis() % 1000000000L).toString() },
                            department = department,
                            building = building,
                            floor = floor,
                            room = room,
                            purchasePrice = price,
                            purchaseDate = purchaseDate,
                            supplier = supplier,
                            invoiceNumber = invoiceNumber,
                            warrantyEndDate = warrantyEndDate,
                            description = description,
                            status = status,
                            condition = condition
                        )
                        viewModel.saveAsset(newAsset, isNew = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = BackgroundDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bilgileri Onayla ve Kaydet", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Camera Failure Fallback Dialog (Requirement 7)
    if (showCameraErrorDialog) {
        AlertDialog(
            onDismissRequest = { showCameraErrorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusMaintenance)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kamera Uyarısı", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = cameraErrorMessage.ifBlank { "Kamera açılamadı. Kamera iznini kontrol edin veya galeriden fotoğraf seçin." },
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCameraErrorDialog = false
                        pickPhotoLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Galeriden Seç", color = BackgroundDark)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showCameraErrorDialog = false
                        startCameraCapture()
                    }) {
                        Text("Tekrar Dene", color = AccentBlue)
                    }
                    TextButton(onClick = { showCameraErrorDialog = false }) {
                        Text("İptal", color = TextMuted)
                    }
                }
            },
            containerColor = CardSurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun FormLabel(title: String, required: Boolean) {
    Row(modifier = Modifier.padding(bottom = 5.dp)) {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        if (required) {
            Text(text = " *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusFaulty)
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

// Helper: Safely downsample bitmap
private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val originalWidth = bitmap.width
    val originalHeight = bitmap.height
    var resizedWidth = maxDimension
    var resizedHeight = maxDimension

    if (originalHeight > originalWidth) {
        resizedHeight = maxDimension
        resizedWidth = (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
    } else if (originalWidth > originalHeight) {
        resizedWidth = maxDimension
        resizedHeight = (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
    } else {
        resizedHeight = maxDimension
        resizedWidth = maxDimension
    }
    return if (originalWidth > maxDimension || originalHeight > maxDimension) {
        Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
    } else {
        bitmap
    }
}

// Helper: Safe stream decode to prevent OutOfMemory with high-res photos
private fun decodeSampledBitmapFromStream(inputStream: InputStream, reqWidth: Int, reqHeight: Int): Bitmap? {
    return try {
        val bytes = inputStream.readBytes()
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        var inSampleSize = 1
        val height = options.outHeight
        val width = options.outWidth
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    } catch (e: Exception) {
        Log.e(TAG, "Stream decode hatası", e)
        null
    }
}
