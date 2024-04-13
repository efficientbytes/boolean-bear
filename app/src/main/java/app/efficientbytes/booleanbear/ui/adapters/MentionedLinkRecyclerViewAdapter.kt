package app.efficientbytes.booleanbear.ui.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import app.efficientbytes.booleanbear.databinding.RecyclerViewItemMentionedLinkViewBinding
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLink

class MentionedLinkRecyclerViewAdapter(
    private var itemList: List<RemoteMentionedLink>,
    private var context: Context,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<MentionedLinkRecyclerViewAdapter.ItemViewHolder>() {

    fun setMentionedLinkList(itemList: List<RemoteMentionedLink>) {
        this.itemList = emptyList()
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            RecyclerViewItemMentionedLinkViewBinding.inflate(layoutInflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ItemViewHolder(private val binding: RecyclerViewItemMentionedLinkViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RemoteMentionedLink) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            binding.mentionedLink = item
            binding.onClick = View.OnClickListener {
                itemClickListener.onMentionedExternalLinkClicked(absoluteAdapterPosition, item)
            }
            binding.copyToClipboardImageButton.setOnClickListener {
                val clip: ClipData = ClipData.newPlainText(item.name, item.link)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied Link", Toast.LENGTH_SHORT).show()
            }
            binding.externalLinkImageButton.setOnClickListener {
                itemClickListener.onMentionedExternalLinkClicked(absoluteAdapterPosition, item)
            }
        }

    }

    interface OnItemClickListener {

        fun onMentionedExternalLinkClicked(position: Int, mentionedLink: RemoteMentionedLink)

    }

}