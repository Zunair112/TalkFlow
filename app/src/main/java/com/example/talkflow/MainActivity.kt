package com.example.talkflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.talkflow.Screens.ChatListScreen
import com.example.talkflow.Screens.LoginScreen
import com.example.talkflow.Screens.ProfileScreen
import com.example.talkflow.Screens.SignupScreen
import com.example.talkflow.Screens.SingleChatScreen
//import com.example.talkflow.Screens.StatusScreen
import com.example.talkflow.ui.theme.TalkFlowTheme
import dagger.hilt.android.AndroidEntryPoint


sealed class DestinationScreen(var route : String){

object  Signup : DestinationScreen  ( "Signup")
object  Login : DestinationScreen("Login")
object  Profile : DestinationScreen("Profile")
object  ChatList : DestinationScreen("ChatList")
object  SingleChat : DestinationScreen("SingleChat/{chatid}") {
    fun createRoute(Id: String) = "SingleChat/$Id"
}
object StatusList : DestinationScreen("StatusList")

object SingleStatus : DestinationScreen("SingleStatus/{userid}"){

        fun createRoute(userId: String) = "SingleStatus/$userId"
}




}
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TalkFlowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                }
                ChatAppNavigation()
            }
        }
    }

    @Composable
    fun ChatAppNavigation() {

        val navController = rememberNavController()
        var vm = hiltViewModel<TFViewModel>()
        NavHost(navController = navController, startDestination = DestinationScreen.Signup.route) {

            composable(DestinationScreen.Signup.route) {
                SignupScreen(navController, vm)
            }
            composable(DestinationScreen.Login.route) {
                LoginScreen(navController= navController, vm= vm)

            }


            composable(DestinationScreen.ChatList.route) {
                ChatListScreen(navController = navController, vm= vm )

            }

           // composable(DestinationScreen.StatusList.route) {
               // StatusScreen(navController = navController, vm= vm)

           // }

            composable(DestinationScreen.Profile.route){
                ProfileScreen(navController = navController, vm= vm)

            }
            composable(DestinationScreen.SingleChat.route){
                val ChatId=it.arguments?.getString("ChatId")
                ChatId?.let {
                    SingleChatScreen(navController = navController ,   vm=vm, chatId = ChatId)
                }

            }



        }
    }
}

