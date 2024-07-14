package com.shyptsolution.branchinternational.RecyclerViews

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.shyptsolution.branchinternational.Chat
import com.shyptsolution.branchinternational.Entity.Message
import com.shyptsolution.branchinternational.MainActivity.GlobalObject.util
import com.shyptsolution.branchinternational.R

class ChatAdapter(private var context: Context): RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private var allMessages = listOf<Message>()

    fun submitList(allMessages: List<Message>){
        this.allMessages = allMessages
        notifyItemInserted(this.allMessages.size-1)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val date : TextView = itemView.findViewById(R.id.chat_start_date)
        val receivedMsg : TextView = itemView.findViewById(R.id.received_message_text)
        val sentMsg : TextView = itemView.findViewById(R.id.send_message_text)
        val sentTime : TextView = itemView.findViewById(R.id.my_message_time)
        val receivedTime : TextView = itemView.findViewById(R.id.received_message_time)
        val sentMessageCard : CardView = itemView.findViewById(R.id.send_message_card)
        val receivedMessageCard : CardView = itemView.findViewById(R.id.received_message_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_message_layout, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMsg = allMessages[position]
        if(position==0){
            holder.date.text = util.getDateOnly(currentMsg.timestamp)
        }
        else if(util.getDateOnly(allMessages[position-1].timestamp)!=util.getDateOnly(currentMsg.timestamp)){
            holder.date.text = util.getDateOnly(currentMsg.timestamp)
        }
        else holder.date.visibility = View.GONE
        if(currentMsg.agentId=="-1"){
            holder.receivedMsg.text = currentMsg.body
            holder.receivedTime.text = util.getTimeOnly(currentMsg.timestamp)
            holder.sentMessageCard.visibility = View.GONE
            holder.sentTime.visibility = View.GONE
        }
        else{
            holder.sentMsg.text = currentMsg.body
            holder.sentTime.text = util.getTimeOnly(currentMsg.timestamp) + " Agent ID: ${currentMsg.agentId}"
            holder.receivedMessageCard.visibility = View.GONE
            holder.receivedTime.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return allMessages.size
    }
}