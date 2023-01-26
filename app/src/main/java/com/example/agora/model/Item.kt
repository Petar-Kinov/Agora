package com.example.agora.model

import com.google.gson.annotations.SerializedName

class Item(
    @SerializedName("seller") val seller: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: String,
    @SerializedName("downloadUrl") val downloadUrl: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Item

        if (title != other.title) return false
        if (description != other.description) return false
        if (price != other.price) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + price.hashCode()
        return result
    }
}