package app.efficientbytes.booleanbear.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class GenericAdapter<T>(
    private val itemList: List<T>,
    @LayoutRes private val layoutResId: Int,
    private val variableId: Int
) : RecyclerView.Adapter<GenericAdapter<T>.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding =
            DataBindingUtil.inflate(layoutInflater, layoutResId, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(item: T) {
            binding.setVariable(variableId, item)
            binding.root.setOnClickListener(this)
            binding.executePendingBindings()
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            listener?.onItemClick(position)
        }
    }

    interface OnItemClickListener {

        fun onItemClick(position: Int)
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

}