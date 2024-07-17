package com.example.rockscreen.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.example.rockscreen.ApiClient
import com.example.rockscreen.Place
import com.example.rockscreen.dialog.PlaceSearchDialogFragment
import com.example.rockscreen.R
import com.example.rockscreen.dialog.RewardDialogFragment
import com.example.rockscreen.dialog.ShopReviewDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class LockScreenActivity : FragmentActivity(), PlaceSearchDialogFragment.PlaceSearchDialogListener,
    RewardDialogFragment.RewardDialogListener, ShopReviewDialogFragment.ShopReviewDialogListener {

    private lateinit var lockScreenLayout: ConstraintLayout
    private lateinit var tmpInfoTv: TextView
    private lateinit var timeTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var batteryTextView: TextView
    private lateinit var textViewTimer: TextView
    private lateinit var reduceTimer: ConstraintLayout
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var contentsType1Cl: ConstraintLayout
    private lateinit var contentsType2Cl: ConstraintLayout
    private lateinit var contentsType4Cl: ConstraintLayout
    private lateinit var contentsType4ShopName: TextView
    private lateinit var contentsType4ShopNameSub: TextView
    private lateinit var contentsType4ShopAddress: TextView
    private lateinit var contentsType4Btn1: ConstraintLayout
    private lateinit var contentsType4Btn2: ConstraintLayout
    private lateinit var contentsType5Cl: ConstraintLayout
    private lateinit var rouletteImageView: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("M월 d일 (E)", Locale.getDefault())
    private val timeUpdateRunnable = object : Runnable {
        override fun run() {
            updateTimeAndDate()
            handler.postDelayed(this, 1000)
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = level * 100 / scale.toFloat()
            batteryTextView.text = getString(R.string.battery_level, batteryPct.toInt())
        }
    }

    private var totalTimeInMillis = TimeUnit.SECONDS.toMillis(300)
    private var remainingTimeInMillis = totalTimeInMillis

    private var initialX: Float = 0F

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        // 화면에 터치 이벤트 리스너를 설정합니다.
        lockScreenLayout = findViewById(R.id.lock_screen_root)
        lockScreenLayout.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    // 터치가 시작된 지점의 X 좌표를 저장합니다.
                    initialX = event.x
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    // 터치가 이동 중일 때, X 좌표를 조정하여 밀리는 효과를 추가합니다.
                    val currentX = event.x
                    val deltaX = currentX - initialX

                    // X 좌표를 이동시켜 lockScreenLayout에 애니메이션 적용
                    lockScreenLayout.translationX = deltaX

                    true
                }

                MotionEvent.ACTION_UP -> {
                    // 터치가 끝난 지점의 X 좌표를 저장합니다.
                    val finalX = event.x

                    // 터치 시작점과 끝점의 거리가 충분히 크면 잠금 해제 처리를 수행합니다.
                    if (finalX - initialX >= dpToPx(100)) { // 예시로 100dp 이상으로 밀어야 해제되도록 설정
                        onConfirm()
                    } else {
                        // 애니메이션 초기화
                        lockScreenLayout.animate().translationX(0f).setDuration(300).start()
                    }
                    true
                }

                else -> false
            }
        }

        tmpInfoTv = findViewById(R.id.tmp_info_tv)
        timeTextView = findViewById(R.id.time_tv)
        dateTextView = findViewById(R.id.date_tv)
        batteryTextView = findViewById(R.id.battery_textview)
        textViewTimer = findViewById(R.id.contents_type1_timer_tv)
        reduceTimer = findViewById(R.id.show_ad_cl)
        reduceTimer.setOnClickListener {
            decreaseTimerByTenSeconds()
        }
        contentsType1Cl = findViewById(R.id.contents_type1_cl)
        contentsType2Cl = findViewById(R.id.contents_type2_cl)
        contentsType4Cl = findViewById(R.id.contents_type4_cl)
        contentsType4ShopName = findViewById(R.id.contents_type4_tv1)
        contentsType4ShopNameSub = findViewById(R.id.contents_type4_tv2)
        contentsType4ShopAddress = findViewById(R.id.contents_type4_tv3)
        contentsType4Btn1 = findViewById(R.id.contents_type4_btn1)
        contentsType4Btn2 = findViewById(R.id.contents_type4_btn2)
        contentsType5Cl = findViewById(R.id.contents_type5_cl)
        rouletteImageView = findViewById(R.id.contents_type5_iv)

        contentsType2Cl.visibility = View.GONE
        contentsType2Cl.setOnClickListener {
            showPlaceSearchDialog()
        }

        contentsType4Cl.visibility = View.GONE
        contentsType5Cl.visibility = View.GONE

        findViewById<Button>(R.id.unlock_button).setOnClickListener {
            finishAfterTransition()
        }

        updateTimeAndDate()
        handler.post(timeUpdateRunnable)

        val batteryIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, batteryIntentFilter)

        hideSystemUI()

        startCountDownTimer()

        // API 호출 예제.
        ApiClient.postExampleApi { responseData ->
            runOnUiThread {
                updateUI(responseData)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeUpdateRunnable)
        unregisterReceiver(batteryReceiver)

        // CountDownTimer 종료
        countDownTimer.cancel()
    }

    override fun onBackPressed() {
        // 뒤로가기 버튼 비활성화
        // super.onBackPressed() 호출하지 않음
    }

    private fun updateTimeAndDate() {
        val currentTime = Calendar.getInstance().time
        timeTextView.text = timeFormat.format(currentTime)
        dateTextView.text = dateFormat.format(currentTime)
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // -10초 버튼 클릭 시 호출될 함수
    private fun decreaseTimerByTenSeconds() {
        if (remainingTimeInMillis >= 60000) { // 60초보다 많이 남아 있을 때만 처리
            remainingTimeInMillis -= 60000 // 60초 감소
            countDownTimer.cancel()
            startCountDownTimer()
        } else if (remainingTimeInMillis > 0) {
            remainingTimeInMillis = 0
            countDownTimer.cancel()
            startCountDownTimer()
        }

    }

    // 타이머 시작 함수
    private fun startCountDownTimer() {
        countDownTimer = object : CountDownTimer(remainingTimeInMillis, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                // 타이머 종료 시 contents type2로 변경.
                contentsType1Cl.visibility = View.GONE
                contentsType2Cl.visibility = View.VISIBLE

                //finishAfterTransition()
            }
        }

        // CountDownTimer 시작
        countDownTimer.start()
    }

    // 타이머 업데이트 함수
    private fun updateTimerText() {
        val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(remainingTimeInMillis)
        val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(remainingTimeInMillis) -
                TimeUnit.MINUTES.toSeconds(minutesLeft)

        val formattedTime = String.format("%02d:%02d", minutesLeft, secondsLeft)
        textViewTimer.text = formattedTime
    }

    // 장소&가게 검색 다이얼로그
    private fun showPlaceSearchDialog() {
        val dialog = PlaceSearchDialogFragment()
        dialog.show(supportFragmentManager, "PlaceSearchDialog")
    }

    // 장소&가게 검색 다이얼로그
    private fun showShopReviewDialog() {
        val dialog = ShopReviewDialogFragment()
        dialog.show(supportFragmentManager, "ShopReviewDialog")
    }

    override fun onCloseButtonClicked() {
        // Dialog(화면3)에서 닫기 버튼 눌렀을 때의 동작 처리
    }

    override fun onSearchButtonClicked(place: Place) {
        // Dialog(화면3)에서 검색하기 버튼 눌렀을 때의 동작 처리
        contentsType2Cl.visibility = View.INVISIBLE
        reduceTimer.visibility = View.INVISIBLE
        contentsType4Cl.visibility = View.VISIBLE
        contentsType4ShopName.text = place.name
        contentsType4ShopNameSub.text = place.subName
        contentsType4ShopAddress.text = place.address

        contentsType4Btn1.setOnClickListener {
            enableRouletteContents()
        }
        contentsType4Btn2.setOnClickListener {
            showShopReviewDialog()
        }
    }

    override fun onFinish() {
        // 화면4에서 이동한 가게리뷰 남기기 Dialog에서 작성완료 눌렀을 때.
        Toast.makeText(this, "가게리뷰가 정상적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private var rotateAnimation: Animation? = null
    private fun enableRouletteContents() {
        contentsType4Cl.visibility = View.GONE
        contentsType5Cl.visibility = View.VISIBLE
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation)
        rouletteImageView.startAnimation(rotateAnimation)

        rouletteImageView.setOnClickListener {
            // 애니메이션이 null이 아니고 실행 중이면 애니메이션을 멈춥니다.
            rotateAnimation?.let {
                if (!it.hasEnded()) {
                    it.cancel() // 애니메이션을 취소합니다.
                }
            }
            // 리워드 팝업
            showRewardPopup()
        }
    }

    private fun showRewardPopup() {
        val dialog = RewardDialogFragment()
        dialog.show(supportFragmentManager, "RewardDialog")
    }

    override fun onConfirm() {
        // Dialog(화면6)에서 리워드 확인 후 돌아옴.
        contentsType4Cl.visibility = View.VISIBLE
        contentsType5Cl.visibility = View.GONE
        //캐시 받았으니 비활성화.
        contentsType4Btn1.isEnabled = false
        contentsType4Btn1.setBackgroundResource(R.drawable.contents_type4_btn_disable_bg)
    }

    private fun dpToPx(dp: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(responseData: String?) {
        responseData?.let {
            tmpInfoTv.text = it
        } ?: run {
            tmpInfoTv.text = "API 호출 실패"
        }
    }
}
