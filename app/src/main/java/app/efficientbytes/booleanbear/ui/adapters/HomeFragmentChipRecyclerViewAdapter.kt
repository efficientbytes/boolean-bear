package app.efficientbytes.booleanbear.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.RecyclerViewItemChipCategoryFooterViewBinding
import app.efficientbytes.booleanbear.databinding.RecyclerViewItemChipCategoryViewBinding
import app.efficientbytes.booleanbear.services.models.RemoteReelTopic

class HomeFragmentChipRecyclerViewAdapter(
    private var itemList: List<RemoteReelTopic>,
    private var context: Context,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var checkedPosition = 0
    fun setReelTopics(itemList: List<RemoteReelTopic>) {
        this.itemList = emptyList()
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ViewBinding
        if (viewType == TYPE_ITEM) {
            binding = RecyclerViewItemChipCategoryViewBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(binding)
        } else if (viewType == TYPE_FOOTER) {
            binding =
                RecyclerViewItemChipCategoryFooterViewBinding.inflate(layoutInflater, parent, false)
            return FooterViewHolder(binding)
        }
        throw RuntimeException("There is no type that matches the type $viewType. Make sure you are using view types correctly!")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_ITEM -> (holder as ItemViewHolder).bind(itemList[position])
            TYPE_FOOTER -> (holder as FooterViewHolder).bind()
        }
    }

    override fun getItemCount(): Int {
        return itemList.size + 1
    }

    inner class ItemViewHolder(private val binding: RecyclerViewItemChipCategoryViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RemoteReelTopic) {
            if (checkedPosition != -1) {
                if (checkedPosition == bindingAdapterPosition) {
                    binding.chipCardView.strokeWidth = 0
                    binding.chipCardView.setCardBackgroundColor(context.getColor(R.color.md_theme_primary))
                    binding.titleValueTextView.setTextColor(context.getColor(R.color.md_theme_onPrimary))
                } else {
                    binding.chipCardView.strokeWidth = 2
                    binding.chipCardView.strokeColor =
                        context.getColor(R.color.md_theme_surfaceVariant)
                    binding.chipCardView.setCardBackgroundColor(context.getColor(R.color.cell_chip_playlist_color))
                    binding.titleValueTextView.setTextColor(context.getColor(R.color.md_theme_onSurface))
                }
                binding.titleValueTextView.text = item.topic
                binding.chipCardView.setOnClickListener {
                    itemClickListener.onChipItemClicked(bindingAdapterPosition, item)
                    binding.chipCardView.strokeWidth = 0
                    binding.chipCardView.setCardBackgroundColor(context.getColor(R.color.md_theme_primary))
                    binding.titleValueTextView.setTextColor(context.getColor(R.color.md_theme_onPrimary))
                    if (checkedPosition != bindingAdapterPosition) {
                        notifyItemChanged(checkedPosition)
                        checkedPosition = bindingAdapterPosition
                    }
                }
            }
        }
    }

    inner class FooterViewHolder(private val binding: RecyclerViewItemChipCategoryFooterViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.chipCardView.setOnClickListener {
                itemClickListener.onChipLastItemClicked()
            }
        }

    }

    interface OnItemClickListener {

        fun onChipItemClicked(position: Int, remoteReelTopic: RemoteReelTopic)
        fun onChipLastItemClicked()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemList.size) {
            TYPE_FOOTER
        } else {
            TYPE_ITEM
        }
    }

    companion object {

        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 0
    }
}