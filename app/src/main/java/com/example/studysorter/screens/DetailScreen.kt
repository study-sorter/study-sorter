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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.studysorter.File
import com.example.studysorter.SchoolObject
import com.example.studysorter.Subbject
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.rememberVerticalPdfReaderState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalPagerApi::class,
    ExperimentalMaterial3Api::class
)
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
                        taskSnapshot.storage.metadata.addOnSuccessListener { metadata ->
                            type.value = metadata.contentType.toString().split("/").last()

                        }.await()
                        Log.d("DetailScreen", "typ: ${type.value} url: ${imageUrl.value}")
                        subjectObject.imageUrls.add(File(imageUrl.value, type.value))
                    }
                    // File uploaded successfully

                }.addOnFailureListener {
                    // Handle failure
                }

            }
        }

    var listaImage = mutableListOf<File>()
    val listapdf = remember { mutableStateListOf<File>() }
    for (file in subjectObject.imageUrls) {
        when (file.type) {
            "jpeg", "image", "jpg", "webp" -> listaImage.add(file)
            "pdf" -> listapdf.add(file)
            else -> Log.d("DetailScreen", "type not recognize: ${file.type}")
        }
    }
    Column {
        Spacer(modifier = Modifier.height(70.dp))

        Box {
            var selectedFile by remember { mutableStateOf(File("", "")) }
            var deleteWindow by remember { mutableStateOf(false) }
            var DetailWindow by remember { mutableStateOf(false) }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(85.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.Center
            ) {
                items(subjectObject.imageUrls) { File ->
                    Text(text = "zdjęcia$}")
                    Card(
                        modifier = Modifier
                            .size(100.dp)
                            .combinedClickable(
                                onClick = {
                                    DetailWindow = true
                                    selectedFile = File
                                },
                                onLongClick = {
                                    selectedFile = File
                                    deleteWindow = true
                                }
                            )
                    ) {
                        //wyświetlanie miniatórek
                        when (File.type) {
                            "jpeg", "image", "jpg", "webp" -> {
                                //tu jest wyświetlanie zdjęć
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        File.Url,
                                        placeholder = null
                                    ),
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                )
                            }

                            "pdf" -> {
                                //tu jest wyświetlanie pdfów
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "pdf",
                                    tint = Color.Red,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                        }


                    }
                }

            }
            if (DetailWindow) {
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
                        when (file.type) {
                            "jpeg", "image", "jpg", "webp" -> {
                                //zdjęcia
                                var scale by remember { mutableStateOf(1f) }
                                var offset by remember { mutableStateOf(Offset(0f, 0f)) }
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        file.Url,
                                        placeholder = null
                                    ),
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

                            "pdf" -> {
                                //pdfy

                                val pdfState = rememberVerticalPdfReaderState(
                                    resource = ResourceType.Remote(file.Url),
                                    isZoomEnable = true
                                )
                                VerticalPDFReader(
                                    state = pdfState,
                                    modifier = Modifier
                                        .background(color = Color.Transparent)
                                        .fillMaxHeight()
                                )
                            }

                        }
                    }
                }

            }
            if (deleteWindow) {
                ModalBottomSheet(
                    onDismissRequest = {
                        deleteWindow = false
                    },
                    sheetState = rememberModalBottomSheetState(true)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(start = 20.dp, bottom = 50.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .clickable {
                                    selectedFile.let { file ->
                                        val imageRef = firebaseStorage.getReferenceFromUrl(file.Url)
                                        imageRef.delete()
                                            .addOnSuccessListener {
                                                Log.d("Del","deleted")

                                            }
                                            .addOnFailureListener(){
                                                Log.d("Del","not deleted")
                                            }
                                        subjectObject.imageUrls.remove(file)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "delete", tint = Color.Red)
                            Text("Usuń")
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
