package com.sinhro.songturn.app.ui.objects

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sinhro.songturn.app.R
import com.sinhro.songturn.rest.model.PublicUserInfo

class AppUsersRecyclerViewAdapter(
    context: Context,
    private val users: MutableList<PublicUserInfo>
) : RecyclerView.Adapter<AppUsersRecyclerViewAdapter.CustomViewHolder>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

//    private lateinit var parentRecyclerView: RecyclerView

//    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
//        super.onAttachedToRecyclerView(recyclerView)
//        parentRecyclerView = recyclerView
//    }

    override fun getItemId(position: Int): Long {
        return users[position].id.toLong()
    }


    fun clear() {
        users.clear()
        notifyDataSetChanged()
    }

    fun setUsers(si: List<PublicUserInfo>) {
        users.clear()
        users.addAll(si)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val v = layoutInflater.inflate(
            R.layout.fragment_room_info_users_user_view_holder,
            parent,
            false
        )
        return CustomViewHolder(v)
    }

    override fun onBindViewHolder(holderCustom: CustomViewHolder, position: Int) {
        holderCustom.userTextView.text = users[position].nickname
    }

    override fun getItemCount(): Int {
        return users.count()
    }

    class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userTextView: TextView =
            view.findViewById(R.id.fragment_room_info_users_user_view_holder_textView)
    }
}