package app.efficientbytes.booleanbear.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.efficientbytes.booleanbear.databinding.FragmentShuffledContentDescriptionBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.utils.ContentDetailsLiveListener
import app.efficientbytes.booleanbear.utils.InstructorLiveListener
import app.efficientbytes.booleanbear.utils.MentionedLinksLiveListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject

class ShuffledContentDescriptionFragment : BottomSheetDialogFragment() {

    private lateinit var _binding: FragmentShuffledContentDescriptionBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val instructorLiveListener: InstructorLiveListener by inject()
    private val mentionedLinksLiveListener: MentionedLinksLiveListener by inject()
    private val contentDetailsLiveListener: ContentDetailsLiveListener by inject()

    companion object {

        const val SHUFFLED_DESCRIPTION_FRAGMENT: String = "frag-shuffled-description"
        var isOpened: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShuffledContentDescriptionBinding.inflate(inflater, container, false)
        rootView = binding.root
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentDetailsLiveListener.liveData.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.EmptyResult -> {}

                DataStatus.Status.Failed -> {}

                DataStatus.Status.Loading -> {
                    shimmerContentDetails()
                }

                DataStatus.Status.NoInternet -> {}

                DataStatus.Status.Success -> {
                    val playDetails = it.data
                    playDetails?.let { details ->
                        displayContentDetails()
                        binding.contentTitleValueTextView.text = details.title
                        binding.contentDescriptionValueTextView.text = details.description
                        //check for if there are hast tags
                        //if so hide the shimmer
                        //if so populate the chip group
                        //else hide the shimmer and the label - hash tag
                    }
                }

                DataStatus.Status.TimeOut -> {

                }

                DataStatus.Status.UnAuthorized -> {}

                DataStatus.Status.UnKnownException -> {}
            }
        }
    }

    private fun shimmerContentDetails() {
        binding.contentTitleValueTextView.visibility = View.GONE
        binding.shimmerTitle.visibility = View.VISIBLE
        binding.shimmerTitle.startShimmer()
        binding.contentDescriptionValueTextView.visibility = View.GONE
        binding.shimmerDescription.visibility = View.VISIBLE
        binding.shimmerDescription.startShimmer()
        binding.hashtagsChipGroup.visibility = View.GONE
        binding.shimmerHashtags.visibility = View.VISIBLE
        binding.shimmerHashtags.startShimmer()
    }

    private fun displayContentDetails() {
        binding.shimmerTitle.stopShimmer()
        binding.shimmerTitle.visibility = View.GONE
        binding.contentTitleValueTextView.visibility = View.VISIBLE
        binding.shimmerDescription.stopShimmer()
        binding.shimmerDescription.visibility = View.GONE
        binding.contentDescriptionValueTextView.visibility = View.VISIBLE
        binding.shimmerHashtags.stopShimmer()
        binding.shimmerHashtags.visibility = View.GONE
        binding.hashtagsChipGroup.visibility = View.VISIBLE
    }

    private fun shimmerInstructorCard() {
        binding.instructorCardView.visibility = View.GONE
        binding.shimmerInstructor.visibility = View.VISIBLE
        binding.shimmerInstructor.startShimmer()
    }

    private fun displayInstructorCard() {
        binding.shimmerInstructor.stopShimmer()
        binding.shimmerInstructor.visibility = View.GONE
        binding.instructorCardView.visibility = View.VISIBLE
    }

    private fun shimmerMentionedLinks() {
        binding.mentionedLinksRecyclerView.visibility = View.GONE
        binding.shimmerMentionedLinks.visibility = View.VISIBLE
        binding.shimmerMentionedLinks.startShimmer()
    }

    private fun displayMentionedLinks() {
        binding.shimmerMentionedLinks.stopShimmer()
        binding.shimmerMentionedLinks.visibility = View.GONE
        binding.mentionedLinksRecyclerView.visibility = View.VISIBLE
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isOpened = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isOpened = false
    }

    override fun onDetach() {
        super.onDetach()
        isOpened = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isOpened = false
    }

}