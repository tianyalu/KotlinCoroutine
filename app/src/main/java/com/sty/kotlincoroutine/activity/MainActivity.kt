package com.sty.kotlincoroutine.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.sty.kotlincoroutine.R
import com.sty.kotlincoroutine.api.User
import com.sty.kotlincoroutine.api.userServiceApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlin.coroutines.*

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope(){
    private lateinit var tvText: TextView
    private lateinit var btnSubmitByAsyncTask: Button
    private lateinit var btnSubmitByCoroutine: Button
    private lateinit var btnMainScope: Button
    private lateinit var btnMvvm: Button
    private lateinit var btnException: Button
    private lateinit var btnDemoActivity: Button

    //private val mainScope = MainScope()
    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.e("sty", "Caught $exception")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        //test()
    }

    @SuppressLint("StaticFieldLeak", "SetTextI18n")
    private fun initView() {
        tvText = findViewById(R.id.tv_text)
        btnSubmitByAsyncTask = findViewById(R.id.btn_submit_by_async_task)
        btnSubmitByCoroutine = findViewById(R.id.btn_submit_by_coroutine)
        btnMainScope = findViewById(R.id.btn_main_scope)
        btnMvvm = findViewById(R.id.btn_mvvm)
        btnException = findViewById(R.id.btn_exception)
        btnDemoActivity = findViewById(R.id.btn_demo_activity)



        tvText.text = "Jack"
        btnSubmitByAsyncTask.setOnClickListener {
            object: AsyncTask<Void, Void, User>() {
                override fun doInBackground(vararg params: Void?): User? {
                    //return userServiceApi.loadUser("xxx").execute().body()
                    return userServiceApi.loadUser().execute().body()
                }

                override fun onPostExecute(user: User?) {
                    tvText.text = "lastName: ${user?.lastName}"
                }
            }.execute()
        }

        btnSubmitByCoroutine.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val user = withContext(Dispatchers.IO) {
                    userServiceApi.getUser()
                }
                tvText.text = "lastName: ${user?.lastName}"
            }
        }

        btnMainScope.setOnClickListener {
//                mainScope.launch {
//                    val user = userServiceApi.getUser()
//                    tvText.text = "lastName: ${user?.lastName}"
//                }
            launch {
                val user = userServiceApi.getUser()
                tvText.text = "lastName: ${user?.lastName}"
            }
        }

        btnMvvm.setOnClickListener {
            val intent = Intent(this, MvvmActivity::class.java)
            startActivity(intent)
        }

        btnException.setOnClickListener {
            //GlobalScope.launch(handler) {
            GlobalScope.launch {
                Log.d("sty", "on exception clicked")
                "abc".substring(10)
            }
        }

        btnDemoActivity.setOnClickListener {
            startActivity(Intent(this, DemoActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        mainScope.cancel()
        cancel()
    }

    // Kotlin协程基础框架层
    private fun baseCoroutine() {
        //协程体
        val continuation = suspend {
            5
        }.createCoroutine(object: Continuation<Int>{
            override val context: CoroutineContext = EmptyCoroutineContext

            override fun resumeWith(result: Result<Int>) {
                println("Coroutine End: $result")
            }
        })
        //执行
        continuation.resume(Unit)
    }

    private fun test() {
        GlobalScope.launch {
            Log.e("sty", "thread: $coroutineContext   ${Thread.currentThread().name}")
            (1..4).asFlow().map {
                if(it == 2) {
                    delay(2000)
                }else {
                    delay(1000)
                }
                it
            }.collect {
                Log.d("sty", "collect: $it")
            }
        }
    }
}