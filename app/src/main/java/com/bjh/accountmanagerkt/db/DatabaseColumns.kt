package com.bjh.accountmanagerkt.db

import android.provider.BaseColumns

class DatabaseColumns : BaseColumns {
    companion object {
        const val TIME_SECTION = "time_section"  /* 기준 시/분 구분 */
        const val BASE_TIME = "base_time"        /* 기준 시간 */
        const val BASE_AMOUNT = "base_amount"    /* 기준 금액 */
        const val BASE_DAY_OF_MONTH = "base_day_Of_Month"   /* 월 기준일 */

        const val WORK_DAY = "work_day"          /* 근무일자 */
        const val WORK_NM = "work_nm"            /* 근무명 */
        const val WORK_TIME = "work_time"        /* 근무시간 */
        const val WORK_AMOUNT = "work_amount"    /* 근무금액 */

        val _TABLENAME0 = "base_info"
        val _TABLENAME1 = "daily_info"

        val _CREATE0 = "create table " + _TABLENAME0 + " (" + BaseColumns._ID + " integer primary key autoincrement, " + TIME_SECTION + " text not null, " + BASE_TIME + " text not null, " + BASE_AMOUNT + " integer not null, " + BASE_DAY_OF_MONTH + " text not null)"
        val _CREATE1 = "create table " + _TABLENAME1 + " (" + BaseColumns._ID + " integer primary key autoincrement, " + WORK_NM + " text not null, " + WORK_DAY + " text not null, " + WORK_TIME + " text not null, " + WORK_AMOUNT + " integer not null)"
    }
}
