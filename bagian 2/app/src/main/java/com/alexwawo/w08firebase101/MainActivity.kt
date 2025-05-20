package com.alexwawo.w08firebase101

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alexwawo.w08firebase101.ui.theme.W08Firebase101Theme
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            MaterialTheme {
                StudentRegistrationScreen()
            }
        }
    }
}

@Composable
fun StudentRegistrationScreen(viewModel: StudentViewModel = viewModel()) {
    var studentId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var program by remember { mutableStateOf("") }

    var currentPhone by remember { mutableStateOf("") }
    var phoneList by remember { mutableStateOf(listOf<String>()) }

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()) {

        TextField(value = studentId, onValueChange = { studentId = it }, label = { Text("Student ID") })
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        TextField(value = program, onValueChange = { program = it }, label = { Text("Program") })

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = currentPhone,
                onValueChange = { currentPhone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                if (currentPhone.isNotBlank()) {
                    phoneList = phoneList + currentPhone
                    currentPhone = ""
                }
            }, modifier = Modifier.padding(start = 8.dp)) {
                Text("Add")
            }
        }

        if (phoneList.isNotEmpty()) {
            Text("Phone Numbers:", style = MaterialTheme.typography.labelLarge)
            phoneList.forEach {
                Text("- $it")
            }
        }

        Button(onClick = {
            viewModel.addStudent(Student(studentId, name, program, phoneList))
            studentId = ""
            name = ""
            program = ""
            phoneList = listOf()
        }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Submit")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text("Student List", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(viewModel.students) { student ->
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("ID: ${student.id}")
                    Text("Name: ${student.name}")
                    Text("Program: ${student.program}")
                    if (student.phones.isNotEmpty()) {
                        Text("Phones:")
                        student.phones.forEach {
                            Text("- $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Divider()
                }
            }
        }
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    W08Firebase101Theme {
        Greeting("Android")
    }
}