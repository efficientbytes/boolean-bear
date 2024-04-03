package app.efficientbytes.booleanbear.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.database.models.ShuffledCategory
import app.efficientbytes.booleanbear.databinding.CellChipCategoriesBinding
import app.efficientbytes.booleanbear.databinding.CellChipCategoriesFooterBinding

class HomeFragmentChipRecyclerViewAdapter(
    private var itemList: List<ShuffledCategory>,
    private var context: Context,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var checkedPosition = 0
    fun setContentCategories(itemList: List<ShuffledCategory>) {
        this.itemList = emptyList()
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ViewBinding
        if (viewType == TYPE_ITEM) {
            binding = CellChipCategoriesBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(binding)
        } else if (viewType == TYPE_FOOTER) {
            binding = CellChipCategoriesFooterBinding.inflate(layoutInflater, parent, false)
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

    inner class ItemViewHolder(private val binding: CellChipCategoriesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShuffledCategory) {
            if (checkedPosition != -1) {
                if (checkedPosition == bindingAdapterPosition) {
                    binding.chipCardView.strokeWidth = 0
                    binding.chipCardView.setCardBackgroundColor(context.getColor(R.color.cell_chip_playlist_checked_color))
                    binding.titleValueTextView.setTextColor(context.getColor(R.color.white))
                } else {
                    binding.chipCardView.strokeWidth = 2
                    binding.chipCardView.strokeColor =
                        context.getColor(R.color.md_theme_surfaceVariant)
                    binding.chipCardView.setCardBackgroundColor(context.getColor(R.color.cell_chip_playlist_color))
                    binding.titleValueTextView.setTextColor(context.getColor(R.color.md_theme_onSurface))
                }
                binding.titleValueTextView.text = item.title
                binding.chipCardView.setOnClickListener {
                    itemClickListener.onChipItemClick(bindingAdapterPosition, item)
                    binding.chipCardView.strokeWidth = 0
                    binding.chipCardView.setCardBackgroundColor(context.getColor(R.color.cell_chip_playlist_checked_color))
                    binding.titleValueTextView.setTextColor(context.getColor(R.color.white))
                    if (checkedPosition != bindingAdapterPosition) {
                        notifyItemChanged(checkedPosition)
                        checkedPosition = bindingAdapterPosition
                    }
                }
            }
        }
    }

    inner class FooterViewHolder(private val binding: CellChipCategoriesFooterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.chipCardView.setOnClickListener {
                itemClickListener.onChipLastItemClicked()
            }
        }

    }

    interface OnItemClickListener {

        fun onChipItemClick(position: Int, shuffledCategory: ShuffledCategory)
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