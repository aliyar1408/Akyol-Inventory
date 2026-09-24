package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Employee
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(viewModel: InventoryViewModel) {
    val employees by viewModel.allEmployees.collectAsState()
    val allAssets by viewModel.allAssets.collectAsState()
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }

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
                text = "Personel & Zimmet Dağılımı",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = NavyDark
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(employees) { emp ->
                val assignedAssets = allAssets.filter { it.assignedUserId == emp.employeeNumber }
                val isExpanded = selectedEmployee?.employeeNumber == emp.employeeNumber

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedEmployee = if (isExpanded) null else emp
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(TurquoiseDark.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${emp.name.firstOrNull() ?: 'P'}${emp.surname.firstOrNull() ?: 'E'}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TurquoiseDark
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = emp.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${emp.title} • ${emp.department}",
                                        fontSize = 12.sp,
                                        color = TextSecondaryLight
                                    )
                                }
                            }

                            Surface(
                                color = if (assignedAssets.isNotEmpty()) StatusAssigned.copy(alpha = 0.15f) else NeutralCardBorder.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${assignedAssets.size} Demirbaş",
                                    color = if (assignedAssets.isNotEmpty()) StatusAssigned else TextSecondaryLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = emp.email,
                                fontSize = 11.sp,
                                color = TextSecondaryLight
                            )
                            Text(
                                text = emp.phone,
                                fontSize = 11.sp,
                                color = TextSecondaryLight
                            )
                        }

                        // Expanded view: list of assets assigned to this employee
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = NeutralCardBorder)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Zimmetindeki Demirbaşlar:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = NavyDark
                            )

                            if (assignedAssets.isEmpty()) {
                                Text(
                                    text = "Bu personele atanmış aktif demirbaş bulunmuyor.",
                                    fontSize = 11.sp,
                                    color = TextSecondaryLight,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                assignedAssets.forEach { a ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeutralBgLight)
                                            .clickable { viewModel.navigateTo(AppScreen.ASSET_DETAIL, a.assetCode) }
                                            .padding(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(a.assetCode, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TurquoiseDark)
                                            Text(a.assetName, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                        }
                                        Text(a.room, fontSize = 10.sp, color = TextSecondaryLight)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMutedLight, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
