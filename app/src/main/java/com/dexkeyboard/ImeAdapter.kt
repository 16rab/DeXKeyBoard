package com.dexkeyboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodInfo
import androidx.recyclerview.widget.RecyclerView
import com.dexkeyboard.databinding.ItemImeBinding

class ImeAdapter(
    private val context: Context,
    private var imeList: List<InputMethodInfo>,
    private var currentImeId: String,
    private val onSetDefaultClick: (String) -> Unit
) : RecyclerView.Adapter<ImeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemImeBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateData(newList: List<InputMethodInfo>, newCurrentId: String) {
        imeList = newList
        currentImeId = newCurrentId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ime = imeList[position]
        holder.binding.tvName.text = ime.loadLabel(context.packageManager)
        holder.binding.tvPackage.text = ime.packageName
        holder.binding.ivIcon.setImageDrawable(ime.loadIcon(context.packageManager))

        if (ime.id == currentImeId) {
            holder.binding.btnSetDefault.text = context.getString(R.string.current)
            holder.binding.btnSetDefault.isEnabled = false
        } else {
            holder.binding.btnSetDefault.text = context.getString(R.string.set_default)
            holder.binding.btnSetDefault.isEnabled = true
            holder.binding.btnSetDefault.setOnClickListener {
                onSetDefaultClick(ime.id)
            }
        }
    }

    override fun getItemCount() = imeList.size
}
