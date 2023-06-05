package com.sekhgmainuddin.timeshare.di

import android.content.Context
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.sekhgmainuddin.timeshare.utils.Keys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OtherModule {

    @Provides
    @Singleton
    fun provideGiphy(@ApplicationContext context: Context): GiphyDialogFragment {
        Giphy.configure(context, Keys.giphyKey())
        return GiphyDialogFragment.newInstance()
    }


}