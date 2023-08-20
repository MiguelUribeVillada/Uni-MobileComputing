package com.example.taller1_migueluribe

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.taller1_migueluribe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tictactoe.setOnClickListener { // 1. Para cuando se oprima el botón de itctactoe se redirija a su actividad correspondiente.
            startActivity(Intent(baseContext, TicTacToeActivity::class.java)) }
        binding.countries.setOnClickListener { // 2. Para cuando se oprima el botón de countries se redirija a su actividad correspondiente.
            startActivity(Intent(baseContext, CountriesActivity::class.java)) }
        val listLanguages = arrayOf("Español", "Ingles", "Japones", "Italiano", "Portugues") // Para darle valores al spinner.
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, listLanguages) // Segun entiendo se necesita un adaptador para el spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Este es como el diseño de cuando se selecciona el spinner.
        binding.languages.adapter = adapter // Finalmente acá es donde le asigno el adaptador a mi spinner
        binding.greet.setOnClickListener { // 3. Para cuando se oprima el botón de greet se redirija a su actividad correspondiente.
            val selectedLanguage = binding.languages.selectedItem.toString() // Obtener el idioma seleccionado del Spinner
            val intent = Intent(baseContext, GreetActivity::class.java) // Aquí creé un intent para abrir la actividad GreetActivity y pasar el idioma que seleccionó previamente el usuario
            intent.putExtra("selectedLanguage", selectedLanguage)
            startActivity(intent)// Finalmente inicio acá a la actividad GreetActivity }
        }
    }
}