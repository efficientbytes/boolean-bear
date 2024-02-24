package app.efficientbytes.androidnow.di

import android.app.Application
import app.efficientbytes.androidnow.utils.ConnectivityListener

fun provideConnectivityListener(application: Application) = ConnectivityListener(application)