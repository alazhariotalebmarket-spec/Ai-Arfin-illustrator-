package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DesignTemplate
import com.example.model.TemplateCatalog
import com.example.ui.theme.OrangePrimary

@Composable
fun TemplatesScreen(
    onOpenTemplate: (DesignTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember {
        listOf("All", "মাদ্রাসা", "ইসলামিক", "ব্যবসা", "উৎসব", "খেলা")
    }
    var selectedCat by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }

    val list = remember(selectedCat, search) {
        val q = search.trim().lowercase()
        TemplateCatalog.templates.filter { tpl ->
            val catOk = selectedCat == "All" || tpl.category == selectedCat
            val qOk = q.isEmpty() || tpl.titleBn.lowercase().contains(q) || tpl.title.lowercase().contains(q)
            catOk && qOk
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text("রেডি টেমপ্লেট • Ready Templates", style = MaterialTheme.typography.headlineMedium)
        Text(
            "যে কোনো টেমপ্লেটে ট্যাপ করে লেখা, ছবি ও কালার পরিবর্তন করুন",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (search.isNotEmpty()) {
                    IconButton(onClick = { search = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            placeholder = { Text("বাংলা নামে টেমপ্লেট খুঁজুন...", fontSize = 13.sp) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCat == cat,
                    onClick = { selectedCat = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(list, key = { it.id }) { tpl ->
                TemplatePreviewCard(
                    template = tpl,
                    onClick = { onOpenTemplate(tpl) }
                )
            }
        }
    }
}
