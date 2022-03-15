package com.bjh.accountmanagerkt.event

import android.content.Context
import android.database.sqlite.SQLiteException
import android.view.View
import android.widget.Toast
import com.bjh.accountmanagerkt.R
import com.bjh.accountmanagerkt.databinding.ActivitySettingBinding
import com.bjh.accountmanagerkt.db.DatabaseAction
import com.bjh.accountmanagerkt.db.DatabaseHelper
import com.bjh.accountmanagerkt.util.StringUtil
import java.util.*

class SettingEventImpl(private val settingBinding: ActivitySettingBinding
                       , private val applicationContext: Context
                       , private val setModChk : Boolean) : SettingEvent {

    /**
     * setting 화면 저장 버튼 클릭
     */
    override fun onSave() : Boolean {
        var chk = true

        // 구분 선택 체크
        if (!settingBinding.radioHour.isChecked && !settingBinding.radioMinute.isChecked) {
            Toast.makeText(applicationContext, R.string.msgValidationSection, Toast.LENGTH_LONG).show()
            chk = false
        }

        // 시간 선택 체크
        if (chk && (settingBinding.txtBaseTime.text.toString() == "" || settingBinding.txtBaseTime.text.toString() == "0")) {
            Toast.makeText(applicationContext, R.string.msgValidationTime, Toast.LENGTH_LONG).show()
            chk = false
        }

        // 금액 선택 체크
        if (chk && (settingBinding.txtBaseAmt.text.toString() == "" || settingBinding.txtBaseAmt.text.toString() == "0")) {
            Toast.makeText(applicationContext, R.string.msgValidationAmount, Toast.LENGTH_LONG).show()
            chk = false
        }

        // 월 기준일 비교를 위한 cast
        val strBaseDayOfMonthVal = if(settingBinding.txtBaseDayOfMonth.text.toString() == ""){
            0
        } else {
            Integer.valueOf(settingBinding.txtBaseDayOfMonth.text.toString())
        }

        if (chk && (strBaseDayOfMonthVal < 1 || strBaseDayOfMonthVal > 31)) {
            Toast.makeText(applicationContext, R.string.msgBaseDayOfMonth, Toast.LENGTH_LONG).show()
            chk = false
        }

        val timeSection: String = when(settingBinding.radioHour.isChecked){ true -> {"HOUR"} else -> {"MINUTE"} }

        if (chk) {
            try {
                val retVal: Long = when(setModChk){
                    true -> {
                        DatabaseAction.updateBaseColumn(DatabaseHelper(applicationContext).readableDatabase, timeSection, settingBinding.txtBaseTime.text.toString(), Integer.valueOf(settingBinding.txtBaseAmt.text.toString().replace(",".toRegex(), "")).toLong(), settingBinding.txtBaseDayOfMonth.text.toString())
                    }
                    else -> {
                        DatabaseAction.insertBaseColumn(DatabaseHelper(applicationContext).readableDatabase, timeSection, settingBinding.txtBaseTime.text.toString(), Integer.valueOf(settingBinding.txtBaseAmt.text.toString().replace(",".toRegex(), "")).toLong(), settingBinding.txtBaseDayOfMonth.text.toString())
                    }
                }

                if (retVal == 0L) {
                    Toast.makeText(applicationContext, "FAIL", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "SUCCESS", Toast.LENGTH_SHORT).show()
                }

            } catch (e: SQLiteException) {
                Toast.makeText(applicationContext, "Database unavailable btnSave onClick", Toast.LENGTH_SHORT).show()
            }
        }

        return chk
    }

    /**
     * setting 화면 취소 버튼 클릭
     */
    override fun onCancel() : Boolean {
        return true
    }

    /**
     * 월 기준일 포커스 변경 시 설명 자동 세팅 및 입력 값 유효성 체크
     */
    override fun onFocus() : View.OnFocusChangeListener {
        return View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val strBaseDay = settingBinding.txtBaseDayOfMonth.text.toString()    // 기준일
                if(strBaseDay != ""){
                    val intVal = Integer.valueOf(strBaseDay)    // 형변환 ( 문자 -> 숫자 )
                    if (intVal < 1 || intVal > 31) {
                        Toast.makeText(applicationContext, R.string.msgBaseDayOfMonth, Toast.LENGTH_SHORT).show()

                        settingBinding.txtBaseDayOfMonth.isFocusable = true    // 기준일 포커스 세팅
                        settingBinding.txtBaseTime.isFocusable = false    // 기준 시간 포커스 제거
                        settingBinding.txtBaseAmt.isFocusable = false     // 기준 금액 포커스 제거
                    }
                    settingBinding.baseDayOfMonthInfo.text = getExampleText(intVal) // ex 안내 세팅
                }
            }
        }
    }

    /**
     * 월 기준일 시작 ~ 종료일 예시 출력
     */
    fun getExampleText(argVal: Int): String {
        val charMonth = applicationContext.getString(R.string.month)
        val charDay = applicationContext.getString(R.string.day)

        val startDate = StringUtil.getCalculatorDay(2022, 3, argVal, -15, Calendar.DAY_OF_YEAR)
        val endDate = StringUtil.getCalculatorDay(2022, 3, argVal, 15, Calendar.DAY_OF_YEAR)

        return " ex) " + startDate.substring(4, 6) + charMonth + startDate.substring(6, 8
        ) + charDay + " ~ " + endDate.substring(4, 6) + charMonth + endDate.substring(6, 8) + charDay
    }
}