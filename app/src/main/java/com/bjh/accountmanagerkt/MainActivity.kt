package com.bjh.accountmanagerkt

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.bjh.accountmanagerkt.databinding.ActivityMainBinding

import com.bjh.accountmanagerkt.db.DatabaseAction
import com.bjh.accountmanagerkt.db.DatabaseColumns
import com.bjh.accountmanagerkt.db.DatabaseHelper
import com.bjh.accountmanagerkt.db.DatabaseQuery
import com.bjh.accountmanagerkt.util.StringUtil
import java.util.*

class MainActivity : AppCompatActivity() {

    private var choiceDay: String = "" // 선택한 날짜
    private var modChk = false         // 등록 / 수정 체크
    private var setModChk = false      // 기본 세팅 등록 / 수정 체크

    private var chooseYear: Int = 0         // 선택 년
    private var chooseMonth: Int = 0        // 선택 월
    private var chooseDayOfMonth: Int = 0   // 선택 일

    private var strBaseTimeSection: String = "MINUTE"  // 시 / 분 구분
    private var strBaseTime: Int = 0                 // 기본 근무 시간
    private var intBaseAmt: Int = 0                  // 기본 근무 금액
    private var strBaseDayOfMonth: Int = 15          // 월 기준일

    private lateinit var binding: ActivityMainBinding   // activity main view binding

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("strBaseTimeSection", strBaseTimeSection)
        savedInstanceState.putInt("strBaseTime", strBaseTime)
        savedInstanceState.putInt("intBaseAmt", intBaseAmt)
        savedInstanceState.putInt("strBaseDayOfMonth", strBaseDayOfMonth)
    }

    private fun funSavedInstanceStateChk(savedInstanceState: Bundle?){
        if (savedInstanceState != null) {
            strBaseTimeSection = savedInstanceState.getString("strBaseTimeSection").toString()
            strBaseTime = savedInstanceState.getInt("strBaseTime")
            intBaseAmt = savedInstanceState.getInt("intBaseAmt")
            strBaseDayOfMonth = savedInstanceState.getInt("strBaseDayOfMonth")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 기본 값 조회 후 세팅
        funSavedInstanceStateChk(savedInstanceState)

        chooseYear = Integer.parseInt(StringUtil.curYear)           // 선택 년도
        chooseMonth = Integer.parseInt(StringUtil.curMonth)         // 선택 월
        chooseDayOfMonth = Integer.parseInt(StringUtil.curDay)      // 선택 일자

        getTotalInfoToMonth(chooseYear, chooseMonth)    // 월 근무 시간 및 근무 금액 조회
        getBaseSettingInfo()    // 기본 세팅 시간 및 금액 불러오기
        setTimeSectionName(strBaseTimeSection)    // 월, 일 시간 명 세팅 ( 시 or 분 )

        // 달력 날짜 선택 이벤트
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            if (strBaseTimeSection == "" || intBaseAmt <= 0) {
                Toast.makeText(applicationContext, R.string.settingMessage, Toast.LENGTH_SHORT).show()
            }

            chooseYear = year              // 선택 년도
            chooseMonth = month + 1        // 선택 월
            chooseDayOfMonth = dayOfMonth  // 선택 일

            getTotalInfoToMonth(chooseYear, chooseMonth)       // 월 근무 시간 및 근무 금액 조회

            val titleDate = StringUtil.convertFullDateKo(applicationContext, chooseYear, chooseMonth, chooseDayOfMonth)
            val titleMonth = chooseMonth.toString() + " " + resources.getString(R.string.month) + " "

            // 달력에 일자 선택 시 상세 정보에 일 세팅
            binding.txtDaily.text = titleDate

            // 월별 근무시간 / 금액 타이틀 세팅
            binding.monthTitle.text = titleMonth

            // 선택 날짜 값 세팅
            choiceDay = StringUtil.convertDateYYYYMMDD(chooseYear, chooseMonth, chooseDayOfMonth)

            try {

                // 달력 일자 선택 시 상세 내역 조회
                val cursor = DatabaseHelper(applicationContext).readableDatabase.rawQuery(DatabaseQuery.getDetailInfo(), arrayOf(choiceDay))

                when(cursor.count > 0){
                    true -> {
                        if (cursor.moveToFirst()) {
                            binding.txtDailyWork.setText(cursor.getString(0))
                            binding.txtDailyTimes.setText(cursor.getString(1))
                            binding.txtDailyAmount.setText(StringUtil.convertNumberToComma(cursor.getString(2)))
                        }
                    }
                    else -> {
                        binding.txtDailyWork.setText("")
                        binding.txtDailyTimes.setText("")
                        binding.txtDailyAmount.setText("")
                        modChk = false
                    }
                }

                cursor.close()
                DatabaseHelper(applicationContext).readableDatabase.close()

            } catch (e: SQLiteException) {
                Toast.makeText(applicationContext, "Database unavailable onSelectedDayChange", Toast.LENGTH_SHORT).show()
            }
        }

        // 일일 근무 시간 및 근무 금액 저장 버튼 클릭 이벤트
        binding.btnSave.setOnClickListener {
            var chk = true

            if (binding.txtDaily.text == "") {
                Toast.makeText(applicationContext, R.string.msgChooseDay, Toast.LENGTH_LONG).show()
                chk = false
            }

            if (chk && binding.txtDailyWork.text.toString() == "") {
                Toast.makeText(applicationContext, R.string.msgDailyValidationNm, Toast.LENGTH_LONG).show()
                chk = false
            }

            if (chk && binding.txtDailyTimes.text.toString() == "") {
                Toast.makeText(applicationContext, R.string.msgDailyValidationTime, Toast.LENGTH_LONG).show()
                chk = false
            }

            if (chk && binding.txtDailyAmount.text.toString() == "") {
                Toast.makeText(applicationContext, R.string.msgDailyValidationAmount, Toast.LENGTH_LONG).show()
                chk = false
            }

            // 포커스 클리어
            binding.txtDailyWork.clearFocus()
            binding.txtDailyTimes.clearFocus()
            binding.txtDailyAmount.clearFocus()

            if (chk) {
                try {
                    val retVal: Long = when(modChk){
                        true -> { DatabaseAction.updateDailyColumn(DatabaseHelper(applicationContext).readableDatabase, choiceDay, binding.txtDailyWork.text.toString(), binding.txtDailyTimes.text.toString(), Integer.parseInt(binding.txtDailyAmount.text.toString().replace(",".toRegex(), "")).toLong())}
                        else -> { DatabaseAction.insertDailyColumn(DatabaseHelper(applicationContext).readableDatabase, choiceDay, binding.txtDailyWork.text.toString(), binding.txtDailyTimes.text.toString(), Integer.parseInt(binding.txtDailyAmount.text.toString().replace(",".toRegex(), "")).toLong())}
                    }

                    if (retVal == 0L) {
                        Toast.makeText(applicationContext, "FAIL", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, "SUCCESS", Toast.LENGTH_SHORT).show()

                        // 월 근무 시간 및 근무 금액 조회
                        getTotalInfoToMonth(chooseYear, chooseMonth)

                        // 키보드 숨기기
                        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputManager.hideSoftInputFromWindow(binding.txtDailyAmount.windowToken, 0)
                    }

                } catch (e: SQLiteException) {
                    Toast.makeText(applicationContext, "Database unavailable btnSave onClick", Toast.LENGTH_SHORT).show()
                }

            }
        }

        // 세팅 버튼 클릭
        binding.btnSetting.setOnClickListener {
            val settingIntent = Intent(applicationContext, SettingActivity::class.java)
            settingIntent.putExtra("strBaseTimeSection", strBaseTimeSection)
            settingIntent.putExtra("strBaseTime", strBaseTime)
            settingIntent.putExtra("intBaseAmt", intBaseAmt)
            settingIntent.putExtra("strBaseDayOfMonth", strBaseDayOfMonth)
            settingIntent.putExtra("setModChk", setModChk)
            startActivity(settingIntent)
        }

        // 통계 버튼 클릭
        binding.btnStatistics.setOnClickListener {
            val statisticsIntent = Intent(applicationContext, StatisticsActivity::class.java)
            statisticsIntent.putExtra("strBaseTimeSection", strBaseTimeSection)
            statisticsIntent.putExtra("strBaseTime", strBaseTime)
            statisticsIntent.putExtra("intBaseAmt", intBaseAmt)
            statisticsIntent.putExtra("strBaseDayOfMonth", strBaseDayOfMonth)
            startActivity(statisticsIntent)
        }

        // 시간 대비 금액 자동 계산 ( 포커스 변경 시 )¡
        binding.txtDailyTimes.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val douDailyTime = (if ((v as EditText).text.toString() == "") 0 else Integer.valueOf(v.text.toString())).toDouble()
                binding.txtDailyAmount.setText(StringUtil.convertNumberToComma((douDailyTime / strBaseTime * intBaseAmt).toInt().toString()))
            }
        }
    }

    override fun onResume(){
        super.onResume()
        getBaseSettingInfo()    // 기본 세팅 값 불러오기
        binding.calendarView.performClick()
    }

    /**
     * 상단 월 전체 근무 시간 및 금액 출력 처리
     * @param chooseYear
     * @param chooseMonth
     */
    private fun getTotalInfoToMonth(chooseYear: Int, chooseMonth: Int) {

        val strMonthTitle = chooseMonth.toString() + " " + resources.getString(R.string.month) + " "

        // 월별 근무시간 / 금액 타이틀 세팅
        binding.monthTitle.text = strMonthTitle

        val startDate = StringUtil.getCalculatorDay(chooseYear, chooseMonth - 1, strBaseDayOfMonth, -15, Calendar.DAY_OF_YEAR)
        val endDate = StringUtil.getCalculatorDay(chooseYear, chooseMonth - 1, strBaseDayOfMonth, 15, Calendar.DAY_OF_YEAR)

        try {
            val cursor = DatabaseHelper(applicationContext).readableDatabase.query(DatabaseColumns._TABLENAME1, arrayOf(DatabaseColumns.WORK_TIME, DatabaseColumns.WORK_AMOUNT), DatabaseColumns.WORK_DAY + " >= ? and " + DatabaseColumns.WORK_DAY + " <= ?", arrayOf(startDate, endDate), null, null, null)

            if (cursor.count > 0) {

                var intSumTime = 0
                var intSumAmt = 0

                for (i in 0 until cursor.count) {
                    if (cursor.moveToNext()) {
                        intSumTime += cursor.getInt(0)
                        intSumAmt += cursor.getInt(1)
                    }
                }

                binding.txtSumTimes.text = intSumTime.toString()
                binding.txtSumAmount.text = StringUtil.convertNumberToComma(intSumAmt.toString())
            } else {
                binding.txtSumTimes.text = ""
                binding.txtSumAmount.text = ""
            }

            cursor.close()
            DatabaseHelper(applicationContext).readableDatabase.close()
        } catch (e: SQLiteException) {
            Toast.makeText(applicationContext, "Database unavailable getTotalInfoToMonth()", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 기본 세팅 시간 및 금액 불러오기
     */
    private fun getBaseSettingInfo() {

        // 저장된 세팅값 가져오기
        try {
            val cursor = DatabaseHelper(applicationContext).readableDatabase.query(DatabaseColumns._TABLENAME0, arrayOf(DatabaseColumns.TIME_SECTION, DatabaseColumns.BASE_TIME, DatabaseColumns.BASE_AMOUNT, DatabaseColumns.BASE_DAY_OF_MONTH), null, null, null, null, null)

            if (cursor.count > 0) {
                if (cursor.moveToFirst()) {
                    strBaseTimeSection = cursor.getString(0)
                    strBaseTime = cursor.getInt(1)
                    intBaseAmt = cursor.getInt(2)
                    strBaseDayOfMonth = cursor.getInt(3)
                }
                setModChk = true
            } else {
                strBaseTimeSection = "MINUTE"
                strBaseTime = 0
                intBaseAmt = 0
                strBaseDayOfMonth = 15
                setModChk = false
            }

            cursor.close()
            DatabaseHelper(applicationContext).readableDatabase.close()

        } catch (e: SQLiteException) {
            Toast.makeText(applicationContext, "Database unavailable btnSetting.setOnClickListener onclick()", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * 월 / 일 시간 명 세팅
     */
    private fun setTimeSectionName(argBaseTimeSection : String) {
        when (argBaseTimeSection) {
            "HOUR" -> {
                binding.timeComment1.setText(R.string.labelHour) // 시
                binding.timeComment2.setText(R.string.labelHour) // 시
            }
            "MINUTE" -> {
                binding.timeComment1.setText(R.string.labelMinute) // 분
                binding.timeComment2.setText(R.string.labelMinute) // 분
            }
            else -> {
                binding.timeComment1.text = ""
                binding.timeComment2.text = ""
            }
        }
    }
}
