package com.example.agora.data.core.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class Item(
    @SerializedName("seller") val seller: String,
    @SerializedName("sellerId") val sellerId : String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("price") val price: String,
    @SerializedName("storageRef") val storageRef: String,
    @SerializedName("imagesCount") val imagesCount: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
        seller = parcel.readString() ?: "",
        sellerId = parcel.readString() ?: "",
        title = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        category = parcel.readString() ?: "",
        price = parcel.readString() ?: "",
        storageRef = parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(seller)
        parcel.writeString(sellerId)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(category)
        parcel.writeString(price)
        parcel.writeString(storageRef)
        parcel.writeInt(imagesCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Item

        if (seller != other.seller) return false
        if (sellerId != other.sellerId) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (category != other.category) return false
        if (price != other.price) return false
        if (storageRef != other.storageRef) return false
        if (imagesCount != other.imagesCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = seller.hashCode()
        result = 31 * result + sellerId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + storageRef.hashCode()
        result = 31 * result + imagesCount
        return result
    }


    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item {
            return Item(parcel)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)
        }
    }
}