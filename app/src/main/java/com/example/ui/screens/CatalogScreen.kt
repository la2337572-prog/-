package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryEntity
import com.example.data.ProductEntity
import com.example.ui.theme.BrandOrange
import com.example.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<ProductEntity>,
    categories: List<CategoryEntity>,
    searchQuery: String,
    selectedCategoryId: String?,
    selectedBrand: String?,
    selectedBikeType: String?,
    sortOption: SortOption,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onSelectBrand: (String?) -> Unit,
    onSelectBikeType: (String?) -> Unit,
    onSelectSort: (SortOption) -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onAddToCart: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    val brands = remember(products) {
        listOf("الكل") + products.map { it.brand }.distinct().sorted()
    }

    val bikeTypes = listOf("الكل", "سبورت", "كروزر", "سكوتر", "أوف رود")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search TextField
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search_input"),
                    placeholder = { Text("ابحث عن قطعة، خوذة، ماركة، أو دراجة...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = BrandOrange
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { onSelectCategory(null) },
                            label = { Text("جميع الأقسام", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = {
                                if (selectedCategoryId == category.id) onSelectCategory(null)
                                else onSelectCategory(category.id)
                            },
                            label = { Text(category.name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Brand and Sort Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Brands Row
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(brands) { brand ->
                            val isSelected = (brand == "الكل" && selectedBrand == null) || (selectedBrand == brand)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) BrandOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    if (brand == "الكل") onSelectBrand(null) else onSelectBrand(brand)
                                }
                            ) {
                                Text(
                                    text = brand,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) BrandOrange else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Sort button
                    Box {
                        FilledTonalButton(
                            onClick = { showSortMenu = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "ترتيب",
                                modifier = Modifier.size(16.dp),
                                tint = BrandOrange
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = sortOption.titleAr, fontSize = 11.sp, color = BrandOrange)
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.titleAr) },
                                    onClick = {
                                        onSelectSort(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Product Count & Reset filters hint
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "النتائج (${products.size} منتج)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            if (selectedCategoryId != null || selectedBrand != null || searchQuery.isNotEmpty()) {
                TextButton(
                    onClick = {
                        onSearchChange("")
                        onSelectCategory(null)
                        onSelectBrand(null)
                        onSelectBikeType(null)
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "إعادة ضبط التصفية",
                        fontSize = 11.sp,
                        color = BrandOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Products List / Empty State
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لا توجد منتجات مطابقة لبحثك",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "جرّب تغيير كلمات البحث أو إعادة تعيين الفلاتر",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onSearchChange("")
                            onSelectCategory(null)
                            onSelectBrand(null)
                            onSelectBikeType(null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Text("عرض جميع المنتجات")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                val pairs = products.chunked(2)
                items(pairs) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProductCard(
                            product = pair[0],
                            onClick = { onProductClick(pair[0]) },
                            onAddToCart = { onAddToCart(pair[0].id) },
                            modifier = Modifier.weight(1f)
                        )

                        if (pair.size > 1) {
                            ProductCard(
                                product = pair[1],
                                onClick = { onProductClick(pair[1]) },
                                onAddToCart = { onAddToCart(pair[1].id) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
