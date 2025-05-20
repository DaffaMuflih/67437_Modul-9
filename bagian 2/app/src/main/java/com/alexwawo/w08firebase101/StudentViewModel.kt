package com.alexwawo.w08firebase101

import android.util.Log
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
            "program" to student.program,
            "phones" to student.phones
        )

        db.collection("students")
            .add(studentMap)
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot added with ID: ${it.id}")
                fetchStudents()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }

    fun updateStudent(student: Student) {
        val updatedData = hashMapOf(
            "id" to student.id,
            "name" to student.name,
            "program" to student.program,
            "phones" to student.phones
        )

        db.collection("students")
            .document(student.docId)
            .set(updatedData)
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot updated with ID: ${student.docId}")
                fetchStudents()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating document", e)
            }
    }

    fun deleteStudent(student: Student) {
        db.collection("students")
            .document(student.docId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot deleted with ID: ${student.docId}")
                fetchStudents()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error deleting document", e)
            }
    }

    private fun fetchStudents() {
        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<Student>()
                for (document in result) {
                    val id = document.getString("id") ?: ""
                    val name = document.getString("name") ?: ""
                    val program = document.getString("program") ?: ""
                    val phones = document.get("phones") as? List<String> ?: emptyList()
                    val docId = document.id // simpan docId di student

                    list.add(Student(id, name, program, phones, docId))
                }
                students = list
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents.", exception)
            }
    }
}
