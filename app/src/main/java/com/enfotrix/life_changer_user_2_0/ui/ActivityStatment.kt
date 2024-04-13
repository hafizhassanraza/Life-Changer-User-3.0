package com.enfotrix.life_changer_user_2_0.ui

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.enfotrix.life_changer_user_2_0.Adapters.StatmentAdapter
import com.enfotrix.life_changer_user_2_0.ApiUrls
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.ModelUser
import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityStatmentBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class ActivityStatment : AppCompatActivity() {



    private lateinit var binding: ActivityStatmentBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager

    private lateinit var selectedCalendar: Calendar
    private var startFormattedDate = ""
    private var endFormattedDate= ""
    @RequiresApi(Build.VERSION_CODES.N)
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityStatment
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)



        binding.imgBack.setOnClickListener{finish()}

        binding.rvInvestments.layoutManager = LinearLayoutManager(mContext)


        getTransaction( "All", "Approved" )



        binding.refreshLayout.setOnRefreshListener {

            getTransaction( "All", "Approved" )


            binding.refreshLayout.isRefreshing = false
        }

    }


    private fun getTransaction( type:String,status:String ) {


        utils.startLoadingAnimation()

        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.ALL_TRANSACTION_API,
            com.android.volley.Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if (jsonObject != null) {

                        if (jsonObject.getBoolean("success") == true) {


                            val gson = Gson()
                            val transactions: List<TransactionModel> = gson.fromJson(
                                jsonObject.getJSONArray("data").toString(),
                                object : TypeToken<List<TransactionModel>>() {}.type
                            )

                            if (transactions.isNotEmpty()) {
                                binding.rvInvestments.adapter = StatmentAdapter(transactions)
                            } else {
                                Toast.makeText(mContext, "No Data Found", Toast.LENGTH_SHORT).show()
                            }




                        } else if (jsonObject.getBoolean("success") == false) {

                            var error = jsonObject.getString("message")
                            Toast.makeText(mContext, " ${error}", Toast.LENGTH_SHORT).show()
                        }

                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(mContext, e.message.toString(), Toast.LENGTH_SHORT).show()
                    // Handle JSON parsing error
                }


            },
            com.android.volley.Response.ErrorListener { error ->
                // Handle errors
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "Response: ${error.message}", Toast.LENGTH_SHORT).show()

                Log.e("VolleyError", "Error: $error")
            }) {


            override fun getParams(): MutableMap<String, String> {

                val params = HashMap<String, String>()


                params["type"] = type
                params["status"] = status

                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] =
                    "Bearer ${sharedPrefManager.getToken()}" // Replace "token" with your actual token
                return headers
            }


        }


        Volley.newRequestQueue(mContext).add(stringRequest)




    }

}