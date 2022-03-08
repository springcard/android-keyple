/*
 * Copyright (c)2022 SpringCard - www.springcard.com.com
 * All right reserved
 * This software is covered by the SpringCard SDK License Agreement - see LICENSE.txt
 */
package com.springcard.keyple.plugin.android.pcsc.example.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.springcard.keyple.plugin.android.pcsc.example.R
import com.springcard.keyple.plugin.android.pcsc.example.model.EventModel
import kotlinx.android.synthetic.main.card_action_event.view.cardActionTextView

class EventAdapter(private val events: ArrayList<EventModel>) :
    RecyclerView.Adapter<EventAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return when (viewType) {
      EventModel.TYPE_ACTION ->
          ViewHolder(
              LayoutInflater.from(parent.context)
                  .inflate(R.layout.card_action_event, parent, false))
      EventModel.TYPE_RESULT ->
          ViewHolder(
              LayoutInflater.from(parent.context)
                  .inflate(R.layout.card_result_event, parent, false))
      else ->
          ViewHolder(
              LayoutInflater.from(parent.context)
                  .inflate(R.layout.card_header_event, parent, false))
    }
  }

  override fun getItemCount(): Int {
    return events.size
  }

  override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
    viewHolder.bind(events[position])
  }

  override fun getItemViewType(position: Int): Int {
    return events[position].type
  }

  open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    open fun bind(event: EventModel) {
      with(itemView) { cardActionTextView.text = event.text }
    }
  }
}
