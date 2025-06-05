package com.siraj.smarttravelplanningassistant

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.siraj.smarttravelplanningassistant.ui.theme.SmartTravelPlanningAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartTravelPlanningAssistantTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestLayout(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TestLayout(name: String, modifier: Modifier = Modifier) { // It's good practice to use the passed modifier on the root element
    Row(
        modifier = modifier.fillMaxSize() // Apply the passed modifier here
    ) {
        Column(modifier = Modifier.fillMaxHeight() // Use a new Modifier for this Column
            .width(100.dp).background(Color.Yellow),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally) {
            repeat(6){
                val context = LocalContext.current
                Image(
                    painter = painterResource(image_ids[it]),
                    contentDescription = "Dice $it",
                    // Apply a new clickable modifier to the Image
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Clicked $it", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        Column(modifier = Modifier.fillMaxHeight().width(100.dp).background(Color.Red),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Column 2")
        }
        Column(
            modifier = Modifier.fillMaxHeight().fillMaxWidth().background(Color.Gray),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = "Column 3")
        }
    }
}
// test another screen
@Composable
fun TestLoginScreeen(modifier: Modifier = Modifier){

}

@Preview(showBackground = true)
@Composable
fun TestLayout() {
    SmartTravelPlanningAssistantTheme {
        TestLayout("Android")
    }
}

private val image_ids = listOf(
    R.drawable.dice_1,
    R.drawable.dice_2,
    R.drawable.dice_3,
    R.drawable.dice_4,
    R.drawable.dice_5,
    R.drawable.dice_6
)