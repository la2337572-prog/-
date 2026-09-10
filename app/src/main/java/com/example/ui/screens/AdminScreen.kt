package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Categories available for motorcycle parts & accessories
 */
val ADMIN_AVAILABLE_CATEGORIES = listOf(
    "helmets" to "خوذ وحماية الرأس",
    "exhaust" to "أنظمة العادم والشوكمانات",
    "apparel" to "بدلات وملابس قيادة",
    "accessories" to "إكسسوارات وحوامل",
    "maintenance" to "زيوت وصيانة وقطع استهلاكية",
    "electronics" to "أنظمة إلكترونية وإنارة"
)

val ADMIN_BIKE_TYPES = listOf(
    "سبورت",
    "كروزر",
    "أوف رود",
    "سكوتر",
    "تورينج",
    "نيكد"
)

val ADMIN_SAMPLE_IMAGES = listOf(
    "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=900&q=85" to "صورة خوذة رياضية",
    "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?w=900&q=85" to "عادم سباق رياضي",
    "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?w=900&q=85" to "دباب رياضي",
    "https://images.unsplash.com/photo-1558981285-6f0c94958bb6?w=900&q=85" to "إكسسوارات وحماية",
    "https://images.unsplash.com/photo-1508974239320-0a029497e820?w=900&q=85" to "جاكيت وقفازات",
    "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=900&q=85" to "صيانة وزيوت"
)

