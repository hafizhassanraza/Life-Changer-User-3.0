package com.enfotrix.life_changer_user_2_0.ui

import User
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Data.Repo
import com.enfotrix.life_changer_user_2_0.Models.ModelNominee
import com.enfotrix.life_changer_user_2_0.Models.NomineeViewModel
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityNomineeBinding
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class ActivityNominee : AppCompatActivity() {

    private val nomineeViewModel: NomineeViewModel by viewModels()

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var binding : ActivityNomineeBinding
    private lateinit var repo: Repo
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNomineeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityNominee
        repo= Repo(mContext)
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)






        binding.backImage.setOnClickListener{

            if(intent.getStringExtra(constants.KEY_ACTIVITY_FLOW).equals(constants.VALUE_ACTIVITY_FLOW_USER_DETAILS)){
                startActivity(Intent(mContext,ActivityUserDetails::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                finish()
            }

        }
        binding.tvTermsCondition.setOnClickListener{
            Toast.makeText(mContext, "Available Soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnNomineeRegister.setOnClickListener{
            if((!IsEmpty()) && IsValid() ) {



                saveNominee(ModelNominee(
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
                    binding.etLastName.editText?.text.toString()
            }

        }
    }






    fun saveNominee(nominee:ModelNominee){





        utils.startLoadingAnimation()
        val url = "http://192.168.0.103:8000/api/add-nominee"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            com.android.volley.Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if(jsonObject!=null){

                        if(jsonObject.getBoolean("success")==true){



                            Toast.makeText(mContext, jsonObject.getString("message").toString(), Toast.LENGTH_SHORT).show()

                            /*if(intent.getStringExtra(constants.KEY_ACTIVITY_FLOW).equals(constants.VALUE_ACTIVITY_FLOW_USER_DETAILS)){
                                startActivity(Intent(mContext,ActivityUserDetails::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                                finish()
                            }*/



                        }
                        else if(jsonObject.getBoolean("success")==false) {

                            utils.endLoadingAnimation()
                            var error= jsonObject.getString("message")
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
            // Override getParams() to add POST parameters
            override fun getParams(): MutableMap<String, String> {

                val params = HashMap<String, String>()
                params["address"] = nominee.address
                params["cnic"] = nominee.cnic
                params["name"] = nominee.name
                params["father_name"] = nominee.father_name
                params["user_relation"] = nominee.user_relation
                params["phone"] = nominee.phone

                return params
            }
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${sharedPrefManager.getToken()}" // Replace "token" with your actual token
                return headers
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







        /*lifecycleScope.launch {
            nomineeViewModel.addNominee(nominee).observe(this@ActivityNominee) {
                utils.endLoadingAnimation()
                if (it == true) {
                    sharedPrefManager.saveNominee(nominee)// id overwrite in repo
                    Toast.makeText(mContext, constants.NOMINEE_SIGNUP_MESSAGE, Toast.LENGTH_SHORT).show()
                    if(intent.getStringExtra(constants.KEY_ACTIVITY_FLOW).equals(constants.VALUE_ACTIVITY_FLOW_USER_DETAILS)){
                        startActivity(Intent(mContext,ActivityUserDetails::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                        finish()
                    }
                }
                else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

            }
        }*/




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
}