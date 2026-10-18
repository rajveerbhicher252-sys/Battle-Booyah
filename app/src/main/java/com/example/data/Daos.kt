package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class DepositWithUser(
    @Embedded val deposit: DepositEntity,
    val username: String
)

data class WithdrawalWithUser(
    @Embedded val withdrawal: WithdrawalEntity,
    val username: String,
    val userUpiId: String?
)

data class ParticipantWithUser(
    @Embedded val participant: ParticipantEntity,
    val username: String,
    val email: String
)

@Dao
interface BannerDao {
    @Query("SELECT * FROM banners ORDER BY sortOrder ASC")
    fun getAllBanners(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: BannerEntity): Long

    @Delete
    suspend fun deleteBanner(banner: BannerEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM game_categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<GameCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: GameCategoryEntity): Long

    @Delete
    suspend fun deleteCategory(category: GameCategoryEntity)

    @Query("SELECT * FROM game_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): GameCategoryEntity?
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND isAdmin = 0")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE (username = :query OR email = :query) AND isAdmin = 0")
    suspend fun getUserByUsernameOrEmail(query: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUser(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isAdmin = 0")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM admin_settings WHERE id = 1")
    suspend fun getAdminSettings(): AdminSettingsEntity?

    @Query("SELECT * FROM admin_settings WHERE id = 1")
    fun observeAdminSettings(): Flow<AdminSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminSettings(settings: AdminSettingsEntity)
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY createdAt DESC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE status = 'Upcoming' ORDER BY createdAt DESC")
    fun getUpcomingTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE status = 'Upcoming' AND categoryId = :categoryId ORDER BY createdAt DESC")
    fun getTournamentsByCategory(categoryId: Long): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    suspend fun getTournamentById(tournamentId: Long): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity): Long

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Delete
    suspend fun deleteTournament(tournament: TournamentEntity)
}

@Dao
interface ParticipantDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParticipant(participant: ParticipantEntity): Long

    @Query("SELECT * FROM participants WHERE tournamentId = :tournamentId")
    fun getParticipantsForTournament(tournamentId: Long): Flow<List<ParticipantEntity>>

    @Query("""
        SELECT p.*, u.username, u.email 
        FROM participants p 
        JOIN users u ON p.userId = u.id 
        WHERE p.tournamentId = :tournamentId
    """)
    fun getParticipantsWithUsersForTournament(tournamentId: Long): Flow<List<ParticipantWithUser>>

    @Query("""
        SELECT t.* FROM tournaments t
        JOIN participants p ON t.id = p.tournamentId
        WHERE p.userId = :userId AND t.status != 'Completed'
    """)
    fun getJoinedActiveTournaments(userId: Long): Flow<List<TournamentEntity>>

    @Query("""
        SELECT t.* FROM tournaments t
        JOIN participants p ON t.id = p.tournamentId
        WHERE p.userId = :userId AND t.status = 'Completed'
    """)
    fun getJoinedCompletedTournaments(userId: Long): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM participants WHERE userId = :userId AND tournamentId = :tournamentId")
    suspend fun getParticipant(userId: Long, tournamentId: Long): ParticipantEntity?

    @Update
    suspend fun updateParticipant(participant: ParticipantEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactionsForUser(userId: Long): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)
}

@Dao
interface DepositDao {
    @Insert
    suspend fun insertDeposit(deposit: DepositEntity)

    @Query("""
        SELECT d.*, u.username 
        FROM deposits d 
        JOIN users u ON d.userId = u.id 
        ORDER BY d.createdAt DESC
    """)
    fun getAllDepositsWithUsers(): Flow<List<DepositWithUser>>

    @Query("SELECT * FROM deposits WHERE id = :depositId")
    suspend fun getDepositById(depositId: Long): DepositEntity?

    @Update
    suspend fun updateDeposit(deposit: DepositEntity)
}

@Dao
interface WithdrawalDao {
    @Insert
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

    @Query("""
        SELECT w.*, u.username, u.upiId as userUpiId 
        FROM withdrawals w 
        JOIN users u ON w.userId = u.id 
        ORDER BY w.createdAt DESC
    """)
    fun getAllWithdrawalsWithUsers(): Flow<List<WithdrawalWithUser>>

    @Query("SELECT * FROM withdrawals WHERE id = :withdrawalId")
    suspend fun getWithdrawalById(withdrawalId: Long): WithdrawalEntity?

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
}

