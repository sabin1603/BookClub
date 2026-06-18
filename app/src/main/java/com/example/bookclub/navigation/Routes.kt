package com.example.bookclub.navigation

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Home = "home"
    const val BookSearch = "bookSearch"

    const val CreateRoom = "createRoom?bookId={bookId}"
    const val RoomDetails = "roomDetails/{roomId}"
    const val RoomSettings = "roomSettings/{roomId}"

    fun createRoom(bookId: Long? = null): String {
        return if (bookId == null) {
            "createRoom"
        } else {
            "createRoom?bookId=$bookId"
        }
    }

    fun roomDetails(roomId: Long): String {
        return "roomDetails/$roomId"
    }

    fun roomSettings(roomId: Long): String {
        return "roomSettings/$roomId"
    }
}