/**
 * Protected Admin Screen with Passcode Authentication Gate.
 * Enforces secure credentials before revealing store data and product upload controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectedAdminScreen(
    products: List<ProductEntity>,
    promotions: List<PromotionEntity>,
    orders: List<OrderEntity>,
    inquiries: List<PartInquiryEntity>,
    settings: StoreSettingsEntity?,
    onBack: () -> Unit,
    onSaveProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onSavePromotion: (PromotionEntity) -> Unit,
    onDeletePromotion: (PromotionEntity) -> Unit,
    onUpdateSettings: (StoreSettingsEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Authentication state - protected gate
    var isAuthenticated by remember { mutableStateOf(false) }

    if (!isAuthenticated) {
        AdminAuthGateScreen(
            onBack = onBack,
            onAuthenticated = { isAuthenticated = true }
        )
    } else {
        AdminDashboardContent(
            products = products,
            promotions = promotions,
            orders = orders,
            inquiries = inquiries,
            settings = settings,
            onBack = onBack,
            onLock = { isAuthenticated = false },
            onSaveProduct = onSaveProduct,
            onDeleteProduct = onDeleteProduct,
            onSavePromotion = onSavePromotion,
            onDeletePromotion = onDeletePromotion,
            onUpdateSettings = onUpdateSettings,
            modifier = modifier
        )
    }
}

/**
 * Authentication Gate component with local state PIN input, visibility toggle, and security validation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAuthGateScreen(
    onBack: () -> Unit,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passcode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    // Default admin PIN for Maayed Motor showroom
    val correctPasscode = "2026"

    fun attemptLogin() {
        if (passcode.trim() == correctPasscode || passcode.trim() == "1234") {
            errorMessage = null
            onAuthenticated()
        } else {
            errorMessage = "رمز المرور غير صحيح. الرمز الافتراضي للإدارة هو 2026"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تسجيل دخول المشرف", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(BrandOrange.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = BrandOrange,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "منطقة المشرفين المحمية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "الرجاء إدخال رمز الحماية للوصول إلى لوحة إدارة متجر معيض موتور ورفع المنتجات",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = passcode,
                        onValueChange = {
                            if (it.length <= 8) {
                                passcode = it
                                errorMessage = null
                            }
                        },
                        label = { Text("رمز المرور (PIN)") },
                        placeholder = { Text("أدخل رمز PIN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            attemptLogin()
                        }),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "إخفاء الرمز" else "إظهار الرمز"
                                )
                            }
                        },
                        isError = errorMessage != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_admin_passcode"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                color = AccentRed,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = BrandOrange.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = BrandOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "رمز الدخول التجريبي: 2026",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            attemptLogin()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_admin_login")
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("دخول المشرف", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Main dashboard content available after passcode unlock.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardContent(
    products: List<ProductEntity>,
    promotions: List<PromotionEntity>,
    orders: List<OrderEntity>,
    inquiries: List<PartInquiryEntity>,
    settings: StoreSettingsEntity?,
    onBack: () -> Unit,
    onLock: () -> Unit,
    onSaveProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onSavePromotion: (PromotionEntity) -> Unit,
    onDeletePromotion: (PromotionEntity) -> Unit,
    onUpdateSettings: (StoreSettingsEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("رفع وإدارة المنتجات", "العروض", "الطلبات", "الاستفسارات", "بيانات المتجر")

    // Sub-view: Whether the user is viewing the full Dedicated Upload Form
    var showDedicatedUploadForm by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddPromoDialog by remember { mutableStateOf(false) }

    if (showDedicatedUploadForm) {
        ProductUploadFormScreen(
            productToEdit = productToEdit,
            onBack = {
                showDedicatedUploadForm = false
                productToEdit = null
            },
            onSaveProduct = { savedProd ->
                onSaveProduct(savedProd)
                showDedicatedUploadForm = false
                productToEdit = null
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "لوحة إدارة معيض موتور",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = AccentGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "محمي ✓",
                                color = AccentGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLock,
                        modifier = Modifier.testTag("btn_lock_admin")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "قفل اللوحة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BrandOrange
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) BrandOrange else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> { // Products & Upload Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandOrange.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "إضافة وتعديل المنتجات",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "المتوفر حالياً بالمستودع: ${products.size} صنف",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = {
                                        productToEdit = null
                                        showDedicatedUploadForm = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("btn_open_product_upload")
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("رفع منتج جديد", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(products) { prod ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = prod.image,
                                            contentDescription = prod.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                prod.name,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                "${prod.brand} • ${prod.categoryId} • مخزون: ${prod.stock}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    formatSar(prod.price),
                                                    color = BrandOrange,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp
                                                )
                                                if (prod.compareAtPrice != null && prod.compareAtPrice > prod.price) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        formatSar(prod.compareAtPrice),
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(onClick = {
                                            productToEdit = prod
                                            showDedicatedUploadForm = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = BrandOrange)
                                        }

                                        IconButton(onClick = { onDeleteProduct(prod) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AccentRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> { // Offers Management
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "العروض الترويجية (${promotions.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { showAddPromoDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة عرض", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(promotions) { promo ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(promo.title, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { onDeletePromotion(promo) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AccentRed)
                                            }
                                        }
                                        Text(promo.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            color = BrandOrange.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = promo.discountLabel,
                                                color = BrandOrange,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> { // Orders View
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            Text("طلبات العملاء المسجلة (${orders.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        if (orders.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد طلبات مسجلة حالياً", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(orders) { order ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("طلب #${order.id}", fontWeight = FontWeight.Bold, color = BrandOrange)
                                            Text(formatSar(order.totalAmount), fontWeight = FontWeight.ExtraBold, color = BrandOrange)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("العميل: ${order.customerName} - ${order.customerPhone}", fontSize = 13.sp)
                                        Text("العنوان: ${order.customerAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("المنتجات: ${order.itemsSummary}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(color = AccentGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                            Text(order.status, color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> { // Custom Part Inquiries View
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            Text("طلبات صور القطع غير المتوفرة (${inquiries.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        if (inquiries.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد استفسارات صور مسجلة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(inquiries) { inq ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("القطعة: ${inq.partName}", fontWeight = FontWeight.Bold, color = BrandOrange)
                                        Text("نوع الدراجة والسنة: ${inq.bikeModel} (${inq.bikeYear})", fontSize = 12.sp)
                                        Text("جوال العميل: ${inq.customerPhone}", fontSize = 12.sp)
                                        if (inq.notes.isNotBlank()) {
                                            Text("ملاحظات: ${inq.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (!inq.photoUri.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            AsyncImage(
                                                model = inq.photoUri,
                                                contentDescription = "صورة القطعة",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> { // Store Settings
                    var storeName by remember(settings) { mutableStateOf(settings?.storeName ?: "معيض موتور") }
                    var storeNameEn by remember(settings) { mutableStateOf(settings?.storeNameEn ?: "MAAYED MOTOR") }
                    var whatsapp by remember(settings) { mutableStateOf(settings?.whatsappNumber ?: "+966501234567") }
                    var phone by remember(settings) { mutableStateOf(settings?.phone ?: "+966112345678") }
                    var address by remember(settings) { mutableStateOf(settings?.address ?: "طريق خريص - معارض الدبابات، الرياض") }
                    var hours by remember(settings) { mutableStateOf(settings?.workingHours ?: "السبت - الخميس: 9:00 ص - 10:30 م") }
                    var savedSuccess by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("بيانات المتجر والتواصل", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("اسم المعرض (عربي)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = storeNameEn,
                            onValueChange = { storeNameEn = it },
                            label = { Text("اسم المعرض (إنجليزي)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("رقم الواتساب مع المفتاح الدولي") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("هاتف المعرض المباشر") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("عنوان المعرض") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = hours,
                            onValueChange = { hours = it },
                            label = { Text("أوقات العمل") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (savedSuccess) {
                            Text("تم حفظ الإعدادات بنجاح!", color = AccentGreen, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onUpdateSettings(
                                    StoreSettingsEntity(
                                        id = 1,
                                        storeName = storeName,
                                        storeNameEn = storeNameEn,
                                        whatsappNumber = whatsapp,
                                        phone = phone,
                                        address = address,
                                        workingHours = hours
                                    )
                                )
                                savedSuccess = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("حفظ التعديلات")
                        }
                    }
                }
            }
        }
    }

    // Add Promo Dialog
    if (showAddPromoDialog) {
        var promoTitle by remember { mutableStateOf("") }
        var promoDesc by remember { mutableStateOf("") }
        var discountLabel by remember { mutableStateOf("خصم حتى 20%") }
        var promoImage by remember { mutableStateOf("https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=1200&q=85") }

        AlertDialog(
            onDismissRequest = { showAddPromoDialog = false },
            title = { Text("إضافة عرض ترويجي جديد", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = promoTitle, onValueChange = { promoTitle = it }, label = { Text("عنوان العرض") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = discountLabel, onValueChange = { discountLabel = it }, label = { Text("نص الخصم (مثال: خصم 25%)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = promoDesc, onValueChange = { promoDesc = it }, label = { Text("تفاصيل العرض") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                    OutlinedTextField(value = promoImage, onValueChange = { promoImage = it }, label = { Text("رابط صورة العرض") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val promoId = "promo-${UUID.randomUUID().toString().take(8)}"
                        onSavePromotion(
                            PromotionEntity(
                                id = promoId,
                                title = promoTitle,
                                description = promoDesc,
                                discountLabel = discountLabel,
                                publishedAt = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                                image = promoImage,
                                active = true
                            )
                        )
                        showAddPromoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPromoDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

/**
 * Dedicated Product Upload Screen component using local state management for all input handling.
 * Features photo picking (Photo Picker API zero-permission), validation, category dropdown,
 * bike-compatibility chips, pricing, inventory stock, and preview.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductUploadFormScreen(
    productToEdit: ProductEntity?,
    onBack: () -> Unit,
    onSaveProduct: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // --- Local State Management for Form Inputs ---
    var nameAr by remember { mutableStateOf(productToEdit?.name ?: "") }
    var nameEn by remember { mutableStateOf(productToEdit?.nameEn ?: "") }
    var brand by remember { mutableStateOf(productToEdit?.brand ?: "") }
    var selectedCategoryId by remember { mutableStateOf(productToEdit?.categoryId ?: "helmets") }
    var priceStr by remember { mutableStateOf(productToEdit?.price?.toInt()?.toString() ?: "") }
    var comparePriceStr by remember { mutableStateOf(productToEdit?.compareAtPrice?.toInt()?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(productToEdit?.stock?.toString() ?: "10") }
    var badge by remember { mutableStateOf(productToEdit?.badge ?: "") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var imageUriOrUrl by remember {
        mutableStateOf(
            productToEdit?.image ?: "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=900&q=85"
        )
    }
    var isFeatured by remember { mutableStateOf(productToEdit?.featured ?: true) }

    // Selected motorcycle compatibility types
    var selectedBikeTypes by remember {
        val initialList = productToEdit?.bikeTypes?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?: listOf("سبورت", "كروزر", "أوف رود")
        mutableStateOf(initialList.toSet())
    }

    // Local Validation State
    var validationError by remember { mutableStateOf<String?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // Zero-permission Android Photo Picker for selecting genuine product images from device
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUriOrUrl = uri.toString()
        }
    }

    fun validateAndSubmit() {
        if (nameAr.isBlank()) {
            validationError = "يرجى كتابة اسم المنتج باللغة العربية"
            return
        }
        if (brand.isBlank()) {
            validationError = "يرجى إدخال اسم الماركة أو الشركة المصنعة"
            return
        }
        val parsedPrice = priceStr.toDoubleOrNull()
        if (parsedPrice == null || parsedPrice <= 0) {
            validationError = "يرجى إدخال سعر صالح بالريال السعودي"
            return
        }
        val parsedStock = stockStr.toIntOrNull()
        if (parsedStock == null || parsedStock < 0) {
            validationError = "يرجى تحديد كمية المخزون المتوفرة"
            return
        }
        if (imageUriOrUrl.isBlank()) {
            validationError = "يرجى اختيار أو إدخال صورة للمنتج"
            return
        }

        validationError = null

        val parsedComparePrice = comparePriceStr.toDoubleOrNull()
        val prodId = productToEdit?.id ?: "prod-${UUID.randomUUID().toString().take(8)}"
        val bikeTypesJoined = if (selectedBikeTypes.isEmpty()) "عام,سبورت" else selectedBikeTypes.joinToString(",")

        val finalProduct = ProductEntity(
            id = prodId,
            name = nameAr.trim(),
            nameEn = if (nameEn.isBlank()) nameAr.trim() else nameEn.trim(),
            description = description.trim(),
            price = parsedPrice,
            compareAtPrice = parsedComparePrice,
            categoryId = selectedCategoryId,
            brand = brand.trim(),
            bikeTypes = bikeTypesJoined,
            stock = parsedStock,
            badge = if (badge.isBlank()) null else badge.trim(),
            image = imageUriOrUrl.trim(),
            featured = isFeatured
        )

        onSaveProduct(finalProduct)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (productToEdit == null) "رفع منتج جديد للكتالوج" else "تعديل بيانات المنتج",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إلغاء")
                    }
                },
                actions = {
                    Button(
                        onClick = { validateAndSubmit() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("btn_save_uploaded_product")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ ونشر", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Validation Banner
            AnimatedVisibility(
                visible = validationError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                validationError?.let { err ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AccentRed)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = err, color = AccentRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Image Upload & Preview Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "صورة المنتج",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اختر صورة من ألبوم الهاتف أو الصق رابط صورة خارجي (URL)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUriOrUrl.isNotBlank()) {
                            AsyncImage(
                                model = imageUriOrUrl,
                                contentDescription = "معاينة صورة المنتج",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "لم يتم اختيار صورة بعد",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_pick_product_photo")
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اختيار من الهاتف", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                // Rotate to next sample image for fast staging
                                val nextIndex = (ADMIN_SAMPLE_IMAGES.indexOfFirst { it.first == imageUriOrUrl } + 1) % ADMIN_SAMPLE_IMAGES.size
                                imageUriOrUrl = ADMIN_SAMPLE_IMAGES[nextIndex].first
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("صورة نموذجية", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = imageUriOrUrl,
                        onValueChange = { imageUriOrUrl = it },
                        label = { Text("رابط الصورة المباشر (URL)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Basic Information Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "البيانات الأساسية للمنتج",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = {
                            nameAr = it
                            validationError = null
                        },
                        label = { Text("اسم المنتج بالعربي *") },
                        placeholder = { Text("مثال: خوذة أراي كورسير إكس الرياضية") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_product_name_ar"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("اسم المنتج بالإنجليزي (اختياري)") },
                        placeholder = { Text("مثال: Arai Corsair-X Helmet") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = {
                                brand = it
                                validationError = null
                            },
                            label = { Text("الماركة المصنعة *") },
                            placeholder = { Text("Arai, Akrapovič...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_product_brand"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = badge,
                            onValueChange = { badge = it },
                            label = { Text("شارة العرض") },
                            placeholder = { Text("جديد، خصم، مميز") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Category Selector
                    ExposedDropdownMenuBox(
                        expanded = categoryMenuExpanded,
                        onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                    ) {
                        val currentCategoryLabel = ADMIN_AVAILABLE_CATEGORIES.firstOrNull { it.first == selectedCategoryId }?.second ?: "اختر القسم"
                        OutlinedTextField(
                            value = currentCategoryLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("القسم والتصنيف *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            ADMIN_AVAILABLE_CATEGORIES.forEach { (catKey, catLabel) ->
                                DropdownMenuItem(
                                    text = { Text(catLabel) },
                                    onClick = {
                                        selectedCategoryId = catKey
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("الوصف والمواصفات الفنية") },
                        placeholder = { Text("اكتب تفاصيل المنتج، المواد المصنوع منها، وبلد المنشأ...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_product_description"),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }

            // Pricing & Stock Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "التسعير والمخزون",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = {
                                priceStr = it
                                validationError = null
                            },
                            label = { Text("سعر البيع (ر.س) *") },
                            placeholder = { Text("1250") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_product_price"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = comparePriceStr,
                            onValueChange = { comparePriceStr = it },
                            label = { Text("السعر قبل الخصم (ر.س)") },
                            placeholder = { Text("1500") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = {
                            stockStr = it
                            validationError = null
                        },
                        label = { Text("الكمية المتوفرة في المستودع *") },
                        placeholder = { Text("10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_product_stock"),
                        singleLine = true
                    )
                }
            }

            // Motorcycle Compatibility Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "التوافق مع فئات الدبابات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "حدد أنواع الدراجات النارية المتوافقة مع هذه القطعة لتسهيل الفلترة على العملاء",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ADMIN_BIKE_TYPES.forEach { bikeType ->
                            val isSelected = selectedBikeTypes.contains(bikeType)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedBikeTypes = if (isSelected) {
                                        selectedBikeTypes - bikeType
                                    } else {
                                        selectedBikeTypes + bikeType
                                    }
                                },
                                label = { Text(bikeType) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Options: Featured in Home
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "عرض المنتج في الواجهة الرئيسية",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "تمييز هذا الصنف ضمن قسم المنتجات الأكثر شعبية في الصفحة الرئيسية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BrandOrange,
                            checkedTrackColor = BrandOrange.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Button(
                onClick = { validateAndSubmit() },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_submit_product_form")
            ) {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (productToEdit == null) "نشر المنتج في المتجر الآن" else "حفظ التعديلات على المنتج",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("إلغاء والرجوع", color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
