package online.dailyq.ui.details

import android.content.Intent
import android.provider.ContactsContract.Profile
import android.text.format.DateUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import online.dailyq.R
import online.dailyq.api.response.Answer
import online.dailyq.databinding.ItemAnswerBinding
import online.dailyq.ui.image.ImageViewerActivity
import online.dailyq.ui.profile.ProfileActivity

class AnswerViewHolder(val binding: ItemAnswerBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(answer: Answer) {
        binding.userName.text = answer.answerer?.name
        if (!answer.answerer?.photo.isNullOrBlank()) {
            binding.userPhoto.load(answer.answerer?.photo) {
                placeholder(R.drawable.ph_user)
                error(R.drawable.ph_user)
                transformations(CircleCropTransformation())
            }
        }

        binding.userPhoto.setOnClickListener {
            val context = itemView.context
            context.startActivity(
                Intent(context, ProfileActivity::class.java).apply {
                    putExtra(ProfileActivity.EXTRA_UID, answer.answerer?.id)
                })
        }

        binding.textAnswer.text = answer.text
        binding.textAnswer.isVisible = !answer.text.isNullOrEmpty()
        binding.photoAnswer.load(answer.photo) {
            placeholder(R.drawable.ph_image)
            error(R.drawable.ph_image)
        }
        binding.photoAnswer.isVisible = !answer.photo.isNullOrEmpty()
        binding.photoAnswer.setOnClickListener {
            val context = itemView.context
            context.startActivity(Intent(context, ImageViewerActivity::class.java).apply {
                putExtra(ImageViewerActivity.EXTRA_URL, answer.photo)
            })
        }

        binding.elapsedTime.text = DateUtils.getRelativeTimeSpanString(answer.createdAt.time)
    }
}