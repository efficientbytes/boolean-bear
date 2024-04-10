package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.database.dao.UtilityDataDao
import app.efficientbytes.booleanbear.services.UtilityDataService
import app.efficientbytes.booleanbear.services.models.IssueCategory
import app.efficientbytes.booleanbear.services.models.Profession
import app.efficientbytes.booleanbear.utils.NoInternetException
import app.efficientbytes.booleanbear.utils.ServiceError
import app.efficientbytes.booleanbear.utils.UtilityCoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class UtilityDataRepository(
    private val externalScope: UtilityCoroutineScope,
    private val serviceError: ServiceError,
    private val utilityDataService: UtilityDataService,
    private val utilityDataDao: UtilityDataDao,
) {

    val professionAdapterListFromDB: Flow<MutableList<Profession>> =
        utilityDataDao.getProfessionAdapterList()

    fun getProfessionAdapterList() {
        externalScope.getScope().launch {
            try {
                val response = utilityDataService.getProfessionAdapterList()
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        val professionList = response.body() ?: emptyList()
                        if (professionList.isNotEmpty()) {
                            deleteProfessions()
                            saveProfessionAdapterList(professionList)
                        }
                    }

                    responseCode >= 400 -> {
                        serviceError.postValue(response.message().toString())
                    }
                }
            } catch (noInternet: NoInternetException) {
                //serviceError.postValue("No Internet Connection")
            } catch (socketTimeOutException: SocketTimeoutException) {
                //serviceError.postValue("TimeOut")
            } catch (exception: IOException) {
                serviceError.postValue(exception.message.toString())
            }
        }
    }

    private fun saveProfessionAdapterList(professionAdapterList: List<Profession>) {
        externalScope.getScope().launch {
            utilityDataDao.insertProfessionAdapterList(professionAdapterList)
        }
    }

    private fun deleteProfessions() {
        externalScope.getScope().launch {
            utilityDataDao.deleteProfessionAdapterList()
        }
    }

    val issueCategoryAdapterListFromDB: Flow<MutableList<IssueCategory>> =
        utilityDataDao.getIssueCategoryAdapterList()

    fun getIssueCategoryAdapterList() {
        externalScope.getScope().launch {
            val response: Response<List<IssueCategory>>
            try {
                response = utilityDataService.getIssueCategoryAdapterList()
                val responseCode = response.code()
                when {
                    responseCode == 200 -> {
                        val issueCategoryList = response.body() ?: emptyList()
                        if (issueCategoryList.isNotEmpty()) {
                            deleteIssueCategories()
                            saveIssueCategoryAdapterList(
                                issueCategoryList
                            )
                        }
                    }

                    responseCode >= 400 -> {
                        serviceError.postValue(response.message().toString())
                    }
                }
            } catch (noInternet: NoInternetException) {
                //serviceError.postValue("No Internet Connection")
            } catch (socketTimeOutException: SocketTimeoutException) {
                //serviceError.postValue("TimeOut")
            } catch (exception: IOException) {
                serviceError.postValue(exception.message.toString())
            }
        }
    }

    private fun saveIssueCategoryAdapterList(issueCategoryAdapterList: List<IssueCategory>) {
        externalScope.getScope().launch {
            utilityDataDao.insertIssueCategoryAdapterList(issueCategoryAdapterList)
        }
    }

    private fun deleteIssueCategories() {
        externalScope.getScope().launch {
            utilityDataDao.deleteIssueCategoryAdapterList()
        }
    }


}