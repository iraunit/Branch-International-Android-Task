package com.shyptsolution.branchinternational

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.shyptsolution.branchinternational.Entity.Message
import com.shyptsolution.branchinternational.MainActivity.GlobalObject.allMessageInOrder
import com.shyptsolution.branchinternational.MainActivity.GlobalObject.util
import com.shyptsolution.branchinternational.RecyclerViews.ChatAdapter
import org.json.JSONObject

class Chat : AppCompatActivity() {
    private val TAG = "CHAT"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var typedMessage : EditText
    private lateinit var messageThread : MutableList<Message>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var progressBar : ProgressBar
    private var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        sharedPreferences = getSharedPreferences("branch-international", AppCompatActivity.MODE_PRIVATE)
        editor =  sharedPreferences.edit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val intent = intent
        index = intent.getIntExtra("index",0)
        messageThread = allMessageInOrder[index].reversed().toMutableList()
        supportActionBar?.title = "User ID: ${messageThread[0].userId}"
        progressBar = findViewById(R.id.sendMessageProgressBar)
        recyclerView = findViewById(R.id.chat_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter(this)
        adapter.submitList(messageThread)
        recyclerView.itemAnimator = null
        recyclerView.adapter = adapter
        scrollToBottom()
        Log.d(TAG, allMessageInOrder[index].toString())
        typedMessage = findViewById(R.id.typed_message_edit_text)
        val newMsgButton = findViewById<ImageView>(R.id.send_new_message)
        newMsgButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            sendNewMessage(messageThread[0].threadId,typedMessage.text.toString()) }
        recyclerView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                view: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                val keyboardHeight = getKeyboardHeight()
                if (keyboardHeight > 0) {
                    scrollToBottom()
                }
            }
        })
    }

    private fun sendNewMessage(threadId:Int,body:String){
        if(body == "" || body.isEmpty())return;
        val queue = Volley.newRequestQueue(this)
        val jsonBody = """
        {
        "thread_id": $threadId,
        "body": "$body"
        }
        """.trimIndent()

        val request = object : JsonObjectRequest(
            Method.POST,
            MainActivity.GlobalObject.API_URL + "messages",
            JSONObject(jsonBody),
            Response.Listener { response ->
                Log.d(TAG, response.toString())
                progressBar.visibility = View.GONE
                val msg = Message(response.getInt("id"),response.getInt("thread_id"),response.getString("user_id"),response.getString("body"),response.getString("timestamp"),response.getString("agent_id"))
                messageThread.add(msg)
                adapter.submitList(messageThread)
                scrollToBottom()
                typedMessage.setText("")
            },
            Response.ErrorListener { error ->
                Log.d(TAG,error.toString())
                progressBar.visibility = View.GONE
                when(error){
                    is AuthFailureError -> {
                        MainActivity.GlobalObject.util.notify("Session expired, Please login.", this)
                        editor.apply {
                            putLong("lastLoginTime",0)
                            apply()
                        }
                        startActivity(Intent(this,Login::class.java))
                    }
                    is NoConnectionError -> MainActivity.GlobalObject.util.notify("Please connect to Internet.",this)
                    is TimeoutError -> MainActivity.GlobalObject.util.notify("Try Logging in again.",this)
                    else -> MainActivity.GlobalObject.util.notify("Couldn't fetch messages",this)
                }
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val requestHeaders = HashMap<String, String>()
                requestHeaders["X-Branch-Auth-Token"] = sharedPreferences.getString("auth_token", "").toString()
                return requestHeaders
            }
        }
            queue.add(request)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        startActivity(Intent(this,MainActivity::class.java))
        finish()
        super.onBackPressed()
    }

    fun getKeyboardHeight(): Int {
        val rootView = findViewById<ConstraintLayout>(R.id.chat_layout)
        val visibleHeight = rootView.height
        val insets = rootView.rootWindowInsets
        val keyboardHeight = insets.systemWindowInsetBottom
        return visibleHeight - keyboardHeight
    }

    fun scrollToBottom() {
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this,MainActivity::class.java))
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}