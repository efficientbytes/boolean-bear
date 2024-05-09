package app.efficientbytes.booleanbear.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.ViewPagerItemHomepageBannerViewBinding
import app.efficientbytes.booleanbear.services.models.RemoteHomePageBanner

class InfiniteViewPagerAdapter(
    private var itemList: List<RemoteHomePageBanner>,
    private val onItemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<InfiniteViewPagerAdapter.ViewHolder>() {

    private var updatedList: List<RemoteHomePageBanner> = if (itemList.isEmpty()) {
        emptyList()
    } else {
        listOf(itemList.last()) + itemList + listOf(itemList.first())
    }

    fun setViewPagerList(itemList: List<RemoteHomePageBanner>) {
        this.itemList = emptyList()
        this.itemList = itemList
        this.updatedList = if (itemList.isEmpty()) {
            emptyList()
        } else {
            listOf(itemList.last()) + itemList + listOf(itemList.first())
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ViewPagerItemHomepageBannerViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RemoteHomePageBanner) {
            if (item.createdOn == -1L && item.startingDate == -1L && item.closingDate == -1L && item.bannerId.isEmpty()) {
                binding.banner = null
                binding.shimmerLayout.startShimmer()
            } else {
                binding.shimmerLayout.stopShimmer()
                binding.banner = item
                binding.onClick = View.OnClickListener {
                    if (item.clickAble) {
                        onItemClickListener.onBannerClicked(
                            absoluteAdapterPosition,
                            item
                        )
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ViewPagerItemHomepageBannerViewBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.view_pager_item_homepage_banner_view,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = updatedList[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int {
        return updatedList.size
    }

    interface OnItemClickListener {

        fun onBannerClicked(position: Int, remoteHomePageBanner: RemoteHomePageBanner)
    }
}