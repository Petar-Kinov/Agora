package com.example.agora.model

import android.graphics.Bitmap

class RecyclerItem(val seller: String,
                   val title: String,
                   val description: String,
                   val price: String,
                   var bitmap: Bitmap?
                   ) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecyclerItem

        if (seller != other.seller) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (price != other.price) return false
        if (bitmap != other.bitmap) return false

        return true
    }

    override fun hashCode(): Int {
        var result = seller.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + bitmap.hashCode()
        return result
    }
}