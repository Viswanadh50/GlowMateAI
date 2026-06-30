package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Singleton profile
    val emailOrPhone: String,
    val name: String,
    val skinType: String = "Normal",
    val primaryConcerns: String = "None",
    val age: Int = 25,
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val overallHealthScore: Int,
    val acneScore: Int,
    val wrinkleScore: Int,
    val textureScore: Int,
    val dryScore: Int,
    val recommendedRoutine: String, // morning and evening summary
    val reasoning: String, // Gemini AI analysis detail reasoning
    val localImagePath: String? = null // Captured or selected image
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val brand: String,
    val category: String, // Cleanser, Toner, Moisturizer, Serum, Sunscreen
    val price: Double,
    val rating: Double,
    val activeIngredient: String, // Salicylic Acid, Retinol, Hyaluronic Acid, Vitamin C, Niacinamide
    val description: String,
    val benefits: String,
    val isMorning: Boolean,
    val isEvening: Boolean,
    val skinTypeCompat: String // All, Dry, Oily, Sensitive, Combination
)

@Entity(tableName = "hydration_logs")
data class HydrationLog(
    @PrimaryKey val dateStr: String, // yyyy-MM-dd
    val amountMl: Int,
    val goalMl: Int = 2000
)

@Entity(tableName = "fitness_syncs")
data class FitnessSync(
    @PrimaryKey val dateStr: String, // yyyy-MM-dd
    val steps: Int,
    val sleepHours: Double,
    val sleepQuality: String, // Excellent, Good, Average, Poor
    val activeMinutes: Int,
    val lastSynced: Long = System.currentTimeMillis()
)

@Entity(tableName = "forum_posts")
data class ForumPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val avatarColorIndex: Int, // to render beautiful custom avatar dynamically
    val content: String,
    val category: String, // Routine Tip, Product Review, Skincare Question
    val rating: Int = 0, // optional product rating
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val likedByMe: Boolean = false
)
