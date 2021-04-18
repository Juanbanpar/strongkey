package es.uc3m.strongkey

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PWND {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.pwnedpasswords.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val api: Interfaz by lazy {
        retrofit.create(Interfaz::class.java)
    }
}