package com.sekhgmainuddin.timeshare.di

import android.content.Context
import androidx.room.Room
import com.sekhgmainuddin.timeshare.data.db.TimeShareDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideTimeShareDatabase(@ApplicationContext context: Context): TimeShareDb {
        return synchronized(this){
            Room.databaseBuilder(
                context.applicationContext,
                TimeShareDb::class.java,
                "time_share_db"
            ).fallbackToDestructiveMigration().build()
        }
    }

}