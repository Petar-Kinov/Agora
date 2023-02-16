package com.example.agora.model

import com.google.firebase.firestore.DocumentReference

data class ItemsWithReference(val item: Item, val documentReference: DocumentReference) {

}