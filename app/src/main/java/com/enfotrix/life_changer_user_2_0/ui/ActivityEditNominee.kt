package com.enfotrix.life_changer_user_2_0.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Data.Repo
import com.enfotrix.life_changer_user_2_0.Models.ModelUser
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityEditNomineeBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
        getUser()
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
}
