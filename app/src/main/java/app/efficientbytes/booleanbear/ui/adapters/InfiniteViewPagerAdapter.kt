package app.efficientbytes.booleanbear.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.ViewPagerItemCoursesBannerBinding
import app.efficientbytes.booleanbear.ui.models.CoursesBanner

class InfiniteViewPagerAdapter(byteCoursesBannerList: List<CoursesBanner>) :
    RecyclerView.Adapter<InfiniteViewPagerAdapter.ViewHolder>() {

    private val updatedByteCoursesBannerList: List<CoursesBanner> =
        listOf(byteCoursesBannerList.last()) + byteCoursesBannerList + listOf(byteCoursesBannerList.first())

    class ViewHolder(val binding: ViewPagerItemCoursesBannerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ViewPagerItemCoursesBannerBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.view_pager_item_courses_banner,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = updatedByteCoursesBannerList[position]
        holder.binding.banner = data
    }

    override fun getItemCount(): Int {
        return updatedByteCoursesBannerList.size
    }
}