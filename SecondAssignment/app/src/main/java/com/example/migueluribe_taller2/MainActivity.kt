package com.example.migueluribe_taller2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.migueluribe_taller2.databinding.ActivityMainBinding

// Esta es la interfaz principal de la aplicaciòn, acà el usuario decidirà a que
// otra interfaz/opciòn de la aplicaciòn quiere ir.
class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Acà estàn los correspondientes listeners por si mi usuario quiere usar una de las 3 opciones disponibles
        // en mi aplicaciòn-
        binding.map.setOnClickListener {
            startActivity(Intent(baseContext, MapActivity::class.java))
        }
        binding.contacts.setOnClickListener {
            startActivity(Intent(baseContext, ContactsActivity::class.java))
        }
        binding.camera.setOnClickListener {
            startActivity(Intent(baseContext, CameraGalleryActivity::class.java))
        }
    }
}