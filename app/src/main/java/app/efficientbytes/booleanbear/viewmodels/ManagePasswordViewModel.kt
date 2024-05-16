package app.efficientbytes.booleanbear.viewmodels

import androidx.lifecycle.ViewModel
import app.efficientbytes.booleanbear.repositories.UserProfileRepository
import kotlinx.coroutines.CoroutineScope

class ManagePasswordViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val externalScope: CoroutineScope
) : ViewModel() {

    fun authenticateUserWithPassword(userAccountId: String, password: String) {

    }

}