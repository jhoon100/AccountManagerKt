package com.bjh.accountmanagerkt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bjh.accountmanagerkt.databinding.ActivitySettingBinding

import com.bjh.accountmanagerkt.event.SettingEventImpl
import com.bjh.accountmanagerkt.util.StringUtil

class SettingActivity : AppCompatActivity() {

    private lateinit var settingBinding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingBinding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(settingBinding.root)

        val strBaseTimeSection = intent.getStringExtra("strBaseTimeSection") // 시 / 분 구분
        val strBaseTime = intent.getIntExtra("strBaseTime", 0)    // 기본 근무 시간
        val intBaseAmt = intent.getIntExtra("intBaseAmt", 0)      // 기본 근무 금액
        val strBaseDayOfMonth = intent.getIntExtra("strBaseDayOfMonth", 15)   // 월 기준일
        val setModChk = intent.getBooleanExtra("setModChk", false) // 기본 세팅 등록 / 수정 체크

        // 기준 시/분 라디오 버튼 선택
        if (strBaseTimeSection != null && strBaseTimeSection == "HOUR") {
            settingBinding.radioHour.isChecked = true
            settingBinding.radioMinute.isChecked = false
        } else if (strBaseTimeSection != null && strBaseTimeSection == "MINUTE") {
            settingBinding.radioHour.isChecked = false
            settingBinding.radioMinute.isChecked = true
        }

        settingBinding.txtBaseTime.setText(strBaseTime.toString())   // 기준 시간 값 세팅
        settingBinding.txtBaseAmt.setText(StringUtil.convertNumberToComma(intBaseAmt.toString()))    // 기준 금액 값 세팅
        settingBinding.txtBaseDayOfMonth.setText(strBaseDayOfMonth.toString())   // 기준일 값 세팅

        // setting 버튼 클릭 객체 생성
        val settingEvent = SettingEventImpl(settingBinding, applicationContext, setModChk)

        // 기준일 예시 입력
        val baseDay = if(settingBinding.txtBaseDayOfMonth.text.toString() != "") {
            Integer.parseInt(settingBinding.txtBaseDayOfMonth.text.toString())
        } else {
            15
        }

        settingBinding.baseDayOfMonthInfo.text = settingEvent.getExampleText(baseDay)

        // 저장 버튼 클릭
        settingBinding.btnSave.setOnClickListener {
            if(settingEvent.onSave()) finish() // 정상 저장 체크 후 setting 창 닫기
        }

        // 취소 버튼 클릭
        settingBinding.btnCancel.setOnClickListener {
            finish()
        }

        // 월 기준일 포커스 변경 시 설명 자동 세팅 및 입력 값 유효성 체크
        settingBinding.txtBaseDayOfMonth.onFocusChangeListener = settingEvent.onFocus()
    }
}
