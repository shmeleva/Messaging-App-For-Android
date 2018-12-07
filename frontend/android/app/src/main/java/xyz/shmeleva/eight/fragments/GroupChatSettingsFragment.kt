package xyz.shmeleva.eight.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_group_chat_settings.*

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.activities.BaseFragmentActivity
import xyz.shmeleva.eight.adapters.MemberListAdapter
import xyz.shmeleva.eight.adapters.UserListAdapter
import xyz.shmeleva.eight.models.Chat
import xyz.shmeleva.eight.models.User
import xyz.shmeleva.eight.utilities.DoubleClickBlocker

class GroupChatSettingsFragment : Fragment() {

    private val TAG = "GroupChatSettingsFrgmnt"

    private var chatId: String? = null
    private var sourceUserId: String? = null

    private var mListener: OnFragmentInteractionListener? = null
    private val doubleClickBlocker: DoubleClickBlocker = DoubleClickBlocker()

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            chatId = arguments!!.getString(ARG_CHAT_ID)
            sourceUserId = arguments!!.getString(ARG_SOURCE_USER_ID)
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_group_chat_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupChatBackImageView.setOnClickListener {_ ->
            if (doubleClickBlocker.isSingleClick()) {
                activity?.onBackPressed()
            }
        }
        groupChatGalleryRelativeLayout.setOnClickListener { _ ->
            if (doubleClickBlocker.isSingleClick()) {
                //(activity as BaseFragmentActivity).addFragment(GalleryFragment.newInstance(false))
            }
        }

        // populate chat info
        getChatAndPopulate()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private val ARG_CHAT_ID = "chatId"
        private val ARG_SOURCE_USER_ID = "sourceUserId"

        fun newInstance(chatId: String, sourceUserId: String): GroupChatSettingsFragment {
            val fragment = GroupChatSettingsFragment()
            val args = Bundle()
            args.putString(ARG_CHAT_ID, chatId)
            args.putString(ARG_SOURCE_USER_ID, sourceUserId)
            fragment.arguments = args
            return fragment
        }
    }

    private fun getChatAndPopulate() {
        var chatOneTimeListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value == null) {
                    Log.i(TAG, "Chat not found")
                    return
                }

                val chat = dataSnapshot.getValue(Chat::class.java)!!

                var usersOneTimeListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.value == null) {
                            Log.i(TAG, "Users not found")
                            return
                        }

                        val membersNames : ArrayList<String> = ArrayList()

                        chat.members.forEach {
                            val member = dataSnapshot.child(it.key).getValue(User::class.java)!!
                            membersNames.add(member.username)
                        }

                        populateMemberList(membersNames)
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                }

                database.child("users")
                        .addListenerForSingleValueEvent(usersOneTimeListener)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, databaseError.message)
            }
        }

        database.child("chats").child(chatId!!)
                .addListenerForSingleValueEvent(chatOneTimeListener)
    }

    private fun populateMemberList(members : ArrayList<String>) {
        val viewAdapter = MemberListAdapter(members)

        groupChatMemberListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        groupChatMemberListRecyclerView.adapter = viewAdapter
    }
}
