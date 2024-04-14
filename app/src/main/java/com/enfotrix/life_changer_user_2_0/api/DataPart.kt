package com.enfotrix.life_changer_user_2_0.api

class DataPart(val name: String, val data: ByteArray, val mimeType: String) {
    fun getBytes(): ByteArray {
        return data
    }
}