package online.dailyq.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import online.dailyq.api.response.HelloWorld
import online.dailyq.databinding.FragmentTodayBinding
import online.dailyq.ui.base.BaseFragment
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
import java.util.*

class TodayFragment : BaseFragment() {
    var _binding: FragmentTodayBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Thread {
            val url = URL("http://10.0.2.2:5000/v1/hello-world")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connect()

            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val body = reader.readText()
            reader.close()
            conn.disconnect()

//            val gson = Gson()

            val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.KOREA)
            val helloWorld = gson.fromJson(body, HelloWorld::class.java)

//            val json = JSONObject(body)
//            val date = json.getString("date").substring(0,10)
//            val message = json.getString("message")
            activity?.runOnUiThread {
                binding.question.text = helloWorld.message
                binding.date.text = dateFormat.format(helloWorld.date)
            }
        }.start()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }



}