package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CartItemWithProduct
import com.example.ui.theme.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItemWithProduct>,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onPlaceOrder: (String, String, String, (Long) -> Unit) -> Unit,
    onNavigateToCatalog: () -> Unit,
    whatsappNumber: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var placedOrderId by remember { mutableStateOf<Long?>(null) }

    val subtotal = cartItems.sumOf { it.product.price * it.cartItem.quantity }
    val shipping = if (subtotal >= 300 || subtotal == 0.0) 0.0 else 35.0
    val total = subtotal + shipping

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = BrandOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سلة التسوق (${cartItems.sumOf { it.cartItem.quantity }} قطعة)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = onClearCart) {
                        Text("تفريغ السلة", color = AccentRed, fontSize = 12.sp)
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "سلة التسوق فارغة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "استكشف أحدث الدبابات وقطع الغيار والإكسسوارات وأضفها إلى سلتك",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateToCatalog,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_start_shopping")
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ابدأ التسوق الآن")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.product.image,
                                contentDescription = item.product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(74.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.product.brand,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatSar(item.product.price),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandOrange
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                IconButton(
                                    onClick = { onRemoveItem(item.product.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "حذف",
                                        tint = AccentRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            onUpdateQuantity(item.product.id, item.cartItem.quantity - 1)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "تقليل",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Text(
                                        text = "${item.cartItem.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            onUpdateQuantity(item.product.id, item.cartItem.quantity + 1)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "زيادة",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Cost Summary Card
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ملخص الطلب",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("المجموع الفرعي:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatSar(subtotal), fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("رسوم التوصيل:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (shipping == 0.0) "مجاني" else formatSar(shipping),
                                    color = if (shipping == 0.0) AccentGreen else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "المجموع الكلي (شامل الضريبة):",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    formatSar(total),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandOrange
                                )
                            }
                        }
                    }
                }
            }

            // Checkout Bottom Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("الإجمالي المستحق:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatSar(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = BrandOrange)
                    }

                    Button(
                        onClick = { showCheckoutDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_proceed_checkout")
                    ) {
                        Text("إتمام الطلب", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    }
                }
            }
        }
    }

    // Checkout Details Dialog
    if (showCheckoutDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = {
                Text(
                    text = "بيانات شحن وتوصيل الطلب",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "طريقة الدفع المعتمدة: الدفع نقدًا عند الاستلام (COD)",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكامل") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الجوال (05xxxxxxx)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("المدينة والحي والشارع") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    if (errorMsg.isNotEmpty()) {
                        Text(errorMsg, color = AccentRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank() || address.isBlank()) {
                            errorMsg = "يرجى تعبئة جميع الحقول لإكمال الطلب"
                        } else {
                            onPlaceOrder(name, phone, address) { orderId ->
                                showCheckoutDialog = false
                                placedOrderId = orderId
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("تأكيد وحفظ الطلب")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Order Success Dialog
    placedOrderId?.let { orderId ->
        AlertDialog(
            onDismissRequest = { placedOrderId = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(54.dp)
                )
            },
            title = {
                Text(
                    text = "تم تسجيل طلبك بنجاح!",
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "رقم الطلب: #$orderId",
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandOrange,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "سيقوم فريق مبيعات معيض موتور بالتواصل معك هاتفياً لتأكيد الشحن والتسليم.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanPhone = whatsappNumber.replace("+", "").replace(" ", "").replace("-", "")
                        val msg = "السلام عليكم، لقد قمت بتسجيل الطلب رقم #$orderId من تطبيق معيض موتور بقيمة ${formatSar(total)}، وأود متابعة تفاصيل التوصيل."
                        val encoded = URLEncoder.encode(msg, StandardCharsets.UTF_8.toString())
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone?text=$encoded"))
                        context.startActivity(intent)
                        placedOrderId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("متابعة عبر واتساب")
                }
            },
            dismissButton = {
                TextButton(onClick = { placedOrderId = null }) {
                    Text("تم")
                }
            }
        )
    }
}
