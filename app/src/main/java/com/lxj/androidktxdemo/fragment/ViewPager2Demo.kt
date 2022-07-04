package com.lxj.androidktxdemo.fragment

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.LogUtils
import com.lxj.androidktx.core.*
import com.lxj.androidktxdemo.R
import com.lxj.easyadapter.EasyAdapter
import com.lxj.easyadapter.ViewHolder
import kotlinx.android.synthetic.main.fragment_recyclerview_ext.*
import kotlinx.android.synthetic.main.fragment_viewpager2.*


class ViewPager2Demo : BaseFragment() {
    override fun getLayoutId(): Int {
        return R.layout.fragment_viewpager2
    }

    lateinit var data: ArrayList<String>
    override fun initView() {
        super.initView()
        p1.bind(5, bindView = {container, position ->
            View.inflate(context, R.layout.adapter_vp1, null)
        })
        p1.asCard(pageMargin = -dp2px(200f))

        p2.offscreenPageLimit = 3
        p2.adapter = object : EasyAdapter<String>(listOf("1", "2", "3"), R.layout.adapter_vp1){
            override fun bind(holder: ViewHolder, t: String, position: Int) {
                holder.getView<TextView>(R.id.text).text = "大萨达所大撒"
            }
        }
        p2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                LogUtils.d("posi: $position")
            }
        })
        p2.setPageTransformer(CardPager2Transformer(context!!))
        p2.setPadding(80,80,80,80)
        p2.clipToPadding = false
        p2.clipChildren = false


    }

    class CardPager2Transformer(context: Context) : ViewPager2.PageTransformer {
        private val maxTranslateOffsetX: Int = 180.dp
        private var viewPager: RecyclerView? = null

        override fun transformPage(view: View, position: Float) {
            if (viewPager == null) {
                viewPager = view.parent as RecyclerView
            }
            val leftInScreen = view.left - viewPager!!.scrollX
            val centerXInViewPager = leftInScreen + view.measuredWidth / 2
            val offsetX = centerXInViewPager - viewPager!!.measuredWidth / 2
            val offsetRate = offsetX.toFloat() * 0.38f / viewPager!!.measuredWidth
            val scaleFactor = 1 - Math.abs(offsetRate)
            if (scaleFactor > 0) {
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor
                view.translationX = -maxTranslateOffsetX * offsetRate
            }
        }
    }
}
