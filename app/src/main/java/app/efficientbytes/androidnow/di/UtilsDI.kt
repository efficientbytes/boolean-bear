package app.efficientbytes.androidnow.di

import android.content.Context
import app.efficientbytes.androidnow.utils.ConnectivityListener

fun provideConnectivityListener(context: Context) = ConnectivityListener(context)