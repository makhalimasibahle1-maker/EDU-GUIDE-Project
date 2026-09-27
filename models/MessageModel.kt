package com.example.videoconfrence.models

data class MessageModel(
    val type: String? = null,
    val name: String? = null,
    val target: String? = null,
    val data: Any? = null
)