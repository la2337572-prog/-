package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        PromotionEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        PartInquiryEntity::class,
        StoreSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun promotionDao(): PromotionDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun partInquiryDao(): PartInquiryDao
    abstract fun storeSettingsDao(): StoreSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "maayed_motor.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed database on creation
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedInitialData(database)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val categories = listOf(
                CategoryEntity("helmets", "خوذات ودروع", "Helmets", "sports_motorsports", true),
                CategoryEntity("performance", "قطع الأداء والتعديل", "Performance", "speed", true),
                CategoryEntity("protection", "ملابس وحماية الركاب", "Protection", "shield", true),
                CategoryEntity("accessories", "إكسسوارات وحوامل", "Accessories", "build", true),
                CategoryEntity("touring", "حقائب ومستلزمات رحلات", "Touring", "luggage", true),
                CategoryEntity("maintenance", "زيوت وصيانة سريعة", "Maintenance", "handyman", true)
            )
            database.categoryDao().insertCategories(categories)

            val products = listOf(
                ProductEntity(
                    id = "arai-tour-x4",
                    name = "خوذة Arai Tour-X4 ادفنشر",
                    nameEn = "Arai Tour-X4 Adventure Helmet",
                    description = "خوذة مغامرات احترافية يابانية الصنع بتهوية ثلاثية ممتازة، قناع مقاوم للخدش، وطبقة داخلية قابلة للفك والغسيل معتمدة لمعايير السلامة ECE/DOT.",
                    price = 1890.0,
                    compareAtPrice = 2190.0,
                    categoryId = "helmets",
                    brand = "Arai",
                    bikeTypes = "أوف رود,كروزر,مغامرات",
                    stock = 8,
                    badge = "عرض حصري",
                    image = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=900&q=85",
                    featured = true
                ),
                ProductEntity(
                    id = "akrapovic-slip-on",
                    name = "عادم Akrapovič Slip-On تيتانيوم",
                    nameEn = "Akrapovič Titanium Slip-On Exhaust",
                    description = "عادم خفيف الوزن مصنع من التيتانيوم المقاوم للحرارة العالية يمنح الدراجة نغمة رياضية هادرة وزيادة مؤكدة في عزم الدوران وقوة الأحصنة.",
                    price = 3250.0,
                    compareAtPrice = 3600.0,
                    categoryId = "performance",
                    brand = "Akrapovič",
                    bikeTypes = "سبورت,نيكد",
                    stock = 4,
                    badge = "الأكثر طلبًا",
                    image = "https://images.unsplash.com/photo-1609630875171-b1321377ee65?w=900&q=85",
                    featured = true
                ),
                ProductEntity(
                    id = "alpinestars-gloves",
                    name = "قفازات Alpinestars GP Pro كربون",
                    nameEn = "Alpinestars GP Pro Carbon Gloves",
                    description = "قفازات سباق احترافية من جلد الماعز الطبيعي مدعمة بدروع كربون فايبر للمفاصل وحماية جانبية للمعصم لتوفير أقصى درجات الأمان في المنعطفات السريعة.",
                    price = 395.0,
                    compareAtPrice = 470.0,
                    categoryId = "protection",
                    brand = "Alpinestars",
                    bikeTypes = "سبورت,كروزر,سكوتر",
                    stock = 21,
                    badge = "جديد",
                    image = "https://images.unsplash.com/photo-1558980664-10ea1d4a1d5c?w=900&q=85",
                    featured = true
                ),
                ProductEntity(
                    id = "quadlock-pro",
                    name = "حامل جوال Quad Lock Pro مانع اهتزاز",
                    nameEn = "Quad Lock Pro Phone Mount Kit",
                    description = "طقم تثبيت الجوال المتين للدراجات النارية مزود بمثبط اهتزاز لحماية حساسات كاميرا الهاتف من ترددات المحرك والطرق الوعرة، مع رأس قفل بزاوية 360 درجة.",
                    price = 289.0,
                    compareAtPrice = 330.0,
                    categoryId = "accessories",
                    brand = "Quad Lock",
                    bikeTypes = "سبورت,كروزر,سكوتر,أوف رود",
                    stock = 16,
                    badge = "ملحق أساسي",
                    image = "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?w=900&q=85",
                    featured = false
                ),
                ProductEntity(
                    id = "givi-top-case",
                    name = "صندوق خلفي GIVI Trekker 47L ألمنيوم",
                    nameEn = "GIVI Trekker 47L Aluminum Case",
                    description = "صندوق رحلات وسفر علوي بسعة 47 لتر يتسع لخوذتين كاملتين، مصنوع من صفائح الألمنيوم القوية مع عازل ضد الماء والغبار وأقفال أمان Monokey.",
                    price = 799.0,
                    compareAtPrice = 899.0,
                    categoryId = "touring",
                    brand = "GIVI",
                    bikeTypes = "كروزر,أوف رود,تورينج",
                    stock = 11,
                    badge = "الأكثر طلبًا",
                    image = "https://images.unsplash.com/photo-1558981359-219d6364c9c8?w=900&q=85",
                    featured = true
                ),
                ProductEntity(
                    id = "motul-7100",
                    name = "زيت محرك Motul 7100 10W-40 تخليقي 1L",
                    nameEn = "Motul 7100 4T 10W-40 Synthetic 1L",
                    description = "زيت تخليقي 100% بتقنية Ester مخصص للدراجات عالية الأداء يوفر حماية فائقة لعلبة التروس ويوفر استجابة سريعة للكلاتش وفق مواصفات JASO MA2.",
                    price = 68.0,
                    compareAtPrice = 80.0,
                    categoryId = "maintenance",
                    brand = "Motul",
                    bikeTypes = "سبورت,كروزر,سكوتر,أوف رود",
                    stock = 45,
                    badge = "صيانة معتمدة",
                    image = "https://images.unsplash.com/photo-1558981285-6f0c94958bb6?w=900&q=85",
                    featured = false
                ),
                ProductEntity(
                    id = "dainese-jacket",
                    name = "جاكيت دراج Dainese Racing 4 جلدي",
                    nameEn = "Dainese Racing 4 Leather Jacket",
                    description = "جاكيت جلد التوتو الفاخر مع حشوات ألمنيوم على الأكتاف، وفتحات تهوية جانبية، وجيب مخصص لحامي الظهر G1 و G2، مثالي للسرعات العالية وأجواء الصيف والربيع.",
                    price = 2290.0,
                    compareAtPrice = 2550.0,
                    categoryId = "protection",
                    brand = "Dainese",
                    bikeTypes = "سبورت,نيكد",
                    stock = 7,
                    badge = "فخامة وأمان",
                    image = "https://images.unsplash.com/photo-1558980394-0a06c4631733?w=900&q=85",
                    featured = true
                ),
                ProductEntity(
                    id = "brembo-pads",
                    name = "فحمات فرامل Brembo Sintered Sport",
                    nameEn = "Brembo Sintered Brake Pads",
                    description = "فحمات فرامل رياضية متقدمة مصنوعة من مركبات متكلسة تضمن قوة كبح فورية واستقرار حراري عالي بدون تآكل سريع لأقراص الفرامل.",
                    price = 240.0,
                    compareAtPrice = 280.0,
                    categoryId = "performance",
                    brand = "Brembo",
                    bikeTypes = "سبورت,مغامرات",
                    stock = 14,
                    badge = "أداء رياضي",
                    image = "https://images.unsplash.com/photo-1609630875171-b1321377ee65?w=900&q=85",
                    featured = false
                )
            )
            database.productDao().insertProducts(products)

            val promotions = listOf(
                PromotionEntity(
                    id = "promo-summer-ride",
                    title = "جهّز دراجتك لموسم الانطلاق",
                    description = "خصومات تصل إلى 20% على الخوذ وملابس الحماية وقطع الصيانة الدورية لفترة محدودة!",
                    discountLabel = "خصم حتى 20%",
                    publishedAt = "2026-09-08T12:00:00.000Z",
                    image = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=1200&q=85",
                    categoryId = "helmets",
                    active = true
                ),
                PromotionEntity(
                    id = "promo-touring-gear",
                    title = "وصل حديثًا: أطقم ومعدات الرحلات",
                    description = "أحدث صناديق الألمنيوم وحوامل الأمتعة من GIVI متوفرة الآن مع شحن مجاني لكافة مدن المملكة.",
                    discountLabel = "جديد وحصري",
                    publishedAt = "2026-09-06T10:00:00.000Z",
                    image = "https://images.unsplash.com/photo-1558981359-219d6364c9c8?w=1200&q=85",
                    categoryId = "touring",
                    active = true
                ),
                PromotionEntity(
                    id = "promo-maintenance",
                    title = "باقة الصيانة وتغيير الزيت",
                    description = "اشترِ 4 عبوات زيت موتل Motul 7100 واحصل على فلتر زيت أصلي مجاناً!",
                    discountLabel = "عرض باقة",
                    publishedAt = "2026-09-04T08:00:00.000Z",
                    image = "https://images.unsplash.com/photo-1558981285-6f0c94958bb6?w=1200&q=85",
                    categoryId = "maintenance",
                    active = true
                )
            )
            database.promotionDao().insertPromotions(promotions)

            val initialSettings = StoreSettingsEntity(
                id = 1,
                storeName = "معيض موتور",
                storeNameEn = "MAAYED MOTOR",
                whatsappNumber = "+966501234567",
                phone = "+966112345678",
                instagram = "maayed_motor",
                tiktok = "maayed_motor",
                telegram = "maayed_motor",
                city = "الرياض",
                address = "طريق خريص - معارض الدبابات، الرياض",
                workingHours = "السبت - الخميس: 9:00 ص - 10:30 م"
            )
            database.storeSettingsDao().insertSettings(initialSettings)
        }
    }
}
