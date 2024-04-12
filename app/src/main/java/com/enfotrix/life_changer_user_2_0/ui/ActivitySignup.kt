package com.enfotrix.life_changer_user_2_0.ui


import ApiClient
import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData

import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Data.Repo
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.api.Requests.ReqSignup
import com.enfotrix.life_changer_user_2_0.api.Responses.ResSignup
import com.enfotrix.life_changer_user_2_0.databinding.ActivitySignupBinding


import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject


class ActivitySignup : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var binding : ActivitySignupBinding
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var repo: Repo
    private lateinit var user: User
    private lateinit var dialog : Dialog
    val apiService = ApiClient.service()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivitySignup
        repo= Repo(mContext)
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager=SharedPrefManager(mContext)
        binding.imgBack.setOnClickListener{
            startActivity(Intent(mContext,ActivityLogin::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
        }
        binding.tvTermsCondition.setOnClickListener{
            Toast.makeText(mContext, "Available Soon!", Toast.LENGTH_SHORT).show()
        }
        binding.btnProfileRegister.setOnClickListener{
            if((!IsEmpty()) && IsValid() ) checkCNIC(utils.cnicFormate( binding.etCNIC.editText?.text.toString()))
        }
    }

    private fun checkCNIC(cnic:String) {








        val url = "http://192.168.0.103:8000/api/cnic-validator"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if(jsonObject!=null){

                        if(jsonObject.getBoolean("success")==true){

                            showDialogPin(ReqSignup(
                                binding.etName.editText?.text.toString(),
                                binding.etFatherName.editText?.text.toString(),
                                cnic,
                                "",
                                binding.etAddress.editText?.text.toString(),
                                binding.etPhoneNumber.editText?.text.toString()
                            ))


                        }
                        else if(jsonObject.getBoolean("success")==false) {
                            var error= jsonObject.getString("message")
                            binding.etCNIC.editText?.error = error
                            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show()
                        }

                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(mContext, e.message.toString(), Toast.LENGTH_SHORT).show()
                    // Handle JSON parsing error
                }



            },
            Response.ErrorListener { error ->
                // Handle errors
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "Response: ${error.message}", Toast.LENGTH_SHORT).show()

                Log.e("VolleyError", "Error: $error")
            }) {
            // Override getParams() to add POST parameters
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["cnic"] = cnic
                return params
            }
        }

        Volley.newRequestQueue(mContext).add(stringRequest)






































        /*val stringRequest = StringRequest(Request.Method.POST, url,

            { response ->
                Toast.makeText(applicationContext, "Response: $response", Toast.LENGTH_LONG).show()
            },
            { error ->
                Log.e(TAG, "Error: $error")
            }
        )*/


       /* val postUrl = "yourURL....."
        val requestQueue = Volley.newRequestQueue(this)



        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            postUrl,
            postData,
            object : Listener<JSONObject?>() {
                fun onResponse(response: JSONObject) {
                    Toast.makeText(applicationContext, "Response: $response", Toast.LENGTH_LONG)
                        .show()
                }
            },
            object : ErrorListener() {
                fun onErrorResponse(error: VolleyError) {
                    error.printStackTrace()
                }
            })

        requestQueue.add(jsonObjectRequest)
*/














        /*utils.startLoadingAnimation()

        val client = OkHttpClient()
        val mediaType = "text/plain".toMediaType()
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("cnic","3840180166145")
            .build()
        val request = Request.Builder()
            .url("http://192.168.0.103:8000/api/cnic-validator")
            .post(body)
            .build()
        utils.endLoadingAnimation()
        val response = client.newCall(request).execute()


        Toast.makeText(mContext, "${response}", Toast.LENGTH_SHORT).show()*/
       // val requestBody = "cnic:$cnic"

        /*val requestQueue = Volley.newRequestQueue(this)

        val postData = JSONObject()
        try {
            postData.put("cnic", cnic)

        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val objectReq = JsonObjectRequest(
            Request.Method.POST,
            url,
            postData,
            { response ->
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "$response", Toast.LENGTH_SHORT).show()
            },
            { error ->
                utils.endLoadingAnimation()
                Log.e("33", "$error")
                Toast.makeText(mContext, "d1 ${error}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(objectReq)*/


        /*val objectReq = object : JsonObjectRequest(Request.Method.POST, url,postData,
            { response ->
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "$response", Toast.LENGTH_SHORT).show()

            },
            { error ->
                utils.endLoadingAnimation()
                Log.e("33", "$error" )
                Toast.makeText(mContext, "${error.message}", Toast.LENGTH_SHORT).show()
            }
        )*/


