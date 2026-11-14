package com.example.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private val firestore: FirebaseFirestore? by lazy {
        try {
            val app = getApplication<Application>()
            if (FirebaseApp.getApps(app).isEmpty()) {
                FirebaseApp.initializeApp(app)
            }
            if (FirebaseApp.getApps(app).isNotEmpty()) {
                val dbId = try {
                    app.getString(com.example.R.string.firestore_database_id)
                } catch (_: Exception) {
                    ""
                }
                if (dbId.isNotBlank()) FirebaseFirestore.getInstance(dbId)
                else FirebaseFirestore.getInstance()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(getApplication()).isEmpty()) {
                FirebaseApp.initializeApp(getApplication())
            }
            if (FirebaseApp.getApps(getApplication()).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun DocumentSnapshot.getLongSafe(field: String): Long? =
        try { (get(field) as? Number)?.toLong() } catch (e: Exception) { null }

    private fun DocumentSnapshot.getDoubleSafe(field: String, default: Double = 0.0): Double =
        try { (get(field) as? Number)?.toDouble() ?: default } catch (e: Exception) { default }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCurrentUserUpdates() {
        viewModelScope.launch {
            try {
                _currentUser
                    .map { it?.id }
                    .distinctUntilChanged()
                    .flatMapLatest { id ->
                        if (id == null) flowOf(null) else userDao.observeUser(id)
                    }
                    .collect { updated ->
                        if (updated != null) {
                            _currentUser.value = updated
                        }
                    }
            } catch (e: Exception) {
                // Safely ignore observation errors
            }
        }
    }

    init {
        observeCurrentUserUpdates()
        viewModelScope.launch {
            try {
                val fs = firestore
                // Listen to Firestore tournaments for cross-device real-time sync
                fs?.collection("tournaments")?.addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    viewModelScope.launch {
                        try {
                            for (doc in snapshot.documents) {
                                val id = doc.getLongSafe("id") ?: continue
                                val title = doc.getString("title") ?: ""
                                val gameName = doc.getString("gameName") ?: ""
                                val categoryId = doc.getLongSafe("categoryId") ?: 1L
                                val entryFee = doc.getDoubleSafe("entryFee", 0.0)
                                val prizePool = doc.getDoubleSafe("prizePool", 0.0)
                                val perKill = doc.getDoubleSafe("perKill", 5.0)
                                val matchType = doc.getString("matchType") ?: "Squad"
                                val version = doc.getString("version") ?: "TPP"
                                val mapName = doc.getString("mapName") ?: "Bermuda"
                                val maxSlots = doc.getLongSafe("maxSlots")?.toInt() ?: 48
                                val bannerImageUrl = doc.getString("bannerImageUrl") ?: ""
                                val matchTime = doc.getString("matchTime") ?: ""
                                val rules = doc.getString("rules") ?: ""
                                val status = doc.getString("status") ?: "Upcoming"
                                val commissionPercent = doc.getDoubleSafe("commissionPercent", 20.0)

                                val existing = tournamentDao.getTournamentById(id)
                                if (existing == null) {
                                    tournamentDao.insertTournament(
                                        TournamentEntity(
                                            id = id,
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
                                            rules = rules,
                                            status = status,
                                            commissionPercent = commissionPercent
                                        )
                                    )
                                }
                            }
                        } catch (ex: Exception) {
                            // Safely ignore individual sync failures
                        }
                    }
                }

                // Listen to Firestore users
                fs?.collection("users")?.addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    viewModelScope.launch {
                        try {
                            for (doc in snapshot.documents) {
                                val id = doc.getLongSafe("id") ?: 0L
                                val username = doc.getString("username") ?: ""
                                val email = doc.getString("email") ?: doc.id
                                if (email.isBlank() && username.isBlank()) continue
                                val password = doc.getString("password") ?: ""
                                val walletBalance = doc.getDoubleSafe("walletBalance", 0.0)
                                val upiId = doc.getString("upiId")
                                val isBlocked = doc.getBoolean("isBlocked") ?: false
                                val isAdmin = doc.getBoolean("isAdmin") ?: false
                                val createdAt = doc.getLongSafe("createdAt") ?: System.currentTimeMillis()

                                val existingByEmail = if (email.isNotBlank()) userDao.getUserByUsernameOrEmail(email) else null
                                val existingByName = existingByEmail ?: if (username.isNotBlank()) userDao.getUserByUsername(username) else null
                                val existing = existingByName
                                if (existing == null) {
                                    val idOccupied = if (id > 0L) userDao.getUserById(id) != null else true
                                    userDao.insertUser(
                                        UserEntity(
                                            id = if (idOccupied) 0L else id,
                                            username = username,
                                            email = email,
                                            password = password,
                                            walletBalance = walletBalance,
                                            upiId = upiId,
                                            isBlocked = isBlocked,
                                            isAdmin = isAdmin,
                                            createdAt = createdAt
                                        )
                                    )
                                } else {
                                    if (existing.walletBalance != walletBalance || existing.isBlocked != isBlocked || existing.upiId != upiId || existing.username != username) {
                                        userDao.updateUser(
                                            existing.copy(
                                                username = username.ifBlank { existing.username },
                                                walletBalance = walletBalance,
                                                isBlocked = isBlocked,
                                                upiId = upiId
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            // Safely ignore individual sync failures
                        }
                    }
                }

                // Listen to Firestore deposits
                fs?.collection("deposits")?.addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    viewModelScope.launch {
                        try {
                            for (doc in snapshot.documents) {
                                val id = doc.getLongSafe("id") ?: continue
                                val userId = doc.getLongSafe("userId") ?: continue
                                val amount = doc.getDoubleSafe("amount", 0.0)
                                val transactionId = doc.getString("transactionId") ?: ""
                                val status = doc.getString("status") ?: "Pending"
                                val createdAt = doc.getLongSafe("createdAt") ?: System.currentTimeMillis()

                                val existing = depositDao.getDepositById(id)
                                if (existing == null) {
                                    depositDao.insertDeposit(
                                        DepositEntity(
                                            id = id,
                                            userId = userId,
                                            amount = amount,
                                            transactionId = transactionId,
                                            status = status,
                                            createdAt = createdAt
                                        )
                                    )
                                }
                            }
                        } catch (ex: Exception) {
                            // Safely ignore individual sync failures
                        }
                    }
                }

                // Listen to Firestore withdrawals
                fs?.collection("withdrawals")?.addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    viewModelScope.launch {
                        try {
                            for (doc in snapshot.documents) {
                                val id = doc.getLongSafe("id") ?: continue
                                val userId = doc.getLongSafe("userId") ?: continue
                                val amount = doc.getDoubleSafe("amount", 0.0)
                                val status = doc.getString("status") ?: "Pending"
                                val createdAt = doc.getLongSafe("createdAt") ?: System.currentTimeMillis()

                                val existing = withdrawalDao.getWithdrawalById(id)
                                if (existing == null) {
                                    withdrawalDao.insertWithdrawal(
                                        WithdrawalEntity(
                                            id = id,
                                            userId = userId,
                                            amount = amount,
                                            status = status,
                                            createdAt = createdAt
                                        )
                                    )
                                }
                            }
                        } catch (ex: Exception) {
                            // Safely ignore individual sync failures
                        }
                    }
                }

                // Listen to Firestore participants
                fs?.collection("participants")?.addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    viewModelScope.launch {
                        try {
                            for (doc in snapshot.documents) {
                                val userId = doc.getLongSafe("userId") ?: continue
                                val tournamentId = doc.getLongSafe("tournamentId") ?: continue
                                val inGameName = doc.getString("inGameName") ?: ""
                                val result = doc.getString("result") ?: "Participated"

                                val existing = participantDao.getParticipant(userId, tournamentId)
                                if (existing == null) {
                                    participantDao.insertParticipant(
                                        ParticipantEntity(
                                            userId = userId,
                                            tournamentId = tournamentId,
                                            inGameName = inGameName,
                                            result = result
                                        )
                                    )
                                }
                            }
                        } catch (ex: Exception) {
                            // Safely ignore individual sync failures
                        }
                    }
                }

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
        }
    }

    private fun syncUserToFirestore(user: UserEntity) {
        try {
            val map = hashMapOf(
                "id" to user.id,
                "username" to user.username,
                "email" to user.email,
                "password" to user.password,
                "walletBalance" to user.walletBalance,
                "upiId" to (user.upiId ?: ""),
                "isBlocked" to user.isBlocked,
                "isAdmin" to user.isAdmin,
                "createdAt" to user.createdAt
            )
            firestore?.collection("users")?.document(user.email)?.set(map)
        } catch (e: Exception) {
            // Safely ignore sync failures
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
            if (username.isBlank() || pass.isBlank()) {
                _message.value = "Username and password are required!"
                onResult(false, "Username and password are required!")
                return@launch
            }
            if (username.contains("@")) {
                try {
                    firebaseAuth?.signInWithEmailAndPassword(username, pass)
                        ?.addOnFailureListener {
                            try {
                                firebaseAuth?.createUserWithEmailAndPassword(username, pass)
                            } catch (_: Exception) {}
                        }
                } catch (_: Exception) {}
            }
            var user = userDao.getUserByUsernameOrEmail(username)
            if (user == null) {
                _message.value = "Account not found! Please sign up first."
                onResult(false, "Account not found! Please sign up first.")
                return@launch
            } else if (user.password != pass) {
                _message.value = "Invalid password"
                onResult(false, "Invalid password")
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
            syncUserToFirestore(user)
            _message.value = "Login successful!"
            onResult(true, "Login successful!")
        }
    }

    fun loginOrRegisterGoogleUser(username: String, email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                firebaseAuth?.signInWithEmailAndPassword(email, "google_auth_secure")
                    ?.addOnFailureListener {
                        try {
                            firebaseAuth?.createUserWithEmailAndPassword(email, "google_auth_secure")
                        } catch (_: Exception) {}
                    }
            } catch (_: Exception) {}
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
                syncUserToFirestore(user)
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
            if (username.lowercase() == "rajveerbhicher252@gmail.com" || username.lowercase() == "admin" || email.lowercase() == "rajveerbhicher252@gmail.com") {
                _message.value = "This email or username is reserved for Admin!"
                onResult(false, "This email or username is reserved for Admin!")
                return@launch
            }
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

            try {
                firebaseAuth?.createUserWithEmailAndPassword(email, pass)
                    ?.addOnFailureListener {
                        try {
                            firebaseAuth?.signInWithEmailAndPassword(email, pass)
                        } catch (_: Exception) {}
                    }
            } catch (_: Exception) {}

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
                syncUserToFirestore(created)
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
            if (res.isSuccess) {
                val p = ParticipantEntity(userId = user.id, tournamentId = tournamentId, inGameName = inGameName, result = "Participated")
                val map = hashMapOf(
                    "userId" to p.userId,
                    "tournamentId" to p.tournamentId,
                    "inGameName" to p.inGameName,
                    "result" to p.result
                )
                try {
                    firestore?.collection("participants")?.add(map)
                } catch (_: Exception) {}
                userDao.getUserById(user.id)?.let { syncUserToFirestore(it) }
            }
            _message.value = res.exceptionOrNull()?.message ?: res.getOrNull()
        }
    }

    fun createDeposit(amount: Double, txId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.createDepositRequest(user.id, amount, txId)
            if (res.isSuccess) {
                val deposit = DepositEntity(userId = user.id, amount = amount, transactionId = txId, status = "Pending")
                val map = hashMapOf(
                    "userId" to deposit.userId,
                    "amount" to deposit.amount,
                    "transactionId" to deposit.transactionId,
                    "status" to deposit.status,
                    "createdAt" to deposit.createdAt
                )
                try {
                    firestore?.collection("deposits")?.add(map)
                } catch (_: Exception) {}
            }
            _message.value = res.exceptionOrNull()?.message ?: res.getOrNull()
        }
    }

    fun createWithdrawal(amount: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.createWithdrawalRequest(user.id, amount)
            if (res.isSuccess) {
                val w = WithdrawalEntity(userId = user.id, amount = amount, status = "Pending")
                val map = hashMapOf(
                    "userId" to w.userId,
                    "amount" to w.amount,
                    "status" to w.status,
                    "createdAt" to w.createdAt
                )
                try {
                    firestore?.collection("withdrawals")?.add(map)
                } catch (_: Exception) {}
                userDao.getUserById(user.id)?.let { syncUserToFirestore(it) }
            }
            _message.value = res.exceptionOrNull()?.message ?: res.getOrNull()
        }
    }

    fun updateProfile(newUsername: String, newEmail: String, newUpi: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(username = newUsername, email = newEmail, upiId = newUpi)
            userDao.updateUser(updated)
            _currentUser.value = updated
            syncUserToFirestore(updated)
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
            val tournament = TournamentEntity(
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
            val id = tournamentDao.insertTournament(tournament)
            
            // Sync tournament to Firestore
            val map = hashMapOf(
                "id" to id,
                "title" to tournament.title,
                "gameName" to tournament.gameName,
                "categoryId" to tournament.categoryId,
                "entryFee" to tournament.entryFee,
                "prizePool" to tournament.prizePool,
                "perKill" to tournament.perKill,
                "matchType" to tournament.matchType,
                "version" to tournament.version,
                "mapName" to tournament.mapName,
                "maxSlots" to tournament.maxSlots,
                "bannerImageUrl" to tournament.bannerImageUrl,
                "matchTime" to tournament.matchTime,
                "rules" to tournament.rules,
                "status" to tournament.status,
                "commissionPercent" to tournament.commissionPercent,
                "createdAt" to tournament.createdAt
            )
            try {
                firestore?.collection("tournaments")?.document(id.toString())?.set(map)
            } catch (_: Exception) {}

            // Send notification that tournament registration is open
            val notif = NotificationEntity(
                title = "🎮 Tournament Registration Open!",
                message = "New tournament '$title' is live for registration! Entry Fee: ₹$entryFee, Prize Pool: ₹$prizePool. Join now!",
                target = "All Users"
            )
            repository.notificationDao.insertNotification(notif)
            val notifMap = hashMapOf(
                "title" to notif.title,
                "message" to notif.message,
                "target" to notif.target,
                "targetUser" to notif.targetUser,
                "createdAt" to notif.createdAt
            )
            try {
                firestore?.collection("notifications")?.add(notifMap)
            } catch (_: Exception) {}

            _message.value = "Tournament created & notifications sent successfully!"
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
        refundPolicyText: String = "",
        telegramUrl: String = "",
        whatsappUrl: String = "",
        instagramUrl: String = ""
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
                    refundPolicyText = if (refundPolicyText.isNotBlank()) refundPolicyText else (existing?.refundPolicyText ?: ""),
                    telegramUrl = if (telegramUrl.isNotBlank()) telegramUrl else (existing?.telegramUrl ?: "https://t.me/battlebooyah"),
                    whatsappUrl = if (whatsappUrl.isNotBlank()) whatsappUrl else (existing?.whatsappUrl ?: "https://whatsapp.com"),
                    instagramUrl = if (instagramUrl.isNotBlank()) instagramUrl else (existing?.instagramUrl ?: "https://instagram.com")
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
