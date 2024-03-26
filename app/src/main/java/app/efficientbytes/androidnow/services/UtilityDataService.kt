package app.efficientbytes.androidnow.services

import app.efficientbytes.androidnow.services.models.IssueCategory
import app.efficientbytes.androidnow.services.models.Profession
import retrofit2.Response
import retrofit2.http.GET

interface UtilityDataService {

    @GET("utility/professions")
    suspend fun getProfessionAdapterList(): Response<List<Profession>>

    @GET("utility/issue-categories")
    suspend fun getIssueCategoryAdapterList(): Response<List<IssueCategory>>

}