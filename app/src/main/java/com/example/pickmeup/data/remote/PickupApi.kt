package com.example.pickmeup.data.remote

import com.example.pickmeup.data.remote.dto.CreateLocationRequestBody
import com.example.pickmeup.data.remote.dto.LocationRequestResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * PickMeUp 서버 REST API.
 *
 * ⚠️ 초안: 서버 API 가 아직 없어서 앱에서 필요한 기능 기준으로 만든 제안입니다.
 * 서버의 Swagger 문서가 나오면 경로·필드를 그 문서에 맞춰 수정하세요.
 */
interface PickupApi {

    /** 위치 공유 요청 생성 (서버가 대상자에게 문자 발송) */
    @POST("api/v1/location-requests")
    suspend fun createLocationRequest(@Body body: CreateLocationRequestBody): LocationRequestResponse

    /** 위치 공유 요청 상태 조회 (대상자 응답 대기 중에는 status = PENDING) */
    @GET("api/v1/location-requests/{id}")
    suspend fun getLocationRequest(@Path("id") id: Long): LocationRequestResponse

    /** 픽업 완료 */
    @POST("api/v1/location-requests/{id}/complete")
    suspend fun completePickup(@Path("id") id: Long)

    /** 픽업 취소 */
    @POST("api/v1/location-requests/{id}/cancel")
    suspend fun cancelPickup(@Path("id") id: Long)
}
