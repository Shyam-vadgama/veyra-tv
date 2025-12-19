package com.example.iptv_player.model

data class Channel(
    val name: String,
    val logoUrl: String?,
    val streamUrl: String,
    val category: String
)
