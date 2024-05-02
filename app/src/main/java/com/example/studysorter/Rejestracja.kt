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
import androidx.compose.ui.graphics.Color
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

    }



}

data class User(val Email:String,val User_UID:String, val Nickname:String)
@Preview
@Composable
private fun zarejestru() {
    val mycontext = LocalContext.current

    var mail by remember { mutableStateOf("") }
    var haslo by remember { mutableStateOf("") }
    var haslopowt by remember { mutableStateOf("") }
    var Nickname by remember { mutableStateOf("") }
    val chmura = FirebaseFirestore.getInstance()

    var hasloWidocznosc by rememberSaveable { mutableStateOf(false) }
    var haslopowWidocznosc by rememberSaveable { mutableStateOf(false) }


    val isNicknameCorrect = remember { mutableStateOf(true)}
    val isMailCorrect = remember { mutableStateOf(true)}
    val isHasloCorrect = remember { mutableStateOf(true)}
    val isHasloPowtCorrect = remember { mutableStateOf(true)}
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxSize()
    ) {
        Box {
            Text(
                stringResource(R.string.teskt_Rejestracja),
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            )
        }
        /*co się znajduje w danym boxie można sobie zwinąć i kod jest czytelniejszy*/
        /*nick*/Box {
            OutlinedTextField(
                value = Nickname,
                onValueChange = {
                    Nickname = it
                    isNicknameCorrect.value = true
                },
                modifier = Modifier.padding(top = 50.dp),
                singleLine = true,
                placeholder = { Text(text = "Nickname") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    focusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    unfocusedBorderColor = if (isNicknameCorrect.value)  Color.Unspecified else Color.Red ,
                )
            )
        }
        /*mail*/Box {
           OutlinedTextField(
                value = mail,
                onValueChange = { mail = it
                                isMailCorrect.value = true},
                modifier = Modifier.padding(top = 15.dp),
                singleLine = true,
                placeholder = { Text(text = stringResource(id = R.string.mail)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    focusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    unfocusedBorderColor = if (isMailCorrect.value)  Color.Unspecified else Color.Red ,
                )
            )
        }
        /*haslo*/Box {
            OutlinedTextField(
                value = haslo,
                onValueChange = {
                    haslo = it
                    isHasloCorrect.value = true
                },
                modifier = Modifier.padding(top =15.dp),
                singleLine = true,
                placeholder = { Text(text = stringResource(id = R.string.haslo)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (hasloWidocznosc) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    focusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    unfocusedBorderColor = if (isHasloCorrect.value)  Color.Unspecified else Color.Red ,
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
        /*powtorz hasło*/Box {
            OutlinedTextField(
                value = haslopowt,
                onValueChange = {
                    haslopowt = it
                    isHasloPowtCorrect.value  =true
                },
                modifier = Modifier.padding(top = 15.dp).padding(bottom = 20.dp),
                singleLine = true,
                placeholder = { Text(text = "Powtórz hasło") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (haslopowWidocznosc) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    focusedContainerColor = colorResource(id = R.color.tlo_polaTekstowe),
                    unfocusedBorderColor = if (isHasloPowtCorrect.value)  Color.Unspecified else Color.Red ,
                ),
                trailingIcon = {
                    val image = if (haslopowWidocznosc)
                        Icons.Outlined.Visibility
                    else Icons.Outlined.VisibilityOff

                    // Please provide localized description for accessibility services
                    val description = if (haslopowWidocznosc) "Hide password" else "Show password"

                    IconButton(onClick = { haslopowWidocznosc = !haslopowWidocznosc }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
        }
        /*stare można wyrzucić jest tu imie i nazwisko*/Row(verticalAlignment = Alignment.CenterVertically) {
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
        /*przycisk potwierdziający*/Button(
            onClick = {
                //warunki sprawdzane przed rejestracją
                if (Nickname.isEmpty()){ isNicknameCorrect.value = false}
                if (mail.isEmpty()){ isMailCorrect.value = false}
                if (haslo.isEmpty()){ isHasloCorrect.value = false}
                if (haslopowt.isEmpty()){ isHasloPowtCorrect.value = false}
                if (!haslopowt.equals(haslo)){
                    isHasloCorrect.value = false
                    isHasloPowtCorrect.value = false
                    Toast.makeText(mycontext, "hasła niezgadzają się ", Toast.LENGTH_SHORT).show()
                }
                if (haslo.length < 6 && isHasloCorrect.value ){
                    isHasloCorrect.value = false
                    isHasloPowtCorrect.value = false
                    Toast.makeText(mycontext, "hasło jest za krótkie powinno mieć co najmniej 6 znaków ", Toast.LENGTH_SHORT).show()
                }

                //sprawdzanie czy pola są wypełnione dobrze zakładając warunki sprawdzane wcześniej
                if(
                    isNicknameCorrect.value
                    && isMailCorrect.value
                    && isHasloCorrect.value
                    && isHasloPowtCorrect.value
                ) {
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
                                    val nowy: User = User(
                                        currentUser.email.toString(),
                                        currentUser.uid,
                                        Nickname
                                    )

                                    // Save the user to Firestore
                                    val userMap = hashMapOf(
                                        "nickname" to Nickname,
                                        "email" to currentUser.email
                                    )

                                    chmura.collection("users").document(currentUser.uid)
                                        .set(userMap)
                                        .addOnSuccessListener {
                                            Log.d(
                                                "Firestore",
                                                "DocumentSnapshot successfully written!"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("Firestore", "Error writing document", e)
                                        }
                                }

                                mycontext.startActivity(Intent(mycontext, Logowanie::class.java))
                            }
                            .addOnFailureListener { exception ->
                                Log.d(
                                    "LOG_DEBUD_REJESTRACJA_FIREBASE",
                                    exception.message.toString()
                                )
                                Log.e("Registration", "Failed: ${exception.message}", exception)
                                Toast.makeText(
                                    mycontext,
                                    "Registration failed: ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
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