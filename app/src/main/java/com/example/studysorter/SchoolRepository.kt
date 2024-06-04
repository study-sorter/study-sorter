package com.example.studysorter

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.painter.Painter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
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

data class File(var Url: String, var type: String?, var painter: Painter?)
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
        val storage = FirebaseStorage.getInstance()
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
                                                val listaFiles = mutableStateListOf<File>()

                                                val listaUrls =
                                                    if (subject.data.contains("files")) {
                                                        subject.data["files"] as List<*>
                                                    } else {
                                                        emptyList<String>()
                                                    }
                                                GlobalScope.launch {
                                                    for (Url in listaUrls) {

                                                        var type = mutableStateOf("None")
                                                        var painter: Painter
                                                        val fileRef =
                                                            storage.getReferenceFromUrl(Url.toString())
                                                        try {

                                                            fileRef.metadata
                                                                .addOnSuccessListener { metadata ->
                                                                    type.value =
                                                                        metadata.contentType.toString()
                                                                            .split("/").last()
                                                                }.await()
                                                            Log.d("Data", type.value)
                                                            listaFiles.add(
                                                                File(
                                                                    Url.toString(),
                                                                    type.value,
                                                                    null
                                                                )
                                                            )
                                                        } catch (e: StorageException) {
                                                            Log.d(
                                                                "Data",
                                                                "DownloadData: nie udało się pobrać zdjęcia ${e.message} ${e.errorCode} "
                                                            )
                                                        }
                                                    }
                                                    listaSubject.add(
                                                        Subbject(
                                                            subject.id,
                                                            subject.data["ulubione"] as Boolean,
                                                            listaFiles
                                                        )
                                                    )

                                                    Log.d(
                                                        "Data",
                                                        "downloading imges ends ${listaFiles.size}"
                                                    )
                                                }

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