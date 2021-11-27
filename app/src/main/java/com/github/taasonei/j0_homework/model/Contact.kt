package com.github.taasonei.j0_homework.model

import com.github.taasonei.j0_homework.R

data class Contact(
    val name: String,
    val photo: Int,
    val phone: List<String>,
    val email: List<String>,
    val description: String
)

fun getContactsInfo(): List<Contact> {
    return listOf(
        Contact(
            "Иван Иванов",
            R.drawable.example_avatar,
            listOf("+79998887766", "+79997778866"),
            listOf("ivanov@test.com", "ivanov2@test.com"),
            "Какое-то очень важное описание"
        ),
        Contact(
            "Марина Маринина",
            R.drawable.example_avatar,
            listOf("+79995554433", "+79994445533"),
            listOf("marinina@test.com", "marinina2@test.com"),
            "Какое-то очень важное описание и еще немного описания"
        ),
        Contact(
            "Петр Петров",
            R.drawable.example_avatar,
            listOf("+79998882211", "+79992228811"),
            listOf("petrov@test.com", "petrov2@test.com"),
            "Рыба описания рыба описания рыба описания рыба описания рыба описания"
        ),
        Contact(
            "Екатерина Катеринина",
            R.drawable.example_avatar,
            listOf("+79991117744", "+79997771144"),
            listOf("katerinina@test.com", "katerinina2@test.com"),
            "Очередное какое-то очень важное описание"
        ),
    )
}
