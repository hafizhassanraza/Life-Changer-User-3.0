package com.enfotrix.life_changer_user_2_0.ui

import User
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.enfotrix.life_changer_user_2_0.Adapters.InvestorAccountsAdapter
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.ModelBankAccount
import com.enfotrix.life_changer_user_2_0.Models.ModelUser
import com.enfotrix.life_changer_user_2_0.Models.NotificationModel
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.databinding.ActivityInvestorAccountsBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class ActivityInvestorAccounts : AppCompatActivity() , InvestorAccountsAdapter.OnItemClickListener {




    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding : ActivityInvestorAccountsBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager : SharedPrefManager
    private lateinit var dialog : Dialog
    private lateinit var accountsList: List<ModelBankAccount>



    private lateinit var adapter: InvestorAccountsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestorAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mContext=this@ActivityInvestorAccounts
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)
        setTitle("My Bank Accounts")
        getUser()
       // getInvestorAccounts()
        binding.fbAddInvestorAccount.setOnClickListener { addAccountDialog() }

    }

    fun getInvestorAccounts(){







        binding.rvInvestorAccounts.adapter=userViewModel.getInvestorAccountsAdapter(constants.FROM_INVESTOR_ACCOUNTS,this@ActivityInvestorAccounts)
        /*if (sharedPrefManager.getInvestorBankList().size>0)
            binding.rvInvestorAccounts.adapter = InvestorAccountsAdapter(constants.FROM_INVESTOR_ACCOUNTS,sharedPrefManager.getInvestorBankList(), this@ActivityInvestorAccounts)
        else Toast.makeText(mContext, "No Account Available!", Toast.LENGTH_SHORT).show()*/
    }





    private fun getUser() {


        utils.startLoadingAnimation()
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
                            accountsList=user!!.accounts!!
                            setData(accountsList)

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

    private fun setData(accountsList: List<ModelBankAccount>) {
        binding.rvInvestorAccounts.adapter=userViewModel.getInvestorAccountsAdapter(constants.FROM_INVESTOR_ACCOUNTS,this@ActivityInvestorAccounts)




    }


    fun addAccountDialog(){

        dialog = Dialog (mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_account)
        val tvHeaderBank = dialog.findViewById<TextView>(R.id.tvHeaderBank)
        val tvHeaderBankDisc = dialog.findViewById<TextView>(R.id.tvHeaderBankDisc)
        val spBank = dialog.findViewById<Spinner>(R.id.spBank)
        val etAccountTittle = dialog.findViewById<EditText>(R.id.etAccountTittle)
        val etAccountNumber = dialog.findViewById<EditText>(R.id.etAccountNumber)
        val btnAddAccount = dialog.findViewById<Button>(R.id.btnAddAccount)
        btnAddAccount.setOnClickListener {
            updateInvestorBankList(
                ModelBankAccount(
                    "",
                    spBank.selectedItem.toString(),
                    etAccountTittle.text.toString(),
                    etAccountNumber.text.toString(),
                    sharedPrefManager.getToken()
                )
            )
        }
        dialog.show()


    }
    fun updateInvestorBankList(modelBankAccount: ModelBankAccount) {

        utils.startLoadingAnimation()
        lifecycleScope.launch {
            userViewModel.addUserAccount(modelBankAccount)
                .observe(this@ActivityInvestorAccounts) {
                    dialog.dismiss()
                    if (it == true) {




                        lifecycleScope.launch{
                            userViewModel.getUserAccounts(sharedPrefManager.getToken())
                                .addOnCompleteListener{task ->
                                    utils.endLoadingAnimation()
                                    if (task.isSuccessful) {
                                        val list = ArrayList<ModelBankAccount>()
                                        if(task.result.size()>0){
                                            for (document in task.result) list.add( document.toObject(ModelBankAccount::class.java))
                                            sharedPrefManager.putInvestorBankList(list)
                                            getInvestorAccounts()
                                            Toast.makeText(mContext, constants.ACCOPUNT_ADDED_MESSAGE, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()

                                }
                                .addOnFailureListener{
                                    utils.endLoadingAnimation()
                                    Toast.makeText(mContext, it.message+"", Toast.LENGTH_SHORT).show()

                                }


                        }








                    }
                    else {
                        utils.endLoadingAnimation()
                        Toast.makeText(mContext, constants.SOMETHING_WENT_WRONG_MESSAGE, Toast.LENGTH_SHORT).show()
                    }

                }
        }




    }


    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }


    override fun onItemClick(modelBankAccount: ModelBankAccount) {

    }

    override fun onDeleteClick(modelBankAccount: ModelBankAccount) {

        Toast.makeText(mContext, "Available soon...", Toast.LENGTH_SHORT).show()
    }
}