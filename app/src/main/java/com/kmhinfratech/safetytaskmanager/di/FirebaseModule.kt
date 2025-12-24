package com.kmhinfratech.safetytaskmanager.di


import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides @Singleton
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(@ApplicationContext context: Context): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        // Offline support: Firestore persistence is enabled by default on Android,
        // but set explicit settings for clarity.
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        return db
    }

    @Provides @Singleton
    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
