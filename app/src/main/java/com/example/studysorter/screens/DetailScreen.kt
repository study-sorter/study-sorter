package com.example.studysorter.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.rememberGlidePreloadingData
import com.bumptech.glide.signature.MediaStoreSignature
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.studysorter.File
import com.example.studysorter.R
import com.example.studysorter.SchoolObject
import com.example.studysorter.Subbject
import com.example.studysorter.navigation.Screens
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.rememberVerticalPdfReaderState
import kotlin.math.cos
import kotlin.math.log
import kotlin.math.roundToInt
import kotlin.math.sin


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun DetailScreen(subjectId: String?, navController: NavController, innerPadding: PaddingValues) {
    var listaSchool = SchoolObject.getData()
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid
    val chmura = FirebaseFirestore.getInstance()
    val firebaseStorage = FirebaseStorage.getInstance()
    val context = LocalContext.current
    val subjectPath = subjectId!!.replace("-", "/")
    val uploading = remember { mutableStateOf(false) }
    val subjectStoragePath = subjectPath.split("/")
        .filterIndexed { index, _ -> index in listOf(1, 3, 5) }
        .joinToString(separator = "/")
    val subjectPathList = subjectPath.split("/")
    val subjectRef = chmura.document("users/$userId/$subjectPath")
    val imageFiles = remember { mutableStateListOf<File>() }
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
                uploading.value = true

                uploadTask.addOnSuccessListener { taskSnapshot ->
                    ProcessLifecycleOwner.get().lifecycleScope.launch {
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                            imageUrl.value = uri.toString()
                        }.await()
                        taskSnapshot.storage.metadata.addOnSuccessListener { metadata ->
                            type.value = metadata.contentType.toString().split("/").last()
                        }.await()
                        subjectRef.update("files", FieldValue.arrayUnion(imageUrl.value))
                        subjectObject.imageUrls.add(File(imageUrl.value, type.value, null))
                        refreshCurrentFragment(navController, subjectId)
                    }
                    // File uploaded successfully
                }.addOnFailureListener {
                    // Handle failure
                }
            }
        }

    var sortOption by remember { mutableStateOf("Alphabetically") }

    // Sorted files
    val sortedFiles = remember(subjectObject.imageUrls, sortOption) {
        sortFiles(subjectObject.imageUrls, sortOption)
    }

    Column {
        Spacer(modifier = Modifier.height(70.dp))

        TopAppBar(
            title = { Text("Detail Screen") },
            actions = {
                SortMenu(onSortSelected = { selectedSortOption ->
                    sortOption = selectedSortOption
                })
            }
        )

        filesGridScreen(
            files = sortedFiles,
            modifier = Modifier.padding(innerPadding),
            subjectObject = subjectObject,
            subjectRef = subjectRef,
            navController = navController,
            subjectPath = subjectId
        )
        /*LazyVerticalGrid(
            columns = GridCells.Adaptive(85.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center
        ) {
            items(sortedFiles) { file ->
                Text(text = "zdjęcia")
                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .combinedClickable(
                            onClick = {
                                DetailWindow = true
                                selectedFile = file
                            },
                            onLongClick = {
                                selectedFile = file
                                deleteWindow = true
                            }
                        )
                ) {
                    // Displaying thumbnails
                    when (file.type) {
                        "jpeg", "image", "jpg", "webp" -> {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    file.Url,
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
        }*/

    }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = { pickFileLauncher.launch("*/*") },
            modifier = Modifier
                .padding(bottom = 90.dp, end = 7.dp)
                .align(Alignment.BottomEnd),
            containerColor = Color.Black,

            ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Dodaj",
                tint = Color.White
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun filesGridScreen(
    files: List<File>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    subjectObject: Subbject,
    subjectRef: DocumentReference,
    navController: NavController,
    subjectPath: String
) {
    var selectedFile by remember { mutableStateOf(File("", "", null)) }
    var DetailWindow by remember { mutableStateOf(false) }
    var deleteWindow by remember { mutableStateOf(false) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        items(items = files, key = { file -> file.Url }) { file ->
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .aspectRatio(1.5f),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AsyncImage(
                    model = if(file.type != "pdf") {
                        ImageRequest.Builder(context = LocalContext.current).data(file.Url).crossfade(true).build()
                    }else{
                        R.drawable.pdf
                    },
                    error = painterResource(R.drawable.ic_broken_image),
                    onError = { e ->
                        Log.d("TAG", "filesGridScreen: ${e.result} ")
                    },
                    placeholder = painterResource(R.drawable.loading_img),
                    contentDescription = "Obraz",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onLongClick = {
                                selectedFile = file
                                deleteWindow = true
                            },
                            onClick = {
                                DetailWindow = true
                                selectedFile = file
                            }
                        )
                )
            }
        }
    }
    if (DetailWindow) {
        Log.d("TAG", "filesGridScreen: $ ")

        detailWindow(selectedFile){
            DetailWindow = false
        }
    }
    if (deleteWindow) {
        deleteWindow(
            deleteWindow,
            selectedFile,
            subjectObject,
            subjectRef,
            navController,
            subjectPath
        )
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun deleteWindow(
    deleteWindow: Boolean,
    selectedFile: File,
    subjectObject: Subbject,
    subjectRef: DocumentReference,
    navController: NavController,
    subjectPath: String
) {
    var _deleteWindow = deleteWindow
    val firebaseStorage = FirebaseStorage.getInstance()
    ModalBottomSheet(
        onDismissRequest = {
            _deleteWindow = false
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
                            imageRef
                                .delete()
                                .addOnSuccessListener {
                                    Log.d("Del", "deleted")
                                }
                                .addOnFailureListener {
                                    Log.d("Del", "not deleted")
                                }
                            subjectRef.update("files", FieldValue.arrayRemove(file.Url))
                            subjectObject.imageUrls.remove(file)
                            refreshCurrentFragment(navController, subjectPath)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "delete",
                    tint = Color.Red
                )
                Text("Usuń")
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun detailWindow(selectedFile: File,onClose: () -> Unit) {
    Dialog(

        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {onClose()}
    ) {
        if(selectedFile.type != "pdf"){
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    onClose()
                                }
                            )
                        }
                ) {

                    val angle by remember { mutableStateOf(0f) }
                    var zoom by remember { mutableStateOf(1f) }
                    var offsetX by remember { mutableStateOf(0f) }
                    var offsetY by remember { mutableStateOf(0f) }

                    val configuration = LocalConfiguration.current
                    val screenWidth = configuration.screenWidthDp.dp.value
                    val screenHeight = configuration.screenHeightDp.dp.value

                    GlideImage(
                        model = selectedFile.Url,
                        contentDescription = "fullscreen",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                            .graphicsLayer(
                                scaleX = zoom,
                                scaleY = zoom,
                                rotationZ = angle
                            )
                            .pointerInput(Unit) {
                                detectTransformGestures(
                                    onGesture = { _, pan, gestureZoom, _ ->
                                        zoom = (zoom * gestureZoom).coerceIn(1F..4F)
//                                Log.d("TAG", "detailWindow:$pan ${pan.x} ${pan.y}")
//                                Log.d("TAG", "detailWindow:$offsetX $offsetY ")
                                        if (zoom > 1) {
                                            val x = (pan.x * zoom)
                                            val y = (pan.y * zoom)
                                            val angleRad = angle * 3.14 / 180.0
                                            offsetX =
                                                (offsetX + (x * cos(angleRad) - y * sin(angleRad)).toFloat()).coerceIn(
                                                    -(screenWidth * zoom)..(screenWidth * zoom)
                                                )
                                            offsetY =
                                                (offsetY + (x * sin(angleRad) + y * cos(angleRad)).toFloat()).coerceIn(
                                                    -(screenHeight * zoom)..(screenHeight * zoom)
                                                )
                                        } else {
                                            offsetX = 0F
                                            offsetY = 0F
                                        }
                                    }
                                )
                            }
                            .fillMaxSize()
                    )
                }

            }

        }else{

            val pdfState = rememberVerticalPdfReaderState(
                resource = ResourceType.Remote(selectedFile.Url),
                isZoomEnable = true
            )

            VerticalPDFReader(
                state = pdfState,
                modifier = Modifier
                    .background(color = Color.Transparent)
                    .fillMaxHeight()
            )
            Box(modifier = Modifier.fillMaxSize()) {
                FloatingActionButton(
                    onClick = { onClose() },
                    modifier = Modifier
                        .padding(end = 7.dp)
                        .align(Alignment.TopEnd),
                    containerColor = Color.Black,

                    ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "zamknij",
                        tint = Color.Red
                    )
                }
            }
        }
    }

}

@Composable
fun SortMenu(
    onSortSelected: (sortOption: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.List, contentDescription = "Sort Options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(onClick = {
                onSortSelected("Alphabetically")
                expanded = false
            }) {
                Text("Alphabetically")
            }
            DropdownMenuItem(onClick = {
                onSortSelected("Date (Newest First)")
                expanded = false
            }) {
                Text("Date (Newest First)")
            }
            DropdownMenuItem(onClick = {
                onSortSelected("Date (Oldest First)")
                expanded = false
            }) {
                Text("Date (Oldest First)")
            }
        }
    }
}

fun sortFiles(files: List<File>, sortOption: String): List<File> {
    return when (sortOption) {
        "Alphabetically" -> files.sortedBy { it.Url }
        "Date (Newest First)" -> files.sortedByDescending { it.Url }
        "Date (Oldest First)" -> files.sortedBy { it.Url }
        else -> files
    }
}

private fun refreshCurrentFragment(navController: NavController, subjectPath: String) {
    val id = "${Screens.Przedmioty.route}/${subjectPath}"
    Log.d("DetailScreen", "refreshCurrentFragment: ${id}")
    navController.popBackStack(id, false)
    navController.navigate(id)
}