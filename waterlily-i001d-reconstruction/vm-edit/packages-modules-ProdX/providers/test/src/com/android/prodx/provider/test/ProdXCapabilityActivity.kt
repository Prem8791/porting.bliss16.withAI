package com.android.prodx.provider.test

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView

class ProdXCapabilityActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        val pagePadding = (24 * density).toInt()
        val itemSpacing = (16 * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pagePadding, pagePadding, pagePadding, pagePadding)
        }

        val title = TextView(this).apply {
            text = "ProdX capability test"
            textSize = 24f
        }
        root.addView(title, matchWidth())

        val explanation = TextView(this).apply {
            text = "Select a safe no-op capability, then tap Go. No real device action is performed."
            textSize = 16f
        }
        root.addView(explanation, matchWidth(itemSpacing))

        val capabilities = NoOpCapabilities.all
        val spinner = Spinner(this).apply {
            contentDescription = "ProdX capability"
            adapter = ArrayAdapter(
                this@ProdXCapabilityActivity,
                android.R.layout.simple_spinner_item,
                capabilities.map { "${it.name} — ${it.capabilityId}" },
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        }
        root.addView(spinner, matchWidth(itemSpacing))

        val result = TextView(this).apply {
            text = "Result will appear here."
            textSize = 18f
            setTextIsSelectable(true)
        }

        val go = Button(this).apply {
            text = "Go"
            setOnClickListener {
                val selected = capabilities.getOrNull(spinner.selectedItemPosition)
                result.text = if (selected == null) {
                    "FAIL — select a capability first."
                } else {
                    "${selected.name}\n${selected.capabilityId}\n${NoOpCapabilities.execute(selected)}"
                }
            }
        }
        root.addView(go, matchWidth(itemSpacing))
        root.addView(result, matchWidth(itemSpacing))

        setContentView(root)
    }

    private fun matchWidth(topMargin: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply {
            this.topMargin = topMargin
        }
}
