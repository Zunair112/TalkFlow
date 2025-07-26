package com.example.talkflow.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.talkflow.CommonProgressBar
import com.example.talkflow.DestinationScreen
import com.example.talkflow.R
import com.example.talkflow.TFViewModel
import com.example.talkflow.checkSignedIn
import com.example.talkflow.navigateto

@Composable
fun LoginScreen(vm: TFViewModel, navController: NavController)
    {
        checkSignedIn(vm = vm, navController= navController)

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight()
                    .verticalScroll(
                        rememberScrollState()
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                val emailState = remember {
                    mutableStateOf(TextFieldValue())
                }
                val passwordState = remember {
                    mutableStateOf(TextFieldValue())
                }

                val focus= LocalFocusManager.current

                Image(
                    painter = painterResource(id = R.drawable.meetme), contentDescription = null,
                    modifier = Modifier
                        .width(200.dp)
                        .padding(top = 16.dp)
                        .padding(8.dp)
                )

                Text(
                    text = " Sign In",
                    fontSize = 30.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )

                OutlinedTextField (
                    value = emailState.value,
                    onValueChange = { newValue ->
                        emailState.value = newValue
                        emailState.value
                    },
                    label = { Text(text = "Email") },
                    modifier = Modifier.padding(8.dp)


                )
                OutlinedTextField(
                    value = passwordState.value,
                    onValueChange = { newValue ->
                        passwordState.value = newValue
                        passwordState.value
                    },
                    label = { Text(text = "Password") },
                    modifier = Modifier.padding(8.dp)


                )

                Button(onClick = {
                    vm.logIn(
                        emailState.value.text,
                        passwordState.value.text
                    ) 

                },
                    modifier = Modifier.padding(8.dp)) {
                    Text(text = "Sign IN")

                }

                Text(text = "New User? Go to SignUp - >",
                    color = Color.Blue,
                    modifier = Modifier.padding(8.dp)
                        .clickable {
                            navigateto( navController, DestinationScreen.Signup.route)
                        }


                )

            }

        }
        if (vm.inProcess.value){
            CommonProgressBar()

        }

    }