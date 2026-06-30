package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.api.GeminiClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Analyzing : ScanUiState
    data class Success(val result: ScanHistory) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

enum class SkincareScreen {
    LOGIN,
    DASHBOARD,
    SCANNER,
    MIRROR,
    PRODUCTS,
    FORUM,
    SMARTWATCH
}

enum class WeatherPreset(
    val title: String,
    val icon: String,
    val tempCelsius: Int,
    val humidityPercent: Int,
    val uvIndex: Int,
    val recommendationText: String
) {
    HOT_HUMID(
        title = "Hot & Humid",
        icon = "☀️💧",
        tempCelsius = 32,
        humidityPercent = 85,
        uvIndex = 9,
        recommendationText = "High humidity increases sebum. Use lightweight, oil-free gel moisturizers and apply a strong broad-spectrum SPF 50 sunscreen. Focus on Salicylic Acid cleansers in the evening."
    ),
    COLD_DRY(
        title = "Cold & Dry",
        icon = "❄️🍂",
        tempCelsius = 5,
        humidityPercent = 25,
        uvIndex = 2,
        recommendationText = "Cold, dry air depletes skin hydration. Avoid foaming cleansers. Layer a rich Hyaluronic Acid serum under a ceramides repair cream. Use a gentle sunscreen."
    ),
    SUNNY_MILD(
        title = "Sunny & Balanced",
        icon = "☀️🍃",
        tempCelsius = 22,
        humidityPercent = 50,
        uvIndex = 7,
        recommendationText = "Optimal day! Shield skin from high UV with Niacinamide and Vitamin C serums. Lock moisture in with a standard hydrating gel-cream and SPF 46."
    ),
    POLLUTED_CLOUDY(
        title = "Humid & Polluted",
        icon = "☁️🌫️",
        tempCelsius = 19,
        humidityPercent = 75,
        uvIndex = 3,
        recommendationText = "High ambient particulate matter. Double cleanse in the evening to clear microscopic soot. Use antioxidant-rich Vitamin C to counter oxidative stresses."
    )
}

class SkincareViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = SkincareRepository(database.skincareDao())

    // UI States
    var currentScreen by mutableStateOf(SkincareScreen.LOGIN)
    var isDarkMode by mutableStateOf(true)

    // User Profile
    val userProfile: StateFlow<UserProfile?> = repository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Scan History
    val scanHistory: StateFlow<List<ScanHistory>> = repository.scanHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Products
    val allProducts: StateFlow<List<Product>> = repository.allProductsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Forum Posts
    val forumPosts: StateFlow<List<ForumPost>> = repository.forumPostsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Hydration tracking for Today
    private val todayDateStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todayHydration: StateFlow<HydrationLog?> = repository.getHydrationLogFlow(todayDateStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Fitness tracker for Today
    val todayFitnessSync: StateFlow<FitnessSync?> = repository.getFitnessSyncFlow(todayDateStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Weather Preset
    var activeWeather by mutableStateOf(WeatherPreset.SUNNY_MILD)

    // Filter/Sort constraints
    var searchQuery by mutableStateOf("")
    var selectedCategoryFilter by mutableStateOf("All")
    var selectedIngredientFilter by mutableStateOf("All")
    var selectedSkinTypeFilter by mutableStateOf("All")
    var sortByOption by mutableStateOf("Rating (High to Low)") // "Price Low to High", "Price High to Low", "Rating (High to Low)"

    // Scanning State
    var scanUiState by mutableStateOf<ScanUiState>(ScanUiState.Idle)

    // Mirror split-screen image paths
    var mirrorImageBefore by mutableStateOf<String?>(null)
    var mirrorImageAfter by mutableStateOf<String?>(null)

    // Push notification lists (Mock in-app logs)
    val appNotifications = mutableStateListOf<String>()

    init {
        // Initialize notifications
        addAppNotification("Welcome to GlowSkin AI! Complete your first skin scan to unlock customized regimens.")
        
        // Ensure hydration log for today exists
        viewModelScope.launch {
            val existing = repository.getHydrationLog(todayDateStr)
            if (existing == null) {
                repository.insertHydrationLog(HydrationLog(todayDateStr, 0, 2000))
            }
            val existingFit = repository.getFitnessSync(todayDateStr)
            if (existingFit == null) {
                // Initial empty watch sync
                repository.insertFitnessSync(FitnessSync(todayDateStr, 0, 0.0, "Average", 0))
            }
        }
    }

    fun addAppNotification(message: String) {
        appNotifications.add(0, "[${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}] $message")
    }

    // Login/Signup Action
    fun login(emailOrPhone: String, name: String) {
        viewModelScope.launch {
            val user = UserProfile(
                id = 1,
                emailOrPhone = emailOrPhone,
                name = name,
                skinType = "Combination",
                primaryConcerns = "Dehydration, Dullness",
                isLoggedIn = true
            )
            repository.saveUserProfile(user)
            addAppNotification("Successfully logged in as ${user.name} via ${user.emailOrPhone}!")
            currentScreen = SkincareScreen.DASHBOARD
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            currentScreen = SkincareScreen.LOGIN
            addAppNotification("Logged out successfully.")
        }
    }

    fun updateUserProfile(name: String, skinType: String, primaryConcerns: String, age: Int) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile(id = 1, emailOrPhone = "user@example.com", name = name)
            repository.saveUserProfile(
                current.copy(
                    name = name,
                    skinType = skinType,
                    primaryConcerns = primaryConcerns,
                    age = age
                )
            )
            addAppNotification("Skincare profile updated! Regimens updated successfully.")
        }
    }

    // AI Analysis trigger
    fun triggerSkinScan(customInputConcern: String, mockImagePath: String) {
        scanUiState = ScanUiState.Analyzing
        viewModelScope.launch {
            try {
                val profile = userProfile.value
                val age = profile?.age ?: 25
                val skinType = profile?.skinType ?: "Combination"

                // Request Gemini skincare analysis
                val resultJsonStr = GeminiClient.analyzeSkin(
                    age = age,
                    selectedSkinType = skinType,
                    concerns = customInputConcern,
                    base64Image = null // passing null triggers the ultra-realistic dynamic mock fallback if API key is empty
                )

                val jsonObj = org.json.JSONObject(resultJsonStr)
                val healthScore = jsonObj.optInt("overallHealthScore", 75)
                val acneScore = jsonObj.optInt("acneScore", 20)
                val wrinkleScore = jsonObj.optInt("wrinkleScore", 15)
                val textureScore = jsonObj.optInt("textureScore", 25)
                val dryScore = jsonObj.optInt("dryScore", 30)
                val reasoning = jsonObj.optString("detailedReasoning", "Recommended regimen built around your profile.")
                val morningRoutine = jsonObj.optString("recommendedMorningRoutine", "")
                val eveningRoutine = jsonObj.optString("recommendedEveningRoutine", "")

                val scanResult = ScanHistory(
                    overallHealthScore = healthScore,
                    acneScore = acneScore,
                    wrinkleScore = wrinkleScore,
                    textureScore = textureScore,
                    dryScore = dryScore,
                    recommendedRoutine = "Morning: $morningRoutine\n\nEvening: $eveningRoutine",
                    reasoning = reasoning,
                    localImagePath = mockImagePath
                )

                repository.insertScanHistory(scanResult)
                scanUiState = ScanUiState.Success(scanResult)
                addAppNotification("AI Skin Analysis completed! Skin Health Score: $healthScore%.")
                
                // Add a smart push notification reminder
                addAppNotification("Reminder: Your daily evening skincare routine starts in 3 hours. Don't forget your recommended Retinol!")

                // Also update the before/after mirror comparison automatically
                if (mirrorImageBefore == null) {
                    mirrorImageBefore = mockImagePath
                } else {
                    mirrorImageAfter = mockImagePath
                }

            } catch (e: Exception) {
                scanUiState = ScanUiState.Error("Failed to parse analysis: ${e.message}")
            }
        }
    }

    fun resetScanState() {
        scanUiState = ScanUiState.Idle
    }

    // Hydration Track
    fun logWater(ml: Int) {
        viewModelScope.launch {
            val currentLog = repository.getHydrationLog(todayDateStr) ?: HydrationLog(todayDateStr, 0)
            val updatedAmount = (currentLog.amountMl + ml).coerceAtLeast(0)
            repository.insertHydrationLog(currentLog.copy(amountMl = updatedAmount))
            
            if (updatedAmount >= currentLog.goalMl && currentLog.amountMl < currentLog.goalMl) {
                addAppNotification("Hydration Goal Achieved! 💧 2,000ml consumed. Your skin barrier is highly hydrated!")
            } else {
                addAppNotification("Logged ${ml}ml of water.")
            }
        }
    }

    // Smartband sync simulation
    fun syncSmartwatchData() {
        viewModelScope.launch {
            // Generate realistic steps, sleep, and active minutes
            val randomSteps = kotlin.random.Random.nextInt(5000, 12001)
            val randomSleep = 6.0 + kotlin.random.Random.nextDouble() * 3.0
            val sleepQuality = if (randomSleep >= 8.0) "Excellent" else if (randomSleep >= 7.0) "Good" else "Average"
            val activeMins = kotlin.random.Random.nextInt(15, 91)

            val sync = FitnessSync(
                dateStr = todayDateStr,
                steps = randomSteps,
                sleepHours = String.format(Locale.US, "%.1f", randomSleep).toDouble(),
                sleepQuality = sleepQuality,
                activeMinutes = activeMins,
                lastSynced = System.currentTimeMillis()
            )

            repository.insertFitnessSync(sync)
            addAppNotification("Synced with Smartband! Steps: $randomSteps, Sleep: $randomSleep hrs ($sleepQuality).")
            
            // Correlate with skin health advice!
            if (randomSleep >= 8.0 && randomSteps >= 8000) {
                addAppNotification("Fitness Sync advice: Excellent sleep and high physical activity boosts night skin regeneration. Your natural collagen synthesis is optimal today!")
            } else if (randomSleep < 7.0) {
                addAppNotification("Fitness Sync advice: Low sleep detected. Elevated cortisol can spike skin redness and sebum. Ensure you apply the soothing Niacinamide serum tonight.")
            }
        }
    }

    // Forum post interactions
    fun submitForumPost(content: String, category: String, rating: Int) {
        viewModelScope.launch {
            val profile = userProfile.value
            val authorName = profile?.name ?: "GlowSkin Member"
            val newPost = ForumPost(
                author = authorName,
                avatarColorIndex = (0..4).random(),
                content = content,
                category = category,
                rating = rating,
                likes = 0
            )
            repository.insertForumPost(newPost)
            addAppNotification("Your review/tip has been shared to the GlowSkin Community Forum!")
        }
    }

    fun toggleLikeForumPost(post: ForumPost) {
        viewModelScope.launch {
            val updated = post.copy(
                likes = if (post.likedByMe) post.likes - 1 else post.likes + 1,
                likedByMe = !post.likedByMe
            )
            repository.updateForumPost(updated)
        }
    }

    // Suggestions for morning / evening routine based on weather
    fun getWeatherRecommendations(): List<String> {
        return when (activeWeather) {
            WeatherPreset.HOT_HUMID -> listOf(
                "Cleanser: Salicylic Acid 2% Cleanser to wash excess sebum",
                "Moisturizer: Lightweight Hydro Boost Water Gel (oil-free)",
                "Protection: UV Clear Broad-Spectrum SPF 46"
            )
            WeatherPreset.COLD_DRY -> listOf(
                "Cleanser: Hydrating Facial Cleanser (non-foaming)",
                "Hydrator: Niacinamide & Hyaluronic Acid serum layers",
                "Barrier: Double Repair Cream (Ceramides enriched)"
            )
            WeatherPreset.SUNNY_MILD -> listOf(
                "Cleanser: Hydrating Facial Cleanser",
                "Brightener: Vitamin C Day Serum",
                "Moisturizer: Hydro Boost Water Gel",
                "Protection: UV Clear SPF 46"
            )
            WeatherPreset.POLLUTED_CLOUDY -> listOf(
                "Cleanser: Hydrating Facial Cleanser (double-cleanse at night)",
                "Serum: Vitamin C Day Serum to offset airborne pollutants",
                "Moisturizer: Double Repair Face Moisturizer"
            )
        }
    }

    // Healthy food recommendations for skin health
    fun getSkinFoods(): List<Pair<String, String>> {
        return listOf(
            "Avocados" to "Rich in healthy fats & Vitamin E. Lubricates skin from the inside out and shields cells from oxidative stress.",
            "Walnuts" to "High in Omega-3 fatty acids which reduce inflammation, breakouts, and redness.",
            "Sweet Potatoes" to "Excellent source of Beta-carotene (provitamin A), acting as a natural sun shield and promoting healthy cell turnover.",
            "Green Tea" to "Packed with Catechins that protect skin from aging, reduce acne, and enhance elasticity.",
            "Tomatoes" to "Rich in Lycopene, a powerful carotenoid that guards the skin against UV ray damage."
        )
    }
}

// Extension to allow simple notifications list updating
fun <T> mutableStateListOf(): androidx.compose.runtime.snapshots.SnapshotStateList<T> {
    return androidx.compose.runtime.mutableStateListOf()
}
