package org.mozilla.reference.browser.settings

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.databinding.FragmentNewsletterListingBinding
import org.mozilla.reference.browser.testdata.testNewsletters
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class NewsletterListingFragment : Fragment(), NewsletterAdapter.NewsLetterClickListener {

    private var _binding: FragmentNewsletterListingBinding? = null
    private val binding get() = _binding!!
    var file: File? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentNewsletterListingBinding.inflate(inflater, container, false)
            .also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).title = getString(R.string.preferences_newsletters_page)

        binding.newsletterRefresh.post {
            binding.newsletterRefresh.isRefreshing = true
            fakeRefreshAction()
        }

        // Create adapter object
        val newsletterAdapter = NewsletterAdapter()

        // Populate adapter and set click listener
        newsletterAdapter.submitList(testNewsletters)
        newsletterAdapter.newsLetterClickListener = this

        // Set adapter value
        binding.newsletterRecycler.adapter = newsletterAdapter

        binding.newsletterRefresh.setOnRefreshListener {
            fakeRefreshAction()
        }
    }

    private fun fakeRefreshAction() {
        // Simulate 2-second API call during which the progress views are visible
        CoroutineScope(Dispatchers.Main).launch {
            binding.addonProgressOverlay.root.isVisible = true
            delay(2000)
            binding.addonProgressOverlay.root.isVisible = false
            binding.newsletterRefresh.isRefreshing = false
        }
    }

    override fun onNewsLetterClicked(newsletter: NewsletterAdapter.Newsletter) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm))
            .setMessage("${getString(R.string.proceed_to_download)} ${newsletter.title}${getString(R.string.txt_extension)}?")
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                try {
                    file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "${newsletter.title}${getString(R.string.txt_extension)}")
                    val fileOutputStream = FileOutputStream(file)
                    fileOutputStream.write(newsletter.content.toByteArray(Charsets.UTF_8))
                    fileOutputStream.close()

                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TITLE, "${newsletter.title}${getString(R.string.txt_extension)}")
                    }

                    fileDownloadLauncher.launch(intent)

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, e.message.toString())
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .show()
    }

    private var fileDownloadLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val data: Intent? = result.data

            data?.data?.let { destUri ->

                val inputStream = FileInputStream(file)
                val outputStream = requireActivity().contentResolver?.openOutputStream(destUri)

                inputStream.use { input ->
                    outputStream?.use { output ->
                        input.copyTo(output)
                    }
                }

                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.download_successful))
                    .setMessage(getString(R.string.open_downloads_folder))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                    }
                    .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                        Toast.makeText(requireContext(), getString(R.string.navigate_to_downloads_later), Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }
        }
    }

    companion object {
        const val TAG = "NewsletterFragment"
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}