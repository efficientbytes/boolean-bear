package app.efficientbytes.androidnow.di

import android.content.Context
import app.efficientbytes.androidnow.utils.ConnectivityListener
import com.google.android.play.core.review.ReviewManagerFactory

fun provideConnectivityListener(context: Context) = ConnectivityListener(context)

fun provideReviewManager(context: Context) = ReviewManagerFactory.create(context)