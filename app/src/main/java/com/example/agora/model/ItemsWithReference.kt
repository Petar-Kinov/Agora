package com.example.agora.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference

data class ItemsWithReference(val item: Item, val documentReference: DocumentReference) {
}