package xyz.shmeleva.eight.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_gallery.*

import xyz.shmeleva.eight.R
import xyz.shmeleva.eight.adapters.ImageGroupListAdapter

class GalleryFragment : Fragment() {

    private var groupByOptionIndex: Int = 0
    private var groupByOptions: Int = 0
    private var isPrivate: Boolean = true

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isPrivate = arguments!!.getBoolean(ARG_IS_PRIVATE)
            groupByOptions = if (isPrivate) R.array.gallery_group_by_options_private else R.array.gallery_group_by_options_group
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleryRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        val imageGroups = arrayListOf(
                Pair("December 25", arrayListOf(
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/6/62/Nativity_tree2011.jpg/1200px-Nativity_tree2011.jpg",
                        "https://www.irelandsown.ie/wp-content/uploads/2017/12/hith-father-christmas-lights-iStock_000029514386Large.jpg",
                        "https://www.washingtonpost.com/resizer/8TYuGiVJ_MsDy9uv7JcJRjPi1m0=/1484x0/arc-anglerfish-washpost-prod-washpost.s3.amazonaws.com/public/HD2SOWULW4YNPO7RVYJY7CGSHQ.jpg",
                        "https://www.gannett-cdn.com/-mm-/7f3fb6f68b1522b2988c80acd9b2aa54d16d7949/c=0-137-2700-1662/local/-/media/2017/11/10/USATODAY/USATODAY/636459276688363300-Mickey-s-Very-Merry-Christmas-Party.jpg?width=3200&height=1680&fit=crop")),
                Pair("December 31", arrayListOf(
                        "http://static.diary.ru/userdir/3/1/9/5/3195228/80173352.jpg",
                        "http://ekaterinburgpanavto.ru/wp-content/uploads/2017/11/moskva-zima.jpg",
                        "http://park72.ru/wp-content/uploads/2017/12/moscow_01.jpg",
                        "http://god2018.su/wp-content/uploads/2017/08/novogodnyaya-moskva-2018-foto-puhkinskaya6.jpg",
                        "http://alushta-tour.net/wp-content/uploads/2017/10/i-1.jpg",
                        "https://12millionov.com/wp-content/uploads/2016/12/%D0%9A%D1%83%D0%B4%D0%B0-%D0%BF%D0%BE%D0%B9%D1%82%D0%B8-%D0%B2-%D0%BC%D0%BE%D1%81%D0%BA%D0%B2%D0%B5-%D0%BD%D0%B0-%D0%BD%D0%BE%D0%B2%D0%BE%D0%B3%D0%BE%D0%B4%D0%BD%D0%B8%D0%B5-%D0%BF%D1%80%D0%B0%D0%B7%D0%B4%D0%BD%D0%B8%D0%BA%D0%B8-2017.jpg",
                        "https://img-fotki.yandex.ru/get/9088/36950459.9f/0_aa205_f7ef6ef6_XL.jpg",
                        "https://img0.liveinternet.ru/images/attach/d/1/133/502/133502798_DSC02395ps.jpg",
                        "https://god-2018s.com/wp-content/uploads/2017/10/ng-jarmarki-2018-v-moskve-1-610x300.jpg",
                        "https://kudamoscow.ru/uploads/bfd6582dedc1ba7a8585254b80f1b4d4.jpg",
                        "http://89.108.70.76/pics/large/31927.jpg",
                        "http://god2018.su/wp-content/uploads/2017/08/novogodnyaya-moskva-2018-foto-11.jpg",
                        "https://kudamoscow.ru/uploads/7263b3288640d54bda5ebae9cfc140ad.jpg",
                        "https://cs8.pikabu.ru/post_img/big/2017/12/31/7/1514721434182491989.jpg")))
        val adapter = ImageGroupListAdapter(activity as Context, imageGroups, { user : String ->  })
        galleryRecyclerView.adapter = adapter

        gallerySortButton.setOnClickListener {
            AlertDialog.Builder(context)
                    .setSingleChoiceItems(R.array.gallery_group_by_options_private, groupByOptionIndex) {dialog, i ->
                        if (groupByOptionIndex != i) {
                            groupByOptionIndex = i
                            // TODO
                        }
                        dialog.dismiss()
                    }
                    .show()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        /*if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }*/
    }

    override fun onDetach() {
        super.onDetach()
        //mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private val ARG_IS_PRIVATE = "isPrivate"

        fun newInstance(isPrivate: Boolean): GalleryFragment {
            val fragment = GalleryFragment()
            val args = Bundle()
            args.putBoolean(ARG_IS_PRIVATE, isPrivate)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
