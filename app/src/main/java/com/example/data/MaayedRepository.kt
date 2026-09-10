package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class MaayedRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val categoryDao = database.categoryDao()
    private val promotionDao = database.promotionDao()
    private val cartDao = database.cartDao()
    private val orderDao = database.orderDao()
    private val partInquiryDao = database.partInquiryDao()
    private val storeSettingsDao = database.storeSettingsDao()

    suspend fun checkAndSeedDatabase() {
        withContext(Dispatchers.IO) {
            if (productDao.countProducts() == 0) {
                AppDatabase.seedInitialData(database)
            }
        }
    }

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val featuredProducts: Flow<List<ProductEntity>> = productDao.getFeaturedProducts()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val activePromotions: Flow<List<PromotionEntity>> = promotionDao.getActivePromotions()
    val allPromotions: Flow<List<PromotionEntity>> = promotionDao.getAllPromotions()
    val storeSettings: Flow<StoreSettingsEntity?> = storeSettingsDao.getSettings()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val allInquiries: Flow<List<PartInquiryEntity>> = partInquiryDao.getAllInquiries()

    fun getProductById(id: String): Flow<ProductEntity?> = productDao.getProductById(id)

    val cartItemsWithProducts: Flow<List<CartItemWithProduct>> = combine(
        cartDao.getAllCartItems(),
        productDao.getAllProducts()
    ) { cartItems, products ->
        val productMap = products.associateBy { it.id }
        cartItems.mapNotNull { cartItem ->
            productMap[cartItem.productId]?.let { product ->
                CartItemWithProduct(cartItem, product)
            }
        }
    }

    suspend fun addToCart(productId: String, quantity: Int = 1) {
        withContext(Dispatchers.IO) {
            cartDao.insertCartItem(CartItemEntity(productId = productId, quantity = quantity))
        }
    }

    suspend fun updateCartQuantity(productId: String, quantity: Int) {
        withContext(Dispatchers.IO) {
            if (quantity <= 0) {
                cartDao.removeCartItem(productId)
            } else {
                cartDao.updateQuantity(productId, quantity)
            }
        }
    }

    suspend fun removeFromCart(productId: String) {
        withContext(Dispatchers.IO) {
            cartDao.removeCartItem(productId)
        }
    }

    suspend fun clearCart() {
        withContext(Dispatchers.IO) {
            cartDao.clearCart()
        }
    }

    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        paymentMethod: String,
        itemsSummary: String,
        totalAmount: Double
    ): Long {
        return withContext(Dispatchers.IO) {
            val orderId = orderDao.insertOrder(
                OrderEntity(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerAddress = customerAddress,
                    paymentMethod = paymentMethod,
                    itemsSummary = itemsSummary,
                    totalAmount = totalAmount
                )
            )
            cartDao.clearCart()
            orderId
        }
    }

    suspend fun submitPartInquiry(
        partName: String,
        bikeModel: String,
        bikeYear: String,
        customerPhone: String,
        notes: String,
        photoUri: String? = null
    ): Long {
        return withContext(Dispatchers.IO) {
            partInquiryDao.insertInquiry(
                PartInquiryEntity(
                    partName = partName,
                    bikeModel = bikeModel,
                    bikeYear = bikeYear,
                    customerPhone = customerPhone,
                    notes = notes,
                    photoUri = photoUri
                )
            )
        }
    }

    suspend fun addOrUpdateProduct(product: ProductEntity) {
        withContext(Dispatchers.IO) {
            productDao.insertProduct(product)
        }
    }

    suspend fun deleteProduct(product: ProductEntity) {
        withContext(Dispatchers.IO) {
            productDao.deleteProduct(product)
        }
    }

    suspend fun addOrUpdatePromotion(promotion: PromotionEntity) {
        withContext(Dispatchers.IO) {
            promotionDao.insertPromotion(promotion)
        }
    }

    suspend fun deletePromotion(promotion: PromotionEntity) {
        withContext(Dispatchers.IO) {
            promotionDao.deletePromotion(promotion)
        }
    }

    suspend fun updateSettings(settings: StoreSettingsEntity) {
        withContext(Dispatchers.IO) {
            storeSettingsDao.insertSettings(settings)
        }
    }
}
