package com.bjh.accountmanagerkt

import android.app.DatePickerDialog
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.bjh.accountmanagerkt.adapter.StatRecyclerAdapter
import com.bjh.accountmanagerkt.databinding.ActivityStatisticsBinding
import com.bjh.accountmanagerkt.db.DatabaseColumns
import com.bjh.accountmanagerkt.db.DatabaseHelper
import com.bjh.accountmanagerkt.util.StringUtil

import java.util.ArrayList
import java.util.HashMap

class StatisticsActivity : AppCompatActivity() {

    private var databaseHelper: SQLiteOpenHelper? = null

    private var strRadioSrhChoose: String? = null   // 조회 기준년월 값 세팅
    private var strBaseTimeSection: String? = null  // 시 / 분 구분

    private lateinit var statBinding: ActivityStatisticsBinding // activity statistics view binding

    // 통계 조회 시작일 선택
    private val startListener =
        DatePickerDialog.OnDateSetListener { _, argYear, argMonthOfYear, argDayOfMonth ->
            val strStartDate = argYear.toString() + "-" + (if (argMonthOfYear + 1 < 10) "0" + (argMonthOfYear + 1) else argMonthOfYear + 1) + "-" + if (argDayOfMonth < 10) "0$argDayOfMonth" else argDayOfMonth
            statBinding.txtSrhStartDate.setText(strStartDate)
        }

