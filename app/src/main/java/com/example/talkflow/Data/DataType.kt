package com.example.talkflow.Data

data class UserData(
    var userId: String? = null,
    var name: String? = null,
    var number: String? = null,
    var imageUrl: String? = null,
) {
    fun toMap() {
        mapOf(
            "userId" to userId,
            "name" to name,
            "number" to number,
            "imageUrl" to imageUrl,
        )
    }
}

data class ChatData(
    val chatId: String? ="",
    val user1: ChatUser= ChatUser(),
    val user2: ChatUser= ChatUser()
)


data class ChatUser(
    val userId: String? = null,
    val name: String? = null,
    val imageUrl: String? = null,
    val number: String? = null
)

data class Message(
    var sendBy: String? = "",
    val message: String? = "",
    val timestamp: String? = ""
)