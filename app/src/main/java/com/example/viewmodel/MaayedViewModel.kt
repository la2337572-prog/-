package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CartItemWithProduct
import com.example.data.CategoryEntity
import com.example.data.MaayedRepository
import com.example.data.OrderEntity
import com.example.data.PartInquiryEntity
import com.example.data.ProductEntity
import com.example.data.PromotionEntity
import com.example.data.StoreSettingsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class SortOption(val titleAr: String) {
    POPULAR("الأكثر شعبية"),
    PRICE_ASC("السعر: من الأقل"),
    PRICE_DESC("السعر: من الأعلى"),
    NAME("أبجديًا")
}

class MaayedViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MaayedRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MaayedRepository(database)
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
    }

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredProducts: StateFlow<List<ProductEntity>> = repository.featuredProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePromotions: StateFlow<List<PromotionEntity>> = repository.activePromotions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPromotions: StateFlow<List<PromotionEntity>> = repository.allPromotions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItemWithProduct>> = repository.cartItemsWithProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storeSettings: StateFlow<StoreSettingsEntity?> = repository.storeSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val orders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inquiries: StateFlow<List<PartInquiryEntity>> = repository.allInquiries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _selectedBrand = MutableStateFlow<String?>(null)
    val selectedBrand = _selectedBrand.asStateFlow()

    private val _selectedBikeType = MutableStateFlow<String?>(null)
    val selectedBikeType = _selectedBikeType.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.POPULAR)
    val sortOption = _sortOption.asStateFlow()

    // Selected product for detail navigation
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    private data class FilterParams(
        val query: String,
        val catId: String?,
        val brand: String?,
        val bikeType: String?,
        val sort: SortOption
    )

    private val filterParams = combine(
        _searchQuery,
        _selectedCategoryId,
        _selectedBrand,
        _selectedBikeType,
        _sortOption
    ) { query, catId, brand, bikeType, sort ->
        FilterParams(query, catId, brand, bikeType, sort)
    }

    // Filtered products flow
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        filterParams
    ) { products, filter ->
        val trimmedQuery = filter.query.trim().lowercase()

        val filtered = products.filter { product ->
            val matchesQuery = trimmedQuery.isEmpty() ||
                    product.name.lowercase().contains(trimmedQuery) ||
                    product.nameEn.lowercase().contains(trimmedQuery) ||
                    product.description.lowercase().contains(trimmedQuery) ||
                    product.brand.lowercase().contains(trimmedQuery)

            val matchesCategory = filter.catId == null || product.categoryId == filter.catId
            val matchesBrand = filter.brand == null || product.brand.equals(filter.brand, ignoreCase = true)
            val matchesBikeType = filter.bikeType == null || product.bikeTypes.contains(filter.bikeType)

            matchesQuery && matchesCategory && matchesBrand && matchesBikeType
        }

        when (filter.sort) {
            SortOption.POPULAR -> filtered.sortedByDescending { it.featured }
            SortOption.PRICE_ASC -> filtered.sortedBy { it.price }
            SortOption.PRICE_DESC -> filtered.sortedByDescending { it.price }
            SortOption.NAME -> filtered.sortedBy { it.name }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun selectBrand(brand: String?) {
        _selectedBrand.value = brand
    }

    fun selectBikeType(bikeType: String?) {
        _selectedBikeType.value = bikeType
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun selectProduct(product: ProductEntity?) {
        _selectedProduct.value = product
    }

    // Cart actions
    fun addToCart(productId: String) {
        viewModelScope.launch {
            val currentItem = cartItems.value.firstOrNull { it.cartItem.productId == productId }
            val newQty = (currentItem?.cartItem?.quantity ?: 0) + 1
            repository.addToCart(productId, newQty)
        }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, quantity)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Place Order
    fun placeOrder(
        name: String,
        phone: String,
        address: String,
        paymentMethod: String = "الدفع عند الاستلام",
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val items = cartItems.value
            val summary = items.joinToString(", ") { "${it.product.name} (x${it.cartItem.quantity})" }
            val total = items.sumOf { it.product.price * it.cartItem.quantity }
            val orderId = repository.placeOrder(
                customerName = name,
                customerPhone = phone,
                customerAddress = address,
                paymentMethod = paymentMethod,
                itemsSummary = summary,
                totalAmount = total
            )
            onSuccess(orderId)
        }
    }

    // Submit Custom Part Inquiry
    fun submitPartInquiry(
        partName: String,
        bikeModel: String,
        bikeYear: String,
        customerPhone: String,
        notes: String,
        photoUri: String? = null,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val inquiryId = repository.submitPartInquiry(
                partName = partName,
                bikeModel = bikeModel,
                bikeYear = bikeYear,
                customerPhone = customerPhone,
                notes = notes,
                photoUri = photoUri
            )
            onSuccess(inquiryId)
        }
    }

    // Admin Operations
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.addOrUpdateProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun savePromotion(promotion: PromotionEntity) {
        viewModelScope.launch {
            repository.addOrUpdatePromotion(promotion)
        }
    }

    fun deletePromotion(promotion: PromotionEntity) {
        viewModelScope.launch {
            repository.deletePromotion(promotion)
        }
    }

    fun updateSettings(settings: StoreSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    // WhatsApp Helpers
    fun getProductWhatsAppUrl(product: ProductEntity): String {
        val phone = storeSettings.value?.whatsappNumber ?: "+966501234567"
        val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
        val message = """
            السلام عليكم، أود الاستفسار عن منتج من معيض موتور:
            - المنتج: ${product.name}
            - السعر: ${String.format("%.0f", product.price)} ر.س
            - الماركة: ${product.brand}
            - التوافق: ${product.bikeTypes}
            هل هو متوفر للتسليم الفوري؟
        """.trimIndent()
        val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        return "https://wa.me/$cleanPhone?text=$encodedMessage"
    }

    fun getGeneralWhatsAppUrl(): String {
        val phone = storeSettings.value?.whatsappNumber ?: "+966501234567"
        val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
        val message = "السلام عليكم ورحمة الله، أود الاستفسار عن الدبابات والقطع المتوفرة في معيض موتور."
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        return "https://wa.me/$cleanPhone?text=$encoded"
    }
}
