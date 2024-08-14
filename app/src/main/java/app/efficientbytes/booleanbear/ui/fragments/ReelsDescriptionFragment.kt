package app.efficientbytes.booleanbear.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import app.efficientbytes.booleanbear.R
import app.efficientbytes.booleanbear.databinding.FragmentReelsDescriptionBinding
import app.efficientbytes.booleanbear.repositories.models.DataStatus
import app.efficientbytes.booleanbear.services.models.RemoteMentionedLink
import app.efficientbytes.booleanbear.ui.adapters.MentionedLinkRecyclerViewAdapter
import app.efficientbytes.booleanbear.ui.bindingadapters.loadImageFromUrl
import app.efficientbytes.booleanbear.utils.ContentDetailsLiveListener
import app.efficientbytes.booleanbear.utils.InstructorLiveListener
import app.efficientbytes.booleanbear.utils.MentionedLinksLiveListener
import app.efficientbytes.booleanbear.utils.showUnauthorizedDeviceDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import org.koin.android.ext.android.inject

class ReelsDescriptionFragment : BottomSheetDialogFragment(),
    MentionedLinkRecyclerViewAdapter.OnItemClickListener {

    private lateinit var _binding: FragmentReelsDescriptionBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private val instructorLiveListener: InstructorLiveListener by inject()
    private val mentionedLinksLiveListener: MentionedLinksLiveListener by inject()
    private val contentDetailsLiveListener: ContentDetailsLiveListener by inject()
    private lateinit var mentionedLinkRecyclerViewAdapter: MentionedLinkRecyclerViewAdapter

    companion object {

        var isOpened: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReelsDescriptionBinding.inflate(inflater, container, false)
        rootView = binding.root
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentDetailsLiveListener.liveData.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Loading -> {
                    shimmerContentDetails()
                }

                DataStatus.Status.Success -> {
                    val playDetails = it.data
                    playDetails?.let { details ->
                        displayContentDetails()
                        binding.contentTitleValueTextView.text = details.title
                        binding.contentDescriptionValueTextView.text = details.description
                        val hashTags = details.hashTags
                        if (hashTags != null) {
                            binding.hashtagsChipGroup.removeAllViews()
                            for (item in hashTags) {
                                binding.shimmerHashtags.stopShimmer()
                                binding.shimmerHashtags.visibility = View.GONE
                                binding.hashtagsChipGroup.visibility = View.VISIBLE
                                val chip = Chip(requireContext())
                                chip.textSize = 10F
                                chip.text = getString(R.string.reel_hashtags, item)
                                chip.chipStrokeWidth = 0F
                                chip.elevation = 0F
                                chip.isCheckable = false
                                chip.chipBackgroundColor =
                                    requireContext().getColorStateList(R.color.black_600)
                                binding.hashtagsChipGroup.addView(chip)
                            }
                        } else {
                            binding.shimmerHashtags.stopShimmer()
                            binding.shimmerHashtags.visibility = View.GONE
                            binding.hashTagsLabelTextView.visibility = View.GONE
                            binding.hashtagsChipGroup.visibility = View.GONE
                        }
                    }
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {

                }
            }
        }
        instructorLiveListener.liveData.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Loading -> {
                    shimmerInstructorCard()
                }

                DataStatus.Status.Success -> {
                    displayInstructorCard()
                    val profile = it.data
                    profile?.let { remoteInstructorProfile ->
                        val fullName = if (remoteInstructorProfile.lastName == null) {
                            remoteInstructorProfile.firstName
                        } else {
                            remoteInstructorProfile.firstName + " " + remoteInstructorProfile.lastName
                        }
                        binding.instructorNameValueTextView.text = fullName
                        binding.instructorProfilePicImage.loadImageFromUrl(remoteInstructorProfile.profileImage)
                    }
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    binding.shimmerInstructor.stopShimmer()
                    binding.shimmerInstructor.visibility = View.GONE
                    binding.instructorCardView.visibility = View.GONE
                }
            }
        }
        binding.mentionedLinksRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        mentionedLinksLiveListener.liveData.observe(viewLifecycleOwner) {
            when (it.status) {
                DataStatus.Status.Loading -> {
                    shimmerMentionedLinks()
                }

                DataStatus.Status.Success -> {
                    displayMentionedLinks()
                    val list = it.data
                    list?.let {
                        mentionedLinkRecyclerViewAdapter =
                            MentionedLinkRecyclerViewAdapter(
                                list,
                                requireContext(),
                                this@ReelsDescriptionFragment
                            )
                    }
                    binding.mentionedLinksRecyclerView.adapter = mentionedLinkRecyclerViewAdapter
                }

                DataStatus.Status.UnAuthorized -> showUnauthorizedDeviceDialog(
                    requireContext(),
                    it.message
                )

                else -> {
                    binding.shimmerMentionedLinks.stopShimmer()
                    binding.shimmerMentionedLinks.visibility = View.GONE
                    binding.mentionedLinksLabelTextView.visibility = View.GONE
                    binding.mentionedLinksRecyclerView.visibility = View.GONE
                }
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
        binding.hashTagsLabelTextView.visibility = View.GONE
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
        binding.hashTagsLabelTextView.visibility = View.VISIBLE
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
        binding.mentionedLinksLabelTextView.visibility = View.GONE
        binding.mentionedLinksRecyclerView.visibility = View.GONE
        binding.shimmerMentionedLinks.visibility = View.VISIBLE
        binding.shimmerMentionedLinks.startShimmer()
    }

    private fun displayMentionedLinks() {
        binding.shimmerMentionedLinks.stopShimmer()
        binding.shimmerMentionedLinks.visibility = View.GONE
        binding.mentionedLinksLabelTextView.visibility = View.VISIBLE
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

    override fun onMentionedExternalLinkClicked(position: Int, mentionedLink: RemoteMentionedLink) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mentionedLink.link))
        startActivity(browserIntent)
    }

}