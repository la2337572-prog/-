package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nameEn: String,
    val description: String,
    val price: Double,
    val compareAtPrice: Double? = null,
    val categoryId: String,
    val brand: String,
    val bikeTypes: String, // Comma separated: "سبورت,كروزر"
    val stock: Int,
    val badge: String? = null, // "عرض", "جديد", "الأكثر طلبًا"
    val image: String,
    val featured: Boolean = false,
    val videoUrl: String? = null
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nameEn: String,
    val icon: String,
    val visible: Boolean = true
)

@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val discountLabel: String,
    val publishedAt: String,
    val image: String,
    val categoryId: String? = null,
    val active: Boolean = true
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val quantity: Int
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val paymentMethod: String,
    val itemsSummary: String,
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "جديد - قيد التجهيز"
)

@Entity(tableName = "part_inquiries")
data class PartInquiryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partName: String,
    val bikeModel: String,
    val bikeYear: String,
    val customerPhone: String,
    val notes: String,
    val photoUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "قيد المراجعة"
)

@Entity(tableName = "store_settings")
data class StoreSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val storeName: String = "معيض موتور",
    val storeNameEn: String = "MAAYED MOTOR",
    val whatsappNumber: String = "+966501234567",
    val phone: String = "+966112345678",
    val instagram: String = "maayed_motor",
    val tiktok: String = "maayed_motor",
    val telegram: String = "maayed_motor",
    val city: String = "الرياض",
    val address: String = "طريق خريص - معارض الدبابات، الرياض",
    val workingHours: String = "السبت - الخميس: 9:00 ص - 10:30 م"
)

data class CartItemWithProduct(
    val cartItem: CartItemEntity,
    val product: ProductEntity
)
