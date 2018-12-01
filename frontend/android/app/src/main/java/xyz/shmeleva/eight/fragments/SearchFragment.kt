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
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_search.*

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.activities.BaseFragmentActivity
import xyz.shmeleva.eight.activities.ChatActivity
import xyz.shmeleva.eight.adapters.AddedUsersAdapter
import xyz.shmeleva.eight.adapters.UserListAdapter
import xyz.shmeleva.eight.models.User

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

    private lateinit var database: DatabaseReference

    val users = ArrayList<User>()
    val addedUsers = arrayListOf<User>()
    lateinit var usersAdapter: UserListAdapter
    lateinit var addedUsersAdapter: AddedUsersAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            source = arguments!!.getInt(ARG_SOURCE)
        }

        database = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBackButton.setOnClickListener({ _ -> activity?.onBackPressed()})

        searchRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        usersAdapter = UserListAdapter(users,
                { user : User -> onUserClicked(user) },
                { user: User, isSelected: Boolean -> onUserSelected(user, isSelected) },
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

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(searchString: String): Boolean {
                users.clear()
                usersAdapter.notifyDataSetChanged()

                if (searchString.length < MINIMUM_SEARCH_LENGTH) {
                    database.child("users").orderByChild("username").equalTo(searchString).addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.i(TAG, "users username equal to $searchString onDataChange!")
                            if (snapshot.exists()) {
                                updateUserList(snapshot)
                            }
                        }

                        override fun onCancelled(e: DatabaseError) {
                            Log.e(TAG, "Failed to retrieve a user whose username matches \"$searchString\": ${e.message}")
                        }
                    })
                } else {
                    database.child("users").orderByChild("username").startAt(searchString).endAt("$searchString\uf8ff").addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.i(TAG, "users username starts with $searchString onDataChange!")
                            if (snapshot.exists()) {
                                updateUserList(snapshot)
                            }
                        }

                        override fun onCancelled(e: DatabaseError) {
                            Log.e(TAG, "Failed to retrieve users whose username starts with \"$searchString\": ${e.message}")
                        }
                    })
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // TODO
                return false
            }

        })
    }

    private fun updateUserList(snapshot: DataSnapshot) {
        users.clear()
        for (userSnapshot in snapshot.children) {
            val user = userSnapshot.getValue(User::class.java)
            if (user != null) {
                users.add(user)
            }
        }
        usersAdapter.notifyDataSetChanged()
    }

    private fun onUserClicked(user : User) {
        if (source == SOURCE_SEARCH) {
            (activity as BaseFragmentActivity).addFragment(PrivateChatSettingsFragment.newInstance(true))
            return
        }

        if (source == SOURCE_NEW_PRIVATE_CHAT) {
            val chatActivityIntent = Intent(activity, ChatActivity::class.java)
            startActivity(chatActivityIntent)
            activity?.finishAfterTransition()
            return
        }
    }

    private fun onUserSelected(user: User, isSelected: Boolean) {
        if (isSelected) {
            addedUsers.add(user)
            searchAddedUsersRecyclerView.adapter.notifyItemInserted(addedUsers.size - 1)
            searchAddedUsersRecyclerView.scrollToPosition(addedUsers.size - 1);
        }
        else {
            var userIndex = addedUsers.indexOf(user)
            addedUsers.removeAt(userIndex)
            searchAddedUsersRecyclerView.adapter.notifyItemRemoved(userIndex)
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
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
