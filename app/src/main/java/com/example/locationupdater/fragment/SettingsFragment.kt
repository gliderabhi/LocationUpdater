package com.example.locationupdater.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.locationupdater.R
import com.example.locationupdater.activities.HelpActivity
import com.example.locationupdater.firebase.FirebaseTransactions.clearAllGeoFences

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment(){

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var radiusEdit : EditText
    private var radius = 0f
    private val TAG = "SettingsFragment"
    private lateinit var checkImage : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


    }

    override fun onDestroy() {
        super.onDestroy()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Inflate the layout for this fragment
        val sharedPreferences = context?.getSharedPreferences(getString(R.string.prefName), Context.MODE_PRIVATE)
        radius = sharedPreferences!!.getFloat(getString(R.string.radius), 10f)

        val radiusEdit = view.findViewById<EditText>(R.id.radiusEdit)
        radiusEdit.hint = radius.toString()
        radiusEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (radiusEdit.text.isNotEmpty()) {
                    radius = radiusEdit.text.toString().toFloat()
                    sharedPreferences.edit().putFloat(getString(R.string.radius), radius).apply()
                    Toast.makeText(context, "Radius set as $radius", Toast.LENGTH_SHORT).show()
                }
            }
        })

        val clearGeo = view.findViewById<TextView>(R.id.clearGeoFence)
        clearGeo?.setOnClickListener{
            Toast.makeText(context, "clicked clear", Toast.LENGTH_SHORT).show()
            context?.let { it1 -> clearAllGeoFences(it1) }
        }

        val showHints = view.findViewById<TextView>(R.id.showHint)
        showHints?.setOnClickListener{
            with(sharedPreferences.edit()) {
                putBoolean(getString(R.string.isHintShown), false)
                apply()
            }
            startActivity(Intent(context,HelpActivity::class.java))
        }
    }


    companion object {
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}