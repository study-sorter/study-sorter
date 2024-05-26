package com.example.studysorter

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Szkola(var id: String, var listaSemestr: MutableList<Semestr>)
data class Semestr(var id: String, var listaSubject: MutableList<Subbject>)
data class Subbject(
    var id: String,
    var ulubione: Boolean,
    var imageUrls: MutableList<File>
) //jest Subbject bo koliduje z jakąś gotow klasą

data class File(var Url: String, var type: String?)
class SchoolRepository {
    private var listaSzkola: MutableList<Szkola> = mutableListOf()

    fun getData(): MutableList<Szkola> {

        Log.d("DataGet", "Getting Data")
        /*for (szkola in listaSzkola) {
            for (sem in szkola.listaSemestr){
                for (przed in sem.listaSubject){
                    Log.d("DataGet","${przed.id}-${przed.ulubione}")
                }
            }
        }*/
        return listaSzkola
    }

    fun DownloadData() {
        Log.d("Data", "Downloading Data")
        val chmura = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        listaSzkola.clear()
        if (currentUser != null) {
            chmura
                .collection("users").document(currentUser.uid)
                .collection("szkoly").get().addOnSuccessListener { schoolSnaphot ->
                    Log.d("Data", "semesterSnapshot size: ${schoolSnaphot.size()}")

                    for (school in schoolSnaphot) {

                        var listaSemestr = mutableListOf<Semestr>()

                        school.reference
                            .collection("semestry")
                            .get().addOnSuccessListener { semestrSnapshot ->
                                Log.d("Data", "semesterSnapshot size: ${semestrSnapshot.size()}")
                                for (semestr in semestrSnapshot) {

                                    var listaSubject = mutableListOf<Subbject>()

                                    semestr.reference
                                        .collection("przedmioty")
                                        .get().addOnSuccessListener { subjectSnapshot ->
                                            for (subject in subjectSnapshot) {

                                                Log.d("Data", "downloading imges starts")
                                                /*val listaUrls = if (subject.data.contains("imageUrls")) {
                                                    subject.data["imageUrls"] as List<String>
                                                } else {
                                                    emptyList()
                                                }*/
                                                /*val listaFiles = mutableListOf<File>()
                                                var type = "None"
                                                for (Url in listaUrls){
                                                    type  = URLConnection.guessContentTypeFromName(Url).toString()
                                                    listaFiles.add(File(Url,type))
                                                }*/
                                                val listaFiles = mutableStateListOf<File>()
                                                Firebase.storage.reference.child("users/${currentUser.uid}/${school.id}/${semestr.id}/${subject.id}")
                                                    .listAll().addOnSuccessListener { result ->
                                                    result.items.forEach { item ->
                                                        // Get metadata for each file
                                                        GlobalScope.launch {

                                                            var type = mutableStateOf("None")
                                                            var url = mutableStateOf("None")

                                                            item.metadata
                                                                .addOnSuccessListener { metadata ->
                                                                    type.value =
                                                                        metadata.contentType.toString()
                                                                            .split("/").last()
                                                                }
                                                                .addOnFailureListener { exception ->
                                                                    // Handle any errors that occur during metadata retrieval
                                                                    Log.e(
                                                                        "Data",
                                                                        "Error getting metadata for ${item.name}: $exception"
                                                                    )
                                                                }.await()

                                                            item.downloadUrl
                                                                .addOnSuccessListener { uri ->
                                                                    url.value = uri.toString()
                                                                }
                                                                .addOnFailureListener { exception ->
                                                                    Log.e(
                                                                        "Data",
                                                                        "Error getting download URL for ${item.name}: $exception"
                                                                    )
                                                                }.await()

                                                            Log.d("type", "typ ${type.value}")


                                                            listaFiles.add(
                                                                File(
                                                                    url.value,
                                                                    type.value
                                                                )
                                                            )
                                                        }

                                                    }
                                                }
                                                listaSubject.add(
                                                    Subbject(
                                                        subject.id,
                                                        subject.data["ulubione"] as Boolean,
                                                        listaFiles
                                                    )
                                                )

                                                Log.d("Data", "downloading imges ends")

                                            }
                                        }.addOnFailureListener { e ->
                                            Log.d(
                                                "Data",
                                                "Downloading data incomplete(Subject) \"${e.message}\" "
                                            )
                                        }
                                    listaSemestr.add(Semestr(semestr.id, listaSubject))
                                }
                            }.addOnFailureListener { e ->
                                Log.d(
                                    "Data",
                                    "Downloading data incomplete(Sem) \"${e.message}\" "
                                )
                            }
                        listaSzkola.add(Szkola(school.id, listaSemestr))
                    }
                }.addOnFailureListener { e ->
                    Log.d(
                        "Data",
                        "Downloading data incomplete(School) \"${e.message}\" "
                    )
                }
            Log.d("Data", "Downloading Data Complete")

        }
    }


}


val SchoolObject = SchoolRepository()