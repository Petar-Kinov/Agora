Agora - An Android App for Buying and Selling Items  
This is a mobile application developed for Android devices using Kotlin.  
The reason i started this project was to familiarize myself with Firestore services  
and Kotlin i general, given that i mainly used Java before that.

Features
The project includes the following :

-User registration and authentication via Firebase Authentication  
-Four main activities: Launcher, Login, Main, and Chat  
-Creation of items with name, description, price, and images from storage or camera  
-Items are stored in Firestore, and images are stored in Firebase Storage  
-Uploading and deleting items is done with a service , so it cotinues even if we close tha app  
-Clicking on an item opens a new fragment with the details of the item,  
including the seller's contact information, which allows users to start a chat  
-Chats are stored in Firestore, and images sent in chat are stored in Firebase Storage  
-A fragment with all the chats that shows the person's avatar, name, last message send, and the time of the message  

Fragments:  
Login, Register, Items, MyItems, Settings, Messaging, CreateItem, ItemDetails  

The project follows mainly the MVVM architecture pattern  
Uses Jetpack Navigation, Firebase, Firestore, Firebase Storage,Firebase Messaging, Dagger-Hilt, Glide and Groupie 

I have fixed all the memory leaks that LeakCanary detected so the app should be stable.
