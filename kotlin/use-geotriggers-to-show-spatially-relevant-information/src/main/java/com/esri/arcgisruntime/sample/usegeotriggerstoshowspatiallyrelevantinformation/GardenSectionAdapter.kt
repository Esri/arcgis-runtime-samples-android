package com.esri.arcgisruntime.sample.usegeotriggerstoshowspatiallyrelevantinformation

import android.content.Context
import android.graphics.BitmapFactory
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter


internal class GardenSectionAdapter(context: Context, gardenSections: MutableList<GardenSection>) : PagerAdapter() {

    val mGardenSections = gardenSections

    private val mLayoutInflater: LayoutInflater

    override fun getCount(): Int {
        return mGardenSections.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE;
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View = mLayoutInflater.inflate(R.layout.garden_content, container, false)

        val gardenContentTextView = view.findViewById<TextView>(R.id.gardenContentTextView)
        gardenContentTextView.text = Html.fromHtml(mGardenSections[position].description)
        val gardenContentImageView = view.findViewById<ImageView>(R.id.gardenContentImageView)

        val bitmap = BitmapFactory.decodeFile(mGardenSections[position].imageURI)
        gardenContentImageView.setImageBitmap(bitmap)

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mGardenSections[position].title
    }

    init {
        mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    }
}
