package com.example.studysorter

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Szkola(var id: String,var listaSemestr:List<Semestr>)
data class Semestr(var id:String,var listaSubject: List<Subbject>)
data class Subbject(var id:String) //jest Subbject bo koliduje z jakąś gotow klasą
class SchoolRepository {
    private var listaSzkola:MutableList<Szkola> = mutableListOf()

    fun getData():List<Szkola>{
        Log.d("DataGet","Getting Data")
        return listaSzkola
    }
    fun DownloadData(){
        Log.d("Data","Downloading Data")
        val chmura = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        listaSzkola.clear()
        if(currentUser != null){
            chmura
                .collection("users").document(currentUser.uid)
                .collection("szkoly").get().addOnSuccessListener { schoolSnaphot ->
                    Log.d("Data","semesterSnapshot size: ${schoolSnaphot.size()}")

                    for (school in schoolSnaphot){

                        var listaSemestr = mutableListOf<Semestr>()

                        school.reference
                            .collection("semestry")
                            .get().addOnSuccessListener { semestrSnapshot->
                                Log.d("Data","semesterSnapshot size: ${semestrSnapshot.size()}")
                                for (semestr in semestrSnapshot){

                                    var listaSubject = mutableListOf<Subbject>()

                                    semestr.reference
                                        .collection("przedmioty")
                                        .get().addOnSuccessListener { subjectSnapshot ->
                                            Log.d("Data","semesterSnapshot size: ${subjectSnapshot.size()}")
                                            for (subject in subjectSnapshot){
                                                listaSubject.add(Subbject(subject.id))
                                            }
                                        }.addOnFailureListener{ e -> Log.d("Data","Downloading data incomplete(Subject) \"${e.message}\" ") }
                                    listaSemestr.add(Semestr(semestr.id, listaSubject))
                            }
                        }.addOnFailureListener{ e -> Log.d("Data","Downloading data incomplete(Sem) \"${e.message}\" ") }
                        listaSzkola.add(Szkola(school.id, listaSemestr))
                    }
                }.addOnFailureListener{ e -> Log.d("Data","Downloading data incomplete(School) \"${e.message}\" ")}
            Log.d("Data","Downloading Data Complete")

        }
    }
}


val SchoolObject = SchoolRepository()