//        {
//
//
//
//            override fun getBody(): ByteArray {
//                return requestBody.toByteArray()
//            }
//
//
//        }

        //override fun getBodyContentType() = "application/x-www-form-urlencoded"

        /*override fun getParams(): MutableMap<String, String> {
            val params = HashMap<String, String>()
            // Add any additional parameters here
            params["key1"] = "value1"
            params["key2"] = "value2"
            return params
        }*/

        /*val stringRequest = object : StringRequest(
            Request.Method.POST,
            url,

            Response.Listener<String> { response ->
                // Handle the response
                println("Response: $response")
            },
            Response.ErrorListener { error ->
                // Handle errors
                println("Error: $error")
            })
        {

            // Set the request body
            override fun getBody(): ByteArray {
                val body = "cnic=3840180166145"
                return body.toByteArray()
            }

            // Optionally, you can override the getBodyContentType() method to set the request content type

        }*/










// Create the request
        /*val stringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener<String> { response ->
                // Handle the response
                println("Response: $response")
            },
            Response.ErrorListener { error ->
                // Handle errors
                println("Error: $error")
            }) {

            // Set the request body
            override fun getBody(): ByteArray {
                val body = "example data"
                return body.toByteArray()
            }

            // Optionally, you can override the getBodyContentType() method to set the request content type
            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

// Add the request to the RequestQueue
        queue.add(stringRequest)*/


      /*  var  mRequestQueue: RequestQueue
        var  mStringRequest: StringRequest
        mRequestQueue = Volley.newRequestQueue(this)

        //String Request initialized

        //String Request initialized
        mStringRequest = StringRequest(Request.Method.GET, "",
            { res ->

                Toast.makeText(applicationContext, "Response :$res", Toast.LENGTH_LONG).show() //display the response on screen


            })
        {
            error -> Log.i(TAG, "Error :$error")
        }

        mRequestQueue.add(mStringRequest)*/


