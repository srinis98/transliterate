package com.example.transliterate

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log.d
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    //    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = getSharedPreferences("database", Context.MODE_PRIVATE)
        val savedLanguage = preferences.getString("lastUsedLanguage", "hindi")


// TODO : create a dropdown menu
        val spinner: Spinner = findViewById(R.id.langSpinner)
        var langChoice: String = ""
        val languages = resources.getStringArray(R.array.lang_array)

        ArrayAdapter.createFromResource(
            this,
            R.array.lang_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            val spinnerPosition = adapter.getPosition(savedLanguage)
            spinner.setSelection(spinnerPosition)
            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    langChoice = languages[position]
                    preferences.edit().apply {
                        putString("lastUsedLanguage", langChoice)
                    }.apply()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }

// TODO : transliterate message : display in the output
        val textView: TextView = findViewById<TextView>(R.id.inputString)

        tranliterateButton.setOnClickListener {

            val inputText: String = inputString.text.toString().trim()
            val map: MutableMap<String, String> = mutableMapOf<String, String>()
            d("Srini", "transliterateButton was clicked, text entered: $inputText")
            progressBar.visibility = View.VISIBLE
            // make that call
            val callback = { resultString: String ->
                progressBar.visibility = View.INVISIBLE
                outputText.text = resultString
            }
            val listener = Response.ErrorListener {
                if (it.toString().contains("NoConnection")) { //
                    d("Srini", "no network in volley , It Wokred :')")
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("No Internet Connection")
                    builder.setMessage("This app requires internet for transliteration. ")
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                    dialog.window?.setLayout(800, 450);
                } else {
                    outputText.text = it.toString()//"Invalid Input"
                }
            }
            val savedServerIntValue = preferences.getInt("preferredServer", 0)
            val server = EndPoint.values().find { it.value == savedServerIntValue }
            val queue = VolleySingleton.getInstance(this)
            if (server!!.name == EndPoint.GOOGLE.name)
                google(inputText, langChoice, callback, listener, queue, server!!)
            else
                quillVarnamCall(inputText, langChoice, callback, listener, queue, server!!)
        }

        textClearButton.setOnClickListener {
            inputString.text = null
            outputText.text = null
            d("Srini", "textClearButton was clicked")


        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        textCopyButton.setOnClickListener {
            val textToCopy = outputText.text
            val clipData = ClipData.newPlainText("text", textToCopy)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
        }
        pasteButton.setOnClickListener {
            var clipboardString = clipboard.primaryClip?.getItemAt(0)?.text
            inputString.setText(clipboardString)
        }

        floatingActionButton.setOnClickListener {
            startActivity(Intent(this, SelectServerPreference::class.java))

        }
    }
}


