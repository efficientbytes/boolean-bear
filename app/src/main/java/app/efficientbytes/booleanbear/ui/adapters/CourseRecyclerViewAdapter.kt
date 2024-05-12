package app.efficientbytes.booleanbear.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.efficientbytes.booleanbear.databinding.RecyclerViewItemDiscoverCoursesViewBinding
import app.efficientbytes.booleanbear.services.models.RemoteCourse

class CourseRecyclerViewAdapter(
    private var itemList: List<RemoteCourse>,
    private val context: Context,
) : RecyclerView.Adapter<CourseRecyclerViewAdapter.ViewHolder>() {

    fun setCourseTopicList(itemList: List<RemoteCourse>) {
        this.itemList = emptyList()
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            RecyclerViewItemDiscoverCoursesViewBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount() = itemList.size

    inner class ViewHolder(private val binding: RecyclerViewItemDiscoverCoursesViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RemoteCourse) {
            if (item.courseId.isEmpty() && item.createdOn == -1L) {
                binding.course = null
                binding.shimmerLayout.startShimmer()
            } else {
                binding.shimmerLayout.stopShimmer()
                binding.course = item
            }
        }
    }

}