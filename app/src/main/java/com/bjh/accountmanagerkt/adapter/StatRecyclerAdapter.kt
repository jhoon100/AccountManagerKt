package com.bjh.accountmanagerkt.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.bjh.accountmanagerkt.viewHolder.RecyclerViewHolder
import com.bjh.accountmanagerkt.R

import java.util.ArrayList
import java.util.HashMap

class StatRecyclerAdapter(private val mData: ArrayList<HashMap<String, String>>): RecyclerView.Adapter<RecyclerViewHolder>(){

    override fun getItemCount() : Int {
        return mData.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val context = parent.context

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.activity_statlist, parent, false)

        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val hashMap = mData[position]
        holder.staticsDate.text = hashMap["staticsDate"]
        holder.staticsTime.text = hashMap["staticsTime"]
        holder.staticsAmount.text = hashMap["staticsAmount"]
    }
}