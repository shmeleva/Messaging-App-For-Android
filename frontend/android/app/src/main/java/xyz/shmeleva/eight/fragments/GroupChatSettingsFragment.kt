package xyz.shmeleva.eight.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
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
import xyz.shmeleva.eight.activities.LoginActivity
import xyz.shmeleva.eight.activities.SearchActivity
import xyz.shmeleva.eight.adapters.MemberListAdapter
import xyz.shmeleva.eight.adapters.UserListAdapter
import xyz.shmeleva.eight.models.Chat
import xyz.shmeleva.eight.models.User
import xyz.shmeleva.eight.utilities.DoubleClickBlocker
import java.util.*
import kotlin.collections.ArrayList

class GroupChatSettingsFragment : Fragment() {

    private val TAG = "GroupChatSettingsFrgmnt"
    private val ADD_MEMBERS = 0

    private var chatId: String? = null
    private var sourceUserId: String? = null
    private var joinedAt: Long = 0

    private var chat: Chat? = null
    private var memberNames: ArrayList<String> = ArrayList()

    private var mListener: OnFragmentInteractionListener? = null
    private val doubleClickBlocker: DoubleClickBlocker = DoubleClickBlocker()

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            chatId = arguments!!.getString(ARG_CHAT_ID)
            sourceUserId = arguments!!.getString(ARG_SOURCE_USER_ID)
            joinedAt = arguments!!.getLong(ARG_JOINED_AT)
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
                (activity as BaseFragmentActivity).addFragment(GalleryFragment.newInstance(chatId, false, joinedAt))
            }
        }

        groupChatLeaveTextView.setOnClickListener { _ -> leaveGroup() }
/*

        */
        groupChatInviteRelativeLayout.isEnabled = false

        // populate chat info
        memberNames.clear()
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
        private val ARG_JOINED_AT = "joinedAt"

        fun newInstance(chatId: String, sourceUserId: String, joinedAt: Long): GroupChatSettingsFragment {
            val fragment = GroupChatSettingsFragment()
            val args = Bundle()
            args.putString(ARG_CHAT_ID, chatId)
            args.putString(ARG_SOURCE_USER_ID, sourceUserId)
            args.putLong(ARG_JOINED_AT, joinedAt)
            fragment.arguments = args
            return fragment
        }
    }

    private fun leaveGroup() {
        if (doubleClickBlocker.isDoubleClick()) {
            return
        }

        // delete current user from the chat's members
        // TODO: what if they're the last user left?
        database.child("chats").child(chatId!!).child("members").child(sourceUserId!!).removeValue()

        // delete the chat from current user's list of chats
        database.child("users").child(sourceUserId!!).child("chats").child(chatId!!).removeValue()

        activity?.onBackPressed()
    }

    private fun getChatAndPopulate() {
        var chatOneTimeListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value == null) {
                    Log.i(TAG, "Chat not found")
                    return
                }

                chat = dataSnapshot.getValue(Chat::class.java)!!

                var usersOneTimeListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.value == null) {
                            Log.i(TAG, "Users not found")
                            return
                        }

                        //val membersNames : ArrayList<String> = ArrayList()
                        val membersIds : ArrayList<String> = ArrayList(chat!!.members.keys)

                        chat!!.members.forEach {
                            val member = dataSnapshot.child(it.key).getValue(User::class.java)!!
                            memberNames.add(member.username)
                        }

                        populateMemberList()

                        // enable the "Add members" button
                        groupChatInviteRelativeLayout.isEnabled = true
                        groupChatInviteRelativeLayout.setOnClickListener { _ ->
                            if (doubleClickBlocker.isSingleClick()) {
                                val intent = Intent(activity, SearchActivity::class.java)
                                intent.putExtra(SearchFragment.ARG_SOURCE, SearchFragment.SOURCE_GROUP_ADD_MEMBERS)
                                intent.putExtra(SearchFragment.ARG_USERS_TO_EXCLUDE, membersIds)
                                activity?.startActivityForResult(intent, ADD_MEMBERS)
                            }
                        }
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

    private fun populateMemberList() {
        val viewAdapter = MemberListAdapter(memberNames)

        groupChatMemberListRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        groupChatMemberListRecyclerView.adapter = viewAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == ADD_MEMBERS) {
            val childUpdates = HashMap<String, Any>()
            val selectedUsers = data!!.extras.getStringArrayList("output")

            val newMembers = chat!!.members.toMutableMap()
            selectedUsers.forEach{
                newMembers.put(it!!, true)
            }
            chat!!.members = newMembers

            childUpdates["/chats/$chatId"] = chat!!.toMap()

            val usersOneTimeListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value == null) {
                        Log.i(TAG, "Users not found")
                        return
                    }

                    selectedUsers.forEach {
                        val user = dataSnapshot.child(it).getValue(User::class.java)!!
                        val chatRecord : MutableMap<String, MutableMap<String, Long>> = mutableMapOf()
                        chatRecord.put(chatId!!, mutableMapOf<String, Long>(Pair("joinedAt", Date().time)))
                        user.chats.putAll(chatRecord)
                        val newChats = user.chats

                        childUpdates["/users/$it/chats"] = newChats

                        memberNames.add(user.username)
                    }

                    // update everything at once!
                    database.updateChildren(childUpdates)
                            .addOnSuccessListener {
                                Log.i(TAG, "Success!")
                            }
                            .addOnFailureListener {
                                Log.i(TAG, "Failure!")
                            }

                    populateMemberList()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.i(TAG, databaseError.message)
                }
            }

            database.child("users")
                    .addListenerForSingleValueEvent(usersOneTimeListener)
        }
    }
}
