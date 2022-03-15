package com.bjh.accountmanagerkt.viewHolder

import androidx.recyclerview.widget.RecyclerView

import android.view.View
import android.widget.TextView

import com.bjh.accountmanagerkt.R

class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var staticsDate: TextView = itemView.findViewById(R.id.staticsDate)
    var staticsTime: TextView = itemView.findViewById(R.id.staticsTime)
    var staticsAmount: TextView = itemView.findViewById(R.id.staticsAmount)

}