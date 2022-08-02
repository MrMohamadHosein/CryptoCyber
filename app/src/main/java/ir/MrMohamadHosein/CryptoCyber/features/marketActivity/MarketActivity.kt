package ir.MrMohamadHosein.CryptoCyber.features.marketActivity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import ir.MrMohamadHosein.CryptoCyber.apiManager.ApiManager
import ir.MrMohamadHosein.CryptoCyber.apiManager.model.CoinAboutData
import ir.MrMohamadHosein.CryptoCyber.apiManager.model.CoinAboutItem
import ir.MrMohamadHosein.CryptoCyber.apiManager.model.CoinsData
import ir.MrMohamadHosein.CryptoCyber.databinding.ActivityMarketBinding
import ir.MrMohamadHosein.CryptoCyber.features.coinActivity.CoinActivity

class MarketActivity : AppCompatActivity(), MarketAdapter.RecyclerCallback {
    lateinit var binding: ActivityMarketBinding
    lateinit var adapter: MarketAdapter
    lateinit var dataNews: ArrayList<Pair<String, String>>
    lateinit var aboutDataMap: MutableMap<String, CoinAboutItem>
    val apiManager = ApiManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarketBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.layoutToolbar.toolbar.title = "CryptoCyber Market"

        firstRunShowDialogWelcome()

        binding.layoutWatchlist.btnShowMore.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.livecoinwatch.com/"))
            startActivity(intent)
        }
        binding.swipeRefreshMain.setOnRefreshListener {

            initUi()

            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefreshMain.isRefreshing = false
            }, 1500)

        }

        getAboutDataFromAssets()

    }
    override fun onResume() {
        super.onResume()

        initUi()

    }

    private fun initUi() {

        getNewsFromApi()
        getTopCoinsFromApi()

    }

    private fun getNewsFromApi() {

        apiManager.getNews(object : ApiManager.ApiCallback<ArrayList<Pair<String, String>>> {

            override fun onSuccess(data: ArrayList<Pair<String, String>>) {
                dataNews = data
                refreshNews()
            }

            override fun onError(errorMessage: String) {
                Toast.makeText(this@MarketActivity, "error => " + errorMessage, Toast.LENGTH_SHORT)
                    .show()
            }

        })

    }
    private fun refreshNews() {

        val randomAccess = (0..49).random()
        binding.layoutNews.txtNews.text = dataNews[randomAccess].first
        binding.layoutNews.imgNews.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dataNews[randomAccess].second))
            startActivity(intent)
        }
        binding.layoutNews.txtNews.setOnClickListener {
            refreshNews()
        }

    }

    private fun getTopCoinsFromApi() {

        apiManager.getCoinsList(object : ApiManager.ApiCallback<List<CoinsData.Data>> {
            override fun onSuccess(data: List<CoinsData.Data>) {

                showDataInRecycler(data)

            }

            override fun onError(errorMessage: String) {
                Toast.makeText(this@MarketActivity, "error => " + errorMessage, Toast.LENGTH_SHORT)
                    .show()
                Log.v("testLog", errorMessage)
            }
        })

    }
    private fun showDataInRecycler(data: List<CoinsData.Data>) {

        adapter = MarketAdapter(ArrayList(data), this)
        binding.layoutWatchlist.recyclerMain.adapter = adapter
        binding.layoutWatchlist.recyclerMain.layoutManager = LinearLayoutManager(this)

    }
    override fun onCoinItemClicked(dataCoin: CoinsData.Data) {
        val intent = Intent(this, CoinActivity::class.java)

        val bundle = Bundle()
        bundle.putParcelable("bundle1", dataCoin)
        bundle.putParcelable("bundle2", aboutDataMap[dataCoin.coinInfo.name])

        intent.putExtra("bundle", bundle)
        startActivity(intent)
    }

    private fun getAboutDataFromAssets() {

        val fileInString = applicationContext.assets
            .open("currencyinfo.json")
            .bufferedReader()
            .use { it.readText() }

        aboutDataMap = mutableMapOf<String, CoinAboutItem>()

        val gson = Gson()
        val dataAboutAll = gson.fromJson(fileInString, CoinAboutData::class.java)

        dataAboutAll.forEach {
            aboutDataMap[it.currencyName] = CoinAboutItem(
                it.info.web,
                it.info.github,
                it.info.twt,
                it.info.desc,
                it.info.reddit
            )
        }

    }

    private fun firstRunShowDialogWelcome() {

        val shared = getSharedPreferences("mainSharedPref.xml", Context.MODE_PRIVATE)
        val isFirstRun = shared.getBoolean("isFirstRun", true)
        if (isFirstRun) {

            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("این برنامه یکی از پروژه های داخل دوره یاقوت اندروید وبسایت دانیجت است که در قالب ۱۰ ساعت دوره آموزشی ضبط شده و آماده استفاده است :)" +
                    "\n\n دوره یاقوت اندروید شامل ۴۰ فصل کامل به زبان کاتلین است که کاملا پروژه محور بوده و این پروژه فقط پروژه یکی از فصول این دوره است" +
                    "\n\n در پایان دوره یک پروژه فروشگاهی کامل به همراه درگاه پرداخت هم به زبان کاتلین توسعه داده شده است" +
                    "\n\n در صورت علاقه به حرفه ای شدن در زمینه اندروید به لینک زیر سر بزنید : " +
                    "\n https://dunijet.ir/product/yaghot-android/")
            dialog.show()

            shared.edit().putBoolean("isFirstRun" , false).apply()
        }

    }

}