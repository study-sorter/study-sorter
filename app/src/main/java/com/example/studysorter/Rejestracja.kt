package com.example.studysorter

import android.app.backup.FileBackupHelper
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studysorter.ui.theme.StudySorterTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


class Rejestracja : AppCompatActivity() {
    private val AuthInst :FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent{
            StudySorterTheme {
                Surface(
                    color = colorResource(id = R.color.tlo_aplikacja),
                    modifier = Modifier.fillMaxSize()
                ){
                    zarejestru()
                }

            }
        }
        /* do usunięcia
        setContentView(R.layout.activity_rejestracja)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun rejestracja(view: View) {//pobiera maila i haslo i loguje osobe na razie bez sprawdzania czegokolwiek procz pustego
        val email: String = findViewById<TextView>(R.id.Email_Logowanie).text.toString()
        val haslo: String = findViewById<TextView>(R.id.Haslo_Logowanie).text.toString()
        if (email.isNotEmpty() && haslo.isNotEmpty()) {
            AuthInst.createUserWithEmailAndPassword(email, haslo)
                .addOnSuccessListener {
                    intent = Intent(this, Logowanie::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Log.d("LOG_DEBUD_REJESTRACJA",it.message.toString())
                    Toast.makeText(this,it.message.toString(), Toast.LENGTH_LONG).show()
                }
        }
         */
    }



}

data class User(val Email:String,val User_UID:String, val Nickname:String)
@Preview
@Composable
private fun zarejestru() {
    val mycontext = LocalContext.current
    var hasloWidocznosc by rememberSaveable { mutableStateOf(false) }
    var mail by remember { mutableStateOf("") }
    var haslo by remember { mutableStateOf("") }
    var haslopowt by remember { mutableStateOf("") }
    var Nickname by remember { mutableStateOf("") }
    val chmura = FirebaseFirestore.getInstance()


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.wrapContentHeight().fillMaxSize()

    ) {
        Box {
            Text(
                stringResource(R.string.teskt_Rejestracja),
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            )
        }
        Box {
            OutlinedTextField(
                value = Nickname,
                onValueChange = { Nickname = it },
                modifier = Modifier.padding(top = 50.dp),
                singleLine = true,
                placeholder = { Text(text = "Nickname") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    focusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                ),
            )
        }
        Box {
            OutlinedTextField(
                value = mail,
                onValueChange = { mail = it },
                modifier = Modifier.padding(top = 15.dp),
                singleLine = true,
                placeholder = { Text(text = stringResource(id = R.string.mail)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    focusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                )
            )
        }
        Box {
            OutlinedTextField(
                value = haslo,
                onValueChange = { haslo = it },
                modifier = Modifier.padding(top =15.dp),
                singleLine = true,
                placeholder = { Text(text = stringResource(id = R.string.haslo)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (hasloWidocznosc) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    focusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                ),
                trailingIcon = {
                    val image = if (hasloWidocznosc)
                        Icons.Outlined.Visibility
                    else Icons.Outlined.VisibilityOff

                    // Please provide localized description for accessibility services
                    val description = if (hasloWidocznosc) "Hide password" else "Show password"

                    IconButton(onClick = { hasloWidocznosc = !hasloWidocznosc }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
        }
        /*Box {
            OutlinedTextField(
                value = haslopowt,
                onValueChange = {
                    haslopowt = it
                    TODO("dodanie sprawdzania hasła")

                                },
                modifier = Modifier.padding(top = 15.dp),
                singleLine = true,
                placeholder = { Text(text = "Powtórz hasło") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (hasloWidocznosc) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    focusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                ),
                trailingIcon = {
                    val image = if (hasloWidocznosc)
                        Icons.Outlined.Visibility
                    else Icons.Outlined.VisibilityOff

                    // Please provide localized description for accessibility services
                    val description = if (hasloWidocznosc) "Hide password" else "Show password"

                    IconButton(onClick = { hasloWidocznosc = !hasloWidocznosc }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
        }*/
        Row(verticalAlignment = Alignment.CenterVertically) {
            /*
            Box {
                OutlinedTextField(
                    value = imie,
                    onValueChange = { imie = it },
                    modifier = Modifier.padding(top = 15.dp).width(OutlinedTextFieldDefaults.MinWidth/2),
                    singleLine = true,
                    placeholder = { Text(text = "imie") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                        focusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    ),
                )
            }
            Box {
                OutlinedTextField(
                    value = nazwisko,
                    onValueChange = { nazwisko = it },
                    modifier = Modifier.padding(top = 15.dp).width(OutlinedTextFieldDefaults.MinWidth/2),
                    singleLine = true,
                    placeholder = { Text(text = "nazwisko") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                        focusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    ),
                )
            }
             */
        }

        Button(
            onClick = {
                if (mail.isNotEmpty() && haslo.isNotEmpty()) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail, haslo)
                        .addOnSuccessListener {
                            val currentUser = Firebase.auth.currentUser
                            currentUser?.let {
                                val name = currentUser.displayName
                                val email = currentUser.email
                                val photoUrl = currentUser.photoUrl

                                val emailVerified = currentUser.isEmailVerified

                                val uid = currentUser.uid

                                // Create a new user object
                                val nowy: User = User(currentUser.email.toString(), currentUser.uid, Nickname)

                                // Save the user to Firestore
                                val userMap = hashMapOf(
                                    "nickname" to Nickname,
                                    "email" to currentUser.email
                                )

                                chmura.collection("users").document(currentUser.uid)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "DocumentSnapshot successfully written!")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("Firestore", "Error writing document", e)
                                    }
                            }

                            mycontext.startActivity(Intent(mycontext, Logowanie::class.java))
                        }
                        .addOnFailureListener { exception ->
                            Log.d("LOG_DEBUD_REJESTRACJA_FIREBASE", exception.message.toString())
                            Log.e("Registration", "Failed: ${exception.message}", exception)
                            Toast.makeText(mycontext, "Registration failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }

                }
            },
            Modifier
                .width(OutlinedTextFieldDefaults.MinWidth)
                .height(OutlinedTextFieldDefaults.MinHeight - 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.tlo_przycisk))
        ) {
            Text(stringResource(id = R.string.stworz))
        }

    }
}