package com.example.bookexchange

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class BookAdapter(
    private val books: List<Book>,
    private val isFavoritesMode: Boolean = false // Нова променлива
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvBookTitle)
        val tvAuthor: TextView = view.findViewById(R.id.tvBookAuthor)
        val tvCondition: TextView = view.findViewById(R.id.tvBookCondition)
        val ivImage: ImageView = view.findViewById(R.id.ivBookImage)
        val btnDelete: ImageButton? = view.findViewById(R.id.btnDelete) // Можно е да биде null ако го нема во некој layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.tvTitle.text = book.title
        holder.tvAuthor.text = book.author
        holder.tvCondition.text = "Состојба: ${book.condition}"

        // Логика за копчето за бришење
        if (isFavoritesMode && holder.btnDelete != null) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                // Бришење од Firebase
                FirebaseFirestore.getInstance().collection("books").document(book.id)
                    .update("isFavorite", false)
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Отстрането од омилени", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            holder.btnDelete?.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, BookDetailActivity::class.java)
            intent.putExtra("BOOK_ID", book.id)
            intent.putExtra("BOOK_TITLE", book.title)
            intent.putExtra("BOOK_AUTHOR", book.author)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = books.size
}