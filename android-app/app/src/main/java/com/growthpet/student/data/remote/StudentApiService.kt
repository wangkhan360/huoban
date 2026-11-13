package com.growthpet.student.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface StudentApiService {
    @POST("student/binding/verify")
    suspend fun verifyBinding(@Body request: VerifyBindingRequestDto): VerifyBindingResponseDto

    @POST("student/ai/chat")
    suspend fun sendChatTurn(@Body request: ChatRequestDto): AiStructuredResponse

    @GET("student/{childId}/points")
    suspend fun fetchVerifiedPoints(@Path("childId") childId: String): PointsSummaryDto

    @POST("student/{childId}/wishes/{wishId}/submit")
    suspend fun submitWishForParent(
        @Path("childId") childId: String,
        @Path("wishId") wishId: String
    ): AiStructuredResponse

    @POST("student/{childId}/rewards/{rewardId}/redeem")
    suspend fun requestRewardRedemption(
        @Path("childId") childId: String,
        @Path("rewardId") rewardId: String
    ): AiStructuredResponse

    @Multipart
    @POST("student/{childId}/recordings/upload")
    suspend fun uploadReadingRecording(
        @Path("childId") childId: String,
        @Part audioFile: MultipartBody.Part
    ): AiStructuredResponse
}
