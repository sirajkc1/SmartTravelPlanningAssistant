package com.siraj.smarttravelplanningassistant

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.siraj.smarttravelplanningassistant.ui.theme.SmartTravelPlanningAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartTravelPlanningAssistantTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TravelHomePage(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TravelHomePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.travel_concept_with_landmarks),
            contentDescription = "Travel Cover Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Text(
            text = "Smart Travel Assistant",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF1E88E5)
        )

        Text(
            text = "Welcome back, Helen!",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray
        )

        Button(
            onClick = {
                Toast.makeText(context, "Trip Planner Coming Soon", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Plan a New Trip")
        }

        Button(
            onClick = {
                Toast.makeText(context, "Expense Tracker Coming Soon", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Track Expenses")
        }

        Button(
            onClick = {
                Toast.makeText(context, "Reminders Coming Soon", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upcoming Reminders")
        }
    }
}

@Composable
fun TestLoginScreen(modifier: Modifier = Modifier) {
    // Placeholder for login screen
}

@Preview(showBackground = true)
@Composable
fun TravelHomePagePreview() {
    SmartTravelPlanningAssistantTheme {
        TravelHomePage()
    }
}
