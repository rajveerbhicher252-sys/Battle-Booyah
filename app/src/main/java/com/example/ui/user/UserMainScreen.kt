package com.example.ui.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.viewmodel.AppViewModel
import com.example.data.BannerEntity
import com.example.data.GameCategoryEntity
import com.example.data.TournamentEntity
import com.example.data.TransactionEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentUser by viewModel.currentUser.collectAsState()
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
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = PrimaryCyan
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Tournaments") },
                    label = { Text("My Tourneys") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                    label = { Text("Wallet") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(DarkBackground)) {
            when (selectedTab) {
                0 -> HomeScreen(viewModel)
                1 -> MyTournamentsScreen(viewModel)
                2 -> WalletScreen(viewModel)
                3 -> ProfileScreen(viewModel)
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: AppViewModel) {
    val banners by viewModel.allBanners.collectAsState(initial = emptyList())
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())
    val allTournaments by viewModel.upcomingTournaments.collectAsState(initial = emptyList())
    val currentUser by viewModel.currentUser.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState(initial = emptyList())

    var selectedCategory by remember { mutableStateOf<GameCategoryEntity?>(null) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    val categoryTournaments = allTournaments.filter { selectedCategory == null || it.categoryId == selectedCategory?.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "BATTLE BOOYAH", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
                Text(text = "Welcome, ${currentUser?.username ?: "Player"}", fontSize = 13.sp, color = TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showNotificationsDialog = true }) {
                    Box {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryCyan)
                        if (notifications.isNotEmpty()) {
                            Surface(
                                color = ErrorRed,
                                shape = CircleShape,
                                modifier = Modifier.size(8.dp).align(Alignment.TopEnd)
                            ) {}
                        }
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "₹${currentUser?.walletBalance ?: 0.0}", color = AccentGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showNotificationsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                containerColor = DarkSurface,
                title = { Text("Notifications & Alerts", color = PrimaryCyan, fontSize = 16.sp) },
                text = {
                    if (notifications.isEmpty()) {
                        Text("No notifications at this time.", color = TextSecondary, fontSize = 13.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            notifications.forEach { notif ->
                                Surface(color = DarkSurfaceElevated, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = notif.title, fontWeight = FontWeight.Bold, color = AccentGold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = notif.message, color = TextPrimary, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotificationsDialog = false }) {
                        Text("Close", color = PrimaryCyan)
                    }
                }
            )
        }

        if (banners.isNotEmpty() && selectedCategory == null) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(banners) { banner ->
                    BannerCard(banner = banner)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (selectedCategory == null) "E-Sports Games" else "Tournaments: ${selectedCategory?.name}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (selectedCategory != null) {
                TextButton(onClick = { selectedCategory = null }) {
                    Text("View All Games", color = PrimaryCyan, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedCategory == null) {
            if (categories.isEmpty()) {
                Text(text = "No categories available.", color = TextSecondary)
            } else {
                val chunkedCategories = categories.chunked(2)
                chunkedCategories.forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowCategories.forEach { cat ->
                            CategoryTile(
                                category = cat,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedCategory = cat }
                            )
                        }
                        if (rowCategories.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SocialButton(text = "Telegram", color = Color(0xFF229ED9), modifier = Modifier.weight(1f))
                SocialButton(text = "WhatsApp", color = Color(0xFF25D366), modifier = Modifier.weight(1f))
                SocialButton(text = "Instagram", color = Color(0xFFE4405F), modifier = Modifier.weight(1f))
            }
        } else {
            var subTab by remember { mutableIntStateOf(0) } // 0: Ongoing, 1: Upcoming, 2: Completed

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedCategory = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryCyan)
                    }
                    Text(text = (selectedCategory?.name ?: "").uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = subTab,
                containerColor = DarkSurface,
                contentColor = PrimaryCyan
            ) {
                Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Ongoing") })
                Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Upcoming") })
                Tab(selected = subTab == 2, onClick = { subTab = 2 }, text = { Text("Completed") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredTournaments = when (subTab) {
                0 -> categoryTournaments.filter { it.status == "Live" || it.status == "Ongoing" }
                1 -> categoryTournaments.filter { it.status == "Upcoming" || it.status.isBlank() }
                else -> categoryTournaments.filter { it.status == "Completed" }
            }

            if (filteredTournaments.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = AccentGold, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matches found in this tab!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    filteredTournaments.forEach { tournament ->
                        TournamentCard(tournament = tournament, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun BannerCard(banner: BannerEntity) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkSurfaceElevated)
        ) {
            if (banner.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(text = "SPECIAL EVENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = banner.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = banner.subtitle, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun CategoryTile(category: GameCategoryEntity, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(DarkSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            if (category.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = category.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (category.imageUrl.isNotBlank()) 0.45f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    if (category.imageUrl.isBlank()) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = PrimaryCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = category.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SocialButton(text: String, color: Color, modifier: Modifier = Modifier) {
    Button(
        onClick = { /* simulated link */ },
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun TournamentCard(tournament: TournamentEntity, viewModel: AppViewModel) {
    val participants by viewModel.getParticipantsWithUsers(tournament.id).collectAsState(initial = emptyList())
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var freeFireName by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (tournament.bannerImageUrl.isNotBlank()) {
                AsyncImage(
                    model = tournament.bannerImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(90.dp).background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(36.dp))
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tournament.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (tournament.status == "Live") ErrorRed else AccentGold,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (tournament.status == "Live") "LIVE" else "Paid",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "TIME: ${tournament.matchTime}", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))

                // Stats Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "PRIZE", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(text = "🪙 ${tournament.prizePool.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "PER KILL", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(text = "🪙 ${tournament.perKill.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "ENTRY", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(text = "🪙 ${tournament.entryFee.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "TYPE", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(text = tournament.matchType, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "VERSION", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(text = tournament.version, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "MAP", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(text = tournament.mapName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                val joinedCount = participants.size
                val maxSlots = tournament.maxSlots.takeIf { it > 0 } ?: 48
                val progress = (joinedCount.toFloat() / maxSlots).coerceIn(0f, 1f)
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "👥 $joinedCount/$maxSlots joined", fontSize = 11.sp, color = PrimaryCyan)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = AccentGold,
                        trackColor = DarkSurfaceElevated
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { showDetailsDialog = true },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PrimaryCyan)
                    ) {
                        Text(text = "Details", color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            if (tournament.status == "Live") {
                                showDetailsDialog = true
                            } else {
                                showJoinDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (tournament.status == "Live") "Watch Match" else "Join Now",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            containerColor = DarkSurface,
            title = { Text("Enter Free Fire IGN", color = PrimaryCyan, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Please enter your Free Fire In-Game Name (IGN) to join this tournament.", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = freeFireName,
                        onValueChange = { freeFireName = it },
                        label = { Text("Free Fire Name / IGN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (freeFireName.isNotBlank()) {
                            viewModel.joinTournament(tournament.id, freeFireName)
                            showJoinDialog = false
                            freeFireName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                ) {
                    Text("Confirm & Join", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            containerColor = DarkSurface,
            title = { Text(tournament.title, color = PrimaryCyan, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Game: ${tournament.gameName}", color = TextPrimary, fontSize = 13.sp)
                    Text("Match Time: ${tournament.matchTime}", color = TextPrimary, fontSize = 13.sp)
                    Text("Prize Pool: ₹${tournament.prizePool}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Per Kill: ₹${tournament.perKill}", color = AccentGold, fontSize = 13.sp)
                    Text("Entry Fee: ₹${tournament.entryFee}", color = TextPrimary, fontSize = 13.sp)
                    Text("Type: ${tournament.matchType} | Version: ${tournament.version} | Map: ${tournament.mapName}", color = TextSecondary, fontSize = 12.sp)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tournament Rules:", fontWeight = FontWeight.Bold, color = PrimaryCyan, fontSize = 13.sp)
                    Surface(color = DarkSurfaceElevated, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (tournament.rules.isNotBlank()) tournament.rules else "1. No Hacking/Emulators\n2. Room ID will be shared 10 mins before match",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    if (tournament.status == "Live" && !tournament.roomId.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(color = DarkSurfaceElevated, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("ROOM CREDENTIALS", fontWeight = FontWeight.Bold, color = AccentGold, fontSize = 12.sp)
                                Text("Room ID: ${tournament.roomId}", color = TextPrimary, fontSize = 12.sp)
                                Text("Password: ${tournament.roomPassword}", color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Joined Players (${participants.size}):", fontWeight = FontWeight.Bold, color = PrimaryCyan, fontSize = 13.sp)
                    if (participants.isEmpty()) {
                        Text("No players joined yet.", color = TextSecondary, fontSize = 12.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            participants.forEach { p ->
                                Surface(color = DarkSurfaceElevated, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "🎮 ${p.participant.inGameName.ifBlank { p.username }}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "@${p.username}", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Close", color = PrimaryCyan)
                }
            }
        )
    }
}

@Composable
fun MyTournamentsScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var subTab by remember { mutableIntStateOf(0) }
    
    val activeTourneys by viewModel.getJoinedActiveTournaments(currentUser?.id ?: 0).collectAsState(initial = emptyList())
    val completedTourneys by viewModel.getJoinedCompletedTournaments(currentUser?.id ?: 0).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "My Tournaments", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = subTab,
            containerColor = DarkSurface,
            contentColor = PrimaryCyan
        ) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Upcoming / Live") })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Completed") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (subTab == 0) {
            if (activeTourneys.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "You haven't joined any active tournaments.", color = TextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(activeTourneys) { tournament ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = tournament.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Time: ${tournament.matchTime}", fontSize = 13.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (tournament.status == "Live" && !tournament.roomId.isNullOrBlank()) {
                                    Surface(
                                        color = DarkSurfaceElevated,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(text = "ROOM CREDENTIALS (LIVE)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "Room ID: ${tournament.roomId}", color = TextPrimary)
                                            Text(text = "Password: ${tournament.roomPassword}", color = TextPrimary)
                                        }
                                    }
                                } else {
                                    Text(text = "Status: Room details will appear when match is Live.", fontSize = 12.sp, color = AccentGold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (completedTourneys.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No completed tournaments history.", color = TextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(completedTourneys) { tournament ->
                        val isWinner = tournament.winnerUserId == currentUser?.id
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = tournament.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Prize Pool: ₹${tournament.prizePool}", fontSize = 13.sp, color = AccentGold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = if (isWinner) SuccessGreen.copy(alpha = 0.2f) else DarkSurfaceElevated,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isWinner) "🏆 Result: WINNER (Prize Credited)" else "Result: Participated",
                                        modifier = Modifier.padding(8.dp),
                                        color = if (isWinner) SuccessGreen else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
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
fun WalletScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val adminSettings by viewModel.adminSettings.collectAsState(initial = null)
    val transactions by viewModel.getUserTransactions(currentUser?.id ?: 0).collectAsState(initial = emptyList())

    var showAddMoneyModal by remember { mutableStateOf(false) }
    var showWithdrawModal by remember { mutableStateOf(false) }

    var depositAmount by remember { mutableStateOf("") }
    var depositTxId by remember { mutableStateOf("") }
    var withdrawAmount by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "My Wallet", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Available Balance", fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "₹${currentUser?.walletBalance ?: 0.0}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { showAddMoneyModal = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Money", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showWithdrawModal = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = PrimaryCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Withdraw", color = PrimaryCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Transaction History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "No transactions yet.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { tx ->
                    TransactionItem(tx = tx)
                }
            }
        }
    }

    if (showAddMoneyModal) {
        AlertDialog(
            onDismissRequest = { showAddMoneyModal = false },
            title = { Text("Add Money via UPI") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1. Pay to Admin UPI ID:", fontSize = 13.sp, color = TextSecondary)
                    Text(text = adminSettings?.upiId ?: "admin@upi", fontWeight = FontWeight.Bold, color = PrimaryCyan, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("2. Or scan QR Code:", fontSize = 13.sp, color = TextSecondary)
                    Surface(
                        color = DarkSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                            Text(text = "[QR Code: ${adminSettings?.upiId}]", color = AccentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = depositAmount,
                        onValueChange = { depositAmount = it },
                        label = { Text("Amount Paid (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = depositTxId,
                        onValueChange = { depositTxId = it },
                        label = { Text("UPI Transaction ID (UTR)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = depositAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && depositTxId.isNotBlank()) {
                            viewModel.createDeposit(amt, depositTxId.trim())
                            showAddMoneyModal = false
                            depositAmount = ""
                            depositTxId = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                ) {
                    Text("Submit Deposit", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMoneyModal = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showWithdrawModal) {
        AlertDialog(
            onDismissRequest = { showWithdrawModal = false },
            title = { Text("Request Withdrawal") },
            text = {
                Column {
                    Text("Your Saved UPI ID:", fontSize = 13.sp, color = TextSecondary)
                    Text(text = currentUser?.upiId ?: "Not set (Go to Profile to set UPI ID)", fontWeight = FontWeight.Bold, color = PrimaryCyan)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = { Text("Amount to Withdraw (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Min withdrawal limit: ₹${adminSettings?.minWithdrawalLimit ?: 100.0}", fontSize = 11.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = withdrawAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.createWithdrawal(amt)
                            showWithdrawModal = false
                            withdrawAmount = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                ) {
                    Text("Request Withdrawal", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawModal = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun TransactionItem(tx: TransactionEntity) {
    val isCredit = tx.type == "credit"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tx.description, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "Type: ${tx.type.uppercase()}", fontSize = 11.sp, color = TextSecondary)
            }
            Text(
                text = "${if (isCredit) "+" else "-"}₹${tx.amount}",
                fontWeight = FontWeight.Bold,
                color = if (isCredit) SuccessGreen else ErrorRed,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val adminSettings by viewModel.adminSettings.collectAsState(initial = null)

    var activeDialog by remember { mutableStateOf<String?>(null) }

    var username by remember(currentUser) { mutableStateOf(currentUser?.username ?: "") }
    var email by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    var upiId by remember(currentUser) { mutableStateOf(currentUser?.upiId ?: "") }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var promoCodeInput by remember { mutableStateOf("") }

    val menuItems = listOf(
        Triple("My Profile", Icons.Default.Person, "profile"),
        Triple("My Wallet", Icons.Default.AccountBalanceWallet, "wallet"),
        Triple("My Statistics", Icons.Default.BarChart, "stats"),
        Triple("Top Players", Icons.Default.EmojiEvents, "top_players"),
        Triple("Promo Code", Icons.Default.CardGiftcard, "promo"),
        Triple("Contact Us", Icons.Default.HeadsetMic, "contact"),
        Triple("FAQ", Icons.Default.Help, "faq"),
        Triple("About Us", Icons.Default.Info, "about"),
        Triple("Privacy Policy", Icons.Default.PrivacyTip, "privacy"),
        Triple("Terms & Conditions", Icons.Default.Description, "terms"),
        Triple("Fair Play Policy", Icons.Default.VerifiedUser, "fair_play"),
        Triple("Refund & Reward Policy", Icons.Default.MonetizationOn, "refund"),
        Triple("Logout", Icons.Default.Logout, "logout")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "Profile & Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
        Spacer(modifier = Modifier.height(4.dp))

        menuItems.forEach { (title, icon, key) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (key == "logout") {
                            viewModel.logout()
                        } else {
                            activeDialog = key
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(icon, contentDescription = null, tint = AccentGold, modifier = Modifier.size(22.dp))
                        Text(text = title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    when (activeDialog) {
        "profile" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = DarkSurface,
                title = { Text("My Profile & Security", color = PrimaryCyan) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = { Text("My UPI ID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { viewModel.updateProfile(username, email, upiId); activeDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Update Profile", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Change Password", fontWeight = FontWeight.Bold, color = AccentGold)
                        OutlinedTextField(
                            value = oldPass,
                            onValueChange = { oldPass = it },
                            label = { Text("Old Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newPass,
                            onValueChange = { newPass = it },
                            label = { Text("New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                viewModel.changePassword(oldPass, newPass) { success, _ ->
                                    if (success) {
                                        oldPass = ""
                                        newPass = ""
                                        activeDialog = null
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Change Password", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Close", color = PrimaryCyan)
                    }
                }
            )
        }
        "wallet" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = DarkSurface,
                title = { Text("My Wallet", color = PrimaryCyan) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current Balance: ₹${currentUser?.walletBalance ?: 0.0}", color = AccentGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("You can deposit funds via UPI or withdraw your winnings from the Wallet tab.", color = TextPrimary)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("OK", color = PrimaryCyan)
                    }
                }
            )
        }
        "stats" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = DarkSurface,
                title = { Text("My Statistics", color = PrimaryCyan) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Username: ${currentUser?.username}", color = TextPrimary)
                        Text("Wallet Balance: ₹${currentUser?.walletBalance ?: 0.0}", color = AccentGold)
                        Text("Account Status: Active", color = SuccessGreen)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Close", color = PrimaryCyan)
                    }
                }
            )
        }
        "top_players" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = DarkSurface,
                title = { Text("Top Players", color = PrimaryCyan) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1. ${currentUser?.username ?: "ProPlayer"} - ₹15,000 Won", color = AccentGold, fontWeight = FontWeight.Bold)
                        Text("2. GamerX - ₹10,200 Won", color = TextPrimary)
                        Text("3. SniperQueen - ₹8,500 Won", color = TextPrimary)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Close", color = PrimaryCyan)
                    }
                }
            )
        }
        "promo" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = DarkSurface,
                title = { Text("Promo Code", color = PrimaryCyan) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enter promo code to claim bonus rewards:", color = TextSecondary)
                        OutlinedTextField(
                            value = promoCodeInput,
                            onValueChange = { promoCodeInput = it },
                            label = { Text("Promo Code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (promoCodeInput.isNotBlank()) {
                                    activeDialog = null
                                    promoCodeInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Apply Code", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Close", color = PrimaryCyan)
                    }
                }
            )
        }
        "contact" -> {
            PolicyDialog(title = "Contact Us", content = "Support Email: support@battlebooyah.com\nAdmin UPI ID: ${adminSettings?.upiId ?: "admin@upi"}\nTelegram: @BattleBooyahSupport", onDismiss = { activeDialog = null })
        }
        "faq" -> {
            PolicyDialog(title = "FAQ", content = adminSettings?.faqText ?: "No FAQ available.", onDismiss = { activeDialog = null })
        }
        "about" -> {
            PolicyDialog(title = "About Us", content = adminSettings?.aboutUsText ?: "No About Us content available.", onDismiss = { activeDialog = null })
        }
        "privacy" -> {
            PolicyDialog(title = "Privacy Policy", content = adminSettings?.privacyPolicyText ?: "No Privacy Policy available.", onDismiss = { activeDialog = null })
        }
        "terms" -> {
            PolicyDialog(title = "Terms & Conditions", content = adminSettings?.termsText ?: "No Terms & Conditions available.", onDismiss = { activeDialog = null })
        }
        "fair_play" -> {
            PolicyDialog(title = "Fair Play Policy", content = adminSettings?.fairPlayText ?: "No Fair Play Policy available.", onDismiss = { activeDialog = null })
        }
        "refund" -> {
            PolicyDialog(title = "Refund & Reward Policy", content = adminSettings?.refundPolicyText ?: "No Refund Policy available.", onDismiss = { activeDialog = null })
        }
    }
}

@Composable
fun PolicyDialog(title: String, content: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(title, color = PrimaryCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = content, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PrimaryCyan)
            }
        }
    )
}
