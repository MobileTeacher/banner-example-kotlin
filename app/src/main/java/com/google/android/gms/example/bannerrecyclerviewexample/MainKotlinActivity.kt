package com.google.android.gms.example.bannerrecyclerviewexample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class MainKotlinActivity : AppCompatActivity() {

    private var recyclerViewItems = mutableListOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(this)

        // Update the RecyclerView item's list with menu items and banner ads.
        addMenuItemsFromJson()
        addBannerAds()
        loadBannerAds()

        val adapter = RecyclerViewAdapter(this, recyclerViewItems)
        recycler_view.adapter = adapter
    }

    override fun onResume() {
        for (item in recyclerViewItems){
            if(item is AdView){
                // conversão (casting) implícita
                item.resume()
            }
        }
        super.onResume()
    }

    override fun onPause() {
        for (item in recyclerViewItems){
            if(item is AdView){
                item.pause()
            }
        }
        super.onPause()
    }

    fun addBannerAds(){
        for (i in 0..recyclerViewItems.size ){
            val adView:AdView = AdView(this)
            adView.adSize = AdSize.BANNER
            adView.adUnitId = AD_UNIT_ID
            recyclerViewItems.add(i, adView)
        }
    }
    fun loadBannerAds(){
        loadBannerAd(0)
    }

    private fun loadBannerAd(index: Int) {

        if (index >= recyclerViewItems.size) {
            return
        }

        val item = recyclerViewItems[index]

        if (item is AdView){
            // Set an AdListener on the AdView to wait for the previous banner ad
            // to finish loading before loading the next ad in the items list.

            val adlistener = object: AdListener(){
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    loadBannerAd(index + ITEMS_PER_AD)
                }
                override fun onAdFailedToLoad(p0: Int) {
                    Log.e("MainActivity", "The previous banner ad failed to load. Attempting to"
                            + " load the next banner ad in the items list.");
                    loadBannerAd(index + ITEMS_PER_AD);
                }
            }

            item.setAdListener(adlistener)

            // Load the banner ad.
            item.loadAd(AdRequest.Builder().build())

        } else {
            throw ClassCastException("Expected item at index " + index + " to be a banner ad"
                    + " ad.");
        }


    }

    private fun addMenuItemsFromJson() {
        try {
            val jsonDataString = readJsonDataFromFile()
            val menuItemsJsonArray = JSONArray(jsonDataString)

            for (i in 0 until menuItemsJsonArray.length()) {
                val menuItemObject = menuItemsJsonArray.getJSONObject(i)
                val menuItemName = menuItemObject.getString("name")
                val menuItemDescription= menuItemObject.getString("description")
                val menuItemPrice  = menuItemObject.getString("price")
                val  menuItemCategory  = menuItemObject.getString("category")
                val menuItemImageName  = menuItemObject.getString("photo")

                val  menuItem = MenuItem(menuItemName, menuItemDescription, menuItemPrice,
                        menuItemCategory, menuItemImageName)
                recyclerViewItems.add(menuItem)
            }
        } catch (exception: IOException) {
            Log.e(exception.toString(), "Unable to parse JSON file.")
        } catch (exception: JSONException){
            Log.e(exception.toString(), "Unable to parse JSON file.")
        }
    }



    companion object {
        val ITEMS_PER_AD = 8
        private val AD_UNIT_ID = "ca-app-pub-3940256099942544/4177191030"
    }
}
