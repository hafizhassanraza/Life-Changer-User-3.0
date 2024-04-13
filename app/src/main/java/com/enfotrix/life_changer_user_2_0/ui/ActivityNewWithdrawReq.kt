package com.enfotrix.life_changer_user_2_0.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
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
import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.enfotrix.life_changer_user_2_0.Models.UserViewModel
import com.enfotrix.life_changer_user_2_0.R
import com.enfotrix.life_changer_user_2_0.SharedPrefManager
import com.enfotrix.life_changer_user_2_0.Utils
import com.enfotrix.life_changer_user_2_0.api.Requests.ReqAddAccount
import com.enfotrix.life_changer_user_2_0.databinding.ActivityNewWithdrawReqBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class ActivityNewWithdrawReq : AppCompatActivity(), InvestorAccountsAdapter.OnItemClickListener {

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding: ActivityNewWithdrawReqBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()

    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: BottomSheetDialog
    private lateinit var confirmationDialog: Dialog
    private lateinit var dialogAddA: Dialog
    private var phoneNumber = null
    private var balance_: Int? = null
    private lateinit var userAccounts: List<ModelBankAccount>


    private lateinit var adapter: InvestorAccountsAdapter

    private var accountID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewWithdrawReqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this@ActivityNewWithdrawReq
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("Add Withdraw Request")
        getAccounts()


        confirmationDialog = Dialog(mContext)
        confirmationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        confirmationDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        confirmationDialog.setContentView(R.layout.dialog_check_withdraw_info)
        binding.layInvestorAccountSelect.setOnClickListener { getInvestorAccounts(constants.VALUE_DIALOG_FLOW_INVESTOR) }
        binding.layBalance.setOnClickListener { showAddBalanceDialog() }


        binding.btnWithdraw.setOnClickListener {

            val balance = binding.tvBalance.text.toString()
            var bankTitle = binding.tvAccountTittle.text.toString()
            var accountNumber = binding.tvAccountNumber.text.toString()
            if (balance.isEmpty() || balance.toDoubleOrNull() == 0.0) {
                Toast.makeText(mContext, "Please enter a valid balance", Toast.LENGTH_SHORT).show()
            } else if (bankTitle.isEmpty() || bankTitle.equals("Account Tittle")) {
                Toast.makeText(mContext, "Please Enter Account Details", Toast.LENGTH_SHORT).show()
            } else {

                //val investmentBalance = sharedPrefManager.getInvestment()?.investmentBalance?.takeIf { it.isNotBlank() }?.toInt() ?: 0
                //val lastProfit = sharedPrefManager.getInvestment()?.lastProfit?.takeIf { it.isNotBlank() }?.toInt() ?: 0
                val Balance = binding.tvBalance.text.toString()
                //var totalInvestorBalance=(investmentBalance + lastProfit)
                // Toast.makeText(mContext, ""+totalInvestorBalance, Toast.LENGTH_SHORT).show()
                // Toast.makeText(mContext, ""+Balance, Toast.LENGTH_SHORT).show()

                if (Balance.isNotBlank()) {
                    if (Balance.toInt() > balance_?.toInt()!!) {
                        Toast.makeText(mContext, "Please Enter a Valid Amount", Toast.LENGTH_SHORT).show()
                    } else {
                        withDrawAmount(Balance)
                    }
                    /* if (totalInvestorBalance >= balanceValue) {
                         showConfirmationDialog()
                     } else if (totalInvestorBalance < balanceValue) {
                         Toast.makeText(mContext, "Please Enter Valid Amount", Toast.LENGTH_SHORT).show()
                     }*/
                } else {
                    Toast.makeText(mContext, "Please Enter a Valid Amount", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun withDrawAmount(balanceValue: String) {


        Toast.makeText(mContext, "" + balanceValue + accountID, Toast.LENGTH_SHORT).show()


        utils.startLoadingAnimation()

        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.ADD_TRANSACTION_API,
            Response.Listener { response ->
                // Handle the response
                utils.endLoadingAnimation()


                try {

                    val jsonObject = JSONObject(response)

                    if (jsonObject != null) {

                        if (jsonObject.getBoolean("success") == true) {

                            Toast.makeText(mContext, "Investment Req. Sent!", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(
                                Intent(mContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                            finish()

                        } else if (jsonObject.getBoolean("success") == false) {

                            var error = jsonObject.getString("message")
                            Toast.makeText(mContext, "d1:$error", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(mContext, "d2" + e.message.toString(), Toast.LENGTH_SHORT).show()
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
                params["amount"] = balanceValue
                params["receiver_account_id"] = "adminAccountID"
                params["sender_account_id"] = accountID
                params["type"] = constants.TRANSACTION_TYPE_WITHDRAW
                params["receipt"] = "ikshdksgc"


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
                            val user: ModelUser = gson.fromJson(
                                jsonObject.getJSONObject("data").toString(),
                                ModelUser::class.java
                            )

                            var investment = user.investment

                            val inActiveInvestment = investment?.in_active_investment ?: 0
                            val activeInvestment = investment?.active_investment ?: 0
                            val profit = investment?.profit ?: 0
                            val expectedSum = inActiveInvestment + activeInvestment + profit
                            balance_ = expectedSum


                            binding.tvPhone.text = user.phone
                            userAccounts = user.accounts

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


    fun addAccountDialog(view: View) {

        dialogAddA = Dialog(mContext)
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
            updateInvestorBankList(
                ReqAddAccount(
                    "user",
                    spBank.selectedItem.toString(),
                    etAccountTittle.text.toString(),
                    etAccountNumber.text.toString(),
                )
            )
        }
        dialogAddA.show()


    }

    fun updateInvestorBankList(modelBankAccount: ReqAddAccount) {
        utils.startLoadingAnimation()
        val stringRequest = object : StringRequest(
            Request.Method.POST, ApiUrls.ADMIN_ACCOUNTS_API,
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


    fun addWithdrawReq(transactionModel: TransactionModel) {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            investmentViewModel.addTransactionReq(transactionModel)
                .observe(this@ActivityNewWithdrawReq) {
                    utils.endLoadingAnimation()
                    if (it == true) {
                        Toast.makeText(
                            mContext,
                            "Request Submitted Successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            mContext,
                            constants.SOMETHING_WENT_WRONG_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    fun showAddBalanceDialog() {
        val dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_add_balance)

        val etBalance = dialog.findViewById<EditText>(R.id.etBalance)
        val btnAddBalance = dialog.findViewById<Button>(R.id.btnAddBalance)

        btnAddBalance.setOnClickListener {
            dialog.dismiss()
            binding.tvBalance.text = etBalance.text
        }

        dialog.show()
    }

    fun getInvestorAccounts(from: String) {

        var rvInvestorAccounts: RecyclerView
        dialog = BottomSheetDialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_bottom_sheet_accounts)
        rvInvestorAccounts =
            dialog.findViewById<RecyclerView>(R.id.rvInvestorAccounts) as RecyclerView
        rvInvestorAccounts.layoutManager = LinearLayoutManager(mContext)
        if (from.equals(constants.VALUE_DIALOG_FLOW_INVESTOR)) {

            //Toast.makeText(mContext, userAccounts.size.toString(), Toast.LENGTH_SHORT).show()
            rvInvestorAccounts.adapter = InvestorAccountsAdapter(
                constants.FROM_NEW_INVESTMENT_REQ,
                userAccounts,
                this@ActivityNewWithdrawReq
            )
        }
//        else {
//            investorAccount = false
//            rvInvestorAccounts.adapter = InvestorAccountsAdapter(
//                constants.FROM_NEW_INVESTMENT_REQ,
//                adminAccounts,
//                this@ActivityNewInvestmentReq
//            )
//        }
        dialog.show()
    }

    override fun onItemClick(modelBankAccount: ModelBankAccount) {
        dialog.dismiss()

        binding.tvAccountNumber.text = modelBankAccount.account_number
        binding.tvBankName.text = modelBankAccount.bank_name
        binding.tvAccountTittle.text = modelBankAccount.account_tittle
        accountID = modelBankAccount.id
    }

    override fun onDeleteClick(modelBankAccount: ModelBankAccount) {
        // Handle the delete action here if needed
    }

    private fun showConfirmationDialog() {
        // Populate the confirmationDialog with required information
        val tvSenderAccountName =
            confirmationDialog.findViewById<TextView>(R.id.tvsender_account_name)
        val tvSenderAccountNumber =
            confirmationDialog.findViewById<TextView>(R.id.tvsender_account_number)
        val tvPhone = confirmationDialog.findViewById<TextView>(R.id.tvPhone)
        val tvAmount = confirmationDialog.findViewById<TextView>(R.id.tvamount)
        val btnCancel = confirmationDialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = confirmationDialog.findViewById<Button>(R.id.btnsave)

        tvSenderAccountName?.text = binding.tvBankName.text.toString()
        tvSenderAccountNumber?.text = binding.tvAccountNumber.text.toString()
        tvPhone?.text = binding.tvPhone.text.toString()
        tvAmount?.text = binding.tvBalance.text.toString()

        btnCancel.setOnClickListener {
            confirmationDialog.dismiss()
        }
        btnSave.setOnClickListener {

            /* val investmentBalance = sharedPrefManager.getInvestment().investmentBalance
             val lastProfit = sharedPrefManager.getInvestment().lastProfit
             val lastInvestment = sharedPrefManager.getInvestment().lastInvestment
             val ExpextedSum = getTextFromInvestment(investmentBalance).toDouble() + getTextFromInvestment(lastProfit).toDouble() + getTextFromInvestment(lastInvestment).toDouble()

             addWithdrawReq(
                 TransactionModel(
                     sharedPrefManager.getToken(),
                     constants.TRANSACTION_TYPE_WITHDRAW,
                     constants.TRANSACTION_STATUS_PENDING,
                     binding.tvBalance.text.toString(),
                     accountID,
                     ExpextedSum.toInt().toString(),
                 )
             )*/
            confirmationDialog.dismiss()
        }
        confirmationDialog.show()
    }


    fun getTextFromInvestment(value: String?): String {
        return if (value.isNullOrEmpty()) "0" else value
    }
}
