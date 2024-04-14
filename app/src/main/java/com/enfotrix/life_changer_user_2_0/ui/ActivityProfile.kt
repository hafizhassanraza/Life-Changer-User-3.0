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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.text.Editable
import android.util.Base64
import androidx.core.content.ContextCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse

import com.android.volley.toolbox.JsonObjectRequest
import com.enfotrix.life_changer_user_2_0.api.DataPart
import com.enfotrix.life_changer_user_2_0.api.VolleyMultipartRequest
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer


//class ActivityProfile(method: Int, mListener: Response.Listener<NetworkResponse>) : AppCompatActivity() {
class ActivityProfile : AppCompatActivity() {


    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }
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
//            editName.setOnClickListener {
//                updateNameDialog()
//            }

            layPhone.setOnClickListener {
                updatePhone()
            }
            layNominee.setOnClickListener {
                val intent = Intent(mContext, ActivityEditNominee::class.java)
                startActivity(intent)

            }
        }


    }

    fun convertImageToBase64(context: Context, bitmap:Bitmap): String? {
        val baos = ByteArrayOutputStream()
        /*val bitmapDrawable = ContextCompat.getDrawable(context, R.drawable.logo)
        if (bitmapDrawable == null) {
            Log.e("convertImageToBase64", "Drawable resource not found")
            return null
        }
        val bitmap = (bitmapDrawable as BitmapDrawable).bitmap
        if (bitmap == null) {
            Log.e("convertImageToBase64", "Bitmap could not be decoded")
            return null
        }*/
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        // Save the Base64 string to a file
        val file = File(context.filesDir, "base64String.txt")
        file.writeText(imageString)

        Log.d("convertImageToBase64", imageString)
        return imageString
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteCount = bitmap.byteCount
        val buffer = ByteBuffer.allocate(byteCount)
        bitmap.copyPixelsToBuffer(buffer)
        return buffer.array()
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
        utils.endLoadingAnimation()
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
        etUserName.text = user_.name?.takeIf { it.isNotBlank() }?.let { Editable.Factory.getInstance().newEditable(it) } ?: Editable.Factory.getInstance().newEditable("")
        etFatherName.text = user_.father_name?.takeIf { it.isNotBlank() }?.let { Editable.Factory.getInstance().newEditable(it) } ?: Editable.Factory.getInstance().newEditable("")
        address.text = user_.address?.takeIf { it.isNotBlank() }?.let { Editable.Factory.getInstance().newEditable(it) } ?: Editable.Factory.getInstance().newEditable("")

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
        }

        dialog.show()
    }




    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            selectedImage?.let { uri ->
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                convertImageToBase64(this, bitmap)?.let { uploadImageToServer(it) }
            }
        }
    }

    private fun uploadImageToServer(photo: String) {


        utils.startLoadingAnimation()

        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.ADD_PHOTO_API,
            Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if(jsonObject!=null){

                        if(jsonObject.getBoolean("success")==true){

                            Toast.makeText(mContext, jsonObject.toString(), Toast.LENGTH_SHORT).show()
                            Log.d("outPut", "uploadImageToServer: ${jsonObject.toString()}")

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
                "data:image/jpeg;base64,$photo"
                params["photo"] = "data:image/jpeg;base64,$photo"

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





    }


    private fun getUser() {


    utils.startLoadingAnimation()
        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.USER_DATA_API,
            com.android.volley.Response.Listener { response ->
                // Handle the response

                try {

                    val jsonObject = JSONObject(response)

                    if (jsonObject != null) {

                        if (jsonObject.getBoolean("success") == true) {
                            val gson = Gson()
                            val user: ModelUser = gson.fromJson(jsonObject.getJSONObject("data").toString(), ModelUser::class.java)
                            user_=user

//                            Toast.makeText(mContext, user.toString(), Toast.LENGTH_SHORT).show()
                            sharedPrefManager.setLoginStatus(user.status)
                            sharedPrefManager.saveUser(user)

                            setData(user!!)


                        } else if (jsonObject.getBoolean("success") == false) {
                            utils.endLoadingAnimation()

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



    /*private fun uploadImageToServer(photo: Bitmap) {




             utils.startLoadingAnimation()

             val url = "http://192.168.0.103:8000/api/test-photo"





             uploadImage(photo, url,
                 onSuccess = { response ->
                     utils.endLoadingAnimation()
                     try {



                         if (response != null) {

                             if (response.getBoolean("success") == true) {


                                 Log.e("t2", response.getString("data").toString())
     //                            Toast.makeText(mContext, response.getString("data").toString(), Toast.LENGTH_SHORT).show()



                             } else if (response.getBoolean("success") == false) {

                                 var error = response.getString("message")
                                 Toast.makeText(mContext, " ${error}", Toast.LENGTH_SHORT).show()
                             }

                         }


                     } catch (e: JSONException) {
                         e.printStackTrace()
                         Toast.makeText(mContext, e.message.toString(), Toast.LENGTH_SHORT).show()
                         // Handle JSON parsing error
                     }


                 },
                 onError = { error ->
                     utils.endLoadingAnimation()
                     Toast.makeText(mContext, "d2"+error.toString(), Toast.LENGTH_SHORT).show()

                     // Handle error
                 })


    }*/

    // uploadImage function


    // uploadImage function
    /*fun uploadImage(bitmap: Bitmap, url: String, onSuccess: (response: JSONObject) -> Unit, onError: (error: String) -> Unit) {

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()

        val request = object : VolleyMultipartRequest(
            Request.Method.POST, url,
            Response.Listener { response ->

                val jsonResponse = JSONObject(String(response.data))
                onSuccess(jsonResponse)
            },
            Response.ErrorListener { error ->
                onError(error.message ?: "An error occurred")
            }
        ) {
            override fun getByteData(): MutableMap<String, DataPart> {
                val params = HashMap<String, DataPart>()
                val dataPart = DataPart("photo", imageBytes, "image/png")
                params["photo"] = dataPart
                return params
            }
        }

        request.retryPolicy = DefaultRetryPolicy(
            0,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        Volley.newRequestQueue(mContext).add(request)
    }*/







    private fun updateUserPhoto(photo: Bitmap) {






        val byteArrayOutputStream = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()







        //Log.e("img", imageString)

        /*val byteArrayOutputStream = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
        val photoBase64: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)*/


        utils.startLoadingAnimation()




        val url = "http://192.168.0.103:8000/api/update-profile"

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
                params["photo"] = "photoBase64"
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