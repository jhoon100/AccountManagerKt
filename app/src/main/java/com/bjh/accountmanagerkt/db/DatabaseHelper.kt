package com.bjh.accountmanagerkt.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "myAccountManager"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        updateMyDatabase(db, 0, DATABASE_VERSION)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        updateMyDatabase(db, oldVersion, newVersion)
    }

    private fun updateMyDatabase(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        // 버전 1일경우 신규 테이블 생성
        if (oldVersion < 1) {
            db.execSQL(DatabaseColumns._CREATE0)
            db.execSQL(DatabaseColumns._CREATE1)
        }

        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseColumns._TABLENAME0)
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseColumns._TABLENAME1)

            db.execSQL(DatabaseColumns._CREATE0)
            db.execSQL(DatabaseColumns._CREATE1)

        }
    }
}