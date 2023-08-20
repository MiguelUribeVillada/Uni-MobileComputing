package com.example.taller1_migueluribe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taller1_migueluribe.databinding.GreetBinding

class GreetActivity : AppCompatActivity() {
    private lateinit var binding: GreetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GreetBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // Acá obtengo al idioma seleccionado del intent.
        val selectedLanguage = intent.getStringExtra("selectedLanguage")
        // Acá obtengo un saludo y una imagen correspondiente al idioma seleccionado por el usuario
        // (llamado a las funciones que creé para estas labores y que se encuentran más abajo)
        val greeting = getGreetingForLanguage(selectedLanguage)
        val imageResource = getImageResourceForLanguage(selectedLanguage)
        // Mostrar el saludo y la imagen en la vista (xml) del greet.
        binding.textView.text = greeting
        binding.imageView.setImageResource(imageResource)
    }
    // Acá dependiendo del lenguaje seleccionado saludo al usuaio en dicho lenguaje.
    private fun getGreetingForLanguage(language: String?): String {
        return when (language) {
            "Español" -> "¡Hola!"
            "Ingles" -> "Hello!"
            "Japones" -> "こんにちは！"
            "Italiano" -> "Ciao!"
            "Portugues" -> "Olá!"
            else -> "Hola!" // Saludo que puse predeterminado.
        }
    }
    // Acá dependiendo del idioma que se seleccionara previamente por parte del usuario,
    // pone en greet.xml la imagen correspondiente (En mi caso son banderas de un lugar que hablan ese idioma).
    private fun getImageResourceForLanguage(language: String?): Int {
        return when (language) {
            "Español" -> R.drawable.flag_es
            "Ingles" -> R.drawable.flag_in
            "Japones" -> R.drawable.flag_ja
            "Italiano" -> R.drawable.flag_it
            "Portugues" -> R.drawable.flag_po
            else -> R.drawable.flag_es // Imagen que puse predeterminada.
        }
    }
}
