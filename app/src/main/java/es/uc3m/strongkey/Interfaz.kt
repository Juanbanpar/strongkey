package es.uc3m.strongkey

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface Interfaz {
    @GET("/range/{hash}")
    fun getPWND(@Path("hash") id: String): Call<ResponseBody>

}