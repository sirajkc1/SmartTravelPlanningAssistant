@file:OptIn(ExperimentalMaterial3Api::class)

package com.siraj.smarttravelplanningassistant

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF7B1FA2))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Smart Travel", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(20.dp))

        Text("Welcome!", style = MaterialTheme.typography.titleLarge, color = Color.White)

        Spacer(Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FeatureCard("Plan a New Trip", Icons.Filled.Map) { navController.navigate("planTrip") }
            FeatureCard("Track Expenses", Icons.Filled.AttachMoney) { navController.navigate("trackExpenses") }
            FeatureCard("Upcoming Reminders", Icons.Filled.Notifications) { navController.navigate("reminders") }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                userViewModel.logout()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("Logout", color = Color.Black)
        }
    }
}

@Composable
fun FeatureCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = Color(0xFF7B1FA2), modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(20.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}

data class Destination(val name: String, val imageRes: Int, val summary: String)

@Composable
fun PlanTripScreen(navController: NavHostController) {
    val destinations = listOf(
        Destination("Japan", R.drawable.japan, "Explore Tokyo, Kyoto, and cherry blossoms."),
        Destination("South Korea", R.drawable.southkorea, "Visit Seoul, Bus an, and historical palaces."),
        Destination("Australia", R.drawable.australia, "Enjoy Sydney, Great Barrier Reef, and wildlife."),
        Destination("New Zealand", R.drawable.newzealand, "Adventure in Queenstown, Rotor, and mountains."),
        Destination("Singapore", R.drawable.singapore, "Discover Marina Bay Sands and vibrant city life."),
        Destination("USA", R.drawable.usa, "Explore New York, LA, and national parks."),
        Destination("Italy", R.drawable.italy, "Visit Rome, Venice, and enjoy pasta and art."),
        Destination("Canada", R.drawable.canada, "Experience Toronto, Vancouver, and nature wonders."),
        Destination("United Kingdom", R.drawable.uk, "London, history, and countryside beauty."),
        Destination("Mexico", R.drawable.mexico, "Beaches, tacos, and ancient ruins await.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text("Choose Your Destination", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(destinations) { destination ->
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
    var other: Double by remember { mutableDoubleStateOf(0.0) }
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

        Text("Total Expenses: $${"%.2f".format(total)}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Done")
        }
    }
}

@Composable
fun ExpenseInputField(label: String, value: Double, onValueChange: (Double) -> Unit) {
    var text by remember { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onValueChange(it.toDoubleOrNull() ?: 0.0)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}


@Composable
fun RemindersScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Reminders - Coming Soon")
    }
}
