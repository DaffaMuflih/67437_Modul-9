package com.alexwawo.w08firebase101

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StudentViewModel : ViewModel() {
    private val db = Firebase.firestore
    var students by mutableStateOf(listOf<Student>())
        private set

    init {
        fetchStudents()
    }

    fun addStudent(student: Student) {
        val studentMap = hashMapOf(
            "id" to student.id,
            "name" to student.name,
            "program" to student.program
        )

        db.collection("students")
            .add(studentMap)
            .addOnSuccessListener { documentRef ->
                Log.d("Firestore", "Student added with ID: ${documentRef.id}")

                // Add phones as subcollection
                for (phone in student.phones) {
                    val phoneMap = hashMapOf("number" to phone)
                    documentRef.collection("phones")
                        .add(phoneMap)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Phone added: $phone")
                        }
                        .addOnFailureListener {
                            Log.e("Firestore", "Failed to add phone: $phone", it)
                        }
                }

                fetchStudents()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding student", e)
            }
    }

    private fun fetchStudents() {
        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                val tempList = mutableListOf<Student>()

                for (doc in result) {
                    val studentId = doc.id
                    val id = doc.getString("id") ?: ""
                    val name = doc.getString("name") ?: ""
                    val program = doc.getString("program") ?: ""

                    // Fetch phones for each student
                    doc.reference.collection("phones")
                        .get()
                        .addOnSuccessListener { phoneResults ->
                            val phones = phoneResults.mapNotNull { it.getString("number") }
                            tempList.add(Student(id, name, program, phones))
                            students = tempList.sortedBy { it.name } // refresh list
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching students", e)
            }
    }
}
