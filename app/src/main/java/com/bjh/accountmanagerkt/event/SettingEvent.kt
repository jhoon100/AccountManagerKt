package com.bjh.accountmanagerkt.event

import android.view.View

interface SettingEvent {
    fun onSave() : Boolean
    fun onCancel() : Boolean
    fun onFocus() : View.OnFocusChangeListener
}