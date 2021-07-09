package com.marcio.refactoringstrategy

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Two dummy implementations that won't actually fetch real network.
interface PlaceApi {
    /** Fetch places for a given geo rect. */
    @GET("places/bounding_rect")
    fun getPlaces(@Query("lat_nw") latNW: Double,
                  @Query("lng_nw") lngNW: Double,
                  @Query("lat_se") latSE: Double,
                  @Query("lng_se") lngSE: Double): Single<Response<Page<Place>>>

}

class PriceApi {
    /** Fetch admission price for a given place. */
    fun getAdmissionPrice(@Suppress("UNUSED_PARAMETER") placeId: String): Single<Int> {
        return Single.just(100 + (Math.random() * 100).toInt())
    }
}

data class Page<T>(
    val results: List<T>
)
