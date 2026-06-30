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
        UserProfile::class,
        ScanHistory::class,
        Product::class,
        HydrationLog::class,
        FitnessSync::class,
        ForumPost::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun skincareDao(): SkincareDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glowskin_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.skincareDao())
                }
            }
        }

        suspend fun populateDatabase(dao: SkincareDao) {
            // Check if products exist already
            if (dao.getProductCount() == 0) {
                val initialProducts = listOf(
                    Product(
                        name = "Hydrating Facial Cleanser",
                        brand = "CeraVe",
                        category = "Cleanser",
                        price = 15.99,
                        rating = 4.7,
                        activeIngredient = "Hyaluronic Acid",
                        description = "Gentle, non-foaming cleanser that cleanses and hydrates skin without disrupting the protective barrier.",
                        benefits = "Locks in essential skin hydration; supports skin protective barrier.",
                        isMorning = true,
                        isEvening = true,
                        skinTypeCompat = "Dry"
                    ),
                    Product(
                        name = "Salicylic Acid 2% Acne Cleanser",
                        brand = "The Inkey List",
                        category = "Cleanser",
                        price = 11.99,
                        rating = 4.5,
                        activeIngredient = "Salicylic Acid",
                        description = "A foaming cleanser that removes makeup and dirt while penetrating pores to help reduce blackheads and breakouts.",
                        benefits = "Unclogs pores, targets blackheads and whiteheads, reduces oiliness.",
                        isMorning = true,
                        isEvening = true,
                        skinTypeCompat = "Oily"
                    ),
                    Product(
                        name = "Niacinamide 10% + Zinc 1%",
                        brand = "The Ordinary",
                        category = "Serum",
                        price = 8.50,
                        rating = 4.6,
                        activeIngredient = "Niacinamide",
                        description = "High-strength vitamin and mineral blemish formula to regulate sebum production and minimize pores.",
                        benefits = "Reduces skin congestion, controls oil, brightens hyperpigmentation.",
                        isMorning = true,
                        isEvening = true,
                        skinTypeCompat = "All"
                    ),
                    Product(
                        name = "Retinol 0.5% in Squalane",
                        brand = "The Ordinary",
                        category = "Serum",
                        price = 10.99,
                        rating = 4.4,
                        activeIngredient = "Retinol",
                        description = "Water-free solution containing 0.5% pure Retinol to fight general signs of skin aging.",
                        benefits = "Accelerates cell turnover, reduces fine lines, refines skin texture.",
                        isMorning = false,
                        isEvening = true,
                        skinTypeCompat = "Combination"
                    ),
                    Product(
                        name = "C-Firma Fresh Day Serum",
                        brand = "Drunk Elephant",
                        category = "Serum",
                        price = 78.00,
                        rating = 4.3,
                        activeIngredient = "Vitamin C",
                        description = "A powerful 15% vitamin C day serum packed with antioxidants to firm and brighten skin.",
                        benefits = "Neutralizes free radicals, dissolves surface dead skin cells, firms and brightens.",
                        isMorning = true,
                        isEvening = false,
                        skinTypeCompat = "All"
                    ),
                    Product(
                        name = "Hydro Boost Water Gel",
                        brand = "Neutrogena",
                        category = "Moisturizer",
                        price = 19.49,
                        rating = 4.6,
                        activeIngredient = "Hyaluronic Acid",
                        description = "An oil-free, non-comedogenic water gel moisturizer that absorbs instantly and keeps skin hydrated all day.",
                        benefits = "Provides intense long-lasting hydration, lightweight water-gel texture.",
                        isMorning = true,
                        isEvening = true,
                        skinTypeCompat = "Dry"
                    ),
                    Product(
                        name = "UV Clear Broad-Spectrum SPF 46",
                        brand = "EltaMD",
                        category = "Sunscreen",
                        price = 43.00,
                        rating = 4.8,
                        activeIngredient = "Zinc Oxide",
                        description = "Dermatologist-recommended zinc-oxide sunscreen that calms and protects acne-prone, sensitive skin.",
                        benefits = "Protects against UVA/UVB, leaves no white cast, soothes redness and acne flareups.",
                        isMorning = true,
                        isEvening = false,
                        skinTypeCompat = "Sensitive"
                    ),
                    Product(
                        name = "2% BHA Liquid Exfoliant",
                        brand = "Paula's Choice",
                        category = "Toner",
                        price = 34.00,
                        rating = 4.8,
                        activeIngredient = "Salicylic Acid",
                        description = "A gentle fluid leave-on formula with salicylic acid that sweeps away dead skin cells and unclogs pores.",
                        benefits = "Evens skin tone, visibly clears and minimizes pores, smooths texture.",
                        isMorning = false,
                        isEvening = true,
                        skinTypeCompat = "Oily"
                    ),
                    Product(
                        name = "Double Repair Face Moisturizer",
                        brand = "La Roche-Posay",
                        category = "Moisturizer",
                        price = 22.99,
                        rating = 4.7,
                        activeIngredient = "Niacinamide",
                        description = "Formulated with prebiotic thermal water, ceramide-3, and niacinamide to restore healthy skin barrier in 1 hour.",
                        benefits = "Provides 48-hour hydration, replenishes essential lipids, comforts sensitive skin.",
                        isMorning = true,
                        isEvening = true,
                        skinTypeCompat = "Sensitive"
                    )
                )
                dao.insertProducts(initialProducts)
            }

            // Insert a default profile
            dao.insertUserProfile(
                UserProfile(
                    id = 1,
                    emailOrPhone = "user@example.com",
                    name = "Beautiful Glow User",
                    skinType = "Combination",
                    primaryConcerns = "Acne, Mild Redness",
                    age = 26,
                    isLoggedIn = true
                )
            )

            // Add some initial forum posts
            val initialForumPosts = listOf(
                ForumPost(
                    author = "Aria Carter",
                    avatarColorIndex = 0,
                    content = "Obsessed with Neutrogena Hydro Boost Water Gel! Since I switched to using it in the morning, my combination skin doesn't get dry patches around my cheeks anymore and it sits beautifully under makeup.",
                    category = "Product Review",
                    rating = 5,
                    likes = 14,
                    likedByMe = false
                ),
                ForumPost(
                    author = "Marcus Jenkins",
                    avatarColorIndex = 1,
                    content = "Pro-tip for people starting Retinol: Start slow (only 2 times a week at night) and ALWAYS apply a good barrier-repair moisturizer on top. Don't use any physical exfoliants on Retinol days, your skin barrier will thank you!",
                    category = "Routine Tip",
                    rating = 0,
                    likes = 27,
                    likedByMe = false
                ),
                ForumPost(
                    author = "Sophie Lin",
                    avatarColorIndex = 2,
                    content = "Is the EltaMD SPF 46 sunscreen suitable for extremely oily skin, or does it leave a shiny finish? I'm trying to find an everyday sunscreen that doesn't trigger breakouts.",
                    category = "Skincare Question",
                    rating = 0,
                    likes = 8,
                    likedByMe = false
                ),
                ForumPost(
                    author = "Devon Miller",
                    avatarColorIndex = 3,
                    content = "Salicylic acid is literally magic for pore tightening. I've been using Paula's Choice 2% BHA for 3 weeks now, and my blackheads around the nose are practically gone. Highly recommend!",
                    category = "Product Review",
                    rating = 5,
                    likes = 19,
                    likedByMe = false
                )
            )
            for (post in initialForumPosts) {
                dao.insertForumPost(post)
            }
        }
    }
}
