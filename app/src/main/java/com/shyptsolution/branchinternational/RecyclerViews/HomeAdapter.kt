package com.shyptsolution.branchinternational.RecyclerViews

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.contentValuesOf
import androidx.recyclerview.widget.RecyclerView
import com.shyptsolution.branchinternational.Chat
import com.shyptsolution.branchinternational.Entity.Message
import com.shyptsolution.branchinternational.MainActivity.GlobalObject.util
import com.shyptsolution.branchinternational.R
import org.json.JSONArray
import org.json.JSONObject

class HomeAdapter(private var context: Context, private var allMessages:List<List<Message>>): RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var userID: TextView =itemView.findViewById(R.id.user_id)
        var timeStamp: TextView =itemView.findViewById(R.id.timestamp)
        var body: TextView =itemView.findViewById(R.id.body)
        var cardView : CardView = itemView.findViewById(R.id.messge_card_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.home_message_layout, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentThread = allMessages[position][0]
      holder.userID.text= "User ID: ${currentThread.userId}  Thread ID: ${currentThread.threadId}"
      holder.timeStamp.text=util.convertToBeautifulDateFormat(currentThread.timestamp)
        if(currentThread.agentId=="-1")
      holder.body.text= currentThread.body
        else holder.body.text = currentThread.body + " - Agent ID: ${currentThread.agentId}"
      holder.cardView.setOnClickListener {
          val intent = Intent(context, Chat::class.java)
          intent.putExtra("index", position)
          context.startActivity(intent)
      }
    }

    override fun getItemCount(): Int {
        return allMessages.size
    }
}