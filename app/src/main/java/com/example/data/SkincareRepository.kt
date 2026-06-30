package com.example.data

import kotlinx.coroutines.flow.Flow

class SkincareRepository(private val dao: SkincareDao) {

    // User Profile
    val userProfileFlow: Flow<UserProfile?> = dao.getUserProfileFlow()
    suspend fun getUserProfile(): UserProfile? = dao.getUserProfile()
    suspend fun saveUserProfile(profile: UserProfile) = dao.insertUserProfile(profile)
    suspend fun logout() = dao.clearUserProfile()

    // Scan History
    val scanHistoryFlow: Flow<List<ScanHistory>> = dao.getScanHistoryFlow()
    suspend fun insertScanHistory(scan: ScanHistory) = dao.insertScanHistory(scan)

    // Products
    val allProductsFlow: Flow<List<Product>> = dao.getAllProductsFlow()
    suspend fun getAllProducts(): List<Product> = dao.getAllProducts()
    suspend fun insertProducts(products: List<Product>) = dao.insertProducts(products)

    // Hydration Logs
    fun getHydrationLogFlow(dateStr: String): Flow<HydrationLog?> = dao.getHydrationLogFlow(dateStr)
    suspend fun getHydrationLog(dateStr: String): HydrationLog? = dao.getHydrationLog(dateStr)
    suspend fun insertHydrationLog(log: HydrationLog) = dao.insertHydrationLog(log)

    // Fitness Sync
    fun getFitnessSyncFlow(dateStr: String): Flow<FitnessSync?> = dao.getFitnessSyncFlow(dateStr)
    suspend fun getFitnessSync(dateStr: String): FitnessSync? = dao.getFitnessSync(dateStr)
    val allFitnessSyncsFlow: Flow<List<FitnessSync>> = dao.getAllFitnessSyncsFlow()
    suspend fun insertFitnessSync(sync: FitnessSync) = dao.insertFitnessSync(sync)

    // Forum Posts
    val forumPostsFlow: Flow<List<ForumPost>> = dao.getForumPostsFlow()
    suspend fun insertForumPost(post: ForumPost) = dao.insertForumPost(post)
    suspend fun updateForumPost(post: ForumPost) = dao.updateForumPost(post)
}
