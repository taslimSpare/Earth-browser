package org.mozilla.reference.browser.settings

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.databinding.FragmentNewsletterListingBinding
import org.mozilla.reference.browser.ext.showEditTextDialog
import java.io.File

class NewsletterListingFragment : Fragment(), NewsletterAdapter.NewsLetterClickListener {

    private var _binding: FragmentNewsletterListingBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentNewsletterListingBinding.inflate(inflater, container, false)
            .also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).title = getString(R.string.preferences_newsletters_page)

        val newsletterAdapter = NewsletterAdapter()
        newsletterAdapter.newsLetterClickListener = this

        newsletterAdapter.submitList(listOf(
            NewsletterAdapter.Newsletter(
                id = "1",
                title = "March 2023 edition",
                shortDescription = "Here, we explain the inhumanity behind misogyny and gender-based violence.",
                content = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
            ),
            NewsletterAdapter.Newsletter(
                id = "2",
                title = "April 2023 edition",
                shortDescription = "On this episode, we discuss the importance of education in a child's upbringing.",
                content = "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like)."
            ),
            NewsletterAdapter.Newsletter(
                id = "3",
                title = "May 2023 edition",
                shortDescription = "Special note from our guest: Burna Boy.",
                content = "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section 1.10.32."
            )
        ))

        binding.newsletterRecycler.adapter = newsletterAdapter
    }

    private fun createFile(fileName: String, content: String): File {
        val name = "${fileName}.txt"
        val file = File(Environment.getExternalStorageDirectory(), name)
        file.writeText(content)

        return file
    }

    private fun initiateFileDownload(context: Context, fileUri: Uri, fileName: String, tempFile: File) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(fileUri)
            .setTitle(fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(
                Uri.fromFile(tempFile)
            )

        downloadManager.enqueue(request)
    }

    override fun onNewsLetterClicked(newsletter: NewsletterAdapter.Newsletter) {
        val nameDialog = Dialog(requireContext())
        nameDialog.showEditTextDialog(
            requireContext(),
            newsletter.title
        ) { name ->
            try {
                val file = createFile(name, newsletter.content)
//                Toast.makeText(requireContext(), "File created with name ${file.name}", Toast.LENGTH_SHORT).show()

                // Create temporary file
                val uri = FileProvider.getUriForFile(requireContext(), "org.mozilla.reference.fileprovider", file)

                val cacheDir = requireContext().externalCacheDir ?: requireContext().cacheDir
                val tempFile = File(cacheDir, file.name)
                file.copyTo(tempFile, overwrite = true)

                initiateFileDownload(requireContext(), uri, name, tempFile)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                Log.d("PPPPPP", e.message.toString())
                e.localizedMessage?.let { Log.d("PPPPPP", it.toString()) }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}