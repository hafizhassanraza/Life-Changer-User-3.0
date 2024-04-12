import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiClient {
    private const val BASE_URL = "http://192.168.0.103:8000/api/"

    private var retrofit: Retrofit? = null

    fun service(): Retrofit {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
        if (retrofit == null) {
            retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build()
        }
        return retrofit!!
    }



    /*fun service(): APIService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(APIService::class.java)
    }*/





}