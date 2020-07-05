package com.example.transliterate

import android.content.Context
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.preferences.*

class SelectServerPreference :  AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences)
        val preferences = getSharedPreferences("database", Context.MODE_PRIVATE)
        val savedServer = preferences.getInt("preferredServer", 0)

        radioGroup.check(radioGroup.getChildAt(savedServer).id)

        radioGroup.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radio: RadioButton = findViewById(checkedId)
                val idx: Int = radioGroup.indexOfChild(radio)
                preferences.edit().apply {
                    putInt("preferredServer", idx)
                }.apply()


            })

    }
}