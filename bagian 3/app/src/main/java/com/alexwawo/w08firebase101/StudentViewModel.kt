package com.alexwawo.w08firebase101

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.gms.tasks.Tasks

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

                // Add phones to subcollection
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

    fun updateStudent(student: Student) {
        val studentMap = mapOf(
            "id" to student.id,
            "name" to student.name,
            "program" to student.program
        )

        val studentDocRef = db.collection("students").document(student.docId)

        studentDocRef.set(studentMap)
            .addOnSuccessListener {
                val phonesRef = studentDocRef.collection("phones")

                // Step 1: Hapus semua data lama
                phonesRef.get().addOnSuccessListener { snapshot ->
                    val deleteTasks = snapshot.documents.map { it.reference.delete() }

                    Tasks.whenAllComplete(deleteTasks)
                        .addOnSuccessListener {
                            // Step 2: Tambahkan data baru
                            val addPhoneTasks = student.phones.map { phone ->
                                val phoneMap = mapOf("number" to phone)
                                phonesRef.add(phoneMap)
                            }

                            // Step 3: Selesai
                            Tasks.whenAllComplete(addPhoneTasks)
                                .addOnSuccessListener {
                                    fetchStudents()
                                }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating student", e)
            }
    }

    fun deleteStudent(student: Student) {
        db.collection("students").document(student.docId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Student deleted")
                fetchStudents()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error deleting student", it)
            }
    }

    private fun fetchStudents() {
        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                val tempList = mutableListOf<Student>()

                for (doc in result) {
                    val docId = doc.id
                    val id = doc.getString("id") ?: ""
                    val name = doc.getString("name") ?: ""
                    val program = doc.getString("program") ?: ""

                    doc.reference.collection("phones")
                        .get()
                        .addOnSuccessListener { phoneResults ->
                            val phones = phoneResults.mapNotNull { it.getString("number") }
                            tempList.add(Student(id, name, program, phones, docId))
                            students = tempList.sortedBy { it.name }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching students", e)
            }
    }
}
