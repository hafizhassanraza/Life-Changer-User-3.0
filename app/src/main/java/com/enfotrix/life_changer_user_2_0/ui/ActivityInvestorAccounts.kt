package com.enfotrix.life_changer_user_2_0.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.enfotrix.life_changer_user_2_0.Adapters.InvestorAccountsAdapter
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.ModelBankAccount
import com.enfotrix.life_changer_user_2_0.Models.ModelUser
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.api.Requests.ReqAddAccount
import com.enfotrix.life_changer_user_2_0.databinding.ActivityInvestorAccountsBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class ActivityInvestorAccounts : AppCompatActivity(), InvestorAccountsAdapter.OnItemClickListener {

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding: ActivityInvestorAccountsBinding

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog
    private lateinit var accountsList: List<ModelBankAccount>
    private lateinit var adapter: InvestorAccountsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestorAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this@ActivityInvestorAccounts
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        binding.rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)
        setTitle("My Bank Accounts")
        getUser()

        binding.fbAddInvestorAccount.setOnClickListener { showAddAccountDialog("user") }
    }

    private fun getUser() {
        utils.startLoadingAnimation()
        val url = "http://192.168.0.103:8000/api/user-data"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                utils.endLoadingAnimation()

                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject != null) {
                        if (jsonObject.getBoolean("success")) {
                            val gson = Gson()
                            val user: ModelUser = gson.fromJson(jsonObject.getJSONObject("data").toString(), ModelUser::class.java)
                            accountsList = user.accounts ?: emptyList()
                            setData(accountsList)
                        } else {
                            val error = jsonObject.getString("message")
                            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(mContext, e.message.toString(), Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "Response: ${error.message}", Toast.LENGTH_SHORT).show()
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

    private fun setData(accountsList: List<ModelBankAccount>) {
        Toast.makeText(mContext, "calling", Toast.LENGTH_SHORT).show()
        adapter = InvestorAccountsAdapter(constants.FROM_INVESTOR_ACCOUNTS, accountsList, this@ActivityInvestorAccounts)
        binding.rvInvestorAccounts.adapter = adapter
    }

    private fun showAddAccountDialog(type: String) {
        dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_account)

        val tvHeaderBank = dialog.findViewById<TextView>(R.id.tvHeaderBank)
        val tvHeaderBankDisc = dialog.findViewById<TextView>(R.id.tvHeaderBankDisc)
        val spBank = dialog.findViewById<Spinner>(R.id.spBank)
        val etAccountTittle = dialog.findViewById<EditText>(R.id.etAccountTittle)
        val etAccountNumber = dialog.findViewById<EditText>(R.id.etAccountNumber)
        val btnAddAccount = dialog.findViewById<Button>(R.id.btnAddAccount)

        if (type.equals(constants.VALUE_DIALOG_FLOW_NOMINEE_BANK)) {
            tvHeaderBank.text = "Add Nominee Account"
            tvHeaderBankDisc.text = "Add nominee bank account details for funds transfer and other services"
        }

        btnAddAccount.setOnClickListener {
            val accountTitle = etAccountTittle.text.toString()
            val accountNumber = etAccountNumber.text.toString()

            if (accountTitle.isNotEmpty() && accountNumber.isNotEmpty()) {
                addBankAccount(ReqAddAccount(type, spBank.selectedItem.toString(), accountTitle, accountNumber))
            } else {
                Toast.makeText(mContext, "Please enter account title and number", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun addBankAccount(req: ReqAddAccount) {
        utils.startLoadingAnimation()
        val url = "http://192.168.0.103:8000/api/add-account"
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                utils.endLoadingAnimation()
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject != null) {
                        if (jsonObject.getBoolean("success")) {
                            dialog.dismiss()
//                            val gson = Gson()
//                            val accountListType = object : TypeToken<List<ModelBankAccount>>() {}.type
//                            val accounts: List<ModelBankAccount> = gson.fromJson(jsonObject.getJSONArray("data").toString(), accountListType)
//                            setData(accounts)
//                            Toast.makeText(mContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show()
                            getUser()
                        } else {
                            val error = jsonObject.getString("message")
                            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: JSONException) {
                    dialog.dismiss()

                    e.printStackTrace()
                    Toast.makeText(mContext, e.message.toString(), Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                dialog.dismiss()
                utils.endLoadingAnimation()
                Toast.makeText(mContext, "Response: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("VolleyError", "Error: $error")
            }) {

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
                headers["Authorization"] = "Bearer ${sharedPrefManager.getToken()}"
                return headers
            }
        }

        Volley.newRequestQueue(mContext).add(stringRequest)
    }

    override fun onItemClick(modelBankAccount: ModelBankAccount) {
        // Handle item click here
    }

    override fun onDeleteClick(modelBankAccount: ModelBankAccount) {
        // Handle delete item click here
        Toast.makeText(mContext, "Available soon...", Toast.LENGTH_SHORT).show()
    }
}
