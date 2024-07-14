package com.shyptsolution.branchinternational

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.shyptsolution.branchinternational.Entity.Message
import com.shyptsolution.branchinternational.MainActivity.GlobalObject.API_URL
import com.shyptsolution.branchinternational.MainActivity.GlobalObject.allMessageInOrder
import com.shyptsolution.branchinternational.MainActivity.GlobalObject.util
import com.shyptsolution.branchinternational.RecyclerViews.HomeAdapter
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity(),LifecycleOwner {
    private val TAG = "HOME"
    object GlobalObject{
        val allMessageInOrder = mutableListOf<List<Message>>()
        val util = Utils()
        val API_URL = "https://android-messaging.branch.co/api/"
    }
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences("branch-international", AppCompatActivity.MODE_PRIVATE)
        editor =  sharedPreferences.edit()
        supportActionBar?.title = "Branch International"
        getAllMessages()
    }

    private fun getAllMessages(){
        val progressBar = findViewById<ProgressBar>(R.id.loadMessageProgressBar)
        progressBar.visibility = View.VISIBLE
        val request = object : StringRequest(Request.Method.GET, API_URL+"messages",
           { response ->

               val allMessage = processAllMessage(response)
               recyclerView = findViewById(R.id.homeRecyclerView)
               recyclerView.setHasFixedSize(false)
               recyclerView.layoutManager = LinearLayoutManager(this)
               adapter = HomeAdapter(this,allMessage)
               recyclerView.itemAnimator = null
               recyclerView.adapter = adapter
               adapter.notifyDataSetChanged()
               progressBar.visibility = View.GONE
            },
            { error ->
                Log.d(TAG,error.toString())
                progressBar.visibility = View.GONE
                when(error){
                    is AuthFailureError -> {
                        util.notify("Session expired, Please login.", this)
                        editor.apply {
                            putLong("lastLoginTime",0)
                            apply()
                        }
                        startActivity(Intent(this,Login::class.java))
                    }
                    is NoConnectionError -> util.notify("Please connect to Internet.",this)
                    is TimeoutError -> util.notify("Try Logging in again.",this)
                    else -> util.notify("Couldn't fetch messages",this)
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["X-Branch-Auth-Token"] = sharedPreferences.getString("auth_token","").toString()
                return headers
            }
        }
        request.setShouldCache(false)
        val queue = Volley.newRequestQueue(this)
        queue.cache.clear()
        queue.add(request)

    }

    private fun processAllMessage(response:String):List<List<Message>> {
        allMessageInOrder.clear()
        val messages = JSONArray(response)
        val messageList = mutableListOf<Message>()
        val sortedByTime = hashMapOf<Int,Long>()
        for (i in 0 until messages.length()) {
            val messageObject = messages.getJSONObject(i)
            val id = messageObject.getInt("id")
            val threadId = messageObject.getInt("thread_id")
            val userId = messageObject.getString("user_id")
            val body = messageObject.getString("body")
            val timeStamp = messageObject.getString("timestamp")
            val agent_id= messageObject.getString("agent_id")
            val agentId = if(agent_id=="null") "-1"  else agent_id
            val message = Message(id, threadId,userId,body,timeStamp,agentId)
            val timeStampInMillis = util.convertDateTimeToMilliseconds(timeStamp)
            val currentMax = sortedByTime[threadId]
            if (currentMax == null || timeStampInMillis > currentMax) {
                sortedByTime[threadId] = timeStampInMillis
            }
            messageList.add(message)
        }

        val groupedMessages = messageList.groupBy { it.threadId }
        val sortedGroupedMessages = hashMapOf<Int,List<Message>>()
        for ((key, group) in groupedMessages) {
           sortedGroupedMessages[key] =  group.sortedByDescending { util.convertDateTimeToMilliseconds(it.timestamp) }
        }
        val sortedList = sortedByTime.toList().sortedByDescending { it.second }
        for(key in sortedList ){
            allMessageInOrder.add(sortedGroupedMessages[key.first]!!)
        }
        return allMessageInOrder
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Log.d(TAG,"Back Pressed")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                editor.apply {
                    putLong("lastLoginTime",0)
                    apply()
                }
                startActivity(Intent(this,Login::class.java))
                true
            }

            R.id.clearChat -> {
                val queue = Volley.newRequestQueue(this)
                val jsonBody = """
                {
                 
                }
                """.trimIndent()
                val request = object : JsonObjectRequest(
                    Method.POST,
                    API_URL + "reset",
                    JSONObject(jsonBody),
                    Response.Listener { response ->
                        getAllMessages()
                        util.notify("Chats has been cleared.",this)
                    },
                    Response.ErrorListener { error ->
                        Log.d(TAG,error.toString())
                        when(error){
                            is AuthFailureError -> {
                                util.notify("Session expired, Please login.", this)
                                editor.apply {
                                    putLong("lastLoginTime",0)
                                    apply()
                                }
                                startActivity(Intent(this,Login::class.java))
                            }
                            is NoConnectionError ->util.notify("Please connect to Internet.",this)
                            is TimeoutError -> util.notify("Try Logging in again.",this)
                            else -> util.notify("Couldn't fetch messages",this)
                        }
                    }
                ) {
                    override fun getHeaders(): Map<String, String> {
                        val requestHeaders = HashMap<String, String>()
                        requestHeaders["X-Branch-Auth-Token"] = sharedPreferences.getString("auth_token", "").toString()
                        return requestHeaders
                    }
                }
                queue.cache.clear()
                queue.add(request)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}