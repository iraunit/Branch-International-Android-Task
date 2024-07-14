package com.shyptsolution.branchinternational.Entity

data class Message(
    val id:Int,
    val threadId:Int,
    val userId:String,
    val body: String,
    val timestamp:String,
    val agentId:String
)