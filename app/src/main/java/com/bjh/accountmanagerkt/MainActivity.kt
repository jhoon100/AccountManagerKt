package com.bjh.accountmanagerkt

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast

import com.bjh.accountmanagerkt.db.DatabaseAction
import com.bjh.accountmanagerkt.db.DatabaseColumns
import com.bjh.accountmanagerkt.db.DatabaseHelper
import com.bjh.accountmanagerkt.util.StringUtil

import java.util.Calendar

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var choiceDay: String? = null              // 선택한 날짜
    private var modChk = false        // 등록 / 수정 체크
    private var setModChk = false    // 기본 세팅 등록 / 수정 체크

    private var chooseYear: Int = 0         // 선택 년
    private var chooseMonth: Int = 0        // 선택 월
    private var chooseDayOfMonth: Int = 0   // 선택 일

    private var settingView: View? = null   // 기본 세팅 화면

    private var strBaseTimeSection: String? = null  // 시 / 분 구분
    private var strBaseTime: String? = null         // 기본 근무 시간
    private var intBaseAmt: Int = 0             // 기본 근무 금액
    private var strBaseDayOfMonth: String? = null   // 월 기준일

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("strBaseTimeSection", strBaseTimeSection)
        savedInstanceState.putString("strBaseTime", strBaseTime)
        savedInstanceState.putInt("intBaseAmt", intBaseAmt)
        savedInstanceState.putString("strBaseDayOfMonth", strBaseDayOfMonth)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            strBaseTimeSection = savedInstanceState.getString("strBaseTimeSection")
            strBaseTime = savedInstanceState.getString("strBaseTime")
            intBaseAmt = savedInstanceState.getInt("intBaseAmt")
            strBaseDayOfMonth = savedInstanceState.getString("strBaseDayOfMonth")
        }

        chooseYear = Integer.parseInt(StringUtil.curYear)           // 선택 년도
        chooseMonth = Integer.parseInt(StringUtil.curMonth)         // 선택 월
        chooseDayOfMonth = Integer.parseInt(StringUtil.curDay)      // 선택 일자

        // 기본 세팅 시간 및 금액 불러오기
        getBaseSettingInfo()

        // 월 근무 시간 및 근무 금액 조회
        getTotalInfoToMonth(chooseYear, chooseMonth)

        // 월, 일 시간 명 세팅 ( 시 or 분 )
        setTimeSectionName()

        // 달력 날짜 선택 이벤트
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            if (strBaseTimeSection == null || strBaseTimeSection == "" || strBaseTime == null || strBaseTime == "" || intBaseAmt <= 0) {
                Toast.makeText(applicationContext, R.string.settingMessage, Toast.LENGTH_SHORT).show()
            }

            chooseYear = year              // 선택 년도
            chooseMonth = month + 1        // 선택 월
            chooseDayOfMonth = dayOfMonth  // 선택 일

            getTotalInfoToMonth(chooseYear, chooseMonth)       // 월 근무 시간 및 근무 금액 조회

            val titleDate = chooseYear.toString() + resources.getString(R.string.year) + " " + (if (chooseMonth < 10) "0$chooseMonth" else chooseMonth) + resources.getString(R.string.month) + " " + (if (chooseDayOfMonth < 10) "0$chooseDayOfMonth" else chooseDayOfMonth) + resources.getString(R.string.day)
            val titleMonth = chooseMonth.toString() + " " + resources.getString(R.string.month) + " "

            // 달력에 일자 선택 시 상세 정보에 일 세팅
            txtDaily.text = titleDate

            // 월별 근무시간 / 금액 타이틀 세팅
            monthTitle.text = titleMonth

            choiceDay = chooseYear.toString() + "" + (if (chooseMonth < 10) "0$chooseMonth" else chooseMonth) + "" + if (chooseDayOfMonth < 10) "0$chooseDayOfMonth" else chooseDayOfMonth

            try {

                val cursor = DatabaseHelper(applicationContext).readableDatabase.query(DatabaseColumns._TABLENAME1, arrayOf(DatabaseColumns.WORK_NM, DatabaseColumns.WORK_TIME, DatabaseColumns.WORK_AMOUNT), DatabaseColumns.WORK_DAY + " = ?", arrayOf(choiceDay.toString()), null, null, null)

                when(cursor.count > 0){
                    true -> {
                        if (cursor.moveToFirst()) {
                            txtDailyWork.setText(cursor.getString(0))
                            txtDailyTimes.setText(cursor.getString(1))
                            txtDailyAmount.setText(StringUtil.convertNumberToComma(cursor.getString(2)))
                        }
                    }
                    else -> {
                        txtDailyWork.setText("")
                        txtDailyTimes.setText("")
                        txtDailyAmount.setText("")
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
        btnSave.setOnClickListener {
            var chk = true

            if (txtDaily.text == "") {
                Toast.makeText(applicationContext, R.string.msgChooseDay, Toast.LENGTH_LONG).show()
                chk = false
            }

            if (chk && (txtDailyWork!!.text == null || txtDailyWork!!.text.toString() == "")) {
                Toast.makeText(applicationContext, R.string.msgDailyValidationNm, Toast.LENGTH_LONG).show()
                chk = false
            }

            if (chk && (txtDailyTimes!!.text == null || txtDailyTimes!!.text.toString() == "")) {
                Toast.makeText(applicationContext, R.string.msgDailyValidationTime, Toast.LENGTH_LONG).show()
                chk = false
            }

            if (chk && (txtDailyAmount!!.text == null || txtDailyAmount!!.text.toString() == "")) {
                Toast.makeText(applicationContext, R.string.msgDailyValidationAmount, Toast.LENGTH_LONG).show()
                chk = false
            }

            // 포커스 클리어
            txtDailyWork!!.clearFocus()
            txtDailyTimes!!.clearFocus()
            txtDailyAmount!!.clearFocus()

            if (chk) {
                try {
                    val retVal: Long = when(modChk){
                        true -> { DatabaseAction.updateDailyColumn(DatabaseHelper(applicationContext).readableDatabase, choiceDay.toString(), txtDailyWork!!.text.toString(), txtDailyTimes!!.text.toString(), Integer.parseInt(txtDailyAmount!!.text.toString().replace(",".toRegex(), "")).toLong())}
                        else -> { DatabaseAction.insertDailyColumn(DatabaseHelper(applicationContext).readableDatabase, choiceDay.toString(), txtDailyWork!!.text.toString(), txtDailyTimes!!.text.toString(), Integer.parseInt(txtDailyAmount!!.text.toString().replace(",".toRegex(), "")).toLong())}
                    }

                    if (retVal == 0L) {
                        Toast.makeText(applicationContext, "FAIL", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, "SUCCESS", Toast.LENGTH_SHORT).show()

                        // 월 근무 시간 및 근무 금액 조회
                        getTotalInfoToMonth(chooseYear, chooseMonth)

                        // 키보드 숨기기
                        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputManager.hideSoftInputFromWindow(txtDailyAmount!!.windowToken, 0)
                    }

                } catch (e: SQLiteException) {
                    Toast.makeText(applicationContext, "Database unavailable btnSave onClick", Toast.LENGTH_SHORT).show()
                }

            }
        }

        // 세팅 버튼 클릭
        btnSetting.setOnClickListener { v ->
            settingView = layoutInflater.inflate(R.layout.activity_setting, null)

            // 구분 라디오 버튼
            val radioHour = settingView!!.findViewById<RadioButton>(R.id.radioHour)
            val radioMinute = settingView!!.findViewById<RadioButton>(R.id.radioMinute)

            // 기준 시간 / 금액
            val txtBaseTime = settingView!!.findViewById<EditText>(R.id.txtBaseTime)
            val txtBaseAmt = settingView!!.findViewById<EditText>(R.id.txtBaseAmt)

            // 월 기준일
            val txtBaseDayOfMonth = settingView!!.findViewById<EditText>(R.id.txtBaseDayOfMonth)

            if (strBaseTimeSection != null && strBaseTimeSection == "HOUR") {
                radioHour.isChecked = true
                radioMinute.isChecked = false
            } else if (strBaseTimeSection != null && strBaseTimeSection == "MINUTE") {
                radioHour.isChecked = false
                radioMinute.isChecked = true
            }

            txtBaseTime.setText(strBaseTime)   // 기준 시간 값 세팅
            txtBaseAmt.setText(StringUtil.convertNumberToComma(intBaseAmt.toString()))    // 기준 금액 값 세팅
            txtBaseDayOfMonth.setText(strBaseDayOfMonth)   // 기준일 값 세팅
            setBaseDayOfMonthInfo(Integer.valueOf(strBaseDayOfMonth!!))  // 기준일 예시 세팅

            // dialog 세팅
            val dialog = AlertDialog.Builder(v.context)
                .setTitle(R.string.settingTitle)
                .setView(settingView)
                .setMessage(R.string.settingMessage)
                .setPositiveButton(R.string.buttonSave, null)
                .setNegativeButton(R.string.buttonCancel, null).create()

            dialog.setOnShowListener { dialog ->
                val saveButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

                // 기본 세팅 저장 버튼 클릭
                saveButton.setOnClickListener {
                    var chk = true

                    val timeSection: String

                    // 구분 라디오 버튼
                    val radioHour = settingView!!.findViewById<RadioButton>(R.id.radioHour)
                    val radioMinute = settingView!!.findViewById<RadioButton>(R.id.radioMinute)

                    // 기준 시간 / 금액
                    val txtBaseTime = settingView!!.findViewById<EditText>(R.id.txtBaseTime)
                    val txtBaseAmt = settingView!!.findViewById<EditText>(R.id.txtBaseAmt)
                    val txtBaseDayOfMonth = settingView!!.findViewById<EditText>(R.id.txtBaseDayOfMonth)

                    if (!radioHour.isChecked && !radioMinute.isChecked) {        // 구분 선택 체크
                        Toast.makeText(applicationContext, R.string.msgValidationSection, Toast.LENGTH_LONG).show()
                        chk = false
                    }

                    if (chk && (txtBaseTime.text == null || txtBaseTime.text.toString() == "")) {         // 시간 선택 체크
                        Toast.makeText(applicationContext, R.string.msgValidationTime, Toast.LENGTH_LONG).show()
                        chk = false
                    }

                    if (chk && (txtBaseAmt.text == null || txtBaseAmt.text.toString() == "")) {           // 금액 선택 체크
                        Toast.makeText(applicationContext, R.string.msgValidationAmount, Toast.LENGTH_LONG).show()
                        chk = false
                    }

                    // 월 기준일 비교를 위한 cast
                    val strBaseDayOfMonthVal = Integer.valueOf(txtBaseDayOfMonth.text.toString())

                    if (chk && (strBaseDayOfMonthVal < 1 || strBaseDayOfMonthVal > 31)) {
                        Toast.makeText(applicationContext, R.string.msgBaseDayOfMonth, Toast.LENGTH_LONG).show()
                        chk = false
                    }

                    timeSection = when(radioHour.isChecked){ true -> {"HOUR"} else -> {"MINUTE"} }

                    if (chk) {
                        try {
                            val retVal: Long = when(setModChk){
                                true -> {
                                    DatabaseAction.updateBaseColumn(DatabaseHelper(applicationContext).readableDatabase, timeSection, txtBaseTime.text.toString(), Integer.valueOf(txtBaseAmt.text.toString().replace(",".toRegex(), "")).toLong(), txtBaseDayOfMonth.text.toString())
                                }
                                else -> {
                                    DatabaseAction.insertBaseColumn(DatabaseHelper(applicationContext).readableDatabase, timeSection, txtBaseTime.text.toString(), Integer.valueOf(txtBaseAmt.text.toString().replace(",".toRegex(), "")).toLong(), txtBaseDayOfMonth.text.toString())
                                }
                            }

                            if (retVal == 0L) {
                                Toast.makeText(applicationContext, "FAIL", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(applicationContext, "SUCCESS", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }

                            // 월 근무 시간 및 근무 금액 조회
                            getTotalInfoToMonth(chooseYear, chooseMonth)

                            // 기본 세팅 시간 및 금액 불러오기
                            getBaseSettingInfo()

                            // 월, 일 시간 명 세팅 ( 시 or 분 )
                            setTimeSectionName()

                        } catch (e: SQLiteException) {
                            Toast.makeText(applicationContext, "Database unavailable btnSave onClick", Toast.LENGTH_SHORT).show()
                        }

                    }
                }

                // 취소
                cancelButton.setOnClickListener { dialog.dismiss() }

                val txtBaseDayOfMonth = settingView!!.findViewById<EditText>(R.id.txtBaseDayOfMonth)

                // 월 기준일 포커스 변경 시 설명 자동 세팅 및 입력 값 유효성 체크
                txtBaseDayOfMonth.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        val intVal = Integer.valueOf((v as TextView).text.toString())
                        if (intVal < 1 || intVal > 31) {
                            Toast.makeText(applicationContext, R.string.msgBaseDayOfMonth, Toast.LENGTH_SHORT).show()

                            val txtBaseTime = settingView!!.findViewById<EditText>(R.id.txtBaseTime)  // 기준 시간
                            val txtBaseAmt = settingView!!.findViewById<EditText>(R.id.txtBaseAmt)    // 기준 금액

                            (v as EditText).requestFocus()   // 기준일 포커스 세팅
                            txtBaseTime.isFocusable = false    // 기준 시간 포커스 제거
                            txtBaseAmt.isFocusable = false     // 기준 금액 포커스 제거
                        }
                        // ex 안내 세팅
                        setBaseDayOfMonthInfo(intVal)
                    }
                }
            }

            dialog.show()
        }

        // 통계 버튼 클릭
        btnStatistics.setOnClickListener {
            val statisticsIntent = Intent(applicationContext, StatisticsActivity::class.java)
            statisticsIntent.putExtra("strBaseTimeSection", strBaseTimeSection)
            statisticsIntent.putExtra("strBaseTime", strBaseTime)
            statisticsIntent.putExtra("intBaseAmt", intBaseAmt)
            statisticsIntent.putExtra("strBaseDayOfMonth", strBaseDayOfMonth)
            startActivity(statisticsIntent)
        }

        // 시간 대비 금액 자동 계산 ( 포커스 변경 시 )
        txtDailyTimes.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val douDailyTime = (if ((v as EditText).text.toString() == "") 0 else Integer.valueOf(v.text.toString())).toDouble()
                txtDailyAmount.setText(StringUtil.convertNumberToComma((douDailyTime / java.lang.Double.valueOf(strBaseTime!!) * intBaseAmt).toInt().toString()))
            }
        }
    }

    // 기본일 선택 시 날짜 예시 출력
    private fun setBaseDayOfMonthInfo(argBaseDay: Int) {
        val baseDayOfMonthInfo = settingView!!.findViewById<TextView>(R.id.baseDayOfMonthInfo)

        val startDate = StringUtil.getCalculatorDay(chooseYear, chooseMonth - 1, argBaseDay, -15, Calendar.DAY_OF_YEAR)
        val endDate = StringUtil.getCalculatorDay(chooseYear, chooseMonth - 1, argBaseDay, 15, Calendar.DAY_OF_YEAR)
        val strBaseDate = " ex) " + startDate.substring(4, 6) + resources.getString(R.string.month) + startDate.substring(6, 8) + resources.getString(R.string.day) + " ~ " + endDate.substring(4, 6) + resources.getString(R.string.month) + endDate.substring(6, 8) + resources.getString(R.string.day)
        baseDayOfMonthInfo.text = strBaseDate
    }

    /**
     * 상단 월 전체 근무 시간 및 금액 출력 처리
     * @param chooseYear
     * @param chooseMonth
     */
    private fun getTotalInfoToMonth(chooseYear: Int, chooseMonth: Int) {

        val strMonthTitle = chooseMonth.toString() + " " + resources.getString(R.string.month) + " "

        // 월별 근무시간 / 금액 타이틀 세팅
        monthTitle.text = strMonthTitle

        val startDate = StringUtil.getCalculatorDay(chooseYear, chooseMonth - 1, Integer.valueOf(strBaseDayOfMonth!!), -15, Calendar.DAY_OF_YEAR)
        val endDate = StringUtil.getCalculatorDay(chooseYear, chooseMonth - 1, Integer.valueOf(strBaseDayOfMonth!!), 15, Calendar.DAY_OF_YEAR)

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

                txtSumTimes.text = intSumTime.toString()
                txtSumAmount.text = StringUtil.convertNumberToComma(intSumAmt.toString())
            } else {
                txtSumTimes.text = ""
                txtSumAmount.text = ""
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
                    strBaseTime = cursor.getString(1)
                    intBaseAmt = cursor.getInt(2)
                    strBaseDayOfMonth = cursor.getString(3)
                }
                setModChk = true
            } else {
                strBaseTimeSection = "MINUTE"
                strBaseTime = "0"
                intBaseAmt = 0
                strBaseDayOfMonth = "15"
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
    private fun setTimeSectionName() {
        if (strBaseTimeSection != null && strBaseTimeSection == "HOUR") {
            timeComment1.setText(R.string.labelHour)   // 시
            timeComment2.setText(R.string.labelHour)   // 시
        } else if (strBaseTimeSection != null && strBaseTimeSection == "MINUTE") {
            timeComment1.setText(R.string.labelMinute) // 분
            timeComment2.setText(R.string.labelMinute) // 분
        } else {
            timeComment1.text = ""
            timeComment2.text = ""
        }
    }
}
