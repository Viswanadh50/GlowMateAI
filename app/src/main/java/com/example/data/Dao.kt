package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SkincareDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()

    // Scan History
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getScanHistoryFlow(): Flow<List<ScanHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanHistory(scan: ScanHistory)

    // Products
    @Query("SELECT * FROM products")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    // Hydration Logs
    @Query("SELECT * FROM hydration_logs WHERE dateStr = :dateStr")
    fun getHydrationLogFlow(dateStr: String): Flow<HydrationLog?>

    @Query("SELECT * FROM hydration_logs WHERE dateStr = :dateStr")
    suspend fun getHydrationLog(dateStr: String): HydrationLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHydrationLog(log: HydrationLog)

    // Fitness Sync
    @Query("SELECT * FROM fitness_syncs WHERE dateStr = :dateStr")
    fun getFitnessSyncFlow(dateStr: String): Flow<FitnessSync?>

    @Query("SELECT * FROM fitness_syncs WHERE dateStr = :dateStr")
    suspend fun getFitnessSync(dateStr: String): FitnessSync?

    @Query("SELECT * FROM fitness_syncs ORDER BY dateStr DESC")
    fun getAllFitnessSyncsFlow(): Flow<List<FitnessSync>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFitnessSync(sync: FitnessSync)

    // Forum Posts
    @Query("SELECT * FROM forum_posts ORDER BY timestamp DESC")
    fun getForumPostsFlow(): Flow<List<ForumPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumPost(post: ForumPost)

    @Update
    suspend fun updateForumPost(post: ForumPost)
}
