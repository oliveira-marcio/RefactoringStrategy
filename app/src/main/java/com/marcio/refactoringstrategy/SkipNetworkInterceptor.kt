package com.marcio.refactoringstrategy

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody

private val FAKE_RESULTS: Page<Place>
    get() {
        val stJorgeCastle = Place(id = "1", name = "Castelo de São Jorge", lat = 38.713930, lng = -9.133407, type = "castle", price = null)
        val belemTower = Place(id = "2", name = "Torre de Belém", lat = 38.69158448564067, lng = -9.215961491274998, type = "castle", price = null)
        val april25thBridge = Place(id = "3", name = "Ponte 25 de Abril", lat = 38.6904145541018, lng = -9.177047637559516, type = "bridge", price = null)
        val merchantSquare = Place(id = "4", name = "Praça do Comércio", lat = 38.708211576094286, lng = -9.136129506499602, type = "square", price = null)
        val lisbonZoo = Place(id = "5", name = "Jardim Zoológico de Lisboa", lat = 38.745256738682194, lng = -9.1710207177039, type = "zoo", price = null)
        val eduardoViiGarden = Place(id = "6", name = "Parque Eduardo VII", lat = 38.728228809052425, lng = -9.15286366967169, type = "garden", price = null)
        val carmoCathedral = Place(id = "7", name = "Convento do Carmo", lat = 38.71265233013841, lng = -9.14041300062539, type = "church", price = null)
        val lisbonCathedral = Place(id = "8", name = "Sé de Lisboa", lat = 38.710302240058326, lng = -9.132557590740486, type = "church", price = null)
        val starGarden = Place(id = "9", name = "Jardim da Estrela", lat = 38.71463894406128, lng = -9.159601313999469, type = "garden", price = null)
        val libertyAve = Place(id = "10", name = "Avenida da Liberdade", lat = 38.72100329464356, -9.14606672448059, type = "avenue", price = null)

        val stations = listOf(stJorgeCastle, belemTower, april25thBridge, merchantSquare, lisbonZoo, eduardoViiGarden, carmoCathedral,lisbonCathedral, starGarden, libertyAve)

        return Page(stations.shuffled().subList(0, stations.size/2)) // returns a random list of stations
    }

class SkipNetworkInterceptor: Interceptor {
    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        Thread.sleep(500) // pretend to "block" interacting with the network.
        return makeOkResult(chain.request())

    }

    private fun makeOkResult(request: Request): Response {
        return Response.Builder()
            .code(200)
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .message("OK")
            .body(ResponseBody.create(
                MediaType.get("application/json"),
                gson.toJson(FAKE_RESULTS)))
            .build()
    }

    private fun makeErrorResult(request: Request): Response {
        return Response.Builder()
            .code(500)
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .message("Bad server day")
            .body(
                ResponseBody.create(
                MediaType.get("application/json"),
                gson.toJson(mapOf("cause" to "not sure"))))
            .build()
    }
}
