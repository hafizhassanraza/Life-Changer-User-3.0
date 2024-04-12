package com.enfotrix.life_changer_user_2_0.ui


import ApiClient
import User
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.NomineeViewModel
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.api.Requests.ReqAddAccount
import com.enfotrix.life_changer_user_2_0.databinding.ActivityUserDetailsBinding
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


import okhttp3.MultipartBody;
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream


class ActivityUserDetails : AppCompatActivity() {


    private val IMAGE_PICKER_REQUEST_CODE = 200

    private lateinit var uploadedImageURI: Uri

    val apiService = ApiClient.service()

    private var imageURI: Uri? = null
    private var NomineeCnicFrontURI: Uri? = null
    private var NomineeCnicBackURI: Uri? = null
    private var UserCnicFrontURI: Uri? = null
    private var UserCnicBackURI: Uri? = null

    private lateinit var imgSelectCnicBack: ImageView
    private lateinit var imgSelectCnicFront: ImageView

    private var NomineeCnicFront: Boolean = false
    private var NomineeCnicBack: Boolean = false
    private var UserCnicFront: Boolean = false
    private var UserCnicBack: Boolean = false
    private var UserProfilePhoto: Boolean = false

    private val userViewModel: UserViewModel by viewModels()
    private val nomineeViewModel: NomineeViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()

    private lateinit var binding: ActivityUserDetailsBinding

    private lateinit var imgProfilePhoto: ImageView


    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog


