package com.example.bookexchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent

class NotificationAdapter(
    private val trades: List<TradeOffer>,
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvOfferBookName)
        val tvDetails: TextView = view.findViewById(R.id.tvOfferedBookData)
        val ivBook: ImageView = view.findViewById(R.id.ivNotificationBookImage)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val offer = trades[position]
        val db = FirebaseFirestore.getInstance()

        // ✅ Прикажи различен текст според статусот
        db.collection("books").document(offer.bookWantedId)
            .get()
            .addOnSuccessListener { doc ->
                val title = doc.getString("title") ?: "Непозната книга"
                holder.tvTitle.text = when (offer.status) {
                    "accepted" -> "\u2705 Понудата за $title е прифатена!"
                    "rejected" -> "\u274C Понудата за $title е одбиена."
                    else -> "Понуда за: $title"
                }
            }

        holder.tvDetails.text = "Локација: ${offer.offeredBookData["city"]}"

        // ✅ Сокриј ги копчињата — се pojавуваат само во детален екран
        holder.btnAccept.visibility = View.GONE
        holder.btnReject.visibility = View.GONE

        // ✅ Клик на картичката — отвори детален екран
        holder.itemView.setOnClickListener {
            // Ако е accepted/rejected — нема смисла да се отвора детален екран
            if (offer.status == "accepted" || offer.status == "rejected") return@setOnClickListener

            val intent = Intent(holder.itemView.context, TradeOfferDetailActivity::class.java)
            intent.putExtra("OFFER_ID", offer.id)
            intent.putExtra("BOOK_WANTED_ID", offer.bookWantedId)
            intent.putExtra("SENDER_ID", offer.senderId)
            intent.putExtra("OFFERED_BOOK_NAME", offer.offeredBookData["name"])
            intent.putExtra("OFFERED_BOOK_CITY", offer.offeredBookData["city"])
            intent.putExtra("OFFERED_BOOK_CONDITION", offer.offeredBookData["condition"])
            intent.putExtra("OFFERED_BOOK_IMAGE", offer.offeredBookData["imageUrl"])
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = trades.size
}