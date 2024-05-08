package app.efficientbytes.booleanbear.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.efficientbytes.booleanbear.databinding.RecyclerViewItemReelTopicViewBinding
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic

class ReelTopicsRecyclerViewAdapter(
    private var itemList: List<RemoteReelTopic>,
    private val context: Context,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ReelTopicsRecyclerViewAdapter.ViewHolder>() {

    fun setReelTopicList(itemList: List<RemoteReelTopic>) {
        this.itemList = emptyList()
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            RecyclerViewItemReelTopicViewBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount() = itemList.size

    inner class ViewHolder(private val binding: RecyclerViewItemReelTopicViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RemoteReelTopic) {
            if (item.displayIndex == -1) {
                binding.topic = null
                binding.shimmerParentLayout.startShimmer()
            } else {
                binding.shimmerParentLayout.stopShimmer()
                binding.topic = item
                binding.onClick = View.OnClickListener {
                    itemClickListener.onReelTopicItemClicked(item)
                }
            }
        }
    }

    interface OnItemClickListener {

        fun onReelTopicItemClicked(remoteReelTopic: RemoteReelTopic)

    }
}