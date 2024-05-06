package com.example.studysorter.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DetailScreen(subjectId: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (subjectId != null) {
            Text(text = "Subject Details for ID: $subjectId", color = Color.Black)
        } else {
            Text(text = "Invalid Subject ID", color = Color.Red)
        }
    }
}

@Preview
@Composable
fun PreviewDetailScreen() {
    DetailScreen("Math")
}

