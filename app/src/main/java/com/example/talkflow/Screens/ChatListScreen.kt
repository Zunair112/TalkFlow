package com.example.talkflow.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.talkflow.CommonProgressBar
import com.example.talkflow.CommonRow
import com.example.talkflow.Data.CHATS
import com.example.talkflow.Data.ChatUser
import com.example.talkflow.DestinationScreen
import com.example.talkflow.TFViewModel
import com.example.talkflow.TitleText
import com.example.talkflow.navigateto


@Composable
fun ChatListScreen(navController: NavController, vm: TFViewModel) {
    val inProgress by vm.inProcessChats
    val chats by vm.chats
    val userdata by vm.userData
    val showDialog = remember { mutableStateOf(false) }

    if (userdata == null) {
        CommonProgressBar()
        return
    }

    if (inProgress) {
        CommonProgressBar()
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog.value = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            bottomBar = {
                BottomNavigationMenu(
                    selectedItem = BottomNavigationItem.CHATLIST,
                    navController = navController
                )
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TitleText(txt = "Chats")
                    if (chats.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Chats available")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(chats) { chat ->
                                val chatUser = if (chat.user1.userId == userdata?.userId) {
                                    chat.user2
                                } else {
                                    chat.user1
                                }
                                CommonRow(
                                    imageUrl = chatUser.imageUrl,
                                    name = chatUser.name ?: "---"
                                ) {
                                    chat.chatId?.let {
                                        navController.navigate(DestinationScreen.SingleChat.createRoute(it))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    if (showDialog.value) {
        FABDialog(
            onDismiss = { showDialog.value = false },
            onAddChat = { chatNumber ->
                vm.onAddChat(chatNumber)
                showDialog.value = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FABDialog(
    onDismiss: () -> Unit,
    onAddChat: (String) -> Unit
) {
    val addChatNumber = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            addChatNumber.value = ""
        },
        confirmButton = {
            Button(onClick = {
                onAddChat(addChatNumber.value)
                addChatNumber.value = ""
            }) {
                Text(text = "Add Chat")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
        title = { Text(text = "Add Chat") },
        text = {
            OutlinedTextField(
                value = addChatNumber.value,
                onValueChange = { addChatNumber.value = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    )
}



