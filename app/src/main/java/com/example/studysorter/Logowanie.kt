package com.example.studysorter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.*
import androidx.compose.material3.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.studysorter.ui.theme.StudySorterTheme
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast



class Logowanie : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_logowanie)

        setContent{
                Surface(
                    color = colorResource(id = R.color.tlo_aplikacja),
                    modifier = Modifier.fillMaxSize()
                ){
                        Zaloguj()
                }

        }
// tymczasowe można usunąć komentarze yyy nie wiem czy to zadziała na razie nie ruszać XD
//        FirebaseAuth.getInstance().currentUser?.let { auth -> //jeśli null to się nie wykona
//            intent = Intent(this, MainActivity::class.java).apply {
//                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)//wyczyszczenie tej aktywności czyli że nie można się tu cofnąć
//            }
//            startActivity(intent)
//        }

    }
}

@Preview
@Composable
fun Zaloguj() {
    val mycontext = LocalContext.current
    var hasloWidocznosc by rememberSaveable { mutableStateOf(false) }
    var mail by remember { mutableStateOf("") }
    var haslo by remember { mutableStateOf("") }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.wrapContentHeight()

    ) {
        Box {
            Text(
                stringResource(R.string.powitanie),
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            )
        }
        Box {
            Text(
                stringResource(R.string.app_name),
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            )
        }
        Box() {
            OutlinedTextField(
                value = mail,
                onValueChange = { mail = it },
                modifier = Modifier.padding(top = 50.dp),
                singleLine = true,
                placeholder = { Text(text = stringResource(id = R.string.mail)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Email icon",
                    )
                },
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
                modifier = Modifier.padding(25.dp),
                singleLine = true,
                placeholder = { Text(text = stringResource(id = R.string.haslo)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "hasło icon",
                    )
                },
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
        Button(
            onClick =
            {
                if (mail.isNotEmpty() && haslo.isNotEmpty()) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(mail, haslo)
                        .addOnSuccessListener {
                            SchoolObject.DownloadData()

                            mycontext.startActivity(
                                Intent(
                                    mycontext,
                                    MainActivity::class.java
                                ).apply {
                                    flags =(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)//wyczyszczenie tej aktywności czyli że nie można się tu cofnąć
                                })
                        }
                        .addOnFailureListener {
                            Log.d("LOG_DEBUD_LOGOWANIE", it.message.toString())
                            Toast.makeText(mycontext, "coś poszło nie tak przy logowaniu", Toast.LENGTH_SHORT).show()

                        }
                }else{
                    Toast.makeText(mycontext, "nie podałeś/aś hasła lub e-maila", Toast.LENGTH_SHORT).show()
                }
            },
            Modifier
                .width(OutlinedTextFieldDefaults.MinWidth)
                .height(OutlinedTextFieldDefaults.MinHeight - 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.tlo_przycisk))

        ) {
            Text(stringResource(id = R.string.zaloguj))
        }

        Box(
            modifier = Modifier.padding(top = 60.dp)
        ) {
            Text(stringResource(id = R.string.brak_konta))
        }
        Box(modifier = Modifier.padding(top =10.dp)) {
                Text(stringResource(id = R.string.zarejestruj),
                    modifier = Modifier
                        .clickable(enabled = true) {
                            mycontext.startActivity(Intent(mycontext,Rejestracja::class.java))
                        },
                    fontWeight = FontWeight.Bold

                )

        }
    }
}
