package com.github.taasonei.j0_homework.model

import com.github.taasonei.j0_homework.R
import java.time.LocalDate
import java.time.Month

data class Contact(
    val name: String,
    val birthday: LocalDate,
    val photo: Int,
    val phone: List<String>,
    val email: List<String>,
    val description: String
)

fun getContactsInfo(): List<Contact> {
    return listOf(
        Contact(
            name = "Иван Иванов",
            birthday = LocalDate.of(1992, LocalDate.now().month, LocalDate.now().dayOfMonth),
            photo = R.drawable.example_avatar,
            phone = listOf("+79998887766", "+79997778866"),
            email = listOf("ivanov@test.com", "ivanov2@test.com"),
            description = "Какое-то очень важное описание"
        ),
        Contact(
            name = "Марина Маринина",
            birthday = LocalDate.of(1998, Month.NOVEMBER, 29),
            photo = R.drawable.example_avatar,
            phone = listOf("+79995554433", "+79994445533"),
            email = listOf("marinina@test.com", "marinina2@test.com"),
            description = "Какое-то очень важное описание и еще немного описания"
        ),
        Contact(
            name = "Петр Петров",
            birthday = LocalDate.of(1980, Month.NOVEMBER, 28),
            photo = R.drawable.example_avatar,
            phone = listOf("+79998882211", "+79992228811"),
            email = listOf("petrov@test.com", "petrov2@test.com"),
            description = "Рыба описания рыба описания рыба описания рыба описания рыба описания"
        ),
        Contact(
            "Екатерина Катеринина",
            birthday = LocalDate.of(1994, Month.NOVEMBER, 27),
            R.drawable.example_avatar,
            listOf("+79991117744", "+79997771144"),
            listOf("katerinina@test.com", "katerinina2@test.com"),
            "Очередное какое-то очень важное описание"
        ),
    )
}
