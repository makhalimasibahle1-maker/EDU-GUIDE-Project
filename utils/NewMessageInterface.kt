package com.example.videoconfrence.utils

import com.example.videoconfrence.models.MessageModel

interface NewMessageInterface {
    fun onNewMessage(message: MessageModel)
}