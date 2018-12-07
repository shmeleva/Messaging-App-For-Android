package xyz.shmeleva.eight.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_search.*

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.activities.BaseFragmentActivity
import xyz.shmeleva.eight.activities.ChatActivity
import xyz.shmeleva.eight.adapters.AddedUsersAdapter
import xyz.shmeleva.eight.adapters.UserListAdapter
import xyz.shmeleva.eight.models.Chat
import xyz.shmeleva.eight.models.User
import xyz.shmeleva.eight.utilities.DoubleClickBlocker
import xyz.shmeleva.eight.utilities.hideKeyboard

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {

    private val TAG = "SearchFragment"
    private val MINIMUM_SEARCH_LENGTH = 5

    private var source: Int? = null

    private var mListener: OnFragmentInteractionListener? = null
    private val doubleClickBlocker: DoubleClickBlocker = DoubleClickBlocker()

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    val users = ArrayList<User>(arrayListOf(
            User(id="1", username = "1")))
    val addedUsers = arrayListOf<User>()
    lateinit var usersAdapter: UserListAdapter
    lateinit var addedUsersAdapter: AddedUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            source = arguments!!.getInt(ARG_SOURCE)
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBackButton.setOnClickListener { _ ->
            if (doubleClickBlocker.isSingleClick()) {
                activity?.onBackPressed()
            }
        }

        searchRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        usersAdapter = UserListAdapter(users,
                { user : User -> onUserClicked(user) },
                { user : User, isSelected: Boolean -> onUserSelected(user, isSelected) },
                source == SOURCE_NEW_GROUP_CHAT)
        searchRecyclerView.adapter = usersAdapter

        if (source == SOURCE_NEW_GROUP_CHAT) {
            searchAddedUsersRecyclerView.visibility = View.VISIBLE
            searchStartGroupChatFab.visibility = View.VISIBLE
            searchAddedUsersRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.HORIZONTAL, false)
            addedUsersAdapter = AddedUsersAdapter(addedUsers)
            searchAddedUsersRecyclerView.adapter = addedUsersAdapter
        }
        else {
            searchAddedUsersRecyclerView.visibility = View.GONE
            searchStartGroupChatFab.visibility = View.GONE
        }

        searchStartGroupChatFab.setOnClickListener { _ ->
            if (doubleClickBlocker.isSingleClick()) {
                if (addedUsers.size == 0) {
                    Toast.makeText(activity, "Please select members", Toast.LENGTH_LONG).show()
                } else if (addedUsers.size == 1) {
                    getOrCreatePrivateChat(addedUsers[0])
                } else {
                    val selectedUserIds = addedUsers.map { it -> it.id }.toMutableSet()
                    selectedUserIds.add(auth.currentUser!!.uid)
                    createChat(selectedUserIds)
                }
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(searchString: String): Boolean {
                users.clear()
                usersAdapter.notifyDataSetChanged()
                val lowercaseSearchString = searchString.toLowerCase()

                if (lowercaseSearchString.length < MINIMUM_SEARCH_LENGTH) {
                    database.child("users").orderByChild("lowercaseUsername").equalTo(lowercaseSearchString).addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.i(TAG, "users username equal to $lowercaseSearchString onDataChange!")
                            if (snapshot.exists()) {
                                updateUserList(snapshot)
                            }
                        }

                        override fun onCancelled(e: DatabaseError) {
                            Log.e(TAG, "Failed to retrieve a user whose username matches \"$lowercaseSearchString\": ${e.message}")
                        }
                    })
                } else {
                    database.child("users").orderByChild("lowercaseUsername").startAt(lowercaseSearchString).endAt("$lowercaseSearchString\uf8ff").addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.i(TAG, "users username starts with $lowercaseSearchString onDataChange!")
                            if (snapshot.exists()) {
                                updateUserList(snapshot)
                            }
                        }

                        override fun onCancelled(e: DatabaseError) {
                            Log.e(TAG, "Failed to retrieve users whose username starts with \"$lowercaseSearchString\": ${e.message}")
                        }
                    })
                }
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

        })
    }

    private fun activateChat(chat: Chat) {
        val chatActivityIntent = Intent(activity, ChatActivity::class.java)
        chatActivityIntent.putExtra("chatId", chat.id)
        chatActivityIntent.putExtra("isGroupChat", chat.isGroupChat)
        chatActivityIntent.putExtra("joinedAt", chat.joinedAt)
        startActivity(chatActivityIntent)
        activity?.finishAfterTransition()
    }

    private fun createChat(memberIds: Set<String>) {
        val newChatId: String = database.child("chats").push().key!!
        val newChat = Chat(
                id = newChatId,
                isGroupChat = memberIds.size > 2,
                members = memberIds.map { it to true }.toMap()
        )

        val childUpdates = HashMap<String, Any>()
        childUpdates["/chats/$newChatId"] = newChat.toMap()

        for (userId in memberIds) {
            childUpdates["/users/$userId/chats/$newChatId"] = mapOf("joinedAt" to newChat.updatedAt)
        }

        database.updateChildren(childUpdates)
                .addOnSuccessListener {
                    activateChat(newChat)
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to update new chat $newChatId: ${it.message}")
                }
    }

    private fun getOrCreatePrivateChat(user: User) {
        database.child("chats").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val selectedUserIds = setOf(auth.currentUser!!.uid, user.id)

                    for (chatSnapshot in snapshot.children) {
                        val chat = chatSnapshot.getValue(Chat::class.java)

                        if (chat != null && chat.members.keys == selectedUserIds) {
                            Log.i(TAG, "Found chat ${chat.id}")
                            activateChat(chat)
                            return
                        }
                    }

                    Log.i(TAG, "Private chat not found")
                    createChat(selectedUserIds)
                }
            }

            override fun onCancelled(e: DatabaseError) {
                Log.e(TAG, "Failed to retrieve chats: ${e.message}")
            }
        })
    }

    private fun updateUserList(snapshot: DataSnapshot) {
        users.clear()
        for (userSnapshot in snapshot.children) {
            val user = userSnapshot.getValue(User::class.java)
            if (user != null && user.id != auth.currentUser!!.uid) {
                users.add(user)
            }
        }
        usersAdapter.notifyDataSetChanged()
    }

    private fun onUserClicked(user : User) {
        if (source == SOURCE_SEARCH) {
            activity?.hideKeyboard()
            val fragment = PrivateChatSettingsFragment.newInstance(true, null, auth.currentUser!!.uid)
            fragment.setUser(user)
            (activity as BaseFragmentActivity).addFragment(fragment)
            return
        }

        if (source == SOURCE_NEW_PRIVATE_CHAT) {
            activity?.hideKeyboard()
            getOrCreatePrivateChat(user)
            return
        }
    }

    private fun onUserSelected(user: User, isSelected: Boolean) {
        if (user.isSelected) {
            addedUsers.add(user)
            searchAddedUsersRecyclerView.adapter.notifyItemInserted(addedUsers.size - 1)
            searchAddedUsersRecyclerView.scrollToPosition(addedUsers.size - 1);
        }
        else {
            val userIndex = addedUsers.indexOfFirst { u -> u.id == user.id }
            if (userIndex != -1) {
                addedUsers.removeAt(userIndex)
                searchAddedUsersRecyclerView.adapter.notifyItemRemoved(userIndex)
            }
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other xyz.shmeleva.eight.fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/xyz.shmeleva.eight.fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        val ARG_SOURCE = "param1"

        val SOURCE_SEARCH = 0
        val SOURCE_NEW_PRIVATE_CHAT = 1
        val SOURCE_NEW_GROUP_CHAT = 2

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param source Caller.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(source: Int): SearchFragment {
            val fragment = SearchFragment()
            val args = Bundle()
            args.putInt(ARG_SOURCE, source)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
