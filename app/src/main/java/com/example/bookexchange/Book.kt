package com.example.bookexchange

import com.google.firebase.Timestamp

data class Book(
    var id: String = "",
    var title: String = "",
    var author: String = "",
    var condition: String = "",
    var city: String = "",
    var contact: String = "",
    var imageUrl: String = "",
    var isFavorite: Boolean = false, // ОВА Е КЛУЧНО!,
    var ownerId: String = "", // Додај го ова поле!

    // Овие две полиња ги барат Firebase според твојот лог!
    var publisher: String = "",
    var timestamp: Timestamp? = null
)