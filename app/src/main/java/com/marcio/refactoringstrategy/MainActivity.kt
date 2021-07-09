package com.marcio.refactoringstrategy

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.marcio.refactoringstrategy.databinding.ActivityMainBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

typealias MarkerId = String

internal class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(SkipNetworkInterceptor())
        .build()

    private val placesApi = Retrofit.Builder()
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .baseUrl("http://localhost/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PlaceApi::class.java)

    private val priceApi = PriceApi()

    // We keep a map of marker IDs to places to look up the
    // POI after clicking a marker.
    private val markerToPlace = mutableMapOf<MarkerId, Place>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(binding.map.id) as SupportMapFragment

        binding.btnLoadStations.setOnClickListener {
            mapFragment.getMapAsync { map ->
                val visibleBounds = map.projection.visibleRegion.latLngBounds

                val places = placesApi.getPlaces(
                    latNW = visibleBounds.northeast.latitude,
                    lngNW = visibleBounds.northeast.longitude,
                    latSE = visibleBounds.southwest.latitude,
                    lngSE = visibleBounds.southwest.longitude
                )

                // Fetch the places for the given viewport, then fetch prices for each
                // place and finally display the results:
                places
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { response ->
                        markerToPlace.clear()
                        map.clear()

                        val body = response.body()
                        body?.results?.forEach { place ->

                            priceApi.getAdmissionPrice(place.id)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { price ->
                                    place.price = price

                                    val marker = map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(place.lat, place.lng))
                                    )

                                    markerToPlace[marker.id] = place
                                }
                        }
                    }
            }
        }

        mapFragment.getMapAsync { map ->
            // Set initial map viewport:
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(38.712, -9.168), 12f))

            map.setOnMarkerClickListener { marker ->
                val place = markerToPlace[marker.id]

                if (place != null) {
                    binding.placeDetailContainer.visibility = View.VISIBLE
                    binding.placesCountView.text =
                        "In total there are ${markerToPlace.size} POI's"
                    binding.placeNameView.text = place.name
                    binding.placeTypeView.text = place.type?.capitalize()

                    val price = place.price!! / 100f
                    binding.placePriceView.text = "${price}€"

                    binding.placeGeolocationView.text = "Location: ${place.lat}, ${place.lng}"
                }

                true
            }
        }
    }
}
