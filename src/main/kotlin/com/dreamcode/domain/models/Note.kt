package com.dreamcode.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: Int? = null,
    val userId: Int,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
