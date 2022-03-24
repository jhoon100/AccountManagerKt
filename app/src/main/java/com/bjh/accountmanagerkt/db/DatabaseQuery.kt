package com.bjh.accountmanagerkt.db

object DatabaseQuery {
    /**
     * 선택 일자에 대한 근무 내용 조회
     */
    fun getDetailInfo() : String {
        return "SELECT work_nm " +
                "    , work_time " +
                "    , work_amount " +
                "FROM  daily_info " +
                "WHERE work_day = ?"
    }
}