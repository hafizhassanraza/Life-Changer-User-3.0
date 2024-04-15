package com.enfotrix.life_changer_user_2_0.ui

import User
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
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
import com.enfotrix.life_changer_user_2_0.Models.ContactUsModel
import com.enfotrix.life_changer_user_2_0.Models.Contact_UsModel
import com.enfotrix.life_changer_user_2_0.Models.ModelUser
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityNavDrawerBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject

class ActivityNavDrawer : AppCompatActivity() {


    private val db = Firebase.firestore
    private lateinit var utils: Utils
    private lateinit var investor:User


    private lateinit var designatorWhatsapp:String
    private lateinit var designatorMail:String
    private lateinit var designatorPhone:String

    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var repo: Repo
    var listDesignation=ArrayList<String>()

    var list=ArrayList<ContactUsModel>()
    var listwhatsapp=ArrayList<String>()
    var listmail=ArrayList<String>()
    var listPhoneNumber=ArrayList<String>()
    var designatorList=ArrayList<Contact_UsModel>()

    private lateinit var user: User
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog
    private lateinit var dialogPinUpdate: Dialog
    private lateinit var dialogPhoto: Dialog
    private lateinit var dialogUpdateTaken: Dialog
    private val userViewModel: UserViewModel by viewModels()


    private lateinit var binding : ActivityNavDrawerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext=this@ActivityNavDrawer
        repo= Repo(mContext)
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager=SharedPrefManager(mContext)



        //dialog intilization.
        dialog = Dialog(mContext)
        dialog.setContentView(R.layout.dialog_for_contact_us)
        getContactList()
        binding.layInvestorAccount.setOnClickListener {
            startActivity(Intent(mContext, ActivityInvestorAccounts::class.java))
        }
        binding.layProfile.setOnClickListener {
            startActivity(Intent(mContext, ActivityProfile::class.java))
        }
        binding.imgUser.setOnClickListener {
            startActivity(Intent(mContext, ActivityProfile::class.java))
        }
        binding.imgUser.setOnClickListener {
            startActivity(Intent(mContext, ActivityProfile::class.java))
        }
        binding.layLogout.setOnClickListener {
            showLogoutDialog()
        }
        binding.layClose.setOnClickListener {
            finish()
        }

        binding.layContactUs.setOnClickListener {
            showContactDialog()
        }
        binding.layShare.setOnClickListener {
            Toast.makeText(mContext, "Available Soon...!", Toast.LENGTH_SHORT).show()
        }
        binding.layRateUs.setOnClickListener {
            Toast.makeText(mContext, "Available Soon...!", Toast.LENGTH_SHORT).show()
        }


        binding.layServices.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://enfotrix.com/"))
            startActivity(browserIntent)
        }
        binding.layAboutUs.setOnClickListener {
            startActivity(Intent(mContext,ActivityAboutUs::class.java))
        }



        val whatsapp = dialog.findViewById<ImageView>(R.id.whatsapp)
        val mail = dialog.findViewById<ImageView>(R.id.mail)
        val phone = dialog.findViewById<ImageView>(R.id.dailor)

        whatsapp?.setOnClickListener {
            openWhatsApp(designatorWhatsapp)

        }

        mail?.setOnClickListener {

            openEmail(designatorMail)
        }
        phone?.setOnClickListener{
            openDialer(designatorPhone)

        }





        setData()
    }


    private fun getContactList() {
        utils.startLoadingAnimation()

        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.CONTECT_US_API,
            Response.Listener { response ->
                utils.endLoadingAnimation()
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getBoolean("success")) {
                        val gson = Gson()
                        val dataJson = jsonObject.optJSONObject("data")
                        if (dataJson != null) {
                            val data: Contact_UsModel = gson.fromJson(dataJson.toString(), Contact_UsModel::class.java)
                            designatorList = mutableListOf(data) as ArrayList<Contact_UsModel>

                            // Assuming contacts is the correct field name, adjust accordingly
                            // setData(accountsList)
                        } else {
                            // Handle missing data field
                            Toast.makeText(mContext, "Data not found in response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val error = jsonObject.getString("message")
                        Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(mContext, "JSON Parsing Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "Volley Error: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("VolleyError", "Error: $error")
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${sharedPrefManager.getToken()}"
                return headers
            }
        }

        Volley.newRequestQueue(mContext).add(stringRequest)
    }










    private fun showContactDialog() {
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        val spinnerWithdrawType = dialog.findViewById<Spinner>(R.id.spinnerWithdrawType)
        if (listDesignation.isNotEmpty()) {
            val adapter = ArrayAdapter(mContext, R.layout.custom_spinner,designatorList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerWithdrawType.adapter = adapter
            spinnerWithdrawType.setSelection(0) // Set the first index as the selected value
            spinnerWithdrawType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                   val selectedDesignator = designatorList[position]
                   val designator=dialog.findViewById<TextView>(R.id.designator)
                   designator.setText(selectedDesignator.designation)
                 designatorWhatsapp = selectedDesignator.whatsapp
                   designatorMail=selectedDesignator.email
                    designatorPhone=selectedDesignator.mobile
    }
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        } else {
            // Set a default empty value in the spinner when the list is empty
            val emptyAdapter = ArrayAdapter(mContext, R.layout.custom_spinner, listOf("Empty"))
            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerWithdrawType.adapter = emptyAdapter

            // Set the spinner to display "Empty" as the selected value
            spinnerWithdrawType.setSelection(0)
            spinnerWithdrawType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Handle the selected value when the list is empty
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        dialog.show()
    }

    private fun setData() {
        Glide.with(mContext).load(sharedPrefManager.getUser().photo).centerCrop()
            .placeholder(R.drawable.ic_launcher_background).into(binding.imgUser);
        binding.tvUserName.text = sharedPrefManager.getUser().name
        binding.tvCNIC.text = sharedPrefManager.getUser().cnic

    }
    fun showLogoutDialog(): Boolean {
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.logout_dialog, null)
        val buttonYes: Button = dialogView.findViewById(R.id.btn_yes)
        val buttonNo: Button = dialogView.findViewById(R.id.btn_no)

        val builder = AlertDialog.Builder(mContext)
        builder.setView(dialogView)
        builder.setCancelable(true)
        var flag = false

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        buttonYes.setOnClickListener {
            sharedPrefManager.clearWholeSharedPref()
            sharedPrefManager.logOut()
            startActivity(Intent(mContext, ActivityLogin::class.java))
            finish()
            alertDialog.dismiss()
        }

        buttonNo.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
        return false
    }





    private fun openWhatsApp(phone:String) {
//        val phoneNumber = "+923036307725" // Replace with the phone number you want to chat with
        val message = "Hello, this is a custom message" // Replace with the message you want to send

        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$message")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }



    private   fun openEmail(email:String) {
        val subject = "Hello, this is the email subject" // Replace with the email subject
        val message = "This is the email message body" // Replace with the email message body

        val uri = Uri.parse("mailto:$email?subject=$subject&body=$message")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        startActivity(intent)
    }


    private fun openDialer(phoneNumber: String) {
        val uri = Uri.parse("tel:$phoneNumber")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        startActivity(intent)
    }


}