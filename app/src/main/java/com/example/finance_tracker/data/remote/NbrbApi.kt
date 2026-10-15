package com.example.finance_tracker.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Ежедневные официальные курсы НБ РБ: BYN к набору иностранных валют.
 * Один запрос отдаёт все нужные нам валюты (USD, EUR, RUB, PLN, UAH —
 * все с ежедневной периодичностью).
 */
interface NbrbApi {

    @GET("exrates/rates")
    suspend fun getDailyRates(
        @Query("periodicity") periodicity: Int = 0
    ): List<NbrbRateDto>
}

data class NbrbRateDto(
    @SerializedName("Cur_Abbreviation")
    val abbreviation: String,
    @SerializedName("Cur_Scale")
    val scale: Double,
    @SerializedName("Cur_OfficialRate")
    val officialRate: Double?
)
