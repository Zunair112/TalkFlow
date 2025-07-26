package com.example.talkflow

import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.talkflow.Data.CHATS
import com.example.talkflow.Data.ChatData
import com.example.talkflow.Data.ChatUser
import com.example.talkflow.Data.Event
import com.example.talkflow.Data.Message
import com.example.talkflow.Data.Message_Node
import com.example.talkflow.Data.USER_NODE
import com.example.talkflow.Data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.core.ListenerRegistrationImpl
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class TFViewModel @Inject constructor(
    val auth: FirebaseAuth,
    var db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    var inProcess = mutableStateOf(false)
    var inProcessChats = mutableStateOf(false)
    val eventMutableState = mutableStateOf(Event(""))
    var signIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration? = null

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        Log.d("TFViewModel", "Current User: $currentUser")
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun populateMessages(chatId: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener = db.collection(CHATS).document(chatId)
            .collection(Message_Node).addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chatMessages.value = value.documents.mapNotNull { it.toObject<Message>() }.sortedBy { it.timestamp }
                    inProgressChatMessage.value = false
                }
            }
    }

    fun depopulateMessages() {
        chatMessages.value = listOf()
        currentChatMessageListener = null
    }

    fun populateChats() {
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId),
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull { it.toObject<ChatData>() }
            }
            inProcessChats.value = false
        }
    }

    fun onSendReply(chatId: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(
            userData.value?.userId, message,
            time
        )
        db.collection(CHATS).document(chatId).collection(Message_Node).document().set(msg)
        Log.d("TFViewModel", "Message sent: $msg")
    }

    fun signup(name: String, number: String, email: String, password: String) {
        inProcess.value = true
        if (name.isEmpty() || number.isEmpty() || email.isEmpty() || password.isEmpty()) {
            handleException(customMessage = "Please fill all the fields")
            return
        }

        db.collection(USER_NODE).whereEqualTo("number", number).get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            signIn.value = true
                            createOrUpdateProfile(name, number)
                        } else {
                            handleException(it.exception, customMessage = "Signup Failed")
                        }
                    }
                } else {
                    handleException(customMessage = "Number already exists")
                    inProcess.value = false
                }
            }
    }

    fun logIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            handleException(customMessage = "Please fill all the fields")
            return
        }
        inProcess.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    signIn.value = true
                    inProcess.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                } else {
                    handleException(it.exception, customMessage = "Login Failed")
                }
            }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

    fun uploadImage(uri: Uri?, onSuccess: (Uri) -> Unit) {
        inProcess.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri!!)
        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
        }.addOnFailureListener {
            handleException(it)
        }
    }

    fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: this.userData.value?.name,
            number = number ?: this.userData.value?.number,
            imageUrl = imageUrl ?: this.userData.value?.imageUrl
        )
        uid?.let {
            inProcess.value = true
            db.collection(USER_NODE).document(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    db.collection(USER_NODE).document(uid).set(userData)
                } else {
                    db.collection(USER_NODE).document(uid).set(userData)
                }
                inProcess.value = false
                getUserData(uid)
            }.addOnFailureListener {
                handleException(it, "Cannot Retrieve User Data")
            }
        }
    }

    private fun getUserData(uid: String) {
        inProcess.value = true
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, "Cannot Retrieve User Data")
            }
            if (value != null) {
                val user = value.toObject<UserData>()
                userData.value = user
                inProcess.value = false
                Log.d("TFViewModel", "User data populated: $user")
                if (user != null) {
                    populateChats()
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        depopulateMessages()
        currentChatMessageListener = null
        eventMutableState.value = Event("Logged out")
    }

    fun onAddChat(number: String) {
        if (number.isEmpty() || !number.isDigitsOnly()) {
            handleException(customMessage = "Number must be digit Only")
            return
        }
        db.collection(CHATS).where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("user1.number", number),
                    Filter.equalTo("user2.number", userData.value?.number)
                ),
                Filter.and(
                    Filter.equalTo("user1.number", userData.value?.number),
                    Filter.equalTo("user2.number", number)
                )
            )
        ).get().addOnSuccessListener {
            if (it.isEmpty) {
                db.collection(USER_NODE).whereEqualTo("number", number).get()
                    .addOnSuccessListener {
                        if (it.isEmpty) {
                            handleException(customMessage = "User not found")
                        } else {
                            val chatPartner = it.documents[0].toObject<UserData>()
                            val id = db.collection(CHATS).document().id
                            val chat = ChatData(
                                chatId = id,
                                ChatUser(
                                    userData.value?.userId,
                                    userData.value?.name,
                                    userData.value?.imageUrl,
                                    userData.value?.number,
                                ),
                                ChatUser(
                                    chatPartner?.userId,
                                    chatPartner?.name,
                                    chatPartner?.imageUrl,
                                    chatPartner?.number,
                                )
                            )
                            db.collection(CHATS).document(id).set(chat)
                        }
                    }.addOnFailureListener {
                        handleException(it)
                    }
            } else {
                handleException(customMessage = "Chat already exists")
            }
        }.addOnFailureListener { exception ->
            handleException(exception)
        }
    }

    fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("TalkFlowApp", "Talk Flow exception: ", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        eventMutableState.value = Event(message)
        inProcess.value = false
    }
}
