package online.dailyq.api

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import online.dailyq.AuthManager

class TokenRefreshAuthenticator() : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val accessToken = response.request.header("Authorization")
            ?.split(" ")
            ?.getOrNull(1)

        // accessToken, refreshToken이 null인 경우 null을 리턴하고 함수를 종료.
        accessToken ?: return null
        AuthManager.refreshToken ?: return null

        val api = ApiService.getInstance()


        synchronized(this) {
            if (accessToken == AuthManager.accessToken) {
                val authTokenResponse =
                    api.refreshToken(AuthManager.refreshToken!!).execute().body()!!

                AuthManager.accessToken = authTokenResponse.accessToken
                AuthManager.refreshToken = authTokenResponse.refreshToken
            }
        }
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${AuthManager.accessToken}")
            .build()
    }
}