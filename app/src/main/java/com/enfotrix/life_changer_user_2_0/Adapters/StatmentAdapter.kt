package com.enfotrix.life_changer_user_2_0.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.life_changer_user_2_0.Constants
import com.enfotrix.life_changer_user_2_0.Models.TransactionModel
import com.enfotrix.life_changer_user_2_0.databinding.ItemStatmentBinding
import java.text.SimpleDateFormat
import java.util.Locale


class StatmentAdapter (val data: List<TransactionModel>) : RecyclerView.Adapter<StatmentAdapter.ViewHolder>(){


    var constant= Constants()



    interface OnItemClickListener {
        fun onItemClick(transactionModel: TransactionModel)
        fun onDeleteClick(transactionModel: TransactionModel)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemStatmentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) { holder.bind(data[position]) }
    override fun getItemCount(): Int { return data.size }
    inner class ViewHolder(val itemBinding: ItemStatmentBinding) : RecyclerView.ViewHolder(itemBinding.root){

        fun bind(transactionModel: TransactionModel) {


            itemBinding.tvReqDate.text = transactionModel.created_at
            itemBinding.tvPreviousBalance.text = transactionModel.previous_balance.toString()
            itemBinding.tvNewBalance.text = transactionModel.new_balance.toString()

            when(transactionModel.type){

                constant.TRANSACTION_TYPE_WITHDRAW -> {

                    itemBinding.transactionType.text = "Withdraw"
                    itemBinding.tvReqAmount.text = "-${transactionModel.amount}"
                    itemBinding.tvReqAmount.setTextColor(Color.RED)
                }
                constant.TRANSACTION_TYPE_TAX -> {

                    itemBinding.transactionType.text = "Tax"
                    itemBinding.tvReqAmount.text = "-${transactionModel.amount}"
                    itemBinding.tvReqAmount.setTextColor(Color.RED)
                }
                constant.TRANSACTION_TYPE_INVESTMENT -> {
                    itemBinding.transactionType.text = "Invest"
                    itemBinding.tvReqAmount.text = transactionModel.amount.toString()
                }
                constant.TRANSACTION_TYPE_PROFIT -> {
                    itemBinding.transactionType.text = "Profit"
                    itemBinding.tvReqAmount.text = transactionModel.amount.toString()
                }


            }




        }

    }

}