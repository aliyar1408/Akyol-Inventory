package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Corporate Dark Palette (High Contrast, WCAG AAA Compliant)
// Background & Surfaces
val BackgroundDark = Color(0xFF101923)       // Ana koyu arka plan
val SurfaceDark = Color(0xFF182430)          // İkincil yüzey / Menüler
val CardSurfaceDark = Color(0xFF202D3A)      // Kart yüzeyi
val CardSurfaceElevated = Color(0xFF263645)  // Vurgulu kart / Modal yüzeyi
val BorderDark = Color(0xFF30404D)           // Çizgi ve kenarlıklar
val BorderLight = Color(0xFF3E5162)          // Aktif/Hover kenarlık

// Typography Colors
val TextPrimary = Color(0xFFF7F9FA)          // Ana yazı: Kırık beyaz, %100 okunabilir
val TextSecondary = Color(0xFFB8C2CC)        // İkincil açıklama: Açık gri
val TextMuted = Color(0xFF8A99A8)            // Üçüncül detay: Muted slate
val TextCode = Color(0xFF7DD3FC)             // Kod & Seri No vurgusu

// Brand Accent
val AccentTeal = Color(0xFF6F9FA1)           // Muted teal vurgu (kurumsal, sakin)
val AccentTealLight = Color(0xFF99C2C4)      // Açık teal
val AccentTealDark = Color(0xFF4C7577)       // Koyu teal
val AccentBlue = Color(0xFF38BDF8)           // Bilgi / Vurgu mavisi

// Subtle Status Colors (Kullanımı sakin, kartı boğmayan göstergeler)
val StatusAvailable = Color(0xFF10B981)      // Boşta / Yeşil
val StatusAssigned = Color(0xFF38BDF8)       // Zimmetli / Mavi
val StatusMaintenance = Color(0xFFF59E0B)    // Bakımda / Kehribar Sarı
val StatusFaulty = Color(0xFFEF4444)         // Arızalı / Mercan Kırmızı
val StatusLost = Color(0xFFDC2626)           // Kayıp / Koyu Kırmızı
val StatusRetired = Color(0xFF94A3B8)        // Hurda / Nötr Gri

// Legacy aliases for backward compatibility
val NavyDark = BackgroundDark
val NavySurface = SurfaceDark
val NavyLight = CardSurfaceDark
val TurquoisePrimary = AccentTeal
val TurquoiseLight = AccentTealLight
val TurquoiseDark = AccentTealDark
val BrightBlue = AccentBlue
val WarningAmber = StatusMaintenance
val CriticalCoral = StatusFaulty

val NeutralBgLight = BackgroundDark
val NeutralCardLight = CardSurfaceDark
val NeutralCardBorder = BorderDark
val TextPrimaryLight = TextPrimary
val TextSecondaryLight = TextSecondary
val TextMutedLight = TextMuted

val NeutralBgDark = BackgroundDark
val NeutralCardDark = CardSurfaceDark
val NeutralCardBorderDark = BorderDark
val TextPrimaryDark = TextPrimary
val TextSecondaryDark = TextSecondary
val TextMutedDark = TextMuted
