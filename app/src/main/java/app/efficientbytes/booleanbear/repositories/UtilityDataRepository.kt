package app.efficientbytes.booleanbear.repositories

import app.efficientbytes.booleanbear.database.dao.UtilityDataDao
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.UtilityDataService
import app.efficientbytes.booleanbear.services.models.IssueCategory
import app.efficientbytes.booleanbear.services.models.Profession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UtilityDataRepository(
    private val utilityDataService: UtilityDataService,
    private val utilityDataDao: UtilityDataDao
) {

    val professionAdapterListFromDB: Flow<MutableList<Profession>> =
        utilityDataDao.getProfessionAdapterList()

    suspend fun getProfessionAdapterList() = flow {
        emit(DataStatus.loading())
        val response = utilityDataService.getProfessionAdapterList()
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val professionList = response.body() ?: emptyList()
                if (professionList.isEmpty()) {
                    emit(DataStatus.failed("List is empty"))
                } else {
                    emit(DataStatus.success(professionList))
                }
            }

            responseCode >= 400 -> {
                emit(DataStatus.failed(response.message().toString()))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun saveProfessionAdapterList(professionAdapterList: List<Profession>) {
        utilityDataDao.insertProfessionAdapterList(professionAdapterList)
    }

    val issueCategoryAdapterListFromDB: Flow<MutableList<IssueCategory>> =
        utilityDataDao.getIssueCategoryAdapterList()

    suspend fun getIssueCategoryAdapterList() = flow {
        emit(DataStatus.loading())
        val response = utilityDataService.getIssueCategoryAdapterList()
        val responseCode = response.code()
        when {
            responseCode == 200 -> {
                val issueCategoryList = response.body() ?: emptyList()
                if (issueCategoryList.isEmpty()) {
                    emit(DataStatus.failed("List is empty"))
                } else {
                    emit(DataStatus.success(issueCategoryList))
                }
            }

            responseCode >= 400 -> {
                emit(DataStatus.failed(response.message().toString()))
            }
        }
    }.catch { emit(DataStatus.failed(it.message.toString())) }
        .flowOn(Dispatchers.IO)

    suspend fun saveIssueCategoryAdapterList(issueCategoryAdapterList: List<IssueCategory>) {
        utilityDataDao.insertIssueCategoryAdapterList(issueCategoryAdapterList)
    }


}