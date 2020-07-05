package com.example.test_kotlin

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject


enum class EndPoint(val value: Int) {
    GOOGLE(1),
    VARNAM(2),
    QUILL(0)
}

fun quillVarnamCall(
    inputText: String,
    langChoice: String,
    callBack: (String) -> Any,
    errorListener: Response.ErrorListener,
    queue: VolleySingleton,
    endPoint: EndPoint
) {
    val map: MutableMap<String, String> = mutableMapOf<String, String>()
    Log.d("Srini", "transliterateButton was clicked, text entered: $inputText")

    val inputList = inputText.split(" ")
    inputList.forEach {
        Log.d("Srini", "iterator word is $it")
        val url =
            when (endPoint) {
                EndPoint.VARNAM -> createUrlVarnam(inputString = it, language = langChoice)
                EndPoint.QUILL -> createUrlQuill(inputString = it, language = langChoice)
                EndPoint.GOOGLE -> throw RuntimeException("unsupported endpoint")
            }
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                // Process the json
                try {
                    Log.d("Srini", "in the response body for word $it")
                    val wordResponse: String =
                        when (endPoint) {
                            EndPoint.VARNAM -> getOutputFromResponseVarnam(response)
                            EndPoint.QUILL -> getOutputFromResponseQuill(response)
                            EndPoint.GOOGLE -> throw RuntimeException("unsupported endpoint")
                        }
                    val word: String = it
                    map[word] = wordResponse.toString()
                    if (map.size == inputList.size) {
                        Log.d("Srini", "length Equal to input list length $map")
                        val outputString = inputList.fold("", { acc, s -> "$acc ${map[s]}" })
                        //  progressBar.visibility = View.INVISIBLE
                        callBack(outputString)
                    }

                } catch (e: Exception) {
                    callBack(e.toString())
                }

            }, errorListener
        )
        request.retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 1f)
        queue.addToRequestQueue(request)
    }
}

fun getOutputFromResponseVarnam(jsonObject: JSONObject): String {
    return jsonObject.getJSONArray("result").getString(0).trim('[', ']', '"')
}

fun getOutputFromResponseQuill(jsonObject: JSONObject): String {
    return jsonObject.getJSONArray("twords").getJSONObject(0).getJSONArray("options")
        .getString(0)
}

fun createUrlQuill(inputString: String, language: String): String =
    "http://xlit.quillpad.in/quillpad_backend2/processWordJSON?lang=$language&inString=$inputString"

val languageMapper = mapOf(
    "hi" to "hindi",
    "ta" to "tamil",
    "te" to "telugu",
    "bn" to "bengali",
    "gu" to "gujarati",
    "mr" to "marathi",
    "kn" to "kannada",
    "ml" to "malayalam",
    "pa" to "punjabi",
    "ne" to "nepali"
)

fun createUrlVarnam(inputString: String, language: String): String =
    "https://api.varnamproject.com/tl/${languageMapper[language]}/$inputString"

fun createUrlGoogle(inputString: String, language: String): String =
    "https://inputtools.google.com/request?text=$inputString&itc=${languageMapper[language]}-t-i0-und"

fun google(
    inputText: String,
    langChoice: String,
    callBack: (String) -> Any,
    errorListener: Response.ErrorListener,
    queue: VolleySingleton,
    endPoint: EndPoint
) {
    val url = createUrlGoogle(inputString = inputText, language = langChoice)
    // Volley post request with parameters
    val request = JsonArrayRequest(
        Request.Method.GET, url, null,
        Response.Listener { response ->
            // Process the json
            try {
                callBack(response.getJSONArray(1).getJSONArray(0).getJSONArray(1).getString(0))

            } catch (e: Exception) {
                callBack(e.toString())
            }
        }, errorListener
    )
    request.retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 1f)
    queue.addToRequestQueue(request)
}