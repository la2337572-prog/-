package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE featured = 1")
    fun getFeaturedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    fun getProductsByCategory(categoryId: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE visible = 1")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countCategories(): Int
}

@Dao
interface PromotionDao {
    @Query("SELECT * FROM promotions WHERE active = 1 ORDER BY publishedAt DESC")
    fun getActivePromotions(): Flow<List<PromotionEntity>>

    @Query("SELECT * FROM promotions ORDER BY publishedAt DESC")
    fun getAllPromotions(): Flow<List<PromotionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotions(promotions: List<PromotionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotion(promotion: PromotionEntity)

    @Update
    suspend fun updatePromotion(promotion: PromotionEntity)

    @Delete
    suspend fun deletePromotion(promotion: PromotionEntity)

    @Query("SELECT COUNT(*) FROM promotions")
    suspend fun countPromotions(): Int
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    suspend fun updateQuantity(productId: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun removeCartItem(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long
}

@Dao
interface PartInquiryDao {
    @Query("SELECT * FROM part_inquiries ORDER BY timestamp DESC")
    fun getAllInquiries(): Flow<List<PartInquiryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: PartInquiryEntity): Long

    @Query("UPDATE part_inquiries SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}

@Dao
interface StoreSettingsDao {
    @Query("SELECT * FROM store_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<StoreSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: StoreSettingsEntity)
}
