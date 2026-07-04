package com.example.android_doc_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.android_doc_app.ui.theme.AndroiddocappTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroiddocappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                   Column() {
                       Greeting(
                           name = "Mustafa",
                           modifier = Modifier.padding(innerPadding)
                       )
                       Counter()                   }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Salam, How're you doing? $name",
        modifier = modifier
    )
}

@Composable
fun Counter(){
   var count by remember { mutableStateOf(0)}

    Button(onClick = {count++}){
        Text("Count: $count")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroiddocappTheme {
        Column() {
            Greeting("Android")
            Counter()
        }
    }
}