package com.example.studysorter.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.studysorter.File
import com.example.studysorter.SchoolObject
import com.example.studysorter.Subbject
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.rememberVerticalPdfReaderState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
@Composable
fun DetailScreen(subjectId: String?, navController: NavController) {
    var listaSchool = SchoolObject.getData()
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid
    val firebaseFirestore = FirebaseFirestore.getInstance()
    val firebaseStorage = FirebaseStorage.getInstance()
    val context = LocalContext.current
    val subjectPath = subjectId!!.replace("-", "/")
    val subjectStoragePath = subjectPath.split("/")
        .filterIndexed { index, _ -> index in listOf(1, 3, 5) }
        .joinToString(separator = "/")
    val subjectPathList = subjectPath.split("/")
    var subjectObject = Subbject("", false, mutableListOf())
    for (school in listaSchool) {
        if (school.id == subjectPathList[1]) {
            for (sem in school.listaSemestr) {
                if (sem.id == subjectPathList[3]) {
                    for (sub in sem.listaSubject) {
                        if (sub.id == subjectPathList[5]) {
                            subjectObject = sub
                        }
                    }
                }
            }
        }
    }
        val pickFileLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
                // Handle the returned Uri
                uri?.let {
                    val fileName = it.lastPathSegment
                    val path = "users/$userId/$subjectStoragePath/$fileName"
                    val storageReference = firebaseStorage.reference.child(path)
                    val uploadTask = storageReference.putFile(it)
                    val imageUrl = mutableStateOf("")
                    val type = mutableStateOf("")

                        uploadTask.addOnSuccessListener { taskSnapshot ->
                            GlobalScope.launch {
                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                                imageUrl.value = uri.toString()

                            }.await()
                            taskSnapshot.storage.metadata.addOnSuccessListener { metadata->
                                type.value = metadata.contentType.toString().split("/").last()

                            }.await()
                            Log.d("DetailScreen","typ: ${type.value} url: ${imageUrl.value}")
                                subjectObject.imageUrls.add(File(imageUrl.value,type.value))
                            }
                            // File uploaded successfully

                        }.addOnFailureListener {
                            // Handle failure
                        }

                }
            }

    var listaImage = mutableListOf<File>()
    val listapdf = remember {mutableStateListOf<File>()}
    for (file in subjectObject.imageUrls){
        when (file.type) {
            "jpeg", "image", "jpg","webp" -> listaImage.add(file)
            "pdf" -> listapdf.add(file)
            else -> Log.d("DetailScreen","type not recognize: ${file.type}")
        }
    }
        Column(/*modifier = Modifier.background(Color.Red)*/) {
            Spacer(modifier = Modifier.height(70.dp))

            Box(){
                var selectedFile by remember { mutableStateOf(File("",""))}
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(85.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Center){
                    items(subjectObject.imageUrls){ File ->
                        Text(text = "zdjęcia$}")
                        Card (
                            modifier = Modifier.size(100.dp)
                        ){
                            //wyświetlanie miniatórek
                            when(File.type){
                                "jpeg", "image", "jpg","webp" -> {
                                    //tu jest wyświetlanie zdjęć
                                    Image(
                                        painter =  rememberAsyncImagePainter(File.Url,placeholder = null),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .combinedClickable(
                                                onClick = {
                                                    selectedFile = File
                                                }
                                            ),
                                    )
                                }
                                "pdf"->{
                                    //tu jest wyświetlanie pdfów
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "pdf",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    selectedFile = File
                                                }
                                            )
                                            .fillMaxSize()
                                    )
                                }

                            }


                        }
                    }

                }
                if (selectedFile.Url != ""){
                    selectedFile.let { file ->
                        //wyświetlanie powiększonej wersji

                        Surface(
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures { offset: Offset ->
                                        // Handle click outside of images
                                        selectedFile = File("", "")
                                    }
                                },
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0f),

                        ) {
                            when(file.type){
                                "jpeg", "image", "jpg","webp" -> {
                                    //zdjęcia
                                    var scale by remember { mutableStateOf(1f) }
                                    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
                                    Image(
                                        painter =  rememberAsyncImagePainter(file.Url,placeholder = null),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .pointerInput(Unit) {
                                                detectTransformGestures { _, pan, zoom, _ ->
                                                    // Update the scale based on zoom gestures.
                                                    scale *= zoom

                                                    // Limit the zoom levels within a certain range (optional).
                                                    scale = scale.coerceIn(1f, 3f)

                                                    // Update the offset to implement panning when zoomed.
                                                    offset = if (scale == 1f) Offset(
                                                        0f,
                                                        0f
                                                    ) else offset + pan
                                                }
                                            }
                                            .graphicsLayer(
                                                scaleX = scale, scaleY = scale,
                                                translationX = offset.x, translationY = offset.y
                                            )
                                    )
                                }
                                "pdf" ->{
                                    //pdfy

                                    val pdfState = rememberVerticalPdfReaderState(
                                        resource = ResourceType.Remote(file.Url),
                                        isZoomEnable = true
                                    )
                                    VerticalPDFReader(
                                        state = pdfState,
                                        modifier = Modifier
                                            .background(color = Color.Transparent)
                                            .size(400.dp)
                                    )
                                }

                            }
                        }
                    }

                }
            }
        }




        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { pickFileLauncher.launch("*/*") },
                modifier = Modifier
                    .padding(bottom = 90.dp, end = 7.dp)
                    .align(Alignment.BottomEnd),
                containerColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Dodaj",
                    tint = Color.White
                )
            }
        }
}
@Preview(showBackground = true)
@Composable
fun Preview() {
    Surface(modifier = Modifier.size(300.dp)) {

        val pdfState = rememberVerticalPdfReaderState(
            resource = ResourceType.Remote("https://firebasestorage.googleapis.com/v0/b/study-sorter.appspot.com/o/users%2FkkBHfCrAr6gMfaW0yT3NGDV97dj2%2Fszkola%20%2Fsem1%2Fprzedmiotjeszcz%2Fmsf%3A68?alt=media&token=dc486801-7cc6-4531-a86b-770869ca3f19"),
            isZoomEnable = true
        )
        VerticalPDFReader(
            state = pdfState,
            modifier = Modifier
                .background(color = Color.Transparent)
                .size(400.dp)


        )
    }
}