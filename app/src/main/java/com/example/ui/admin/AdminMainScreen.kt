package com.example.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.viewmodel.AppViewModel
import com.example.data.BannerEntity
import com.example.data.DepositWithUser
import com.example.data.GameCategoryEntity
import com.example.data.ParticipantWithUser
import com.example.data.TournamentEntity
import com.example.data.UserEntity
import com.example.data.WithdrawalWithUser
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(viewModel: AppViewModel) {
    var adminTab by remember { mutableIntStateOf(0) }
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().systemBarsPadding().imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("BATTLE BOOYAH ADMIN", fontWeight = FontWeight.Bold, color = PrimaryCyan) },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = ErrorRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = DarkSurface, contentColor = PrimaryCyan) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Stats") },
                    selected = adminTab == 0,
                    onClick = { adminTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ViewCarousel, contentDescription = "Banners") },
                    label = { Text("Banners") },
                    selected = adminTab == 1,
                    onClick = { adminTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                    label = { Text("Categories") },
                    selected = adminTab == 2,
                    onClick = { adminTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Tournaments") },
                    label = { Text("Tourneys") },
                    selected = adminTab == 3,
                    onClick = { adminTab = 3 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Payment, contentDescription = "Deposits") },
                    label = { Text("Deposits") },
                    selected = adminTab == 4,
                    onClick = { adminTab = 4 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MoneyOff, contentDescription = "Withdrawals") },
                    label = { Text("Withdraw") },
                    selected = adminTab == 5,
                    onClick = { adminTab = 5 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.People, contentDescription = "Users") },
                    label = { Text("Users") },
                    selected = adminTab == 6,
                    onClick = { adminTab = 6 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = adminTab == 7,
                    onClick = { adminTab = 7 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    label = { Text("Notifs") },
                    selected = adminTab == 8,
                    onClick = { adminTab = 8 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(DarkBackground)) {
            when (adminTab) {
                0 -> AdminDashboardTab(viewModel, onNavigateTab = { adminTab = it })
                1 -> AdminBannersTab(viewModel)
                2 -> AdminCategoriesTab(viewModel)
                3 -> AdminTournamentsTab(viewModel)
                4 -> AdminDepositsTab(viewModel)
                5 -> AdminWithdrawalsTab(viewModel)
                6 -> AdminUsersTab(viewModel)
                7 -> AdminSettingsTab(viewModel)
                8 -> AdminNotificationsTab(viewModel)
            }
        }
    }
}

