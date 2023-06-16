package org.mozilla.reference.browser.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mozilla.gecko.util.ThreadUtils
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.databinding.FragmentNewsletterListingBinding
import org.mozilla.reference.browser.ext.createProgressDialog
import org.mozilla.reference.browser.ext.showAlertDialog
import org.mozilla.reference.browser.ext.showEditTextDialogForFileName
import org.mozilla.reference.browser.ext.showToast
import org.mozilla.reference.browser.ext.wrapForTxt
import org.mozilla.reference.browser.testdata.testNewsletters
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class NewsletterListingFragment : Fragment(), NewsletterAdapter.NewsLetterClickListener {

    private var _binding: FragmentNewsletterListingBinding? = null
    private val binding get() = _binding!!

    // Use a background thread to check the progress of downloading
    private var executor: ExecutorService? = null

    var file: File? = null

    private var downloadProgressDialog: AlertDialog? = null


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

        // Show prompt for the user to enter their preferred file name

        requireContext().showEditTextDialogForFileName(
            title = getString(R.string.enter_file_name),
            defaultText = getString(R.string.txt_extension, newsletter.title)
        ) { fileName ->
            when {
                fileName.isEmpty() -> requireContext().showToast(getString(R.string.empty_file_name))
                !fileName.endsWith(getString(R.string.dot_txt)) -> requireContext().showToast(getString(R.string.file_name_no_txt))
                else -> confirmDownload(fileName, newsletter)
            }
        }
    }

    private fun confirmDownload(fileName: String, newsletter: NewsletterAdapter.Newsletter) {

        // Display confirmation dialog for download

        requireContext().showAlertDialog(
            title = getString(R.string.confirm),
            message = getString(R.string.proceed_to_download, fileName),
            callback = {
                if (newsletter.url.isNullOrEmpty()) {
                    downloadLocalFile(fileName, newsletter)
                } else {
                    initRemoteDownload(fileName, newsletter)
                }
            }
        )
    }

    private fun downloadLocalFile(fileName: String, newsletter: NewsletterAdapter.Newsletter) {
        try {
            // Create a file in the Downloads directory
            file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

            // Write the content to the file
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(newsletter.content.wrapForTxt().toByteArray(Charsets.UTF_8))
            fileOutputStream.close()

            // Create an intent to navigate to the Downloads folder
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, fileName)
            }

            // Launch the intent
            localFileDownloadLauncher.launch(intent)

        } catch (e: Exception) {
            requireContext().showToast(e.message.toString())
            Log.d(TAG, e.message.toString())
        }
    }

    private fun initRemoteDownload(fileName: String, newsletter: NewsletterAdapter.Newsletter) {

        // show progress dialog
        downloadProgressDialog = requireContext().createProgressDialog(null, message = "Downloading...")
        downloadProgressDialog?.show()

        val request = DownloadManager.Request(Uri.parse(newsletter.url))
        request.setTitle(fileName)
        request.setDescription("Downloading...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(requireContext(), Environment.DIRECTORY_DOWNLOADS, fileName)

        executeNetworkRequest(request)
    }

    @SuppressLint("Range")
    private fun executeNetworkRequest(request: DownloadManager.Request) {

        val manager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)

        // Run a task in a background thread to check download progress
        executor = Executors.newFixedThreadPool(1)

        executor?.execute {
            var isDownloadFinished = false
            while (!isDownloadFinished) {
                val cursor: Cursor = manager.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            isDownloadFinished = true
                            releaseResources()
                            nudgeToLaunchDownloads()
                        }

                        DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {}
                        DownloadManager.STATUS_FAILED -> {
                            isDownloadFinished = true
                            downloadProgressDialog?.dismiss()
                        }
                    }
                }
            }
        }
    }


    private var localFileDownloadLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            result.data?.data?.let { destUri ->

                // Write to the file
                val inputStream = FileInputStream(file)
                val outputStream = activity?.contentResolver?.openOutputStream(destUri)

                inputStream.use { input ->
                    outputStream?.use { output ->
                        input.copyTo(output)
                    }
                }

                nudgeToLaunchDownloads()
            }
        }
    }

    private fun nudgeToLaunchDownloads() {
        ThreadUtils.runOnUiThread {
            // Nudge user with an option to open the Downloads folder
            requireContext().showAlertDialog(
                title = getString(R.string.download_successful),
                message = getString(R.string.open_downloads_folder),
                callback = {
                    startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                }
            )
        }
    }

    private fun releaseResources() {
        executor?.shutdown()
        downloadProgressDialog?.cancel()
    }

    override fun onStop() {
        super.onStop()
        releaseResources()
    }

    companion object {
        const val TAG = "NewsletterFragment"
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}