package com.aima.koraki.di

import android.content.Context
import androidx.room.Room
import com.aima.koraki.data.local.KorakiDatabase
import com.aima.koraki.data.local.dao.CompanionDao
import com.aima.koraki.data.local.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-level singletons for Room and DAOs.
 * [VaultPreferences] and repositories use @Inject constructor — no manual provision needed.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKorakiDatabase(
        @ApplicationContext context: Context,
    ): KorakiDatabase = Room.databaseBuilder(
        context,
        KorakiDatabase::class.java,
        KorakiDatabase.DATABASE_NAME,
    ).fallbackToDestructiveMigration()
    .build()

    @Provides
    @Singleton
    fun provideNoteDao(db: KorakiDatabase): NoteDao = db.noteDao()

    @Provides
    @Singleton
    fun provideCompanionDao(db: KorakiDatabase): CompanionDao = db.companionDao()
}
