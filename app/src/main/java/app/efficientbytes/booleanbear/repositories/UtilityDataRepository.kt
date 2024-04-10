package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.database.dao.UtilityDataDao
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.UtilityDataService
import app.efficientbytes.booleanbear.services.models.IssueCategory
import app.efficientbytes.booleanbear.services.models.Profession
import app.efficientbytes.booleanbear.utils.NoInternetException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException

class UtilityDataRepository(
    private val externalScope: CoroutineScope,
    private val utilityDataService: UtilityDataService,
    private val utilityDataDao: UtilityDataDao,
) {

    val professionAdapterListFromDB: Flow<MutableList<Profession>> =
        utilityDataDao.getProfessionAdapterList()

    suspend fun getProfessionAdapterList() = flow {
        try {
            emit(DataStatus.loading())
            val response = utilityDataService.getProfessionAdapterList()
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val professionList = response.body() ?: emptyList()
                    if (professionList.isNotEmpty()) {
                        deleteProfessions()
                        saveProfessionAdapterList(professionList)
                        emit(DataStatus.success<Boolean>(true))
                    }
                }

                responseCode >= 400 -> {
                    emit(DataStatus.failed<Boolean>(""))
                }
            }
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet<Boolean>())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut<Boolean>())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException<Boolean>(exception.message.toString()))
        }
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    private fun saveProfessionAdapterList(professionAdapterList: List<Profession>) {
        externalScope.launch {
            utilityDataDao.insertProfessionAdapterList(professionAdapterList)
        }
    }

    private fun deleteProfessions() {
        externalScope.launch {
            utilityDataDao.deleteProfessionAdapterList()
        }
    }

    val issueCategoryAdapterListFromDB: Flow<MutableList<IssueCategory>> =
        utilityDataDao.getIssueCategoryAdapterList()

    suspend fun getIssueCategoryAdapterList() = flow {
        try {
            emit(DataStatus.loading<Boolean>())
            val response = utilityDataService.getIssueCategoryAdapterList()
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val issueCategoryList = response.body() ?: emptyList()
                    if (issueCategoryList.isNotEmpty()) {
                        deleteIssueCategories()
                        saveIssueCategoryAdapterList(
                            issueCategoryList
                        )
                        emit(DataStatus.success<Boolean>(true))
                    }
                }

                responseCode >= 400 -> {
                    emit(DataStatus.failed<Boolean>(""))
                }
            }
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet<Boolean>())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut<Boolean>())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException<Boolean>(exception.message.toString()))
        }
    }.catch { t -> emit(DataStatus.unknownException<Boolean>(t.message.toString())) }
        .flowOn(Dispatchers.IO)

    private fun saveIssueCategoryAdapterList(issueCategoryAdapterList: List<IssueCategory>) {
        externalScope.launch {
            utilityDataDao.insertIssueCategoryAdapterList(issueCategoryAdapterList)
        }
    }

    private fun deleteIssueCategories() {
        externalScope.launch {
            utilityDataDao.deleteIssueCategoryAdapterList()
        }
    }


}