package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        GameCategoryEntity::class,
        BannerEntity::class,
        TournamentEntity::class,
        ParticipantEntity::class,
        TransactionEntity::class,
        WithdrawalEntity::class,
        DepositEntity::class,
        AdminSettingsEntity::class,
        NotificationEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bannerDao(): BannerDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun participantDao(): ParticipantDao
    abstract fun transactionDao(): TransactionDao
    abstract fun depositDao(): DepositDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "battle_booyah_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL("INSERT OR IGNORE INTO game_categories (name, iconName, imageUrl, sortOrder) VALUES ('FF Full Map', 'sports_esports', '', 1)")
                            db.execSQL("INSERT OR IGNORE INTO game_categories (name, iconName, imageUrl, sortOrder) VALUES ('Lone Wolf', 'sports_esports', '', 2)")
                            db.execSQL("INSERT OR IGNORE INTO game_categories (name, iconName, imageUrl, sortOrder) VALUES ('Clash Squad', 'sports_esports', '', 3)")
                            db.execSQL("INSERT OR IGNORE INTO game_categories (name, iconName, imageUrl, sortOrder) VALUES ('LW 2vs2', 'sports_esports', '', 4)")
                            db.execSQL("INSERT OR IGNORE INTO game_categories (name, iconName, imageUrl, sortOrder) VALUES ('CS 2vs2', 'sports_esports', '', 5)")
                            db.execSQL("INSERT OR IGNORE INTO game_categories (name, iconName, imageUrl, sortOrder) VALUES ('CS 4vs4', 'sports_esports', '', 6)")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
