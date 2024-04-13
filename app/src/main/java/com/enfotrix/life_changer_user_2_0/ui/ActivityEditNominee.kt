package com.enfotrix.life_changer_user_2_0.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Data.Repo
import com.enfotrix.life_changer_user_2_0.Models.ModelNominee
import com.enfotrix.life_changer_user_2_0.Models.ModelUser
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityEditNomineeBinding
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject

// Your imports...

class ActivityEditNominee : AppCompatActivity() {
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var binding : ActivityEditNomineeBinding
    private lateinit var repo: Repo
    private lateinit var user: ModelUser
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNomineeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityEditNominee
        repo= Repo(mContext)
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.backImage.setOnClickListener {
            startActivity(Intent(mContext, ActivityNominee::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }

        binding.btnUpdateNominee.setOnClickListener {
            if((!IsEmpty()) && IsValid() ) {
                updateNominee(
                    ModelNominee(
                    "",
                    "",
                    "",
                    binding.etAddress.editText?.text.toString(),
                    "",
                    binding.etCNIC.editText?.text.toString(),
                    "",
                    "",
                    binding.etFirstName.editText?.text.toString(),
                    "",
                    binding.spNominee.selectedItem.toString(),
                    binding.etMobileNumber.editText?.text.toString(),
                    binding.etLastName.editText?.text.toString())
                )
            }
        }
        getUser()
    }


    private fun IsEmpty(): Boolean {

        val result = MutableLiveData<Boolean>()
        result.value=true
        if (binding.etCNIC.editText?.text.toString().isEmpty()) binding.etCNIC.editText?.error = "Empty CNIC"
        else if (binding.etAddress.editText?.text.toString().isEmpty()) binding.etAddress.editText?.error = "Empty Address"
        else if (binding.etFirstName.editText?.text.toString().isEmpty()) binding.etFirstName.editText?.error = "Empty First Name"
        else if (binding.etLastName.editText?.text.toString().isEmpty()) binding.etLastName.editText?.error = "Empty Last Name"
        else if (binding.etMobileNumber.editText?.text.toString().isEmpty()) binding.etMobileNumber.editText?.error = "Empty Phone"
        else result.value = false

        return result.value!!
    }
    private fun IsValid(): Boolean {
        val result = MutableLiveData<Boolean>()
        result.value=false
        if (binding.etCNIC.editText?.text.toString().length<13) binding.etCNIC.editText?.error = "Invalid CNIC"
        else if (binding.etMobileNumber.editText?.text.toString().length<11) binding.etMobileNumber.editText?.error = "Invalid Phone Number"
        else result.value = true

        return result.value!!
    }

    private fun setData(user:ModelUser){
        binding.apply {
            val nomineeArray = resources.getStringArray(R.array.nomineeRelationSpiner)
            val position = nomineeArray.indexOf(user.nominees.user_relation)
            spNominee.setSelection(position)
            etFirstName.editText?.setText(user.nominees.name)
            etLastName.editText?.setText(user.nominees.father_name)
            etMobileNumber.editText?.setText(user.nominees.phone)
            etCNIC.editText?.setText(user.nominees.cnic)
            etAddress.editText?.setText(user.nominees.address)
        }
    }

    private fun getUser() {
        val url = "http://192.168.0.103:8000/api/user-data"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                utils.endLoadingAnimation()
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getBoolean("success")) {
                        val gson = Gson()
                        val userJson = jsonObject.getJSONObject("data").toString()
                        user = gson.fromJson(userJson, ModelUser::class.java)
                        setData(user)
                    } else {
                        val error = jsonObject.getString("message")
                        Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(mContext, e.message.toString(), Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "Response: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("VolleyError", "Error: $error")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${sharedPrefManager.getToken()}"
                return headers
            }
        }

        Volley.newRequestQueue(mContext).add(stringRequest)
    }




    private fun updateNominee(nominee: ModelNominee) {

        Toast.makeText(mContext, "comlete pin", Toast.LENGTH_SHORT).show()
        utils.startLoadingAnimation()
        val url = "http://192.168.0.103:8000/api/update-nominee"
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if(jsonObject!=null){

                        if(jsonObject.getBoolean("success")==true){

//                            val gson = Gson()
//                            val user: ModelUser = gson.fromJson(jsonObject.getJSONObject("data").toString(), ModelUser::class.java)
//                            setData(user)
                            startActivity(Intent(mContext,ActivityProfile::class.java))
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
                params["user_relation"]=nominee.user_relation
                params["name"]=nominee.name
                params["father_name"]=nominee.father_name
                params["phone"]=nominee.phone
                params["cnic"]=nominee.cnic
                params["address"]=nominee.address
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