    // 통계 조회 종료일 선택
    private val endListener =
        DatePickerDialog.OnDateSetListener { _, argYear, argMonthOfYear, argDayOfMonth ->
            val strEndDate =
                argYear.toString() + "-" + (if (argMonthOfYear + 1 < 10) "0" + (argMonthOfYear + 1) else argMonthOfYear + 1) + "-" + if (argDayOfMonth < 10) "0$argDayOfMonth" else argDayOfMonth
            statBinding.txtSrhEndDate.setText(strEndDate)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statBinding = ActivityStatisticsBinding.inflate(layoutInflater)
        val view = statBinding.root
        setContentView(view)

        strBaseTimeSection = intent.getStringExtra("strBaseTimeSection")

        databaseHelper = DatabaseHelper(applicationContext)

        // 날짜 입력항목 선택시 키보드 안올라오게 세팅
        statBinding.txtSrhStartDate.inputType = 0
        statBinding.txtSrhEndDate.inputType = 0

        // 기준년월 default 세팅
        searchRadioButtonClickChk()

        // 기준년월 년 선택 시 값 세팅
        statBinding.radioSrhYear.setOnClickListener { searchRadioButtonClickChk() }

        // 기준년월 월 선택 시 값 세팅
        statBinding.radioSrhMonth.setOnClickListener { searchRadioButtonClickChk() }

        // 통계 조회 시작일 선택 시 달력 팝업 실행 후 선택하면 선택된 데이터 입력 처리
        statBinding.txtSrhStartDate.setOnClickListener {
            val chooseDate =
                statBinding.txtSrhStartDate.text.toString().split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val datePickerDialog = DatePickerDialog(
                this@StatisticsActivity,
                startListener,
                Integer.valueOf(chooseDate[0]),
                Integer.valueOf(chooseDate[1]) - 1,
                Integer.valueOf(chooseDate[2])
            )
            datePickerDialog.show()
        }

        // 통계 조회 종료일 선택 시 달력 팝업 실행 후 선택하면 선택된 데이터 입력 처리
        statBinding.txtSrhEndDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this@StatisticsActivity,
                endListener,
                Integer.valueOf(StringUtil.curYear),
                Integer.valueOf(StringUtil.curMonth) - 1,
                Integer.valueOf(StringUtil.curDay)
            )
            datePickerDialog.show()
        }

        // 조회버튼 클릭
        statBinding.btnSearch.setOnClickListener {
            val strShowColQuery: String
            val strGroupByQuery: String

            val strStartDate = statBinding.txtSrhStartDate.text.toString().replace("-".toRegex(), "")
            val strEndDate = statBinding.txtSrhEndDate.text.toString().replace("-".toRegex(), "")

            if (strRadioSrhChoose == "YEAR") {   // 년별
                strShowColQuery =
                    "SUBSTR(" + DatabaseColumns.WORK_DAY + ", 1, 4) AS WORK_YEAR, SUM(" + DatabaseColumns.WORK_TIME + ") AS WORK_TIME, SUM(" + DatabaseColumns.WORK_AMOUNT + ") AS WORK_AMOUNT"
                strGroupByQuery = "GROUP BY SUBSTR(" + DatabaseColumns.WORK_DAY + ", 1, 4)"
            } else {    // 월별
                strShowColQuery =
                    "SUBSTR(" + DatabaseColumns.WORK_DAY + ", 1, 6) AS WORK_YEAR, SUM(" + DatabaseColumns.WORK_TIME + ") AS WORK_TIME, SUM(" + DatabaseColumns.WORK_AMOUNT + ") AS WORK_AMOUNT"
                strGroupByQuery = "GROUP BY SUBSTR(" + DatabaseColumns.WORK_DAY + ", 1, 6)"
            }

            val strQuery =
                "SELECT " + strShowColQuery + " FROM " + DatabaseColumns._TABLENAME1 + " WHERE " + DatabaseColumns.WORK_DAY + " >= ? and " + DatabaseColumns.WORK_DAY + " <= ? " + strGroupByQuery

            val staticsList = ArrayList<HashMap<String, String>>()

            try {
                val cursor = DatabaseHelper(applicationContext).readableDatabase.rawQuery(strQuery, arrayOf(strStartDate, strEndDate))

                if (cursor.count > 0) {

                    for (i in 0 until cursor.count) {
                        if (cursor.moveToNext()) {

                            val mapListData = HashMap<String, String>()

                            if (strRadioSrhChoose == "YEAR") {
                                mapListData["staticsDate"] =
                                    cursor.getInt(0).toString() + applicationContext.getString(R.string.year)
                            } else {
                                mapListData["staticsDate"] = cursor.getString(0).substring(0, 4) + applicationContext.getString(R.string.year) + " " + cursor.getString(0).substring(4, 6) + applicationContext.getString(R.string.month)
                            }

                            val strTimeSectNm: String = when(strBaseTimeSection == "HOUR"){
                                true -> applicationContext.getString(R.string.labelTime)
                                else -> applicationContext.getString(R.string.labelMinute)
                            }

                            mapListData["staticsTime"] = cursor.getInt(1).toString() + "" + strTimeSectNm
                            mapListData["staticsAmount"] = StringUtil.convertNumberToComma(cursor.getString(2)) + applicationContext.getString(
                                    R.string.labelWon
                                )

                            staticsList.add(mapListData)
                        }
                    }
                }

                cursor.close()
                DatabaseHelper(applicationContext).readableDatabase.close()
            } catch (ex: SQLiteException) {
                Toast.makeText(applicationContext, "Database unavailable btnSearch.setOnClickListener()", Toast.LENGTH_SHORT).show()
            }

            val recyclerView = findViewById<RecyclerView>(R.id.statRecyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this@StatisticsActivity)

            val adapter = StatRecyclerAdapter(staticsList)
            recyclerView.adapter = adapter
        }

    }

    // 기준년월 선택 체크
    private fun searchRadioButtonClickChk() {
        // 조회 시작일
        val strPreDate: String

        // 현재 년월일 yyyy-MM-dd
        val currentDate =
            StringUtil.curYear + "-" + StringUtil.curMonth + "-" + StringUtil.curDay

        if (statBinding.radioSrhYear.isChecked) {
            strPreDate = (Integer.valueOf(StringUtil.curYear) - 3).toString() + "-01-01"
            strRadioSrhChoose = "YEAR"
            statBinding.txtSrhStartDate.setText(strPreDate)
            statBinding.txtSrhEndDate.setText(currentDate)
        } else if (statBinding.radioSrhMonth.isChecked) {
            strPreDate = (Integer.valueOf(StringUtil.curYear) - 1).toString() + "-01-01"
            strRadioSrhChoose = "MONTH"
            statBinding.txtSrhStartDate.setText(strPreDate)
            statBinding.txtSrhEndDate.setText(currentDate)
        }
    }
}