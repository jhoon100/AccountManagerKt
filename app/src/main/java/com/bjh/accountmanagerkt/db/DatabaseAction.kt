package com.bjh.accountmanagerkt.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

object DatabaseAction {

    /**
     * Base insert
     * @param db
     * @param timeSection
     * @param baseTime
     * @param baseAmount
     * @return
     */
    fun insertBaseColumn(
        db: SQLiteDatabase,
        timeSection: String,
        baseTime: String,
        baseAmount: Long,
        baseDayOfMonth: String
    ): Long {
        val values = ContentValues()
        values.put(DatabaseColumns.TIME_SECTION, timeSection)
        values.put(DatabaseColumns.BASE_TIME, baseTime)
        values.put(DatabaseColumns.BASE_AMOUNT, baseAmount)
        values.put(DatabaseColumns.BASE_DAY_OF_MONTH, baseDayOfMonth)
        return db.insert(DatabaseColumns._TABLENAME0, null, values)
    }

    /**
     * Base update
     * @param db
     * @param timeSection
     * @param baseTime
     * @param baseAmount
     * @return
     */
    fun updateBaseColumn(
        db: SQLiteDatabase,
        timeSection: String,
        baseTime: String,
        baseAmount: Long,
        baseDayOfMonth: String
    ): Long {
        val values = ContentValues()
        values.put(DatabaseColumns.TIME_SECTION, timeSection)
        values.put(DatabaseColumns.BASE_TIME, baseTime)
        values.put(DatabaseColumns.BASE_AMOUNT, baseAmount)
        values.put(DatabaseColumns.BASE_DAY_OF_MONTH, baseDayOfMonth)
        return db.update(DatabaseColumns._TABLENAME0, values, "_id = ?", arrayOf(Integer.toString(1))).toLong()
    }

    /**
     * Daily insert
     * @param db
     * @param workNm
     * @param workDay
     * @param workTime
     * @param workAmount
     * @return
     */
    fun insertDailyColumn(
        db: SQLiteDatabase,
        workDay: String,
        workNm: String,
        workTime: String,
        workAmount: Long
    ): Long {
        val values = ContentValues()
        values.put(DatabaseColumns.WORK_NM, workNm)
        values.put(DatabaseColumns.WORK_DAY, workDay)
        values.put(DatabaseColumns.WORK_TIME, workTime)
        values.put(DatabaseColumns.WORK_AMOUNT, workAmount)
        return db.insert(DatabaseColumns._TABLENAME1, null, values)
    }

    /**
     * Daily update
     * @param db
     * @param workNm
     * @param workDay
     * @param workTime
     * @param workAmount
     * @return
     */
    fun updateDailyColumn(
        db: SQLiteDatabase,
        workDay: String,
        workNm: String,
        workTime: String,
        workAmount: Long
    ): Long {
        val values = ContentValues()
        values.put(DatabaseColumns.WORK_NM, workNm)
        values.put(DatabaseColumns.WORK_TIME, workTime)
        values.put(DatabaseColumns.WORK_AMOUNT, workAmount)
        return db.update(DatabaseColumns._TABLENAME1, values, DatabaseColumns.WORK_DAY + " = ?", arrayOf(workDay)).toLong()
    }
}