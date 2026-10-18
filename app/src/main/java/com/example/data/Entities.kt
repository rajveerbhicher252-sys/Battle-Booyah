package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val walletBalance: Double = 0.0,
    val upiId: String? = null,
    val isBlocked: Boolean = false,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_categories")
data class GameCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String = "sports_esports",
    val imageUrl: String = "",
    val sortOrder: Int = 0
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String = "Join tournaments & win big prizes",
    val imageUrl: String = "",
    val sortOrder: Int = 0
)

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val gameName: String,
    val categoryId: Long = 1,
    val entryFee: Double,
    val prizePool: Double,
    val perKill: Double = 5.0,
    val matchType: String = "Squad", // Solo, Duo, Squad
    val version: String = "TPP", // TPP, FPP
    val mapName: String = "Bermuda", // Bermuda, Erangel, etc.
    val maxSlots: Int = 48,
    val bannerImageUrl: String = "",
    val matchTime: String,
    val rules: String = "1. No Hacking/Emulators\n2. Room ID will be shared 10 mins before match\n3. Take screenshot of victory",
    val roomId: String? = null,
    val roomPassword: String? = null,
    val status: String = "Upcoming", // "Upcoming", "Live", "Completed"
    val commissionPercent: Double = 20.0,
    val winnerUserId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "participants")
data class ParticipantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val tournamentId: Long,
    val inGameName: String = "",
    val result: String = "Participated" // "Participated", "Winner", "Runner-Up"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val amount: Double,
    val type: String, // "credit", "debit"
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "deposits")
data class DepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val amount: Double,
    val transactionId: String,
    val status: String = "Pending", // "Pending", "Completed", "Rejected"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val amount: Double,
    val status: String = "Pending", // "Pending", "Completed", "Rejected"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_settings")
data class AdminSettingsEntity(
    @PrimaryKey val id: Long = 1,
    val upiId: String = "admin@upi",
    val qrCodeUrl: String = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=upi://pay?pa=admin@upi&pn=BattleBooyah",
    val minWithdrawalLimit: Double = 100.0,
    val adminPassword: String = "admin123",
    val faqText: String = "Q: How to join tournaments?\nA: Select category, choose match, and click Join Now.\n\nQ: When are room credentials shared?\nA: Room ID & Password are shared 10 minutes before match start time.",
    val aboutUsText: String = "Battle Booyah is the ultimate esports tournament platform for gamers to compete, win cash prizes, and showcase their skills.",
    val privacyPolicyText: String = "We respect your privacy and protect your personal information in accordance with industry standards.",
    val termsText: String = "By participating in tournaments, you agree to fair play rules and terms of service. No hacking/emulators allowed.",
    val fairPlayText: String = "Fair play is strictly enforced. Any usage of scripts, hacks, or unauthorized emulators will result in permanent account ban.",
    val refundPolicyText: String = "Entry fees are non-refundable once the tournament starts or fills up. Prizes are credited directly to your wallet."
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val target: String = "All Users", // "All Users", "Tournament Players", "Specific User"
    val targetUser: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