@Composable
fun AdminDashboardTab(viewModel: AppViewModel, onNavigateTab: (Int) -> Unit) {
    val users by viewModel.allUsers.collectAsState(initial = emptyList())
    val tournaments by viewModel.allTournaments.collectAsState(initial = emptyList())
    val banners by viewModel.allBanners.collectAsState(initial = emptyList())
    
    val totalUsers = users.size
    val totalTournaments = tournaments.size
    val totalBanners = banners.size
    val completedTourneys = tournaments.filter { it.status == "Completed" }
    val prizeDistributed = completedTourneys.sumOf { it.prizePool }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Admin Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(title = "Total Users", value = "$totalUsers", modifier = Modifier.weight(1f), color = PrimaryCyan)
            StatCard(title = "Banners", value = "$totalBanners", modifier = Modifier.weight(1f), color = NeonPurple)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(title = "Tournaments", value = "$totalTournaments", modifier = Modifier.weight(1f), color = AccentGold)
            StatCard(title = "Prize Distributed", value = "₹$prizeDistributed", modifier = Modifier.weight(1f), color = SuccessGreen)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onNavigateTab(3) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Create New Tournament", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AdminBannersTab(viewModel: AppViewModel) {
    val banners by viewModel.allBanners.collectAsState(initial = emptyList())
    var bannerTitle by remember { mutableStateOf("") }
    var bannerSubtitle by remember { mutableStateOf("") }
    var bannerImageUrl by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            bannerImageUrl = uri.toString()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Manage Home Sliding Banners", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Add Sliding Banner", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bannerTitle,
                        onValueChange = { bannerTitle = it },
                        label = { Text("Banner Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bannerSubtitle,
                        onValueChange = { bannerSubtitle = it },
                        label = { Text("Subtitle / Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bannerImageUrl,
                        onValueChange = { bannerImageUrl = it },
                        label = { Text("Image URL (or pick from gallery)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Pick Image from Gallery")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (bannerTitle.isNotBlank()) {
                                viewModel.createBanner(bannerTitle.trim(), bannerSubtitle.trim(), bannerImageUrl.trim())
                                bannerTitle = ""
                                bannerSubtitle = ""
                                bannerImageUrl = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Banner", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Text(text = "Existing Banners (${banners.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        if (banners.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No banners found.", color = TextSecondary)
                }
            }
        } else {
            items(banners) { banner ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = banner.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text(text = banner.subtitle, fontSize = 12.sp, color = TextSecondary)
                            if (banner.imageUrl.isNotBlank()) {
                                Text(text = "Image: ${banner.imageUrl.take(30)}...", fontSize = 10.sp, color = PrimaryCyan)
                            }
                        }
                        IconButton(onClick = { viewModel.deleteBanner(banner) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCategoriesTab(viewModel: AppViewModel) {
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())
    var newCategoryName by remember { mutableStateOf("") }
    var categoryImageUrl by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            categoryImageUrl = uri.toString()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Manage Game Sections / Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Add New Section (e.g. Clash Squad, Giveaways)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category / Section Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = categoryImageUrl,
                        onValueChange = { categoryImageUrl = it },
                        label = { Text("Image URL / PNG (or pick from gallery)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Pick Section Image from Gallery")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                viewModel.createCategory(newCategoryName.trim(), categoryImageUrl.trim())
                                newCategoryName = ""
                                categoryImageUrl = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Section", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Text(text = "Existing Sections (${categories.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        if (categories.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No categories found.", color = TextSecondary)
                }
            }
        } else {
            items(categories) { cat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = cat.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            if (cat.imageUrl.isNotBlank()) {
                                Text(text = "Image: ${cat.imageUrl.take(30)}...", fontSize = 10.sp, color = PrimaryCyan)
                            }
                        }
                        IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTournamentsTab(viewModel: AppViewModel) {
    val tournaments by viewModel.allTournaments.collectAsState(initial = emptyList())
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())

    var showCreateForm by remember { mutableStateOf(false) }
    var selectedTournamentForManage by remember { mutableStateOf<TournamentEntity?>(null) }

    var title by remember { mutableStateOf("") }
    var gameName by remember { mutableStateOf("Free Fire") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var entryFee by remember { mutableStateOf("50") }
    var prizePool by remember { mutableStateOf("1000") }
    var matchTime by remember { mutableStateOf("2026-10-20 08:00 PM") }
    var commission by remember { mutableStateOf("20") }
    var bannerImageUrl by remember { mutableStateOf("") }
    var matchType by remember { mutableStateOf("Duo") }
    var version by remember { mutableStateOf("TPP") }
    var mapName by remember { mutableStateOf("Bermuda") }
    var perKill by remember { mutableStateOf("10") }
    var maxSlots by remember { mutableStateOf("48") }
    var rules by remember { mutableStateOf("1. No Hacking/Emulators\n2. Room ID will be shared 10 mins before match\n3. Take screenshot of victory") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            bannerImageUrl = uri.toString()
        }
    }

    if (selectedTournamentForManage != null) {
        ManageTournamentScreen(viewModel = viewModel, tournament = selectedTournamentForManage!!, onBack = { selectedTournamentForManage = null })
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Tournaments Management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
                Button(
                    onClick = { showCreateForm = !showCreateForm },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                ) {
                    Text(if (showCreateForm) "Cancel" else "+ New Tournament", color = Color.Black)
                }
            }
        }

        if (showCreateForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Create Tournament", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tournament Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = gameName, onValueChange = { gameName = it }, label = { Text("Game Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(modifier = Modifier.height(8.dp))

                        val currentCategory = categories.find { it.id == selectedCategoryId }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { categoryDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = currentCategory?.name ?: "Select Game Section / Category")
                            }
                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategoryId = cat.id
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = bannerImageUrl, onValueChange = { bannerImageUrl = it }, label = { Text("Banner Image URL (or pick PNG from gallery)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Pick Banner PNG from Gallery")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = entryFee, onValueChange = { entryFee = it }, label = { Text("Entry Fee (₹)") }, modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(value = prizePool, onValueChange = { prizePool = it }, label = { Text("Prize Pool (₹)") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = perKill, onValueChange = { perKill = it }, label = { Text("Per Kill Prize (₹)") }, modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(value = maxSlots, onValueChange = { maxSlots = it }, label = { Text("Max Slots") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = matchType, onValueChange = { matchType = it }, label = { Text("Type (Solo/Duo/Squad)") }, modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(value = version, onValueChange = { version = it }, label = { Text("Version (TPP/FPP)") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = mapName, onValueChange = { mapName = it }, label = { Text("Map Name (Bermuda, etc.)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = matchTime, onValueChange = { matchTime = it }, label = { Text("Match Time") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = rules, onValueChange = { rules = it }, label = { Text("Tournament Rules") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = commission, onValueChange = { commission = it }, label = { Text("Commission %") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val fee = entryFee.toDoubleOrNull() ?: 0.0
                                val prize = prizePool.toDoubleOrNull() ?: 0.0
                                val comm = commission.toDoubleOrNull() ?: 20.0
                                val pKill = perKill.toDoubleOrNull() ?: 10.0
                                val slots = maxSlots.toIntOrNull() ?: 48
                                val catId = selectedCategoryId ?: categories.firstOrNull()?.id ?: 1L
                                if (title.isNotBlank()) {
                                    viewModel.createTournament(
                                        title = title,
                                        gameName = gameName,
                                        categoryId = catId,
                                        entryFee = fee,
                                        prizePool = prize,
                                        matchTime = matchTime,
                                        commission = comm,
                                        perKill = pKill,
                                        matchType = matchType,
                                        version = version,
                                        mapName = mapName,
                                        maxSlots = slots,
                                        bannerImageUrl = bannerImageUrl,
                                        rules = rules
                                    )
                                    title = ""
                                    bannerImageUrl = ""
                                    showCreateForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Tournament", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(text = "Existing Tournaments (${tournaments.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        if (tournaments.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No tournaments created yet.", color = TextSecondary)
                }
            }
        } else {
            items(tournaments) { t ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = t.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = t.status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                        }
                        Text(text = "Game: ${t.gameName} | Fee: ₹${t.entryFee} | Prize: ₹${t.prizePool}", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { selectedTournamentForManage = t },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                            ) {
                                Text("Manage / Room / Winner", color = Color.Black, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.deleteTournament(t.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageTournamentScreen(viewModel: AppViewModel, tournament: TournamentEntity, onBack: () -> Unit) {
    val participants by viewModel.getParticipantsWithUsers(tournament.id).collectAsState(initial = emptyList())

    var roomId by remember(tournament) { mutableStateOf(tournament.roomId ?: "") }
    var roomPassword by remember(tournament) { mutableStateOf(tournament.roomPassword ?: "") }
    var rulesText by remember(tournament) { mutableStateOf(tournament.rules) }
    var selectedWinnerUserId by remember { mutableStateOf<Long?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryCyan)
            }
            Text(text = "Manage: ${tournament.title}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Tournament Rules", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = rulesText, onValueChange = { rulesText = it }, label = { Text("Rules") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.updateTournamentRules(tournament.id, rulesText) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Rules", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Room Credentials (Sets Tournament Live)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = roomId, onValueChange = { roomId = it }, label = { Text("Room ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = roomPassword, onValueChange = { roomPassword = it }, label = { Text("Room Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.updateTournamentRoom(tournament.id, roomId, roomPassword) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Room & Go Live", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Participants (${participants.size}) & Free Fire IGNs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                Spacer(modifier = Modifier.height(8.dp))

                if (participants.isEmpty()) {
                    Text(text = "No participants joined yet.", color = TextSecondary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        participants.forEachIndexed { index, p ->
                            Surface(color = DarkSurfaceElevated, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${index + 1}. IGN: ${p.participant.inGameName.ifBlank { p.username }}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("User: ${p.username}", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Declare Winner", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val selectedUser = participants.find { it.participant.userId == selectedWinnerUserId }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = selectedUser?.let { "${it.participant.inGameName} (${it.username})" } ?: "Select Winner from Participants")
                        }
                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            participants.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.participant.inGameName} (${p.username})") },
                                    onClick = {
                                        selectedWinnerUserId = p.participant.userId
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            selectedWinnerUserId?.let { winnerId ->
                                viewModel.declareWinner(tournament.id, winnerId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = tournament.status != "Completed" && selectedWinnerUserId != null
                    ) {
                        Text(text = if (tournament.status == "Completed") "Winner Already Declared" else "Declare Winner & Distribute Prize (₹${tournament.prizePool.toInt()})", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDepositsTab(viewModel: AppViewModel) {
    val deposits by viewModel.allDeposits.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Deposit Requests", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
        Spacer(modifier = Modifier.height(12.dp))

        if (deposits.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "No deposit requests.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(deposits) { dWithUser ->
                    val deposit = dWithUser.deposit
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "User: ${dWithUser.username}", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(text = deposit.status, fontWeight = FontWeight.Bold, color = if (deposit.status == "Completed") SuccessGreen else if (deposit.status == "Rejected") ErrorRed else AccentGold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Amount: ₹${deposit.amount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                            Text(text = "UPI TXN ID: ${deposit.transactionId}", fontSize = 13.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (deposit.status == "Pending") {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.approveDeposit(deposit.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Approve", color = Color.White)
                                    }
                                    Button(
                                        onClick = { viewModel.rejectDeposit(deposit.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reject", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalsTab(viewModel: AppViewModel) {
    val withdrawals by viewModel.allWithdrawals.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Withdrawal Requests", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
        Spacer(modifier = Modifier.height(12.dp))

        if (withdrawals.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "No withdrawal requests.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(withdrawals) { wWithUser ->
                    val w = wWithUser.withdrawal
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "User: ${wWithUser.username}", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(text = w.status, fontWeight = FontWeight.Bold, color = if (w.status == "Completed") SuccessGreen else AccentGold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Amount: ₹${w.amount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                            Text(text = "User UPI ID: ${wWithUser.userUpiId ?: "Not provided"}", fontSize = 13.sp, color = PrimaryCyan)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (w.status == "Pending") {
                                Button(
                                    onClick = { viewModel.completeWithdrawal(w.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Mark Completed (Paid)", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUsersTab(viewModel: AppViewModel) {
    val users by viewModel.allUsers.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Registered Users (${users.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
        Spacer(modifier = Modifier.height(12.dp))

        if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "No users found.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users) { user ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = user.username, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                                Text(text = user.email, fontSize = 12.sp, color = TextSecondary)
                                Text(text = "Balance: ₹${user.walletBalance}", fontSize = 13.sp, color = AccentGold, fontWeight = FontWeight.Bold)
                                if (user.isBlocked) {
                                    Text(text = "BLOCKED", fontSize = 11.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = { viewModel.toggleBlockUser(user.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (user.isBlocked) SuccessGreen else ErrorRed)
                            ) {
                                Text(text = if (user.isBlocked) "Unblock" else "Block", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(viewModel: AppViewModel) {
    val adminSettings by viewModel.adminSettings.collectAsState(initial = null)

    var upiId by remember(adminSettings) { mutableStateOf(adminSettings?.upiId ?: "admin@upi") }
    var qrUrl by remember(adminSettings) { mutableStateOf(adminSettings?.qrCodeUrl ?: "") }
    var minWithdrawal by remember(adminSettings) { mutableStateOf(adminSettings?.minWithdrawalLimit?.toString() ?: "100") }
    var newPassword by remember { mutableStateOf("") }
    
    var faqText by remember(adminSettings) { mutableStateOf(adminSettings?.faqText ?: "") }
    var aboutUsText by remember(adminSettings) { mutableStateOf(adminSettings?.aboutUsText ?: "") }
    var privacyPolicyText by remember(adminSettings) { mutableStateOf(adminSettings?.privacyPolicyText ?: "") }
    var termsText by remember(adminSettings) { mutableStateOf(adminSettings?.termsText ?: "") }
    var fairPlayText by remember(adminSettings) { mutableStateOf(adminSettings?.fairPlayText ?: "") }
    var refundPolicyText by remember(adminSettings) { mutableStateOf(adminSettings?.refundPolicyText ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Admin UPI & Policies / Rules Management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("General Settings", fontWeight = FontWeight.Bold, color = AccentGold, fontSize = 16.sp)
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("Admin UPI ID (For User Deposits)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = qrUrl,
                    onValueChange = { qrUrl = it },
                    label = { Text("QR Code Image URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = minWithdrawal,
                    onValueChange = { minWithdrawal = it },
                    label = { Text("Minimum Withdrawal Limit (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Admin Password (leave blank to keep current)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("App Policies & Rules Content", fontWeight = FontWeight.Bold, color = AccentGold, fontSize = 16.sp)
                
                OutlinedTextField(
                    value = faqText,
                    onValueChange = { faqText = it },
                    label = { Text("FAQ Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = aboutUsText,
                    onValueChange = { aboutUsText = it },
                    label = { Text("About Us Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = privacyPolicyText,
                    onValueChange = { privacyPolicyText = it },
                    label = { Text("Privacy Policy Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = termsText,
                    onValueChange = { termsText = it },
                    label = { Text("Terms & Conditions Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = fairPlayText,
                    onValueChange = { fairPlayText = it },
                    label = { Text("Fair Play Policy Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = refundPolicyText,
                    onValueChange = { refundPolicyText = it },
                    label = { Text("Refund & Reward Policy Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val minW = minWithdrawal.toDoubleOrNull() ?: 100.0
                        viewModel.updateAdminSettings(
                            upiId, qrUrl, minW, newPassword,
                            faqText, aboutUsText, privacyPolicyText, termsText, fairPlayText, refundPolicyText
                        )
                        newPassword = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Save All Settings & Policies", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminNotificationsTab(viewModel: AppViewModel) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("All Users") }
    var targetUser by remember { mutableStateOf("") }

    val notifications by viewModel.allNotifications.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Send Notification", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        placeholder = { Text("Enter notification title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        placeholder = { Text("Enter your message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Text(text = "Send To", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 13.sp)
                    
                    val options = listOf("All Users", "Tournament Players", "Specific User")
                    options.forEach { opt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { target = opt }
                        ) {
                            RadioButton(
                                selected = target == opt,
                                onClick = { target = opt }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = opt, color = TextPrimary)
                        }
                    }

                    if (target == "Specific User") {
                        OutlinedTextField(
                            value = targetUser,
                            onValueChange = { targetUser = it },
                            label = { Text("Username / Player IGN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.sendNotification(title, message, target, targetUser)
                            title = ""
                            message = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Send Notification", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(text = "Sent Notifications History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        items(notifications) { notif ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = notif.title, fontWeight = FontWeight.Bold, color = PrimaryCyan, fontSize = 15.sp)
                        Surface(color = DarkSurfaceElevated, shape = RoundedCornerShape(4.dp)) {
                            Text(text = notif.target, fontSize = 10.sp, color = AccentGold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = notif.message, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

