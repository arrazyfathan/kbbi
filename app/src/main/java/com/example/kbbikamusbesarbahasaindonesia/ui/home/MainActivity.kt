package com.example.kbbikamusbesarbahasaindonesia.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import com.example.kbbikamusbesarbahasaindonesia.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var button: MaterialButton
    private lateinit var card: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.button)
        card = findViewById(R.id.card1)

        card.setOnClickListener {
            showMenu(it, R.menu.sample_menu)
        }


        button.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("This is dialog")
                .setMessage("Lorerm itsum dolro sit ama afas asfas")
                .setNeutralButton("Cancel") { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton("Yes") { dialog, which ->
                    dialog.cancel()
                }
                .setNegativeButton("Decline") { dialog, which ->
                    dialog.cancel()
                }
                .show()
        }

    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.show()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.sample_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.option_1 -> {
                Toast.makeText(this, "option 1 pressed", Toast.LENGTH_LONG).show()
            }
            R.id.option_2 -> {
                Toast.makeText(this, "option 2 pressed", Toast.LENGTH_LONG).show()

            }
            R.id.option_3 -> {
                Toast.makeText(this, "option 3 pressed", Toast.LENGTH_LONG).show()

            }
        }
        return true
    }
}