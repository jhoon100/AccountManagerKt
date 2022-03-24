package com.bjh.accountmanagerkt.db

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.widget.Toast
import com.bjh.accountmanagerkt.util.StringUtil

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

    fun selectOneData(applicationContext: Context, choiceDay : String) : Map<String, String> {

        val retMap = mapOf<String, String>()

        try {
            val cursor = DatabaseHelper(applicationContext).readableDatabase.query(DatabaseColumns._TABLENAME1, arrayOf(DatabaseColumns.WORK_NM, DatabaseColumns.WORK_TIME, DatabaseColumns.WORK_AMOUNT), DatabaseColumns.WORK_DAY + " = ?", arrayOf(choiceDay), null, null, null)

            when(cursor.count > 0){
                true -> {
                    if (cursor.moveToFirst()) {
                        retMap[""]
                    }
                }
                else -> {

                }
            }

            cursor.close()
            DatabaseHelper(applicationContext).readableDatabase.close()

        } catch (e: SQLiteException) {
            Toast.makeText(applicationContext, "Database unavailable onSelectedDayChange", Toast.LENGTH_SHORT).show()
        }

        return retMap
    }
}