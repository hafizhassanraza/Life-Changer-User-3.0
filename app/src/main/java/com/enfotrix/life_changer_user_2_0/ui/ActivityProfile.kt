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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
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
import kotlinx.coroutines.launch
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
            addressLay.setOnClickListener {
                updateAddressDialog()
            }
            layPhone.setOnClickListener {
                updatePhone()
            }
            layNominee.setOnClickListener {
                val intent = Intent(mContext, ActivityEditNominee::class.java)
                val userJson = Gson().toJson(user_)
                intent.putExtra("user_model", userJson)
                startActivity(intent)
            }
        }

        // Set user profile image and check for additional data
        setimage()
        checkData()
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
            Toast.makeText(mContext, "clicked", Toast.LENGTH_SHORT).show()
        }
        dialog.show()

    }

    private fun updateAddressDialog() {
        Toast.makeText(mContext, "clicked", Toast.LENGTH_SHORT).show()

        dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_update_address)
        val address = dialog.findViewById<TextView>(R.id.address)
        val editBtn = dialog.findViewById<MaterialButton>(R.id.updateAddress)
        address.text = user_.address
        editBtn.setOnClickListener {
            Toast.makeText(mContext, "clicked", Toast.LENGTH_SHORT).show()
        }
        dialog.show()

    }


    private fun getUser() {


        val url = "http://192.168.0.103:8000/api/user-data"

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
                lifecycleScope.launch {
                    uploadImage(selectedImageUri,"InvestorProfilePhoto")

                }
            }
        }
    }




    fun updateNameDialog() {
        dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_update_name)

        val etUserName = dialog.findViewById<EditText>(R.id.name)
        val etFatherName = dialog.findViewById<EditText>(R.id.fatherName)
        val btnUpdateName = dialog.findViewById<Button>(R.id.updateName)
        etUserName.setText(user_.name)
        etFatherName.setText(user_.father_name)
        btnUpdateName.setOnClickListener{
            Toast.makeText(mContext, "clicked", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }





    suspend fun uploadImage(imageUri: Uri, type: String) {
        utils.startLoadingAnimation()

       /* try {
            val taskSnapshot = userViewModel.uploadPhoto(imageUri, type).await()

            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val modelFa: User = sharedPrefManager.getUser()
                modelFa.photo = uri.toString()

                lifecycleScope.launch {
                    val updateResult = userViewModel.updateUser(modelFa)

                    updateResult.observe(this@ActivityProfile) { success ->
                        utils.endLoadingAnimation()

                        if (success) {
                            sharedPrefManager.saveUser(modelFa)
                            Toast.makeText(
                                mContext,
                                "Profile Photo Updated",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialogPhoto.dismiss()
                        } else {
                            Toast.makeText(
                                mContext,
                                constants.SOMETHING_WENT_WRONG_MESSAGE,
                                Toast.LENGTH_SHORT
                            ).show()
                            dialogPhoto.dismiss()
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(mContext, exception.message + "", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            utils.endLoadingAnimation()
            Toast.makeText(mContext, "Failed to upload profile pic", Toast.LENGTH_SHORT).show()
        }*/
    }

    private  fun  setimage()
    {

        lifecycleScope.launch {
            userViewModel.getUser(sharedPrefManager.getToken())
                .addOnCompleteListener()
                {task->


                    if(task.isSuccessful)
                    {
                        var docu=task.result
                        investor= docu.toObject(investor::class.java)!!
                    }
                }
        }
    }

    private fun checkData() {
       /* if (!sharedPrefManager.getUser().fa_id.equals("")) {
            db.collection(constants.FA_COLLECTION).document(sharedPrefManager.getUser().fa_id)
                .addSnapshotListener { snapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    snapshot?.let { document ->
                        val modelFA = document.toObject<ModelFA>()
                        if (modelFA != null) {
                            sharedPrefManager.saveFA(modelFA)
                            setData()
                        }
                    }
                }
        }*/

//        db.collection(constants.ANNOUNCEMENT_COLLECTION).document("Rx3xDtgwOH7hMdWxkf94")
//            .addSnapshotListener { snapshot, firebaseFirestoreException ->
//                firebaseFirestoreException?.let {
//                    Toast.makeText(mContext, it.message.toString(), Toast.LENGTH_SHORT).show()
//                    return@addSnapshotListener
//                }
//
//                snapshot?.let { document ->
//                    val announcement = document.toObject<ModelAnnouncement>()
//                    if (announcement != null) {
//                        sharedPrefManager.putAnnouncement(announcement)
//                        setData()
//                    }
//                }
//            }
    }

    private fun setData(user: ModelUser) {
        Glide.with(mContext).load(user!!.photo).centerCrop()
            .placeholder(R.drawable.ic_launcher_background).into(binding.imgUser);
        binding.tvUserName.text = user!!.name
        binding.tvCNIC.text = user.cnic
        binding.tvAddress.text = user.address
        binding.tvFatherName.text = user.father_name
        //Toast.makeText(mContext, "name :"+user.father_name , Toast.LENGTH_SHORT).show()
        binding.tvPhoneNumber.text = user.phone

        /*binding.tvNomineeName.text = sharedPrefManager.getNominee().firstName
        binding.tvNomineeFatherName.text = sharedPrefManager.getNominee().lastName
        binding.tvNomineeCnic.text = sharedPrefManager.getNominee().cnic
        binding.tvNomineeAddress.text = sharedPrefManager.getNominee().address*/
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
            if (completePin == sharedPrefManager.getUser().pin) {
                if (completePin.contains("-")) {
                    Toast.makeText(mContext, "Enter valid pin", Toast.LENGTH_SHORT).show()
                } else {
                    startActivity(Intent(mContext,ActivityUpdatePassword::class.java ))
                    finish()

                }
            } else {
                Toast.makeText(mContext, "Invalid password, try another", Toast.LENGTH_SHORT).show()
            }
        }

        dialogPinUpdate.show()
    }
}