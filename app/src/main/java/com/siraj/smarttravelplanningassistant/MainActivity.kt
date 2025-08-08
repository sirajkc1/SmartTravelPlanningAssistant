@file:OptIn(ExperimentalMaterial3Api::class)

package com.siraj.smarttravelplanningassistant

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.siraj.smarttravelplanningassistant.database.AppDatabase
import com.siraj.smarttravelplanningassistant.database.Trip
import com.siraj.smarttravelplanningassistant.database.User
import com.siraj.smarttravelplanningassistant.repository.TripRepository
import com.siraj.smarttravelplanningassistant.repository.UserRepository
import com.siraj.smarttravelplanningassistant.ui.theme.SmartTravelPlanningAssistantTheme
import com.siraj.smarttravelplanningassistant.viewmodel.TripViewModel
import com.siraj.smarttravelplanningassistant.viewmodel.UserViewModel



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val userRepository = UserRepository(database.userDao())
        val tripRepository = TripRepository(database.tripDao())

        val userViewModel = UserViewModel(userRepository)
        val tripViewModel = TripViewModel(tripRepository)

        setContent {
            SmartTravelPlanningAssistantTheme {
                val navController = rememberNavController()
                val isLoggedIn by userViewModel.loginSuccess.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        if (currentDestination?.route in listOf("home", "planTrip", "trackExpenses", "reminders")) {
                            BottomNavigationBar(navController, currentDestination)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) "home" else "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(navController, userViewModel)
                        }
                        composable("signup") {
                            SignUpScreen(navController, userViewModel)
                        }
                        composable("home") {
                            HomeScreen(navController, userViewModel)
                        }
                        composable("planTrip") {
                            PlanTripScreen(navController)
                        }
                        composable(
                            "tripDetails/{destination}",
                            arguments = listOf(navArgument("destination") { defaultValue = "Unknown" })
                        ) { backStackEntry ->
                            val destination = backStackEntry.arguments?.getString("destination") ?: "Unknown"
                            TripDetailsScreen(navController, destination, tripViewModel, userViewModel)
                        }
                        composable("trackExpenses") {
                            TrackExpensesScreen(navController)
                        }
                        composable("reminders") {
                            RemindersScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, currentDestination: NavDestination?) {
    NavigationBar {
        val items = listOf(
            NavBarItem("home", "Home", Icons.Filled.Home),
            NavBarItem("planTrip", "Plan Trip", Icons.Filled.Map),
            NavBarItem("trackExpenses", "Expenses", Icons.Filled.AttachMoney),
            NavBarItem("reminders", "Reminders", Icons.Filled.Notifications)
        )
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class NavBarItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun LoginScreen(navController: NavHostController, userViewModel: UserViewModel) {
    LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val errorMessage by userViewModel.errorMessage.collectAsState()

    val loginSuccess by userViewModel.loginSuccess.collectAsState()

    // Navigate to home on login success
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { userViewModel.login(email, password) },
            enabled = email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        errorMessage?.let { msg ->
            Spacer(Modifier.height(8.dp))
            Text(msg, color = Color.Red)
        }

        Spacer(Modifier.height(10.dp))

        TextButton(onClick = { navController.navigate("signup") }) {
            Text("Don't have an account? Sign Up")
        }
    }
}

@Composable
fun SignUpScreen(navController: NavHostController, userViewModel: UserViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val errorMessage by userViewModel.errorMessage.collectAsState()
    val signupSuccess by userViewModel.signupSuccess.collectAsState()

    val isSignUpEnabled = email.isNotBlank() && password.isNotBlank() && password == confirmPassword

    val context = LocalContext.current

    LaunchedEffect(signupSuccess) {
        if (signupSuccess) {
            Toast.makeText(context, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
            userViewModel.resetSignupSuccess()
            navController.navigate("login") {
                popUpTo("signup") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                userViewModel.register(User(email, password))
            },
            enabled = isSignUpEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        errorMessage?.let { msg ->
            Spacer(Modifier.height(8.dp))
            Text(msg, color = Color.Red)
        }

        Spacer(Modifier.height(10.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login")
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController, userViewModel: UserViewModel) {
    val recommendedDestinations = listOf(
        Destination("Japan", R.drawable.japan, "Explore Tokyo, Kyoto, and cherry blossoms."),
        Destination("Australia", R.drawable.australia, "Enjoy Sydney, Great Barrier Reef, and wildlife."),
        Destination("Italy", R.drawable.italy, "Visit Rome, Venice, and enjoy pasta and art."),
        Destination("Canada", R.drawable.canada, "Experience Toronto, Vancouver, and nature wonders.")
    )

    // Get current email or fallback to "Guest"
    val email = userViewModel.getCurrentUserEmail() ?: "Guest"
    val username = email.substringBefore('@').replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Match your other pages
            .padding(16.dp)
    ) {
        // Top Bar with Title and Circular Avatar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Smart Travel",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color(0xFFE1BEE7), // subtle purple background for avatar
                shadowElevation = 6.dp,
                tonalElevation = 6.dp
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User Avatar",
                    tint = Color(0xFF7B1FA2),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Welcome message with bigger font
        Text(
            "Welcome, $username",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = Color(0xFF4A148C),
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(24.dp))

        Spacer(Modifier.height(16.dp))

        // Feature Cards Section
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FeatureCard("Plan a New Trip", Icons.Filled.Map) { navController.navigate("planTrip") }
            FeatureCard("Track Expenses", Icons.Filled.AttachMoney) { navController.navigate("trackExpenses") }
            FeatureCard("Upcoming Reminders", Icons.Filled.Notifications) { navController.navigate("reminders") }
        }

        Spacer(Modifier.height(32.dp))

        Spacer(Modifier.height(16.dp))

        // Recommended Places Header
        Text(
            "Recommended Places",
            style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF4A148C), fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(12.dp))

        // Recommended Places LazyRow with spacing & shadows
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(recommendedDestinations) { destination ->
                RecommendedCard(destination) {
                    navController.navigate("tripDetails/${destination.name}")
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Logout Button
        Button(
            onClick = {
                userViewModel.logout()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
        ) {
            Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FeatureCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(
                indication = null, // no ripple
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = Color(0xFF7B1FA2), modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(20.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun RecommendedCard(destination: Destination, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .clickable(
                indication = null, // no ripple
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Image(
                painter = painterResource(id = destination.imageRes),
                contentDescription = destination.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            )
            Spacer(Modifier.height(12.dp))
            Text(
                destination.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                destination.summary,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}


data class Destination(val name: String, val imageRes: Int, val summary: String)

@Composable
fun PlanTripScreen(navController: NavHostController) {
    // Full list of destinations
    val allDestinations = listOf(
        Destination("Japan", R.drawable.japan, "Explore Tokyo, Kyoto, and cherry blossoms."),
        Destination("South Korea", R.drawable.southkorea, "Visit Seoul, Busan, and historical palaces."),
        Destination("Australia", R.drawable.australia, "Enjoy Sydney, Great Barrier Reef, and wildlife."),
        Destination("New Zealand", R.drawable.newzealand, "Adventure in Queenstown, Rotorua, and mountains."),
        Destination("Singapore", R.drawable.singapore, "Discover Marina Bay Sands and vibrant city life."),
        Destination("USA", R.drawable.usa, "Explore New York, LA, and national parks."),
        Destination("Italy", R.drawable.italy, "Visit Rome, Venice, and enjoy pasta and art."),
        Destination("Canada", R.drawable.canada, "Experience Toronto, Vancouver, and nature wonders."),
        Destination("United Kingdom", R.drawable.uk, "London, history, and countryside beauty."),
        Destination("Mexico", R.drawable.mexico, "Beaches, tacos, and ancient ruins await.")
    )

    var searchQuery by remember { mutableStateOf("") }

    // Filter destinations based on search query (case-insensitive)
    val filteredDestinations = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allDestinations
        } else {
            allDestinations.filter {
                it.name.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text("Choose Your Destination", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Destinations") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = "Search Icon")
            }
        )

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredDestinations) { destination ->
                DestinationGridCard(destination) {
                    navController.navigate("tripDetails/${destination.name}")
                }
            }
        }
    }
}


@Composable
fun DestinationGridCard(destination: Destination, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = destination.imageRes),
                contentDescription = destination.name,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                destination.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                destination.summary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun TripDetailsScreen(
    navController: NavHostController,
    destination: String,
    tripViewModel: TripViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var travelers by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button at top left
        Button(
            onClick = { navController.navigateUp() },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Spacer(Modifier.width(4.dp))
            Text("Back")
        }

        Spacer(Modifier.height(16.dp))

        Text("Plan your trip to $destination", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Start Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("End Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = travelers,
            onValueChange = { travelers = it },
            label = { Text("Number of Travelers") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Additional Notes") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val userEmail = userViewModel.getCurrentUserEmail()
                if (userEmail == null) {
                    Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                    navController.navigate("login")
                    return@Button
                }
                val trip = Trip(
                    userEmail = userEmail,
                    destination = destination,
                    startDate = startDate,
                    endDate = endDate,
                    travelers = travelers.toIntOrNull() ?: 1,
                    notes = notes
                )
                tripViewModel.addTrip(trip)
                Toast.makeText(context, "Trip booked!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = startDate.isNotBlank() && endDate.isNotBlank()
        ) {
            Text("Book Trip")
        }
    }
}


@Composable
fun TrackExpensesScreen(navController: NavHostController) {
    var accommodation by remember { mutableDoubleStateOf(0.0) }
    var food by remember { mutableDoubleStateOf(0.0) }
    var transport by remember { mutableDoubleStateOf(0.0) }
    var other by remember { mutableDoubleStateOf(0.0) }
    val total = accommodation + food + transport + other

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Track Your Expenses", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        ExpenseInputField("Accommodation", accommodation) { accommodation = it }
        ExpenseInputField("Food", food) { food = it }
        ExpenseInputField("Transport", transport) { transport = it }
        ExpenseInputField("Other", other) { other = it }

        Spacer(Modifier.height(24.dp))

        Text(
            "Total Expenses: $${"%.2f".format(total)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))

        Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ExpenseInputField(label: String, value: Double, onValueChange: (Double) -> Unit) {
    var text by remember { mutableStateOf(if (value != 0.0) value.toString() else "") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                val number = it.toDoubleOrNull()
                isError = number == null && it.isNotEmpty()
                onValueChange(number ?: 0.0)
            },
            label = { Text(label, fontWeight = FontWeight.Bold) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = "Expense Icon",
                    tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            },
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        when {
            isError -> {
                Text(
                    text = "Please enter a valid number",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            text.isNotBlank() -> {
                val formatted = String.format("$%,.2f", text.toDoubleOrNull() ?: 0.0)
                Text(
                    text = "Entered: $formatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}


@Composable
fun RemindersScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Reminders - Coming Soon")
    }
}
