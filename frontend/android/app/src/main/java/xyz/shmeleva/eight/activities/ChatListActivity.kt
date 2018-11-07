package xyz.shmeleva.eight.activities

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import xyz.shmeleva.eight.R

import kotlinx.android.synthetic.main.activity_chat_list.*
import android.view.animation.Animation
import  android.view.animation.AnimationUtils


class ChatListActivity : AppCompatActivity() {

    private var isFabOpen = false

    private var fabOpenAnimation: Animation? = null
    private var fabCloseAnimation: Animation? = null
    private var rotateForwardAnimation: Animation? = null
    private var rotateBackwardAnimation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        setSupportActionBar(toolbar)

        fabOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fabCloseAnimation = AnimationUtils.loadAnimation(applicationContext,R.anim.fab_close)
        rotateForwardAnimation = AnimationUtils.loadAnimation(applicationContext,R.anim.rotate_forward)
        rotateBackwardAnimation = AnimationUtils.loadAnimation(applicationContext,R.anim.rotate_backward)


        fab.setOnClickListener { view ->
            if(isFabOpen){
                fab.startAnimation(rotateBackwardAnimation);
                fab1.startAnimation(fabCloseAnimation);
                fab2.startAnimation(fabCloseAnimation);
                //
                fab1.isClickable = false;
                fab2.isClickable = false;
                //
                isFabOpen = false;
            } else {
                fab.startAnimation(rotateForwardAnimation);
                fab1.startAnimation(fabOpenAnimation);
                fab2.startAnimation(fabOpenAnimation);
                //
                fab1.isClickable = true;
                fab2.isClickable = true;
                //
                isFabOpen = true;

            }
        }

        fab1.setOnClickListener { view ->

        }

        fab2.setOnClickListener { view ->

        }
    }

}
