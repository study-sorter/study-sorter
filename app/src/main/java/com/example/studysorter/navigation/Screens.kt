package com.example.studysorter.navigation



sealed class Screens(var route: String) {

    data object  Home : Screens("home")
    data object  Profile : Screens("profile")
    data object  Przedmioty : Screens("przedmioty")
    data object  Ulubione : Screens("ulubione")
    data class Detail(val subjectId: String) : Screens("${Screens.Przedmioty.route}/{subjectId}")
}