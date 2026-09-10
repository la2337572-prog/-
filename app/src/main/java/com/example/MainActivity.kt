package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ProductEntity
import com.example.ui.screens.*
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MaayedViewModel

enum class MainTab(val titleAr: String) {
    HOME("الرئيسية"),
    CATALOG("الكتالوج"),
    CART("السلة"),
    ACCOUNT("الحساب")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MaayedApp(
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { isDarkTheme = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaayedApp(
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    viewModel: MaayedViewModel = viewModel()
) {
    val context = LocalContext.current

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var viewingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var isViewingAdmin by remember { mutableStateOf(false) }
    var showPhotoRequestDialog by remember { mutableStateOf(false) }

    // State collections from ViewModel
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val featuredProducts by viewModel.featuredProducts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val activePromotions by viewModel.activePromotions.collectAsStateWithLifecycle()
    val allPromotions by viewModel.allPromotions.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val storeSettings by viewModel.storeSettings.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val inquiries by viewModel.inquiries.collectAsStateWithLifecycle()

    // Filter states
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val selectedBrand by viewModel.selectedBrand.collectAsStateWithLifecycle()
    val selectedBikeType by viewModel.selectedBikeType.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    val totalCartCount = cartItems.sumOf { it.cartItem.quantity }

    val openWhatsApp: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // fallback
        }
    }

    if (isViewingAdmin) {
        ProtectedAdminScreen(
            products = allProducts,
            promotions = allPromotions,
            orders = orders,
            inquiries = inquiries,
            settings = storeSettings,
            onBack = { isViewingAdmin = false },
            onSaveProduct = { viewModel.saveProduct(it) },
            onDeleteProduct = { viewModel.deleteProduct(it) },
            onSavePromotion = { viewModel.savePromotion(it) },
            onDeletePromotion = { viewModel.deletePromotion(it) },
            onUpdateSettings = { viewModel.updateSettings(it) }
        )
        return
    }

    if (viewingProduct != null) {
        ProductDetailScreen(
            product = viewingProduct!!,
            onBack = { viewingProduct = null },
            onAddToCart = { viewModel.addToCart(it) },
            onWhatsAppInquiry = { prod ->
                openWhatsApp(viewModel.getProductWhatsAppUrl(prod))
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentTab != MainTab.CATALOG) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(
                                text = "معيض موتور",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = BrandOrange,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "MOTOR",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { currentTab = MainTab.CATALOG }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "بحث")
                        }
                        IconButton(onClick = { currentTab = MainTab.CART }) {
                            BadgedBox(
                                badge = {
                                    if (totalCartCount > 0) {
                                        Badge(containerColor = BrandOrange) {
                                            Text("$totalCartCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "السلة")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = currentTab == MainTab.HOME,
                    onClick = { currentTab = MainTab.HOME },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.HOME) Icons.Default.Home else Icons.Outlined.Home,
                            contentDescription = "الرئيسية"
                        )
                    },
                    label = { Text(MainTab.HOME.titleAr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandOrange,
                        selectedTextColor = BrandOrange,
                        indicatorColor = BrandOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_home")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.CATALOG,
                    onClick = { currentTab = MainTab.CATALOG },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.CATALOG) Icons.Default.TwoWheeler else Icons.Outlined.TwoWheeler,
                            contentDescription = "الكتالوج"
                        )
                    },
                    label = { Text(MainTab.CATALOG.titleAr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandOrange,
                        selectedTextColor = BrandOrange,
                        indicatorColor = BrandOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_catalog")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.CART,
                    onClick = { currentTab = MainTab.CART },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (totalCartCount > 0) {
                                    Badge(containerColor = BrandOrange) {
                                        Text("$totalCartCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == MainTab.CART) Icons.Default.ShoppingCart else Icons.Outlined.ShoppingCart,
                                contentDescription = "السلة"
                            )
                        }
                    },
                    label = { Text(MainTab.CART.titleAr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandOrange,
                        selectedTextColor = BrandOrange,
                        indicatorColor = BrandOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_cart")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.ACCOUNT,
                    onClick = { currentTab = MainTab.ACCOUNT },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.ACCOUNT) Icons.Default.Person else Icons.Outlined.Person,
                            contentDescription = "الحساب"
                        )
                    },
                    label = { Text(MainTab.ACCOUNT.titleAr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandOrange,
                        selectedTextColor = BrandOrange,
                        indicatorColor = BrandOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_account")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.HOME -> {
                    HomeScreen(
                        featuredProducts = featuredProducts.ifEmpty { allProducts.take(6) },
                        categories = categories,
                        promotions = activePromotions,
                        onProductClick = { viewingProduct = it },
                        onAddToCart = { viewModel.addToCart(it) },
                        onNavigateToCatalog = { categoryId ->
                            viewModel.selectCategory(categoryId)
                            currentTab = MainTab.CATALOG
                        },
                        onSearchClick = { currentTab = MainTab.CATALOG },
                        onOpenPhotoRequest = { showPhotoRequestDialog = true },
                        onOpenWhatsApp = {
                            openWhatsApp(viewModel.getGeneralWhatsAppUrl())
                        }
                    )
                }

                MainTab.CATALOG -> {
                    CatalogScreen(
                        products = filteredProducts,
                        categories = categories,
                        searchQuery = searchQuery,
                        selectedCategoryId = selectedCategory,
                        selectedBrand = selectedBrand,
                        selectedBikeType = selectedBikeType,
                        sortOption = sortOption,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onSelectBrand = { viewModel.selectBrand(it) },
                        onSelectBikeType = { viewModel.selectBikeType(it) },
                        onSelectSort = { viewModel.setSortOption(it) },
                        onProductClick = { viewingProduct = it },
                        onAddToCart = { viewModel.addToCart(it) }
                    )
                }

                MainTab.CART -> {
                    CartScreen(
                        cartItems = cartItems,
                        onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                        onRemoveItem = { viewModel.removeFromCart(it) },
                        onClearCart = { viewModel.clearCart() },
                        onPlaceOrder = { name, phone, address, onDone ->
                            viewModel.placeOrder(name, phone, address, onSuccess = onDone)
                        },
                        onNavigateToCatalog = { currentTab = MainTab.CATALOG },
                        whatsappNumber = storeSettings?.whatsappNumber ?: "+966501234567"
                    )
                }

                MainTab.ACCOUNT -> {
                    AccountScreen(
                        settings = storeSettings,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = onToggleDarkTheme,
                        onOpenPhotoRequest = { showPhotoRequestDialog = true },
                        onOpenAdmin = { isViewingAdmin = true },
                        onOpenWhatsApp = {
                            openWhatsApp(viewModel.getGeneralWhatsAppUrl())
                        }
                    )
                }
            }
        }
    }

    // Photo Request Dialog
    PhotoRequestDialog(
        isOpen = showPhotoRequestDialog,
        onDismiss = { showPhotoRequestDialog = false },
        onSubmit = { name, model, year, phone, notes, uri, onDone ->
            viewModel.submitPartInquiry(name, model, year, phone, notes, uri, onDone)
        },
        whatsappNumber = storeSettings?.whatsappNumber ?: "+966501234567"
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "معيض موتور - $name", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}

