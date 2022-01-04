package com.github.taasonei.j0_homework.model

import java.time.LocalDate

data class ShortContact (
    val id: String,
    val name: String,
    val phone: String,
    val photo: String,
)

data class DetailedContact(
    val id: String,
    val name: String,
    val birthday: LocalDate?,
    val photo: String,
    val phone: List<String>,
    val email: List<String>,
    val description: String
)