package com.bjh.accountmanagerkt.viewHolder

import androidx.recyclerview.widget.RecyclerView

import android.view.View
import android.widget.TextView

import com.bjh.accountmanagerkt.R

class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var staticsDate: TextView
    var staticsTime: TextView
    var staticsAmount: TextView

    init {

        staticsDate = itemView.findViewById(R.id.staticsDate)
        staticsTime = itemView.findViewById(R.id.staticsTime)
        staticsAmount = itemView.findViewById(R.id.staticsAmount)
    }
}