package com.example.studysorter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener
import java.util.*

@Composable
fun HomeScreen(innerPadding: PaddingValues) {
    val calendar = remember { mutableStateOf(Calendar.getInstance()) }
    val openDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(factory = { context ->
            CalendarView(context).apply {
                setOnDateChangeListener(OnDateChangeListener { _, year, month, dayOfMonth ->
                    calendar.value.set(year, month, dayOfMonth)
                })
            }
        })

        Button(onClick = { openDialog.value = true }) {
            Text("Add Event")
        }

        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = { openDialog.value = false },
                confirmButton = {},
                dismissButton = {},
                text = {
                    AndroidView(factory = { context ->
                        CalendarView(context).apply {
                            setOnDateChangeListener(OnDateChangeListener { _, year, month, dayOfMonth ->
                                calendar.value.set(year, month, dayOfMonth)
                                openDialog.value = false
                            })
                        }
                    })
                }
            )
        }
    }
}
