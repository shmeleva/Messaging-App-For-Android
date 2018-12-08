package xyz.shmeleva.eight.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_gallery.*

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.activities.FullscreenImageActivity
import xyz.shmeleva.eight.adapters.ImageGroupListAdapter
import xyz.shmeleva.eight.utilities.DoubleClickBlocker
import xyz.shmeleva.eight.models.Message
import xyz.shmeleva.eight.models.User
import xyz.shmeleva.eight.utilities.TimestampFormatter


class GalleryFragment : Fragment() {

    private val TAG = "GalleryFragment"

    private var groupByOptionIndex: Int = 0
    private var groupByOptions: Int = 0

    private var chatId: String? = null
    private var joinedAt: Long = 0

    private var images = arrayListOf<Message>()
    private var imageGroups = arrayListOf<Pair<String, ArrayList<String>>>()

    private lateinit var adapter: ImageGroupListAdapter

    private val doubleClickBlocker: DoubleClickBlocker = DoubleClickBlocker()

    private lateinit var database: DatabaseReference

    enum class Option {
        DATE, FEATURE, SENDER
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            groupByOptions = R.array.gallery_group_by_options
            chatId = arguments!!.getString(ARG_CHAT_ID)
            joinedAt = arguments!!.getLong(ARG_JOINED_AT)
        }

        database = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleryBackButton.setOnClickListener { _ ->
            if (doubleClickBlocker.isSingleClick()) {
                activity?.onBackPressed()
            }
        }

        if (chatId == null || chatId!!.isEmpty() || joinedAt == 0L) {
            return
        }

        database
                .child("chatMessages")
                .child(chatId!!)
                .orderByChild("imageTimestamp")
                .startAt(joinedAt.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            images.clear()
                            for (messageSnapshot in snapshot.children) {
                                val message = messageSnapshot.getValue(Message::class.java)
                                if (message != null) {
                                    message.date = TimestampFormatter.formatDate(message.imageTimestamp)
                                    images.add(message)
                                }
                            }

                            sortImages()
                        }
                    }

                    override fun onCancelled(e: DatabaseError) {
                        Log.e(TAG, "Failed to retrieve chat $chatId's images: ${e.message}")
                    }
                })

        galleryRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        adapter = ImageGroupListAdapter(activity as Context, imageGroups) { imageUrl : String ->
            if (doubleClickBlocker.isSingleClick()) {
                val intent = Intent(activity, FullscreenImageActivity::class.java)
                intent.putExtra("imageUrl", imageUrl)
                startActivity(intent)
            }
        }
        galleryRecyclerView.adapter = adapter

        gallerySortButton.setOnClickListener {
            if (doubleClickBlocker.isSingleClick()) {
                AlertDialog.Builder(context)
                        .setSingleChoiceItems(R.array.gallery_group_by_options, groupByOptionIndex) {dialog, i ->
                            if (groupByOptionIndex != i) {
                                groupByOptionIndex = i
                                sortImages()
                            }
                            dialog.dismiss()
                        }
                        .show()
            }
        }
    }

    private fun fillSenderNames() {
        val oldGroups = ArrayList(imageGroups)
        imageGroups.clear()

        for (group in oldGroups) {
            database.child("users").child(group.first).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(TAG, "users/${group.first} onDataChange!")

                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        if (user != null) {
                            imageGroups.add(Pair(user.username, group.second))
                            imageGroups.sortWith(compareBy { it.first })
                            adapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onCancelled(e: DatabaseError) {
                    Log.e(TAG, "Failed to retrieve user ${group.first}: ${e.message}")
                }
            })
        }
    }

    private fun groupImages() {
        imageGroups.clear()

        for (image in images) {
            var groupLabel = ""
            when (groupByOptionIndex) {
                Option.DATE.ordinal -> {
                    groupLabel = image.date
                }

                Option.FEATURE.ordinal -> {
                    groupLabel = image.imageFeature
                }

                Option.SENDER.ordinal -> {
                    groupLabel = image.senderId
                }
            }

            val group = imageGroups.find { it.first == groupLabel }
            if (group == null) {
                imageGroups.add(Pair(groupLabel, arrayListOf(image.imageUrl)))
            } else {
                group.second.add(image.imageUrl)
            }
        }

        if (groupByOptionIndex == Option.SENDER.ordinal) {
            fillSenderNames()
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    private fun sortImages() {
        when (groupByOptionIndex) {
            Option.DATE.ordinal -> {
                Log.i(TAG, "Sort by date")
                images.sortWith(compareByDescending<Message> { it.imageTimestamp }
                        .thenByDescending { it.id })
            }

            Option.FEATURE.ordinal -> {
                Log.i(TAG, "Sort by feature")
                images.sortWith(compareBy<Message> { it.imageFeature }
                        .thenByDescending { it.id })
            }

            Option.SENDER.ordinal -> {
                Log.i(TAG, "Sort by sender")
                images.sortWith(compareByDescending { it.id })
            }
        }

        groupImages()
    }

    companion object {
        private val ARG_CHAT_ID = "chatId"
        private val ARG_IS_PRIVATE = "isPrivate"
        private val ARG_JOINED_AT = "joinedAt"

        fun newInstance(chatId: String?, isPrivate: Boolean, joinedAt: Long): GalleryFragment {
            val fragment = GalleryFragment()
            val args = Bundle()
            args.putString(ARG_CHAT_ID, chatId)
            args.putBoolean(ARG_IS_PRIVATE, isPrivate)
            args.putLong(ARG_JOINED_AT, joinedAt)
            fragment.arguments = args
            return fragment
        }
    }
}
