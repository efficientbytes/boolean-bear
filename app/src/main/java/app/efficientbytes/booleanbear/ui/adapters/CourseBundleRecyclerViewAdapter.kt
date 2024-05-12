package app.efficientbytes.booleanbear.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.efficientbytes.booleanbear.databinding.RecyclerViewItemDiscoverCourseTopicsViewBinding
import app.efficientbytes.booleanbear.services.models.RemoteCourseBundle

class CourseBundleRecyclerViewAdapter(
    private var itemList: List<RemoteCourseBundle>,
    private val context: Context,
) : RecyclerView.Adapter<CourseBundleRecyclerViewAdapter.ViewHolder>() {

    fun setCourseTopicList(itemList: List<RemoteCourseBundle>) {
        this.itemList = emptyList()
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            RecyclerViewItemDiscoverCourseTopicsViewBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount() = itemList.size

    inner class ViewHolder(private val binding: RecyclerViewItemDiscoverCourseTopicsViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RemoteCourseBundle) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerView.adapter = CourseRecyclerViewAdapter(item.courseList, context)
            if (item.topicDetails.displayIndex == -1) {
                binding.topic = null
                binding.topicValueTextView.visibility = View.GONE
            } else {
                binding.topicValueTextView.visibility = View.VISIBLE
                binding.topic = item.topicDetails
            }
        }
    }

}