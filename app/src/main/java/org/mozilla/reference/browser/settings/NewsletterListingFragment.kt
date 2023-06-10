package org.mozilla.reference.browser.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.databinding.FragmentNewsletterListingBinding

class NewsletterListingFragment : Fragment() {

    private var _binding: FragmentNewsletterListingBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentNewsletterListingBinding.inflate(inflater, container, false)
            .also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).title = getString(R.string.preferences_newsletters_page)

        val newsletterAdapter = NewsletterAdapter()
        newsletterAdapter.submitList(listOf(
            NewsletterAdapter.Newsletter(
                id = "1",
                title = "March 2023 edition",
                shortDescription = "Here, we explain the inhumanity behind misogyny and gender-based violence."
            ),
            NewsletterAdapter.Newsletter(
                id = "2",
                title = "April 2023 edition",
                shortDescription = "On this episode, we discuss the importance of education in a child's upbringing."
            ),
            NewsletterAdapter.Newsletter(
                id = "3",
                title = "May 2023 edition",
                shortDescription = "Special note from our guest: Burna Boy."
            )
        ))

        binding.newsletterRecycler.adapter = newsletterAdapter
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}