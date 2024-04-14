package com.enfotrix.life_changer_user_2_0.ui

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.enfotrix.life_changer_user_2_0.Adapters.StatmentAdapter
import com.enfotrix.life_changer_user_2_0.Adapters.TransactionsAdapter
import com.enfotrix.life_changer_user_2_0.ApiUrls
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityInvestmentBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale


class ActivityInvestment : AppCompatActivity() {

    private lateinit var binding: ActivityInvestmentBinding
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
        binding = ActivityInvestmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityInvestment
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
        }


        /*setTitle("My Investments")
        */
        /*selectedCalendar = Calendar.getInstance()

        binding.llStartDate.setOnClickListener {
            showDatePickerDialog(true)
        }

        binding.llEndDate.setOnClickListener {
            showDatePickerDialog(false)
        }*/

        /*binding.getInvestment.setOnClickListener {

            if (startFormattedDate != null && endFormattedDate != null) {

                val date1 = dateFormat.parse(startFormattedDate)
                val startDate = Timestamp(date1)

                 val date2 = dateFormat.parse(endFormattedDate)
                val endDate = Timestamp(date2)



                getDataByDate(startDate!!, endDate!!)


            } else {
                Toast.makeText(this, "Select start and end dates", Toast.LENGTH_SHORT).show()
            }
        }*/


        binding.imgBack.setOnClickListener{finish()}

        val spinner = binding.spInvestments

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.investment_options, // Replace with your array of items
            R.layout.item_investment_selection_spiner // Use the custom layout
        )


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        binding.rvInvestments.layoutManager = LinearLayoutManager(mContext)

        binding.spInvestments.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                // Show a Toast message when an item is selected
                //val selectedItem = parentView?.getItemAtPosition(position).toString()

                // Check the index and show a different message for 1st and 2nd index
                when (position) {
                    0 -> {
                        getTransaction( "Investment", "Approved" )

                        //binding.rvInvestments.adapter= investmentViewModel.getApprovedInvestmentReqAdapter(constants.FROM_APPROVED_INVESTMENT_REQ)
                    }
                    1 -> {
                        getTransaction( "Investment", "Pending" )

                        //binding.rvInvestments.adapter= investmentViewModel.getPendingInvestmentReqAdapter(constants.FROM_PENDING_INVESTMENT_REQ)
                    }
                }


            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing if nothing is selected
            }
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




                            if(status.equals("Approved")) binding.rvInvestments.adapter= TransactionsAdapter(constants.FROM_APPROVED_INVESTMENT_REQ,transactions)
                            if(status.equals("Pending")) binding.rvInvestments.adapter= TransactionsAdapter(constants.FROM_PENDING_INVESTMENT_REQ,transactions)







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






    /*

        @RequiresApi(Build.VERSION_CODES.N)
        private fun showDatePickerDialog(isStartDate: Boolean) {
            val calendar = if (isStartDate) startCalendar else endCalendar
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                this,
                { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    calendar.set(year, monthOfYear, dayOfMonth)
                    updateButtonText(isStartDate)
                },
                currentYear,
                currentMonth,
                currentDay
            ).show()
        }

        @RequiresApi(Build.VERSION_CODES.N)
        private fun updateButtonText(isStartDate: Boolean) {
            val calendar = if (isStartDate) startCalendar else endCalendar
            val selectedDate = dateFormat.format(calendar.time)

            if (isStartDate) {
                binding.startDate.text = selectedDate
                startFormattedDate = selectedDate
            } else {
                binding.endDate.text = selectedDate
                endFormattedDate = selectedDate
            }
            Toast.makeText(mContext, ""+startCalendar, Toast.LENGTH_SHORT).show()
        }

        private fun setupTabLayout() {
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                if(position==0) tab.text ="Approved"
                else if(position==1) tab.text="Pending"
            }.attach()
        }

        private fun setupViewPager() {
                      val adapter = InvestmentViewPagerAdapter(this, 2)
            binding.viewPager.adapter = adapter
        }

        private fun getDataByDate(startTimestamp:Timestamp, endTimestamp:Timestamp){

                                      Toast.makeText(mContext, ""+startTimestamp, Toast.LENGTH_SHORT).show()
                                      Toast.makeText(mContext, ""+endTimestamp, Toast.LENGTH_SHORT).show()

            utils.startLoadingAnimation()
            lifecycleScope.launch{
                investmentViewModel.getInvestmentReqByDates(sharedPrefManager.getToken(), startTimestamp, endTimestamp)
                    .addOnCompleteListener{task ->
                        utils.endLoadingAnimation()
                        if (task.isSuccessful) {
                            val list = ArrayList<TransactionModel>()
                            if(task.result.size()>0){
                                for (document in task.result) list.add( document.toObject(TransactionModel::class.java))
                                Toast.makeText(mContext, "List size"+list.size, Toast.LENGTH_SHORT).show()
                                sharedPrefManager.putInvestmentReqList(list)

                                setupViewPager()
                                setupTabLayout()
                            }
                        }
                        else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()



                    }
                    .addOnFailureListener{
                        utils.endLoadingAnimation()
                        Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                    }

            }
        }








        override fun onBackPressed() {
            val viewPager = binding.viewPager
            if (viewPager.currentItem == 0) {
                // If the user is currently looking at the first step, allow the system to handle the
                // Back button. This calls finish() on this activity and pops the back stack.
                super.onBackPressed()
            } else {
                // Otherwise, select the previous step.
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }
    */



}