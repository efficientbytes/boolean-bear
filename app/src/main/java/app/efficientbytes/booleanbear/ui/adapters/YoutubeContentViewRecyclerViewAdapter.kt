package app.efficientbytes.booleanbear.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.efficientbytes.booleanbear.databinding.RecyclerViewItemYoutubeContentViewBinding
import app.efficientbytes.booleanbear.services.models.RemoteReel

class YoutubeContentViewRecyclerViewAdapter(
    private var itemList: List<RemoteReel>,
    private var context: Context,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<YoutubeContentViewRecyclerViewAdapter.ItemViewHolder>() {

    fun setYoutubeContentViewList(itemList: List<RemoteReel>) {
        this.itemList = emptyList()
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            RecyclerViewItemYoutubeContentViewBinding.inflate(layoutInflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ItemViewHolder(private val binding: RecyclerViewItemYoutubeContentViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RemoteReel) {
            if (item.reelId.isEmpty() && item.runTime == -1L && item.createdOn == -1L) {
                binding.reel = null
                binding.shimmerLayout.startShimmer()
            } else {
                binding.shimmerLayout.stopShimmer()
                binding.reel = item
                binding.onClick = View.OnClickListener {
                    itemClickListener.onYoutubeContentViewItemClicked(absoluteAdapterPosition, item)
                }
            }
        }
    }

    interface OnItemClickListener {

        fun onYoutubeContentViewItemClicked(position: Int, remoteReel: RemoteReel)

    }

}