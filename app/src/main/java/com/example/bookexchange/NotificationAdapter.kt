package com.example.bookexchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(
    private val offers: List<TradeOffer>,
    private val onAccept: (TradeOffer) -> Unit,
    private val onReject: (TradeOffer) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    // Ова е внатрешната класа за ViewHolder
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvOfferBookName)
        val tvDetails: TextView = view.findViewById(R.id.tvOfferedBookData)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val offer = offers[position]

        // Поставување на текстот
        holder.tvTitle.text = "Понуда за: ${offer.bookWantedId}"
        holder.tvDetails.text = "Испратено од: ${offer.senderId}"

        // Акции на копчињата
        holder.btnAccept.setOnClickListener { onAccept(offer) }
        holder.btnReject.setOnClickListener { onReject(offer) }
    }

    override fun getItemCount() = offers.size
}