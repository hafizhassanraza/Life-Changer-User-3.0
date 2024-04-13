package com.enfotrix.life_changer_user_2_0.ui

import User
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.enfotrix.life_changer_user_2_0.ApiUrls
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Data.Repo
import com.enfotrix.life_changer_user_2_0.Models.ModelUser
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityProfileBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject


class ActivityProfile : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var utils: Utils
    private lateinit var investor:User

    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var repo: Repo



    private lateinit var user_: ModelUser
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog

    private lateinit var dialogPinUpdate: Dialog
    private lateinit var dialogPhoto: Dialog
    private lateinit var dialogUpdateTaken: Dialog
    private val userViewModel: UserViewModel by viewModels()


    private lateinit var binding : ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize context, repository, utils, constants, and shared preferences manager
        mContext = this@ActivityProfile
        repo = Repo(mContext)
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)


        // Fetch user data
        getUser()

        // Set click listeners for various UI elements
        binding.apply {
            layInvestorAccount.setOnClickListener {
                startActivity(Intent(mContext, ActivityInvestorAccounts::class.java))
            }
            imgUser.setOnClickListener {
                openGallery()
            }
            layPin.setOnClickListener {
                showUpdatePinDialog()
            }
            editName.setOnClickListener {
                updateNameDialog()
            }

            layPhone.setOnClickListener {
                updatePhone()
            }
            layNominee.setOnClickListener {
                val intent = Intent(mContext, ActivityEditNominee::class.java)
                startActivity(intent)

            }
        }


    }

    private fun updatePhone() {
        dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.update_phone_number)
        val phone = dialog.findViewById<TextView>(R.id.phoneNumber)
        val editBtn = dialog.findViewById<MaterialButton>(R.id.updatePhone)
        phone.text = user_.phone
        editBtn.setOnClickListener {
            val phone_ = phone.text.toString().trim()
            if(phone_.isEmpty()){
                phone.error = "Please enter Phone number"
                return@setOnClickListener
            }
            else {
                updatePhoneNumber(phone_)
            }
        }
        dialog.show()

    }

    private fun updatePhoneNumber(phone_: String) {

        utils.startLoadingAnimation()
        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.UPDATE_PROFILE_API,
            Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if(jsonObject!=null){

                        if(jsonObject.getBoolean("success")==true){

                            val gson = Gson()
                            val user: ModelUser = gson.fromJson(jsonObject.getJSONObject("data").toString(), ModelUser::class.java)
                            setData(user)
                            dialog.dismiss()

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
                params["phone"]=phone_
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




    private fun showUpdatePinDialog() {
        dialogPinUpdate = Dialog(mContext)
        dialogPinUpdate.setContentView(R.layout.dialog_for_update_pin)
        dialogPinUpdate.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogPinUpdate.setCancelable(true)
        val pin1 = dialogPinUpdate.findViewById<EditText>(R.id.etPin1)
        val pin2 = dialogPinUpdate.findViewById<EditText>(R.id.etPin2)
        val pin3 = dialogPinUpdate.findViewById<EditText>(R.id.etPin3)
        val pin4 = dialogPinUpdate.findViewById<EditText>(R.id.etPin4)
        val pin5 = dialogPinUpdate.findViewById<EditText>(R.id.etPin5)
        val pin6 = dialogPinUpdate.findViewById<EditText>(R.id.etPin6)
        val btnSetPin = dialogPinUpdate.findViewById<Button>(R.id.btnSetpin)

        pin1.requestFocus()
        utils.moveFocus(listOf(pin1, pin2, pin3, pin4, pin5, pin6))

        val tvClearAll = dialogPinUpdate.findViewById<TextView>(R.id.tvClearAll)
        tvClearAll.setOnClickListener {
            utils.clearAll(listOf(pin1, pin2, pin3, pin4, pin5, pin6))
            pin1.requestFocus()
        }

        btnSetPin.setOnClickListener {
            val completePin = "${pin1.text}${pin2.text}${pin3.text}${pin4.text}${pin5.text}${pin6.text}"

                if (completePin.contains("-")&& completePin.length<6) {
                    Toast.makeText(mContext, "Enter valid pin", Toast.LENGTH_SHORT).show()
                } else {
                    updatePin(completePin)


            }
        }

        dialogPinUpdate.show()
    }

    private fun updatePin(completePin: String) {

        utils.startLoadingAnimation()
        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.UPDATE_PROFILE_API,
            Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if(jsonObject!=null){

                        if(jsonObject.getBoolean("success")==true){

                            val gson = Gson()
                            val user: ModelUser = gson.fromJson(jsonObject.getJSONObject("data").toString(), ModelUser::class.java)
                            setData(user)
                            dialogPinUpdate.dismiss()

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
                params["pin"]=completePin
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


    private fun setData(user: ModelUser) {
        Toast.makeText(mContext, "yes", Toast.LENGTH_SHORT).show()
        Glide.with(mContext).load(sharedPrefManager.getUser().photo).centerCrop()
            .placeholder(R.drawable.ic_launcher_background).into(binding.imgUser);
        binding.tvUserName.text = user.name
        binding.tvCNIC.text = user.cnic
        binding.tvAddress.text = user.address
        binding.tvFatherName.text = user.father_name
        binding.tvPhoneNumber.text = user.phone
        binding.tvNomineeName.text = user.nominees.name
        binding.tvNomineeFatherName.text = user.nominees.father_name
        binding.tvNomineeCnic.text = user.nominees.cnic
        binding.tvNomineeAddress.text = user.nominees.address
    }


    private fun updateData(userName: String, address: String, fatherName: String) {
        utils.startLoadingAnimation()
        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.UPDATE_PROFILE_API,
            Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()
                //Log.d("Response D", response)


                try {

                    val jsonObject = JSONObject(response)

                    if(jsonObject!=null){

                        if(jsonObject.getBoolean("success")==true){

                            val gson = Gson()
                            val user: ModelUser = gson.fromJson(jsonObject.getJSONObject("data").toString(), ModelUser::class.java)
                            setData(user)


                            dialog.dismiss()

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
                params["name"] = userName
                params["father_name"] = fatherName
                params["address"] = address
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

    fun updateNameDialog() {
        dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_update_name)

        val etUserName = dialog.findViewById<EditText>(R.id.name)
        val etFatherName = dialog.findViewById<EditText>(R.id.fatherName)
        val address = dialog.findViewById<EditText>(R.id.address)
        val btnUpdateName = dialog.findViewById<Button>(R.id.updateName)
        etUserName.setText(user_.name)
        etFatherName.setText(user_.father_name)
        address.setText(user_.address)
        btnUpdateName.setOnClickListener {
            val userName = etUserName.text.toString().trim()
            val fatherName = etFatherName.text.toString().trim()
            val userAddress = address.text.toString().trim()

            if (userName.isEmpty()) {
                etUserName.error = "Please enter your name"
                return@setOnClickListener
            }

            else if (fatherName.isEmpty()) {
                etFatherName.error = "Please enter your father's name"
                return@setOnClickListener
            }

            else if (userAddress.isEmpty()) {
                address.error = "Please enter your address"
                return@setOnClickListener
            }
            else {

                updateData(userName,userAddress,fatherName)

            }
//
//            // All fields are valid, display the entered data in a toast message
//            val toastMessage = "Name: $userName\nFather's Name: $fatherName\nAddress: $userAddress"
//            Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }


    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" // Allow only images

        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // Check if the intent data is not null and the data is valid
            data?.data?.let { selectedImageUri ->
                // Now you can use the selectedImageUri to do further operations, such as uploading the image
                // For instance, you can display the selected image in an ImageView
                // imageView.setImageURI(selectedImageUri)
                // Or perform an upload operation using this URI

                updateUserPhoto(selectedImageUri)


            }
        }
    }



    private fun getUser() {



        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.USER_DATA_API,
            com.android.volley.Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if (jsonObject != null) {

                        if (jsonObject.getBoolean("success") == true) {
                            val gson = Gson()
                            val user: ModelUser = gson.fromJson(jsonObject.getJSONObject("data").toString(), ModelUser::class.java)
                            user_=user

                            Toast.makeText(mContext, user.toString(), Toast.LENGTH_SHORT).show()
                            sharedPrefManager.setLoginStatus(user.status)
                            sharedPrefManager.saveUser(user)

                            setData(user!!)


                        } else if (jsonObject.getBoolean("success") == false) {

                            var error = jsonObject.getString("message")
                            Toast.makeText(mContext, " ${error}", Toast.LENGTH_SHORT).show()
                        }

                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                    utils.endLoadingAnimation()
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

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] =
                    "Bearer ${sharedPrefManager.getToken()}" // Replace "token" with your actual token
                return headers
            }


        }


        Volley.newRequestQueue(mContext).add(stringRequest)




    }




    /////////////////////////////// PENDING ////////////////////////////////////
    private fun updateUserPhoto(photo: Uri) {



        utils.startLoadingAnimation()

        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.UPDATE_PROFILE_API,
            Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if(jsonObject!=null){

                        if(jsonObject.getBoolean("success")==true){
                            
                            val gson = Gson()
                            val user: ModelUser = gson.fromJson(jsonObject.getJSONObject("data").toString(), ModelUser::class.java)
                            sharedPrefManager.saveUser(user)

                            setData(user)


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
                params["name"] = "test 123"
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