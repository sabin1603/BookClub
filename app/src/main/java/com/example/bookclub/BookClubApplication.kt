package com.example.bookclub

import android.app.Application
import androidx.room.Room
import com.example.bookclub.data.local.AppDatabase
import com.example.bookclub.session.SessionManager

class BookClubApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "bookclub_database"
        )
            .fallbackToDestructiveMigration()
            .build()

        sessionManager = SessionManager(applicationContext)
    }
}