package com.socialvibe.app.data

import com.socialvibe.app.model.Contact
import com.socialvibe.app.model.Message

object MockData {
    val contacts = listOf(
        Contact("1", "Alex", "brb, making tea", true),
        Contact("2", "Kate", "away from keyboard", false),
        Contact("3", "Nikita", "who wants pizza?", true),
        Contact("4", "Olga", "offline until Monday", false),
        Contact("5", "Dan", "*listening to music*", true)
    )

    val conversations: Map<String, List<Message>> = mapOf(
        "1" to listOf(
            Message("m1", "hey, you there?", false),
            Message("m2", "yeah, what's up", true),
            Message("m3", "wanna hop on a call later?", false)
        ),
        "2" to listOf(
            Message("m1", "sent you the files", false)
        ),
        "3" to listOf(
            Message("m1", "pizza tonight?", false),
            Message("m2", "always", true)
        ),
        "4" to emptyList(),
        "5" to listOf(
            Message("m1", "check out this track", false)
        )
    )
}
