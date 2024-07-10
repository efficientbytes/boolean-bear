package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.database.dao.UtilityDataDao
import app.efficientbytes.booleanbear.models.IssueCategory
import app.efficientbytes.booleanbear.models.Profession
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.UtilityDataService
import app.efficientbytes.booleanbear.utils.NoInternetException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private fun fetchProfessionsAdapterList() = flow {
        try {
            emit(DataStatus.loading())
            val response = utilityDataService.getProfessionAdapterList()
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val professionList = response.body() ?: emptyList()
                    if (professionList.isEmpty()) {
                        emit(DataStatus.emptyResult())
                    } else {
                        emit(DataStatus.success(professionList))
                    }
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    emit(DataStatus.failed(""))
                }
            }
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    fun getProfessionsAdapterList(utilityListener: UtilityListener) {
        externalScope.launch {
            utilityListener.onProfessionsAdapterListStatusChanged(DataStatus.loading())
            val result = utilityDataDao.getProfessionAdapterList()
            if (!result.isNullOrEmpty()) {
                utilityListener.onProfessionsAdapterListStatusChanged(DataStatus.success(result))
            } else {
                fetchProfessionsAdapterList().collect {
                    when (it.status) {
                        DataStatus.Status.EmptyResult -> utilityListener.onProfessionsAdapterListStatusChanged(
                            DataStatus.emptyResult()
                        )

                        DataStatus.Status.Failed -> utilityListener.onProfessionsAdapterListStatusChanged(
                            DataStatus.failed(it.message.toString())
                        )

                        DataStatus.Status.Loading -> utilityListener.onProfessionsAdapterListStatusChanged(
                            DataStatus.loading()
                        )

                        DataStatus.Status.NoInternet -> utilityListener.onProfessionsAdapterListStatusChanged(
                            DataStatus.noInternet()
                        )

                        DataStatus.Status.Success -> {
                            it.data?.let { professionList ->
                                utilityListener.onProfessionsAdapterListStatusChanged(
                                    DataStatus.success(professionList)
                                )
                                utilityDataDao.insertProfessionAdapterList(professionList)
                            }
                        }

                        DataStatus.Status.TimeOut -> utilityListener.onProfessionsAdapterListStatusChanged(
                            DataStatus.timeOut()
                        )

                        DataStatus.Status.UnAuthorized -> utilityListener.onProfessionsAdapterListStatusChanged(
                            DataStatus.unAuthorized(it.message)
                        )

                        DataStatus.Status.UnKnownException -> utilityListener.onProfessionsAdapterListStatusChanged(
                            DataStatus.unknownException(it.message.toString())
                        )
                    }
                }
            }
        }
    }

    fun deleteProfessions() {
        externalScope.launch {
            utilityDataDao.deleteProfessionAdapterList()
        }
    }

    private fun fetchIssueCategoriesAdapterList() = flow {
        try {
            emit(DataStatus.loading())
            val response = utilityDataService.getIssueCategoryAdapterList()
            val responseCode = response.code()
            when {
                responseCode == 200 -> {
                    val issueCategoriesList = response.body() ?: emptyList()
                    if (issueCategoriesList.isEmpty()) {
                        emit(DataStatus.emptyResult())
                    } else {
                        emit(DataStatus.success(issueCategoriesList))
                    }
                }

                responseCode in 414..417 -> {
                    emit(DataStatus.unAuthorized(responseCode.toString()))
                }

                responseCode >= 400 -> {
                    emit(DataStatus.failed(""))
                }
            }
        } catch (noInternet: NoInternetException) {
            emit(DataStatus.noInternet())
        } catch (socketTimeOutException: SocketTimeoutException) {
            emit(DataStatus.timeOut())
        } catch (exception: IOException) {
            emit(DataStatus.unknownException(exception.message.toString()))
        }
    }.catch { t -> emit(DataStatus.unknownException(t.message.toString())) }.flowOn(Dispatchers.IO)

    fun getIssueCategoriesAdapterList(utilityListener: UtilityListener) {
        externalScope.launch {
            utilityListener.onIssueCategoriesAdapterListStatusChanged(DataStatus.loading())
            val result = utilityDataDao.getIssueCategoryAdapterList()
            if (!result.isNullOrEmpty()) {
                utilityListener.onIssueCategoriesAdapterListStatusChanged(DataStatus.success(result))
            } else {
                fetchIssueCategoriesAdapterList().collect {
                    when (it.status) {
                        DataStatus.Status.EmptyResult -> utilityListener.onIssueCategoriesAdapterListStatusChanged(
                            DataStatus.emptyResult()
                        )

                        DataStatus.Status.Failed -> utilityListener.onIssueCategoriesAdapterListStatusChanged(
                            DataStatus.failed(it.message.toString())
                        )

                        DataStatus.Status.Loading -> utilityListener.onIssueCategoriesAdapterListStatusChanged(
                            DataStatus.loading()
                        )

                        DataStatus.Status.NoInternet -> utilityListener.onIssueCategoriesAdapterListStatusChanged(
                            DataStatus.noInternet()
                        )

                        DataStatus.Status.Success -> {
                            it.data?.let { issueCategoriesList ->
                                utilityListener.onIssueCategoriesAdapterListStatusChanged(
                                    DataStatus.success(issueCategoriesList)
                                )
                                utilityDataDao.insertIssueCategoryAdapterList(issueCategoriesList)
                            }
                        }

                        DataStatus.Status.TimeOut -> utilityListener.onIssueCategoriesAdapterListStatusChanged(
                            DataStatus.timeOut()
                        )

                        DataStatus.Status.UnAuthorized -> utilityListener.onIssueCategoriesAdapterListStatusChanged(
                            DataStatus.unAuthorized(it.message)
                        )

                        DataStatus.Status.UnKnownException -> utilityListener.onIssueCategoriesAdapterListStatusChanged(
                            DataStatus.unknownException(it.message.toString())
                        )
                    }
                }
            }
        }
    }

    fun deleteIssueCategories() {
        externalScope.launch {
            utilityDataDao.deleteIssueCategoryAdapterList()
        }
    }

    interface UtilityListener {

        fun onProfessionsAdapterListStatusChanged(status: DataStatus<List<Profession>>)
        fun onIssueCategoriesAdapterListStatusChanged(status: DataStatus<List<IssueCategory>>)
    }

}