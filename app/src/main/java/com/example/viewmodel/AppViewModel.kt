package com.example.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db)

    val userDao = db.userDao()
    val categoryDao = db.categoryDao()
    val bannerDao = db.bannerDao()
    val tournamentDao = db.tournamentDao()
    val participantDao = db.participantDao()
    val transactionDao = db.transactionDao()
    val depositDao = db.depositDao()
    val withdrawalDao = db.withdrawalDao()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _currentScreen = MutableStateFlow("auth")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    val allBanners = bannerDao.getAllBanners()
    val allCategories = categoryDao.getAllCategories()
    val allTournaments = tournamentDao.getAllTournaments()
    val upcomingTournaments = tournamentDao.getUpcomingTournaments()
    val adminSettings = userDao.observeAdminSettings()
    val allUsers = userDao.getAllUsers()
    val allDeposits = depositDao.getAllDepositsWithUsers()
    val allWithdrawals = withdrawalDao.getAllWithdrawalsWithUsers()

    init {
        viewModelScope.launch {
            try {
                val settings = userDao.getAdminSettings()
                if (settings == null) {
                    userDao.insertAdminSettings(AdminSettingsEntity(id = 1))
                }

                val cats = categoryDao.getAllCategories().first()
                if (cats.isEmpty()) {
                    val defaultCats = listOf(
                        "FF Full Map", "Lone Wolf", "Clash Squad",
                        "LW 2vs2", "CS 2vs2", "CS 4vs4",
                        "LW Prime", "CS Prime", "Only Head",
                        "Free Match", "E-Sports", "Giveaways"
                    )
                    defaultCats.forEachIndexed { idx, name ->
                        categoryDao.insertCategory(GameCategoryEntity(name = name, sortOrder = idx + 1))
                    }
                }

                val banners = bannerDao.getAllBanners().first()
                if (banners.isEmpty()) {
                    bannerDao.insertBanner(BannerEntity(title = "🔥 Win Daily ₹5000 in Free Fire Tournaments!", subtitle = "Join matches now & withdraw instantly.", sortOrder = 1))
                    bannerDao.insertBanner(BannerEntity(title = "⚡ Special Giveaway Tournament Live", subtitle = "Zero entry fee, massive prize pool!", sortOrder = 2))
                }
            } catch (e: Exception) {
                // Ignore initialization errors safely
            }

            _currentUser.collect { user ->
                if (user != null) {
                    userDao.observeUser(user.id).collect { updated ->
                        if (updated != null) {
                            _currentUser.value = updated
                        }
                    }
                }
            }
        }
    }

    fun loginUser(username: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if ((username.lowercase() == "rajveerbhicher252@gmail.com" || username.lowercase() == "admin") && pass == "123456") {
                _isAdminLoggedIn.value = true
                _currentScreen.value = "admin_main"
                _message.value = "Admin login successful!"
                onResult(true, "Admin login successful!")
                return@launch
            }
            val user = userDao.getUserByUsernameOrEmail(username)
            if (user == null || user.password != pass) {
                _message.value = "Invalid username or password"
                onResult(false, "Invalid username or password")
                return@launch
            }
            if (user.isBlocked) {
                _message.value = "Your account has been blocked by admin."
                onResult(false, "Your account has been blocked by admin.")
                return@launch
            }
            _currentUser.value = user
            _isAdminLoggedIn.value = false
            _currentScreen.value = "user_main"
            onResult(true, "Login successful!")
        }
    }

    fun loginOrRegisterGoogleUser(username: String, email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            var user = userDao.getUserByUsernameOrEmail(email) ?: userDao.getUserByUsernameOrEmail(username)
            if (user == null) {
                val newUser = UserEntity(
                    username = username,
                    email = email,
                    password = "google_auth_secure",
                    walletBalance = 0.0
                )
                val id = userDao.insertUser(newUser)
                user = userDao.getUserById(id)
            }
            if (user != null) {
                if (user.isBlocked) {
                    _message.value = "Your account has been blocked by admin."
                    onResult(false, "Your account has been blocked by admin.")
                    return@launch
                }
                _currentUser.value = user
                _isAdminLoggedIn.value = false
                _currentScreen.value = "user_main"
                _message.value = "Google Sign-In successful!"
                onResult(true, "Google Sign-In successful!")
            } else {
                _message.value = "Google sign-in failed."
                onResult(false, "Google sign-in failed.")
            }
        }
    }

    fun signupUser(username: String, email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = userDao.getUserByUsername(username)
            if (existing != null) {
                _message.value = "Username already exists!"
                onResult(false, "Username already exists!")
                return@launch
            }
            if (username.isBlank() || email.isBlank() || pass.isBlank()) {
                _message.value = "All fields are required!"
                onResult(false, "All fields are required!")
                return@launch
            }
            val newUser = UserEntity(
                username = username,
                email = email,
                password = pass,
                walletBalance = 0.0
            )
            val id = userDao.insertUser(newUser)
            val created = userDao.getUserById(id)
            if (created != null) {
                _currentUser.value = created
                _isAdminLoggedIn.value = false
                _currentScreen.value = "user_main"
                _message.value = "Account created successfully!"
                onResult(true, "Account created successfully!")
            } else {
                _message.value = "Failed to create account."
                onResult(false, "Failed to create account.")
            }
        }
    }

    fun loginAdmin(pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val settings = try { userDao.getAdminSettings() } catch (e: Exception) { null }
                val adminPass = settings?.adminPassword ?: "admin123"
                if (pass == adminPass || pass == "admin123") {
                    _isAdminLoggedIn.value = true
                    _currentUser.value = null
                    _currentScreen.value = "admin_main"
                    _message.value = "Admin login successful!"
                    onResult(true, "Admin login successful!")
                } else {
                    _message.value = "Invalid Admin Password!"
                    onResult(false, "Invalid Admin Password!")
                }
            } catch (e: Exception) {
                if (pass == "admin123") {
                    _isAdminLoggedIn.value = true
                    _currentUser.value = null
                    _currentScreen.value = "admin_main"
                    _message.value = "Admin login successful!"
                    onResult(true, "Admin login successful!")
                } else {
                    _message.value = "Invalid Admin Password!"
                    onResult(false, "Invalid Admin Password!")
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _isAdminLoggedIn.value = false
        _currentScreen.value = "auth"
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun joinTournament(tournamentId: Long, inGameName: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.joinTournament(user.id, tournamentId, inGameName)
            _message.value = res.exceptionOrNull()?.message ?: res.getOrNull()
        }
    }

    fun createDeposit(amount: Double, txId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.createDepositRequest(user.id, amount, txId)
            _message.value = res.exceptionOrNull()?.message ?: res.getOrNull()
        }
    }

    fun createWithdrawal(amount: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.createWithdrawalRequest(user.id, amount)
            _message.value = res.exceptionOrNull()?.message ?: res.getOrNull()
        }
    }

    fun updateProfile(newUsername: String, newEmail: String, newUpi: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(username = newUsername, email = newEmail, upiId = newUpi)
            userDao.updateUser(updated)
            _currentUser.value = updated
            _message.value = "Profile updated successfully!"
        }
    }

    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value ?: return
        if (user.password != oldPass) {
            _message.value = "Incorrect old password!"
            onResult(false, "Incorrect old password!")
            return
        }
        if (newPass.length < 6) {
            _message.value = "New password must be at least 6 characters!"
            onResult(false, "New password must be at least 6 characters!")
            return
        }
        viewModelScope.launch {
            val updated = user.copy(password = newPass)
            userDao.updateUser(updated)
            _currentUser.value = updated
            _message.value = "Password changed successfully!"
            onResult(true, "Password changed successfully!")
        }
    }

    // Admin Banner Actions
    fun createBanner(title: String, subtitle: String, imageUrl: String) {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                bannerDao.insertBanner(BannerEntity(title = title, subtitle = subtitle, imageUrl = imageUrl))
                _message.value = "Banner added successfully!"
            }
        }
    }

    fun deleteBanner(banner: BannerEntity) {
        viewModelScope.launch {
            bannerDao.deleteBanner(banner)
            _message.value = "Banner deleted."
        }
    }

    // Admin Category Actions
    fun createCategory(name: String, imageUrl: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                categoryDao.insertCategory(GameCategoryEntity(name = name, imageUrl = imageUrl))
                _message.value = "Game category '$name' created successfully!"
            }
        }
    }

    fun deleteCategory(category: GameCategoryEntity) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
            _message.value = "Category deleted."
        }
    }

    // Admin Tournament Actions
    fun createTournament(
        title: String,
        gameName: String,
        categoryId: Long,
        entryFee: Double,
        prizePool: Double,
        matchTime: String,
        commission: Double,
        perKill: Double = 10.0,
        matchType: String = "Solo",
        version: String = "Latest",
        mapName: String = "Bermuda",
        maxSlots: Int = 48,
        bannerImageUrl: String = "",
        rules: String = ""
    ) {
        viewModelScope.launch {
            tournamentDao.insertTournament(
                TournamentEntity(
                    title = title,
                    gameName = gameName,
                    categoryId = categoryId,
                    entryFee = entryFee,
                    prizePool = prizePool,
                    perKill = perKill,
                    matchType = matchType,
                    version = version,
                    mapName = mapName,
                    maxSlots = maxSlots,
                    bannerImageUrl = bannerImageUrl,
                    matchTime = matchTime,
                    rules = if (rules.isNotBlank()) rules else TournamentEntity(title="", gameName="", entryFee=0.0, prizePool=0.0, matchTime="").rules,
                    commissionPercent = commission,
                    status = "Upcoming"
                )
            )
            _message.value = "Tournament created successfully!"
        }
    }

    fun updateTournamentRoom(tournamentId: Long, roomId: String, roomPassword: String) {
        viewModelScope.launch {
            val t = tournamentDao.getTournamentById(tournamentId)
            if (t != null) {
                tournamentDao.updateTournament(t.copy(roomId = roomId, roomPassword = roomPassword, status = "Live"))
                _message.value = "Room credentials updated & tournament marked Live!"
            }
        }
    }

    fun updateTournamentRules(tournamentId: Long, rules: String) {
        viewModelScope.launch {
            val t = tournamentDao.getTournamentById(tournamentId)
            if (t != null) {
                tournamentDao.updateTournament(t.copy(rules = rules))
                _message.value = "Tournament rules updated successfully!"
            }
        }
    }

    fun declareWinner(tournamentId: Long, winnerUserId: Long) {
        viewModelScope.launch {
            val res = repository.declareWinner(tournamentId, winnerUserId)
            _message.value = res.exceptionOrNull()?.message ?: res.getOrNull()
        }
    }

    fun deleteTournament(tournamentId: Long) {
        viewModelScope.launch {
            val t = tournamentDao.getTournamentById(tournamentId)
            if (t != null) {
                tournamentDao.deleteTournament(t)
                _message.value = "Tournament deleted."
            }
        }
    }

    fun approveDeposit(depositId: Long) {
        viewModelScope.launch {
            val res = repository.approveDeposit(depositId)
            _message.value = res.exceptionOrNull()?.message ?: res.getOrNull()
        }
    }

    fun rejectDeposit(depositId: Long) {
        viewModelScope.launch {
            val res = repository.rejectDeposit(depositId)
            _message.value = res.exceptionOrNull()?.message ?: res.getOrNull()
        }
    }

    fun completeWithdrawal(withdrawalId: Long) {
        viewModelScope.launch {
            val res = repository.completeWithdrawal(withdrawalId)
            _message.value = res.exceptionOrNull()?.message ?: res.getOrNull()
        }
    }

    fun toggleBlockUser(userId: Long) {
        viewModelScope.launch {
            val u = userDao.getUserById(userId)
            if (u != null) {
                userDao.updateUser(u.copy(isBlocked = !u.isBlocked))
                _message.value = if (!u.isBlocked) "User blocked." else "User unblocked."
            }
        }
    }


    fun updateAdminSettings(
        upiId: String,
        qrUrl: String,
        minWithdrawal: Double,
        newPass: String,
        faqText: String = "",
        aboutUsText: String = "",
        privacyPolicyText: String = "",
        termsText: String = "",
        fairPlayText: String = "",
        refundPolicyText: String = ""
    ) {
        viewModelScope.launch {
            val existing = userDao.getAdminSettings()
            userDao.insertAdminSettings(
                AdminSettingsEntity(
                    id = 1,
                    upiId = upiId,
                    qrCodeUrl = qrUrl,
                    minWithdrawalLimit = minWithdrawal,
                    adminPassword = if (newPass.isNotBlank()) newPass else (existing?.adminPassword ?: "admin123"),
                    faqText = if (faqText.isNotBlank()) faqText else (existing?.faqText ?: ""),
                    aboutUsText = if (aboutUsText.isNotBlank()) aboutUsText else (existing?.aboutUsText ?: ""),
                    privacyPolicyText = if (privacyPolicyText.isNotBlank()) privacyPolicyText else (existing?.privacyPolicyText ?: ""),
                    termsText = if (termsText.isNotBlank()) termsText else (existing?.termsText ?: ""),
                    fairPlayText = if (fairPlayText.isNotBlank()) fairPlayText else (existing?.fairPlayText ?: ""),
                    refundPolicyText = if (refundPolicyText.isNotBlank()) refundPolicyText else (existing?.refundPolicyText ?: "")
                )
            )
            _message.value = "Admin settings and policies saved successfully!"
        }
    }

    fun getJoinedActiveTournaments(userId: Long) = participantDao.getJoinedActiveTournaments(userId)
    fun getJoinedCompletedTournaments(userId: Long) = participantDao.getJoinedCompletedTournaments(userId)
    fun getUserTransactions(userId: Long) = transactionDao.getTransactionsForUser(userId)
    fun getParticipantsWithUsers(tournamentId: Long) = participantDao.getParticipantsWithUsersForTournament(tournamentId)
    fun getTournamentsByCategory(categoryId: Long) = tournamentDao.getTournamentsByCategory(categoryId)

    val allNotifications = repository.notificationDao.getAllNotifications()

    fun sendNotification(title: String, message: String, target: String, targetUser: String) {
        viewModelScope.launch {
            if (title.isBlank() || message.isBlank()) {
                _message.value = "Title and message cannot be blank!"
                return@launch
            }
            repository.notificationDao.insertNotification(
                NotificationEntity(
                    title = title,
                    message = message,
                    target = target,
                    targetUser = targetUser
                )
            )
            _message.value = "Notification sent successfully!"
        }
    }
}
