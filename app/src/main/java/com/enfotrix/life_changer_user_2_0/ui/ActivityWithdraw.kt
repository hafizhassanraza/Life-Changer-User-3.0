package com.enfotrix.life_changer_user_2_0.ui

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.enfotrix.life_changer_user_2_0.Adapters.StatmentAdapter
import com.enfotrix.life_changer_user_2_0.Adapters.TransactionsAdapter
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityWithdrawBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class ActivityWithdraw : AppCompatActivity() {



    private lateinit var selectedCalendar: Calendar
    private var startFormattedDate = ""
    private var endFormattedDate= ""
    @RequiresApi(Build.VERSION_CODES.N)
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    private lateinit var binding: ActivityWithdrawBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityWithdraw
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        binding.refreshLayout.setOnRefreshListener {


            getTransaction( "Withdraw", "Approved" )

            binding.refreshLayout.isRefreshing = false
        }





        binding.imgBack.setOnClickListener{finish()}

        val spinner = binding.spWithdraws

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.withdraw_options, // Replace with your array of items
            R.layout.item_investment_selection_spiner // Use the custom layout
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        binding.rvWithdraws.layoutManager = LinearLayoutManager(mContext)

        binding.spWithdraws.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                // Show a Toast message when an item is selected
                //val selectedItem = parentView?.getItemAtPosition(position).toString()

                // Check the index and show a different message for 1st and 2nd index
                when (position) {
                    0 -> {
                        getTransaction( "Withdraw", "Approved" )

                        //binding.rvInvestments.adapter= investmentViewModel.getApprovedInvestmentReqAdapter(constants.FROM_APPROVED_INVESTMENT_REQ)
                    }
                    1 -> {
                        getTransaction( "Withdraw  ", "Pending" )

                        //binding.rvInvestments.adapter= investmentViewModel.getPendingInvestmentReqAdapter(constants.FROM_PENDING_INVESTMENT_REQ)
                    }
                }


            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing if nothing is selected
            }
        }



        //withdraw_options

    }

    private fun getTransaction( type:String,status:String ) {


        utils.startLoadingAnimation()
        val url = "http://192.168.0.103:8000/api/all-transaction"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
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



                            Toast.makeText(mContext, "${transactions.size}", Toast.LENGTH_SHORT).show()

                            if(status.equals("Approved")) binding.rvWithdraws.adapter= TransactionsAdapter(constants.FROM_APPROVED_WITHDRAW_REQ,transactions)
                            if(status.equals("Pending")) binding.rvWithdraws.adapter= TransactionsAdapter(constants.FROM_PENDING_WITHDRAW_REQ,transactions)







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