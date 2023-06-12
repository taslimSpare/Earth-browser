package org.mozilla.reference.browser.settings

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.databinding.FragmentNewsletterListingBinding
import org.mozilla.reference.browser.ext.showEditTextDialog
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

    override fun onNewsLetterClicked(newsletter: NewsletterAdapter.Newsletter) {
        val nameDialog = Dialog(requireContext())
        nameDialog.showEditTextDialog(
            requireContext(),
            newsletter.title
        ) { name ->
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Proceed to download ${name}.txt?")
                .setPositiveButton("Yes") { _, _ ->
                    try {
                        file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "${name}.txt")
                        val fileOutputStream = FileOutputStream(file)
                        fileOutputStream.write(newsletter.content.toByteArray(Charsets.UTF_8))
                        fileOutputStream.close()

                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TITLE, "${name}.txt")
                        }

                        startActivityForResult(intent, 10001)

                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                        Log.d("PPPPPP", e.message.toString())
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->

                }
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10001 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { destUri ->

                Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT).show()
                val inputStream = FileInputStream(file)
                val outputStream = requireActivity().contentResolver?.openOutputStream(destUri)

                inputStream.use { input ->
                    outputStream?.use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}