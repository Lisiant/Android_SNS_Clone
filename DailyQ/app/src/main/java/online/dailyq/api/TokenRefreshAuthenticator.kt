package online.dailyq.api

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import online.dailyq.AuthManager

class TokenRefreshAuthenticator() : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // API에 따라 Authorization 헤더를 사용하지 않는 경우 존재
        // -> 원래 요청이 Authorization 헤더로 엑세스 토큰을 보냈는지 확인
        // 엑세스 토큰이 없다면 return null
        val accessToken = response.request.header("Authorization")
            ?.split(" ")
            ?.getOrNull(1)

        accessToken ?: return null

        // refresh Token이 있어야 토큰 갱신을 할 수 있음
        // -> refreshToken이 null인 경우 null을 리턴하고 함수를 종료.
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