package com.example.taller1_migueluribe

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CountryDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country_detail)
        // Acá recupero los detalles del país seleccionado de los extras del Intent.
        val countryName = intent.getStringExtra("countryName")
        val countryCapital = intent.getStringExtra("countryCapital")
        val countryNameInt = intent.getStringExtra("countryNameInt")
        val countrySigla = intent.getStringExtra("countrySigla")
        // Configurar los TextViews del .xml para mostrar los detalles del país del .json.
        val tvCountryName = findViewById<TextView>(R.id.tvCountryName)
        val tvCountryCapital = findViewById<TextView>(R.id.tvCountryCapital)
        val tvCountryNameInt = findViewById<TextView>(R.id.tvCountryNameInt)
        val tvCountrySigla = findViewById<TextView>(R.id.tvCountrySigla)
        // Finalmente Establezco dichos valores en los TextViews.
        tvCountryName.text = countryName
        tvCountryCapital.text = "Capital: $countryCapital"
        tvCountryNameInt.text = "Nombre Internacional: $countryNameInt"
        tvCountrySigla.text = "Sigla: $countrySigla"
    }
}