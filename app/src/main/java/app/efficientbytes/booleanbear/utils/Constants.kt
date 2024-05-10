package app.efficientbytes.booleanbear.utils

import app.efficientbytes.booleanbear.services.models.RemoteHomePageBanner
import app.efficientbytes.booleanbear.services.models.RemoteReel
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic

const val BASE_URL = "https://booleanbear-fb7pgif5zq-uc.a.run.app"
const val DATABASE_NAME = "APP_DATABASE"
const val DUMMY_TABLE = "dummy"
const val USER_PROFILE_TABLE = "user_profile"
const val USER_PROFILE_DOCUMENT_PATH = "/USER/PRIVATE-PROFILE/FILES/"
const val SINGLE_DEVICE_LOGIN_TABLE = "single_device_login"
const val SINGLE_DEVICE_LOGIN_DOCUMENT_PATH = "/USER/SINGLE-DEVICE-TOKENS/FILES/"
const val PROFESSION_ADAPTER_TABLE = "professions"
const val ISSUE_CATEGORY_ADAPTER_TABLE = "issue_categories"
const val REEL_TOPICS_TABLE = "reel_topics"
const val AUTH_CUSTOM_COROUTINE_SCOPE = "auth-scope"
const val USER_SCREEN_TIMING_TABLE = "user_screen_timing"
const val REELS_TABLE = "reels"
const val REELS_TABLE_FTS = "reels_fts"
const val INSTRUCTOR_PROFILE_TABLE = "instructor_profile"
const val MENTIONED_LINKS_TABLE = "mentioned_links"
const val HOME_PAGE_BANNER_AD_TABLE = "home_page_banner_ad"
const val FCM_TOKEN_TABLE = "fcm_token"
const val ID_TOKEN_TABLE = "id_token"
val dummyReelTopicsList = listOf<RemoteReelTopic>(
    RemoteReelTopic("", "", -1, ""),
    RemoteReelTopic("", "", -1, ""),
    RemoteReelTopic("", "", -1, ""),
    RemoteReelTopic("", "", -1, ""),
    RemoteReelTopic("", "", -1, ""),
)
val dummyReelsList = listOf<RemoteReel>(
    RemoteReel("", "", "", "", -1, -1, "", emptyList()),
    RemoteReel("", "", "", "", -1, -1, "", emptyList()),
    RemoteReel("", "", "", "", -1, -1, "", emptyList()),
    RemoteReel("", "", "", "", -1, -1, "", emptyList()),
)
val dummyHomePageBannersList = listOf<RemoteHomePageBanner>(
    RemoteHomePageBanner("", "", "", false, "", -1L, -1L, -1L),
)