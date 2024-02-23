package app.efficientbytes.efficientbytes.di

import android.app.Application
import app.efficientbytes.efficientbytes.utils.ConnectivityListener

fun provideConnectivityListener(application: Application) = ConnectivityListener(application)