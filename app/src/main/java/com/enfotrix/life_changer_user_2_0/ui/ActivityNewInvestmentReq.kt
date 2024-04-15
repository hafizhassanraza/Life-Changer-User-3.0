package com.enfotrix.life_changer_user_2_0.ui
import android.app.Activity
import android.app.Dialog
import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.enfotrix.life_changer_user_2_0.Adapters.InvestorAccountsAdapter
import com.enfotrix.life_changer_user_2_0.ApiUrls
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.InvestmentViewModel
import com.enfotrix.life_changer_user_2_0.Models.ModelBankAccount
import com.enfotrix.life_changer_user_2_0.Models.ModelUser
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.api.Requests.ReqAddAccount
import com.enfotrix.life_changer_user_2_0.databinding.ActivityAddInvestmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class ActivityNewInvestmentReq : AppCompatActivity(), InvestorAccountsAdapter.OnItemClickListener {

    private val userViewModel: UserViewModel by viewModels()
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var binding : ActivityAddInvestmentBinding

    private lateinit var userAccounts: List<ModelBankAccount>
    private lateinit var adminAccounts: List<ModelBankAccount>
    private val MAX_IMAGE_SIZE_BYTES: Long = 2 * 1024 * 1024
    private val COMPRESSION_QUALITY = 80

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : BottomSheetDialog
    private lateinit var dialogAddA : Dialog

    private var expectedSum: Int=0


    private var investorAccount:Boolean=true

    private lateinit var adapter: InvestorAccountsAdapter

    private var accountID:String=""
    private var adminAccountID:String=""

    private var imageURI: Uri? = null
    private var finalImageURL: String? = null
    private val IMAGE_PICKER_REQUEST_CODE = 200
    private var userReceiptPhoto: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddInvestmentBinding.inflate(layoutInflater)

        setContentView(binding.root)

        mContext=this@ActivityNewInvestmentReq
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("Add Investment Request")
        getAccounts()


        binding.cvReceipt.setOnClickListener { showReceiptDialog() }
        binding.layInvestorAccountSelect.setOnClickListener { getAccounts(constants.VALUE_DIALOG_FLOW_INVESTOR) }
        binding.layAdminAccountSelect.setOnClickListener { getAccounts(constants.VALUE_DIALOG_FLOW_ADMIN) }

        binding.layBalance.setOnClickListener { showAddBalanceDialog() }
        binding.btnInvestment.setOnClickListener {
            if(binding.tvAccountNumber.text.isEmpty()|| binding.tvAccountNumber.text=="0000"){
                Toast.makeText(mContext, "Please Select bank Account ", Toast.LENGTH_SHORT).show()

            }

           else  if(binding.tvBalance.text.isEmpty()){
                Toast.makeText(mContext, "Please Enter balance for investment", Toast.LENGTH_SHORT).show()
            }

           else  if(binding.tvAdminAccountNumber.text.isEmpty()|| binding.tvAdminAccountNumber.text=="0000")
            {
                Toast.makeText(mContext, "Select Admin bank Account please", Toast.LENGTH_SHORT).show()
            }
            else if(binding.tvBalance.text.toString().toInt()<5){
                Toast.makeText(mContext, "Please Enter valid balance for investment", Toast.LENGTH_SHORT).show()

            }
            else if (userReceiptPhoto!=true || imageURI == null) Toast.makeText(mContext, "Please select the transaction image", Toast.LENGTH_SHORT).show()

            else{
                imageURI?.let { uri ->
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                         convertImageToBase64(mContext,bitmap)
                            dialog.dismiss()

                    } catch (e: IOException) {
                        // Handle errors related to reading the image bitmap
                        e.printStackTrace()
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

            }



        }


    }

    fun convertImageToBase64(context: Context, bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        val file = File(context.filesDir, "base64String.txt")
        file.writeText(imageString)
        Log.d("convertImageToBase64", imageString)
        addInvestmentReq(imageString.toString())
    }



    private fun getAccounts() {


        utils.startLoadingAnimation()

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
                            /*var investment = user.investment
                            val ExpextedSum =  investment.in_active_investment+ investment.active_investment +investment.profit*/
                            userAccounts= user.accounts

                            //Toast.makeText(mContext, userAccounts.size.toString(), Toast.LENGTH_SHORT).show()



                            getAdminAccount()

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

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] =
                    "Bearer ${sharedPrefManager.getToken()}" // Replace "token" with your actual token
                return headers
            }


        }


        Volley.newRequestQueue(mContext).add(stringRequest)




    }



    private fun getAdminAccount() {



        utils.startLoadingAnimation()

        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.ADMIN_ACCOUNTS_API,
            com.android.volley.Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()

                try {

                    val jsonObject = JSONObject(response)

                    if (jsonObject != null) {

                        if (jsonObject.getBoolean("success") == true) {



                            val gson = Gson()
                            val accounts: List<ModelBankAccount> = gson.fromJson(
                                jsonObject.getJSONArray("data").toString(),
                                object : TypeToken<List<ModelBankAccount>>() {}.type
                            )

                            adminAccounts= accounts







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

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] =
                    "Bearer ${sharedPrefManager.getToken()}" // Replace "token" with your actual token
                return headers
            }


        }


        Volley.newRequestQueue(mContext).add(stringRequest)




    }



    fun showReceiptDialog() {
        userReceiptPhoto = false

        dialogAddA = Dialog(mContext)
        dialogAddA.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogAddA.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAddA.setContentView(R.layout.receiptdialog)

        val tvSelect = dialogAddA.findViewById<TextView>(R.id.tvSelect)
        val  image = dialogAddA.findViewById<ImageView>(R.id.imgProfilePhoto)

        tvSelect?.setOnClickListener {
            userReceiptPhoto = true
            val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImage, IMAGE_PICKER_REQUEST_CODE)
            image.setImageURI(imageURI)


            dialogAddA.dismiss()
        }






        dialogAddA.show()
    }


    fun addAccountDialog(view: View){

        dialogAddA = Dialog (mContext)
        dialogAddA.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogAddA.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAddA.setContentView(R.layout.dialog_add_account)
        val tvHeaderBank = dialogAddA.findViewById<TextView>(R.id.tvHeaderBank)
        val tvHeaderBankDisc = dialogAddA.findViewById<TextView>(R.id.tvHeaderBankDisc)
        val spBank = dialogAddA.findViewById<Spinner>(R.id.spBank)
        val etAccountTittle = dialogAddA.findViewById<EditText>(R.id.etAccountTittle)
        val etAccountNumber = dialogAddA.findViewById<EditText>(R.id.etAccountNumber)
        val btnAddAccount = dialogAddA.findViewById<Button>(R.id.btnAddAccount)
        btnAddAccount.setOnClickListener {
            
            
            
            if(etAccountTittle.text.isNotEmpty()||etAccountNumber.text.isNotEmpty()) {


                updateInvestorBankList(
                    ReqAddAccount(
                        "user",
                        spBank.selectedItem.toString(),
                        etAccountTittle.text.toString(),
                        etAccountNumber.text.toString(),
                    )
                )
            }
            else
                Toast.makeText(mContext, "Please ,enter All fields!!", Toast.LENGTH_SHORT).show()
            
        }
        dialogAddA.show()


    }



    fun updateInvestorBankList(modelBankAccount: ReqAddAccount) {
        utils.startLoadingAnimation()
        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.ADD_ACCOUNTS_API,
            Response.Listener { response ->
                utils.endLoadingAnimation()
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject != null) {
                        if (jsonObject.getBoolean("success")) {
                            getAccounts()
                            dialogAddA.dismiss()
                        } else {
                            val error = jsonObject.getString("message")
                            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: JSONException) {
                    dialogAddA.dismiss()

                    e.printStackTrace()
                    Toast.makeText(mContext, e.message.toString(), Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                dialogAddA.dismiss()
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "Response: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("VolleyError", "Error: $error")
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["type"] = modelBankAccount.type
                params["bank_name"] = modelBankAccount.bank_name
                params["account_tittle"] = modelBankAccount.account_tittle
                params["account_number"] = modelBankAccount.account_number
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${sharedPrefManager.getToken()}"
                return headers
            }
        }

        Volley.newRequestQueue(mContext).add(stringRequest)

    }

   private fun addInvestmentReq(receipt: String) {
        utils.startLoadingAnimation()
        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.ADD_TRANSACTION_API,
            Response.Listener { response ->
                utils.endLoadingAnimation()
                try {

                    val jsonObject = JSONObject(response)
                    if(jsonObject!=null){

                        if(jsonObject.getBoolean("success")==true){
                            Toast.makeText(mContext, "Investment Req. Sent!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(mContext,MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                            finish()

                        }


                        else if(jsonObject.getBoolean("success")==false) {

                            var error= jsonObject.getString("message")
                            Toast.makeText(mContext, "d1:$error", Toast.LENGTH_SHORT).show()
                            //Toast.makeText(mContext, "response fail", Toast.LENGTH_SHORT).show()
                        }



                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(mContext, "d2"+e.message.toString(), Toast.LENGTH_SHORT).show()
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
                params["amount"] = binding.tvBalance.text.toString()
                params["receiver_account_id"] = adminAccountID
                params["sender_account_id"] = accountID
                params["type"] =constants.TRANSACTION_TYPE_INVESTMENT
                params["receipt"] =  "data:image/jpeg;base64,$receipt"
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



    fun showAddBalanceDialog() {


        var dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_balance)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val btnAddBalance = dialog.findViewById<Button>(R.id.btnAddBalance)


        btnAddBalance.setOnClickListener {

            var balance: Int= 0
            if(!etBalance.text.toString().isNullOrEmpty()) balance= Integer.parseInt(etBalance.text.toString())
            if(balance>0){

                if(etBalance.text.toString().isNullOrEmpty()) Toast.makeText(mContext, "enter amount", Toast.LENGTH_SHORT).show()
                else{
                    dialog.dismiss()
                    binding.tvBalance.text=etBalance.text
                }

            }
            else Toast.makeText(mContext, "Please Enter Balance", Toast.LENGTH_SHORT).show()

        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                val size = inputStream?.available()?.toLong() ?: 0
                inputStream?.close()
                if (size > MAX_IMAGE_SIZE_BYTES) {
                    // Image size exceeds limit, compress it
                    val compressedBitmap = compressImage(uri)
                    // Set compressed image to ImageView
                    binding.imgRecieptTransaction.setImageBitmap(compressedBitmap)
                } else {
                    // Image size is within limit, proceed without compression
                    binding.imgRecieptTransaction.setImageURI(uri)
                }
                // Store the selected image URI
                imageURI = uri
            }
        }
    }

    private fun compressImage(uri: Uri): Bitmap {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
        val compressedByteArray = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(compressedByteArray, 0, compressedByteArray.size)
    }


    fun getAccounts(from: String) {

        investorAccount=true
        var rvInvestorAccounts: RecyclerView
        dialog = BottomSheetDialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_bottom_sheet_accounts)
        rvInvestorAccounts = dialog.findViewById<RecyclerView>(R.id.rvInvestorAccounts)as RecyclerView
        rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)
        if(from.equals(constants.VALUE_DIALOG_FLOW_INVESTOR))
        {

            //Toast.makeText(mContext, userAccounts.size.toString(), Toast.LENGTH_SHORT).show()
            rvInvestorAccounts.adapter=InvestorAccountsAdapter(constants.FROM_NEW_INVESTMENT_REQ,userAccounts, this@ActivityNewInvestmentReq)
        }
        else {
            investorAccount=false
            rvInvestorAccounts.adapter=InvestorAccountsAdapter(constants.FROM_NEW_INVESTMENT_REQ,adminAccounts, this@ActivityNewInvestmentReq)
        }
        dialog.show()

    }
    override fun onItemClick(modelBankAccount: ModelBankAccount) {

        dialog.dismiss()

        if(investorAccount){
            binding.tvAccountNumber.text=modelBankAccount.account_number
            binding.tvBankName.text=modelBankAccount.bank_name
            binding.tvAccountTittle.text=modelBankAccount.account_tittle
            accountID=modelBankAccount.id
        }
        else {

            binding.tvAdminAccountNumber.text=modelBankAccount.account_number
            binding.tvAdminBankName.text=modelBankAccount.bank_name
            binding.tvAdminAccountTittle.text=modelBankAccount.account_tittle
            adminAccountID=modelBankAccount.id
        }

    }

    override fun onDeleteClick(modelBankAccount: ModelBankAccount) {

    }


}