    var isNomineeAdded: Boolean = false
    var isNomineeBankAdded: Boolean = false
    var isUserBankAdded: Boolean = false
    var isUserPhotoAdded: Boolean = false
    var isUserCNICAdded: Boolean = false
    var isNomineeCNICAdded: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this@ActivityUserDetails
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)

        binding.btnStart.visibility = View.GONE

        checkProfileStatus()


        /*binding.layInvestorPhone.setOnClickListener {
            if (isPhoneNumberAdded) Toast.makeText(mContext, "Phone already added", Toast.LENGTH_SHORT).show()
            else {
                startActivity(
                    Intent(mContext, ActivityPhoneNumber::class.java).putExtra(
                        constants.KEY_ACTIVITY_FLOW,
                        constants.VALUE_ACTIVITY_FLOW_USER_DETAILS
                    )
                )


            }
        }*/



        binding.layAddNominee.setOnClickListener {

            if (isNomineeAdded) Toast.makeText(
                mContext,
                "Nominee already added",
                Toast.LENGTH_SHORT
            ).show()
            else startActivity(
                Intent(mContext, ActivityNominee::class.java).putExtra(
                    constants.KEY_ACTIVITY_FLOW,
                    constants.VALUE_ACTIVITY_FLOW_USER_DETAILS
                )
            )
        }

        binding.layInvestorBank.setOnClickListener {
            if (isUserBankAdded) Toast.makeText(
                mContext,
                "User Bank already added!",
                Toast.LENGTH_SHORT
            ).show()
            else showAddAccountDialog(constants.VALUE_DIALOG_FLOW_INVESTOR_BANK)

        }


        binding.layInvestorNomineeBank.setOnClickListener {

            if (isNomineeAdded) {
                if (isNomineeBankAdded) Toast.makeText(
                    mContext,
                    "Nominee Bank details already added!",
                    Toast.LENGTH_SHORT
                ).show()
                else showAddAccountDialog(constants.VALUE_DIALOG_FLOW_NOMINEE_BANK)
            } else Toast.makeText(mContext, "Please Add Nominee First!", Toast.LENGTH_SHORT).show()
        }




        binding.layInvestorProfilePhoto.visibility = View.GONE
        binding.layInvestorCNIC.visibility = View.GONE
        binding.layInvestorNomineeCNIC.visibility = View.GONE

        binding.layInvestorProfilePhoto.setOnClickListener {
            showPhotoDialog()
            /*if (isUserPhotoAdded) Toast.makeText(mContext, "User photo already added!", Toast.LENGTH_SHORT).show()
            else showPhotoDialog()*/
        }


        binding.layInvestorCNIC.setOnClickListener {
            if (isUserCNICAdded) Toast.makeText(
                mContext,
                "User CNIC already added!",
                Toast.LENGTH_SHORT
            ).show()
            else showAddCnicDialog(constants.VALUE_DIALOG_FLOW_INVESTOR_CNIC)
        }


        binding.layInvestorNomineeCNIC.setOnClickListener {

            if (isNomineeAdded) {
                if (isNomineeCNICAdded) Toast.makeText(
                    mContext,
                    "Nominee CNIC already added!",
                    Toast.LENGTH_SHORT
                ).show()
                else showAddCnicDialog(constants.VALUE_DIALOG_FLOW_NOMINEE_CNIC)
            } else Toast.makeText(mContext, "Please Add Nominee First!", Toast.LENGTH_SHORT).show()


        }





        binding.btnStart.setOnClickListener {

            updateUserStatus()
        }


        binding.layLogout.setOnClickListener {

            val builder1: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder1.setMessage("Are you sure you want to logout?")
            builder1.setCancelable(true)

            builder1.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                    sharedPrefManager.clearWholeSharedPref()
                    startActivity(Intent(mContext,ActivityLogin::class.java))
                })

            builder1.setNegativeButton(
                "No",
                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
            val alert11: AlertDialog = builder1.create()
            alert11.show()

        }

    }

    private fun updateUserStatus() {



        utils.startLoadingAnimation()
        val url = "http://192.168.0.103:8000/api/update-profile"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            com.android.volley.Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if (jsonObject != null) {

                        if (jsonObject.getBoolean("success") == true) {




                            sharedPrefManager.setLoginStatus(constants.INVESTOR_STATUS_PENDING)
                            Toast.makeText(mContext, " ${jsonObject.getString("message")}", Toast.LENGTH_SHORT).show()


                            startActivity(
                                Intent(mContext, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                            finish()



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
                params["status"] = constants.INVESTOR_STATUS_PENDING


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

    fun getUser() {


        /* utils.startLoadingAnimation()
        apiService.getUser(sharedPrefManager.getToken())
            .enqueue(object : Callback<ResUser> {
                override fun onResponse(call: Call<ResUser>, response: Response<ResUser>) {
                    utils.endLoadingAnimation()

                    if (response.isSuccessful) {
                        val res: ResUser? = response.body()
                        if (res != null) {

                            dialog.dismiss()
                            setData()


                        }



                    } else {
                        dialog.dismiss()
                        setData()
                        Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResUser>, t: Throwable) {
                    utils.endLoadingAnimation()
                    dialog.dismiss()
                    setData()
                    Toast.makeText(mContext, t.message.toString(), Toast.LENGTH_SHORT).show()
                }

            })*/


    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {


            when {
                UserCnicFront -> {
                    UserCnicFrontURI = data?.data
                    imgSelectCnicFront.setImageResource(R.drawable.check_small)
                }

                UserCnicBack -> {
                    UserCnicBackURI = data?.data
                    imgSelectCnicBack.setImageResource(R.drawable.check_small)
                }

                NomineeCnicFront -> {
                    NomineeCnicFrontURI = data?.data
                    imgSelectCnicFront.setImageResource(R.drawable.check_small)
                }

                NomineeCnicBack -> {
                    NomineeCnicBackURI = data?.data
                    imgSelectCnicBack.setImageResource(R.drawable.check_small)
                }

                UserProfilePhoto -> {
                    Glide.with(mContext).load(data?.data).into(imgProfilePhoto)
                    imageURI = data?.data
                }
            }


        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_right_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.top_logout -> {

                sharedPrefManager.clearWholeSharedPref()
                startActivity(Intent(mContext, ActivityLogin::class.java))
                //Toast.makeText(applicationContext, "Available soon!!", Toast.LENGTH_LONG).show()
                true
            }

            R.id.top_contactUs -> {
                Toast.makeText(applicationContext, "click on share", Toast.LENGTH_LONG).show()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setData() {


        var checkCounter: Int = 0
        if (isNomineeAdded) {
            checkCounter++
            binding.tvHeaderNominee.text = "Completed"
            binding.tvHeaderNominee.setTextColor(Color.parseColor("#2F9B47"))
            binding.imgCheckNominee.setImageResource(R.drawable.check_small)
        }
        if (isNomineeBankAdded) {
            checkCounter++
            binding.tvHeaderNomineeBank.text = "Completed"
            binding.tvHeaderNomineeBank.setTextColor(Color.parseColor("#2F9B47"))
            binding.imgCheckNomineeBank.setImageResource(R.drawable.check_small)
        }
        if (isUserBankAdded) {
            checkCounter++
            binding.tvHeaderUserBank.text = "Completed"
            binding.tvHeaderUserBank.setTextColor(Color.parseColor("#2F9B47"))
            binding.imgCheckUserBank.setImageResource(R.drawable.check_small)
        }

//        if (isUserPhotoAdded) {
//            checkCounter++
//            binding.tvHeaderUserPhoto.text = "Completed"
//            binding.tvHeaderUserPhoto.setTextColor(Color.parseColor("#2F9B47"))
//            binding.imgCheckUserPhoto.setImageResource(R.drawable.check_small)
//        }
//        if (isUserCNICAdded) {
//            checkCounter++
//            binding.tvHeaderUserCnic.text = "Completed"
//            binding.tvHeaderUserCnic.setTextColor(Color.parseColor("#2F9B47"))
//            binding.imgCheckUserCnic.setImageResource(R.drawable.check_small)
//        }
//        if (isNomineeCNICAdded) {
//            checkCounter++
//            binding.tvHeaderNomineeCnic.text = "Completed"
//            binding.tvHeaderNomineeCnic.setTextColor(Color.parseColor("#2F9B47"))
//            binding.imgCheckNomineeCnic.setImageResource(R.drawable.check_small)
//        }
        /* if (isPhoneNumberAdded) {
            checkCounter++
            binding.tvHeaderUserPhoneNumber.text = "Completed"
            binding.tvHeaderUserPhoneNumber.setTextColor(Color.parseColor("#2F9B47"))
            binding.imgCheckUserPhoneNumber.setImageResource(R.drawable.check_small)
        }*/



//        if (checkCounter == 1) binding.v1.setBackgroundColor(resources.getColor(R.color.primary))
//        if (checkCounter == 2) binding.v2.setBackgroundColor(resources.getColor(R.color.primary))
//        if (checkCounter == 3) binding.v3.setBackgroundColor(resources.getColor(R.color.primary))
//        if (checkCounter == 4) binding.v4.setBackgroundColor(resources.getColor(R.color.primary))
//        if (checkCounter == 5) binding.v5.setBackgroundColor(resources.getColor(R.color.primary))
//        if (checkCounter == 6) binding.v6.setBackgroundColor(resources.getColor(R.color.primary))

        if (checkCounter == 3) {
            binding.btnStart.visibility = View.VISIBLE

        }

    }

    private fun checkProfileStatus() {




        utils.startLoadingAnimation()
        val url = "http://192.168.0.103:8000/api/check-profile-status"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            com.android.volley.Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if (jsonObject != null) {

                        if (jsonObject.getBoolean("success") == true) {


                            var data = jsonObject.getJSONObject("data")


                            Toast.makeText(mContext, " d1 $data", Toast.LENGTH_SHORT).show()

                            isNomineeAdded = data.getBoolean("isNomineeAdded")
                            isNomineeCNICAdded = data.getBoolean("NomineeCnicFront")
                            isUserCNICAdded = data.getBoolean("UserCnicFront")
                            isUserPhotoAdded = data.getBoolean("UserProfilePhoto")
                            isNomineeBankAdded = data.getBoolean("isNomineeBankAdded")
                            isUserBankAdded = data.getBoolean("isUserBankAdded")



                            setData()


                            Log.e("456", "$data")


                        } else if (jsonObject.getBoolean("success") == false) {


                            var error = jsonObject.getString("message")
                            Toast.makeText(mContext, "d1 ${error}", Toast.LENGTH_SHORT).show()
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

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] =
                    "Bearer ${sharedPrefManager.getToken()}" // Replace "token" with your actual token
                return headers
            }


        }


        Volley.newRequestQueue(mContext).add(stringRequest)




    }

    fun showAddCnicDialog(type: String) {

        NomineeCnicFront = false
        NomineeCnicBack = false
        UserCnicFront = false
        UserCnicBack = false
        UserProfilePhoto = false

        NomineeCnicFrontURI = null
        NomineeCnicBackURI = null
        UserCnicFrontURI = null
        UserCnicBackURI = null
        imageURI = null

        dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_upload_cnic)

        imgSelectCnicFront = dialog.findViewById<ImageView>(R.id.imgSelectCnicFront)
        imgSelectCnicBack = dialog.findViewById<ImageView>(R.id.imgSelectCnicBack)
        val tvSelectCnicFront = dialog.findViewById<TextView>(R.id.tvSelectCnicFront)
        val tvSelectCnicBack = dialog.findViewById<TextView>(R.id.tvSelectCnicBack)
        val tvHeaderDesc = dialog.findViewById<TextView>(R.id.tvHeaderDesc)
        val tvHeader = dialog.findViewById<TextView>(R.id.tvHeader)
        val btnUploadCNIC = dialog.findViewById<Button>(R.id.btnUploadCNIC)
        if (type.equals(constants.VALUE_DIALOG_FLOW_NOMINEE_CNIC)) {
            tvHeader.setText("Nominee CNIC Photo !")
            tvHeaderDesc.setText("Upload both (Front and Back) side photo of your Nominee CNIC")
        }


        tvSelectCnicFront.setOnClickListener {

            NomineeCnicFront = type == constants.VALUE_DIALOG_FLOW_NOMINEE_CNIC
            UserCnicFront = type == constants.VALUE_DIALOG_FLOW_INVESTOR_CNIC
            UserCnicBack = false
            NomineeCnicBack = false


            val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImage, IMAGE_PICKER_REQUEST_CODE)

        }
        tvSelectCnicBack.setOnClickListener {


            NomineeCnicFront = false
            UserCnicFront = false
            UserCnicBack = type == constants.VALUE_DIALOG_FLOW_INVESTOR_CNIC
            NomineeCnicBack = type == constants.VALUE_DIALOG_FLOW_NOMINEE_CNIC


            val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImage, IMAGE_PICKER_REQUEST_CODE)


        }
        btnUploadCNIC.setOnClickListener {
            if (type.equals(constants.VALUE_DIALOG_FLOW_NOMINEE_CNIC)) {

                if (NomineeCnicFrontURI != null && NomineeCnicBackURI != null) {

                    lifecycleScope.launch {
                        //add(NomineeCnicFrontURI!!, NomineeCnicBackURI!!, type)
                    }
                } else Toast.makeText(mContext, "Please Select both photos", Toast.LENGTH_SHORT)
                    .show()


            } else if (type.equals(constants.VALUE_DIALOG_FLOW_INVESTOR_CNIC)) {
                if (UserCnicFrontURI != null && UserCnicBackURI != null)
                    lifecycleScope.launch {
                        // addUserCNIC(UserCnicFrontURI!!, UserCnicBackURI!!, type)
                    }
                else Toast.makeText(mContext, "Please Select Image", Toast.LENGTH_SHORT).show()
            }

        }

        dialog.show()
    }

    fun showAddAccountDialog(type: String) {

        dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_account)

        val tvHeaderBank = dialog.findViewById<TextView>(R.id.tvHeaderBank)
        val tvHeaderBankDisc = dialog.findViewById<TextView>(R.id.tvHeaderBankDisc)
        val spBank = dialog.findViewById<Spinner>(R.id.spBank)
        val etAccountTittle = dialog.findViewById<EditText>(R.id.etAccountTittle)
        val etAccountNumber = dialog.findViewById<EditText>(R.id.etAccountNumber)
        val btnAddAccount = dialog.findViewById<Button>(R.id.btnAddAccount)

        if (type.equals(constants.VALUE_DIALOG_FLOW_NOMINEE_BANK)) {
            tvHeaderBank.setText("Add Nominee Account");
            tvHeaderBankDisc.setText("Add nominee bank account details for funds transfer and other servicest");
        }

        btnAddAccount.setOnClickListener {


            if (etAccountNumber.text.toString().isNotEmpty()) {

                if (etAccountTittle.text.isNotEmpty()) {

                    addBankAccount(
                        ReqAddAccount(
                            type,
                            spBank.selectedItem.toString(),
                            etAccountTittle.text.toString(),
                            etAccountNumber.text.toString()

                        )
                    )
                } else Toast.makeText(mContext, "Please enter account tittle", Toast.LENGTH_SHORT)
                    .show()

            } else Toast.makeText(mContext, "Please enter account number", Toast.LENGTH_SHORT)
                .show()


        }

        dialog.show()
    }

    fun showPhotoDialog() {


        NomineeCnicFront = false
        NomineeCnicBack = false
        UserCnicFront = false
        UserCnicBack = false
        UserProfilePhoto = false

        dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_profile_photo_upload)

        imgProfilePhoto = dialog.findViewById<ImageView>(R.id.imgProfilePhoto)
        val tvSelect = dialog.findViewById<TextView>(R.id.tvSelect)
        val btnUplodProfile = dialog.findViewById<Button>(R.id.btnUpload)

        tvSelect.setOnClickListener {
            UserProfilePhoto = true
            val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImage, IMAGE_PICKER_REQUEST_CODE)
        }

        btnUplodProfile.setOnClickListener {



            dialog.dismiss()

            if (imageURI != null) {


//                lifecycleScope.launch {
//                    val base64 = convertUriToBase64(imageURI)
//                    // Use base64 as needed
//                    Log.d("tag222",base64.toString() )
//
//                    Toast.makeText(mContext, "$base64", Toast.LENGTH_SHORT).show()
//
//                }




                //convertUriToBase64(imageURI)?.let { it1 -> uploadImage(it1) }

                //imageUriToBase64(mContext, imageURI!!)?.let { photo -> uploadImage(photo) }



            } else {
                Toast.makeText(mContext, "d=2", Toast.LENGTH_SHORT).show()
            }


        }

        dialog.show()
    }

    fun addBankAccount(req: ReqAddAccount) {


        utils.startLoadingAnimation()
        val url = "http://192.168.0.103:8000/api/add-account"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            com.android.volley.Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if (jsonObject != null) {

                        if (jsonObject.getBoolean("success") == true) {


                            dialog.dismiss()


                            Toast.makeText(
                                mContext,
                                jsonObject.getString("message").toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            if (req.type.equals(constants.VALUE_DIALOG_FLOW_NOMINEE_BANK)) isNomineeBankAdded =
                                true
                            else isUserBankAdded = true
                            setData()


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
            // Override getParams() to add POST parameters
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["account_number"] = req.account_number
                params["account_tittle"] = req.account_tittle
                params["bank_name"] = req.bank_name
                params["type"] = req.type

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


        /*utils.startLoadingAnimation()
        apiService.addFirstAccount(req)
            .enqueue(object : Callback<ResUserProfileReg> {
                override fun onResponse(call: Call<ResUserProfileReg>, response: Response<ResUserProfileReg>) {
                    utils.endLoadingAnimation()

                    if (response.isSuccessful) {
                        val res: ResUserProfileReg? = response.body()
                        if (res != null) {

                            Toast.makeText(mContext, constants.ACCOPUNT_ADDED_MESSAGE, Toast.LENGTH_SHORT).show()
                            dialog.dismiss()

                            isNomineeAdded=res.isNomineeAdded
                            isNomineeBankAdded=res.isNomineeBankAdded
                            isUserBankAdded=res.isUserBankAdded
                            isUserPhotoAdded=res.isUserPhotoAdded
                            isUserCNICAdded=res.isUserCNICAdded
                            isNomineeCNICAdded=res.isNomineeCNICAdded
                            setData()


                        }



                    } else {
                        dialog.dismiss()
                        Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResUserProfileReg>, t: Throwable) {
                    utils.endLoadingAnimation()
                    dialog.dismiss()
                    Toast.makeText(mContext, t.message.toString(), Toast.LENGTH_SHORT).show()
                }

            })*/


    }


    fun uriToBitmap(contentResolver: ContentResolver, imageUri: Uri): Bitmap? {
        return try {
            // Decode the image from URI
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


     /*suspend fun convertUriToBase64(uri: Uri?) {
         try {
             val bytes = uri?.let { contentResolver.openInputStream(it)?.readBytes() }

             return Base64.encodeToString(bytes, Base64.DEFAULT)
         } catch (error: IOException) {
             error.printStackTrace() // This exception always occurs
         }
    }*/
    fun imageUriToBase64(context: Context, imageUri: Uri): String? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return null
    }
    private fun bitmapToFile(bitmap: Bitmap): File {
        val file = File(mContext.cacheDir, "temp_image.jpg")
        file.createNewFile()

        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bitmapData = bos.toByteArray()

        val fos = FileOutputStream(file)
        fos.write(bitmapData)
        fos.flush()
        fos.close()

        return file
    }


    private fun uploadImage(photo: String){


        Toast.makeText(mContext, "$photo", Toast.LENGTH_SHORT).show()
        /*utils.startLoadingAnimation()
        val url = "http://192.168.0.103:8000/api/add-photo"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            com.android.volley.Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if (jsonObject != null) {

                        if (jsonObject.getBoolean("success") == true) {


                            dialog.dismiss()


                            Toast.makeText(
                                mContext,
                                jsonObject.toString(),
                                //jsonObject.getString("message").toString(),
                                Toast.LENGTH_SHORT
                            ).show()





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
            // Override getParams() to add POST parameters
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["photo"] = photo
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] =
                    "Bearer ${sharedPrefManager.getToken()}" // Replace "token" with your actual token
                return headers
            }


        }


        Volley.newRequestQueue(mContext).add(stringRequest)*/



    }


//    private fun uploadImage(imageURI: Uri, type: String) {
//
//
//        Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show()
//        //utils.startLoadingAnimation()
//        val file = File(imageURI.path)
//
//        val requestBody: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
//        val parts: MultipartBody.Part = MultipartBody.Part.createFormData("newimage", file.name, requestBody)
//
//        val retrofit: Retrofit = ApiClient.service()
//        val uploadApis: APIService = retrofit.create(APIService::class.java)
//
//        val call: Call<RequestBody?>? = uploadApis.uploadImage(parts, type)
//
//        val callback: Callback<RequestBody?> = object : Callback<RequestBody?> {
//            override fun onResponse(call: Call<RequestBody?>, response: Response<RequestBody?>) {
//                //utils.endLoadingAnimation()
//                // Handle response here
//                if (response.isSuccessful) {
//                    // Successful response
//                } else {
//                    // Unsuccessful response
//                }
//            }
//
//            override fun onFailure(call: Call<RequestBody?>, t: Throwable) {
//
//                //utils.endLoadingAnimation()
//
//                // Handle failure here
//            }
//        }
//
//        call?.enqueue(callback)
//    }


}
