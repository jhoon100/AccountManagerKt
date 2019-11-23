package com.bjh.accountmanagerkt.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

object StringUtil {

    /**
     * 현재 년도 반환
     * @return
     */
    val curYear: String
        get() {
            val format = SimpleDateFormat("yyyy", Locale.getDefault())
            return format.format(Calendar.getInstance().time)
        }

    /**
     * 현재 월 반환
     * @return
     */
    val curMonth: String
        get() {
            val format = SimpleDateFormat("MM", Locale.getDefault())
            return format.format(Calendar.getInstance().time)
        }

    /**
     * 현재 일 반환
     * @return
     */
    val curDay: String
        get() {
            val format = SimpleDateFormat("dd", Locale.getDefault())
            return format.format(Calendar.getInstance().time)
        }

    /**
     * 숫자 3자리 마다 콤마 추가 (#,###)
     * @param strArgValue
     * @return
     */
    fun convertNumberToComma(strArgValue: String): String {
        val amount = java.lang.Double.parseDouble(strArgValue)
        val formatter = DecimalFormat("#,###")
        return formatter.format(amount)
    }

    /**
     * 날짜포맷 yyyyMMdd 형식으로 리턴
     * @param strArgValue
     * @return
     */
    fun getDateYYYYMMDD(strArgValue: Long): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date(strArgValue))
    }

    /**
     * 날짜포맷 yyyy 형식으로 리턴
     * @param strArgValue
     * @return
     */
    fun getDateYYYY(strArgValue: Long): String {
        val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
        return sdf.format(Date(strArgValue))
    }

    /**
     * 날짜 계산 후 yyyyMMdd 형식으로 리턴
     * argAmount 값이 + 일 경우 날짜 더하기 - 일 경우 날짜 빼기
     * @param argYear
     * @param argMonth
     * @param argDay
     * @param argAmount
     * @param argCalSection
     * @return
     */
    fun getCalculatorDay(
        argYear: Int,
        argMonth: Int,
        argDay: Int,
        argAmount: Int,
        argCalSection: Int
    ): String {

        val cal = GregorianCalendar(argYear, argMonth, argDay)

        cal.add(argCalSection, argAmount)

        val sf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        return sf.format(cal.time)
    }
}