package app.efficientbytes.booleanbear.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import app.efficientbytes.booleanbear.databinding.FragmentBrandDisplayBinding
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable

class BrandDisplayFragment : Fragment(), AnimationListener {

    private lateinit var _binding: FragmentBrandDisplayBinding
    private val binding get() = _binding
    private lateinit var rootView: View
    private var mode: Int = 0
    private lateinit var gifDrawable: GifDrawable
    private val safeArgs: BrandDisplayFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrandDisplayBinding.inflate(inflater, container, false)
        rootView = binding.root
        binding.lifecycleOwner = viewLifecycleOwner
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.mode = safeArgs.mode

        gifDrawable = binding.booleanBearLoadingGif.drawable as GifDrawable
        gifDrawable.addAnimationListener(this@BrandDisplayFragment)

        when (mode) {
            0 -> { //only logo
                changeVisibility(onlyLogo = View.VISIBLE)
            }

            1 -> { //only name
                changeVisibility(onlyName = View.VISIBLE)
            }

            2 -> { //logo and name
                changeVisibility(logoNName = View.VISIBLE)
            }

            3 -> { //logo animation
                changeVisibility(logoAnimation = View.VISIBLE)
                gifDrawable.start()
            }
        }

    }

    private fun changeVisibility(
        onlyLogo: Int = View.GONE,
        onlyName: Int = View.GONE,
        logoNName: Int = View.GONE,
        logoAnimation: Int = View.GONE
    ) {
        binding.onlyLogoConstraintLayout.visibility = onlyLogo
        binding.onlyNameConstraintLayout.visibility = onlyName
        binding.logoNNameConstraintLayout.visibility = logoNName
        binding.logoGifConstraintLayout.visibility = logoAnimation
    }

    override fun onAnimationCompleted(loopNumber: Int) {
        gifDrawable.reset()
    }

    override fun onPause() {
        super.onPause()
        gifDrawable.stop()
    }

    override fun onStop() {
        super.onStop()
        gifDrawable.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        gifDrawable.stop()
    }
}