//        var req= ReqSignup(
//            "kdsfj",
//            cnic,
//            binding.etName.editText?.text.toString(),
//            binding.etFatherName.editText?.text.toString(),
//            binding.etAddress.editText?.text.toString(),
//            binding.etPhoneNumber.editText?.text.toString()
//        )
        /*var req= ReqCNIC(
            cnic
        )





        Toast.makeText(mContext, cnic, Toast.LENGTH_SHORT).show()
        utils.startLoadingAnimation()



        apiService.isValidCNIC(cnic)
            .enqueue(object : Callback<ResIsValidCNIC> {
                override fun onResponse(call: Call<ResIsValidCNIC>, response: Response<ResIsValidCNIC>) {
                    utils.endLoadingAnimation()
                    if (response.isSuccessful) {


                        val res: ResIsValidCNIC? = response.body()

                        Toast.makeText(mContext, res.toString(), Toast.LENGTH_SHORT).show()
                        Log.e("res22", response.body().toString())

                        if (res != null) {
                            if(res.success ==true){

                                Toast.makeText(mContext, "Valid CNIC", Toast.LENGTH_SHORT).show()
                                //showDialogPin(req)
                            }
                            else Toast.makeText(mContext, "CNIC Already Exist!", Toast.LENGTH_SHORT).show()
                        }



                    } else {
                        Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                        // Handle error
                    }
                }

                override fun onFailure(call: Call<ResIsValidCNIC>, t: Throwable) {
                    utils.endLoadingAnimation()

                    Log.e("API_CALL_FAILURE", t.message.toString(), t)

                    //Toast.makeText(mContext, t.message.toString(), Toast.LENGTH_SHORT).show()
                }

            })*/



    }


    fun showDialogPin(req:ReqSignup) {



        var counter= 0
        dialog = Dialog (mContext,android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)




        //dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_pin_new)

        val tvOne = dialog.findViewById<TextView>(R.id.tvOne)
        val tvTwo = dialog.findViewById<TextView>(R.id.tvTwo)
        val tvThree = dialog.findViewById<TextView>(R.id.tvThree)
        val tvFour = dialog.findViewById<TextView>(R.id.tvFour)
        val tvFive = dialog.findViewById<TextView>(R.id.tvFive)
        val tvSix = dialog.findViewById<TextView>(R.id.tvSix)
        val tvSeven = dialog.findViewById<TextView>(R.id.tvSeven)
        val tvEight = dialog.findViewById<TextView>(R.id.tvEight)
        val tvNine = dialog.findViewById<TextView>(R.id.tvNine)
        val tvZero = dialog.findViewById<TextView>(R.id.tvZero)

        val tvForgotPassword = dialog.findViewById<TextView>(R.id.tvForgotPassword)

        tvForgotPassword.visibility = View.GONE

        val tvInput1 = dialog.findViewById<TextView>(R.id.tvInput1)
        val tvInput2 = dialog.findViewById<TextView>(R.id.tvInput2)
        val tvInput3 = dialog.findViewById<TextView>(R.id.tvInput3)
        val tvInput4 = dialog.findViewById<TextView>(R.id.tvInput4)
        val tvInput5 = dialog.findViewById<TextView>(R.id.tvInput5)
        val tvInput6 = dialog.findViewById<TextView>(R.id.tvInput6)
        val imgBack = dialog.findViewById<ImageView>(R.id.imgBack)
        val imgBackSpace = dialog.findViewById<ImageView>(R.id.imgBackSpace)


        val numberButtons = arrayOf(tvOne, tvTwo, tvThree, tvFour, tvFive, tvSix, tvSeven, tvEight, tvNine, tvZero)
        val inputTextViews = arrayOf(tvInput1, tvInput2, tvInput3, tvInput4, tvInput5, tvInput6)
        val backSpaceViews = arrayOf(tvOne, tvTwo, tvThree, tvFour, tvFive, tvSix)




        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (counter < 6) {
                    vibrate(mContext, 50)
                    counter++
                    if (counter <= inputTextViews.size) {
                        inputTextViews[counter - 1].text = if (index == 9) "0" else (index + 1).toString()
                    }
                }
                if (counter == 6) {

                    if (req != null) {
                        req.pin=""+tvInput1.text+tvInput2.text+tvInput3.text+tvInput4.text+tvInput5.text+tvInput6.text
                        saveUser(req)

                    }
                }
            }
        }



        imgBack.setOnClickListener { dialog.dismiss() }
        imgBackSpace.setOnClickListener {
            if (counter > 0) {
                vibrate(mContext, 50)
                inputTextViews[counter - 1].text = "_"
                counter--
            }
        }






        dialog.show()
    }

    fun vibrate(context: Context, duration: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android 8.0 (API level 26) and above
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // For devices below Android 8.0
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }





    fun saveUser(req:ReqSignup){





        utils.startLoadingAnimation()
        val url = "http://192.168.0.103:8000/api/register"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if(jsonObject!=null){

                        if(jsonObject.getBoolean("success")==true){
                            sharedPrefManager.putToken(jsonObject.getJSONObject("data").getString("token"))
                            sharedPrefManager.setLogin(true)
                            sharedPrefManager.setLoginStatus(constants.INVESTOR_STATUS_INCOMPLETE)

                            dialog.dismiss()
                            Toast.makeText(mContext, constants.INVESTOR_SIGNUP_MESSAGE, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(mContext,ActivityUserDetails::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                            finish()

                        }


                        else if(jsonObject.getBoolean("success")==false) {

                            var error= jsonObject.getString("message")
                            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show()
                        }



                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(mContext, e.message.toString(), Toast.LENGTH_SHORT).show()
                    // Handle JSON parsing error
                }



            },
            Response.ErrorListener { error ->
                // Handle errors
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "Response: ${error.message}", Toast.LENGTH_SHORT).show()

                Log.e("VolleyError", "Error: $error")
            }) {
            // Override getParams() to add POST parameters
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["name"] = req.name
                params["cnic"] = req.cnic
                params["pin"] = req.pin
                params["address"] = req.address
                params["phone"] = req.phone
                params["father_name"] = req.father_name
                return params
            }
            /*override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "name" to req.name,
                    "cnic" to req.cnic,
                    "pin" to req.pin,
                    "address" to req.address,
                    "phone" to req.phone,
                    "father_name" to req.father_name
                )
            }*/

        }


        Volley.newRequestQueue(mContext).add(stringRequest)




        /*utils.startLoadingAnimation()

        apiService.signup(req)
            .enqueue(object : Callback<ResSignup> {
                override fun onResponse(call: Call<ResSignup>, response: Response<ResSignup>) {
                    utils.endLoadingAnimation()
                    if (response.isSuccessful) {
                        val res: ResSignup? = response.body()
                        if (res != null) {
                            //sharedPrefManager.putToken(res.id.toString())
                            Toast.makeText(mContext, constants.INVESTOR_SIGNUP_MESSAGE, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(mContext,ActivityUserDetails::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                            finish()
                        }



                    } else {
                        Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                        // Handle error
                    }
                }

                override fun onFailure(call: Call<ResSignup>, t: Throwable) {
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, t.message.toString(), Toast.LENGTH_SHORT).show()
                }

            })*/

    }


    private fun IsEmpty(): Boolean {

        val result = MutableLiveData<Boolean>()
        result.value=true
        if (binding.etCNIC.editText?.text.toString().isEmpty()) binding.etCNIC.editText?.error = "Empty CNIC"
        else if (binding.etAddress.editText?.text.toString().isEmpty()) binding.etAddress.editText?.error = "Empty Address"
        else if (binding.etName.editText?.text.toString().isEmpty()) binding.etName.editText?.error = "Empty First Name"
        else if (binding.etFatherName.editText?.text.toString().isEmpty()) binding.etFatherName.editText?.error = "Empty Last Name"
        else if (binding.etPhoneNumber.editText?.text.toString().isEmpty()) binding.etPhoneNumber.editText?.error = "Empty Phone Number"
        else result.value = false

        return result.value!!
    }
    private fun IsValid(): Boolean {

        val result = MutableLiveData<Boolean>()
        result.value=false
        if (binding.etCNIC.editText?.text.toString().length<13) binding.etCNIC.editText?.error = "Invalid CNIC"
        //else if (binding.etMobileNumber.editText?.text.toString().length<11) binding.etMobileNumber.editText?.error = "Invalid Phone Number"
        else result.value = true

        return result.value!!
    }

}




