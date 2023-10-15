package com.example.migueluribe_taller2

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.migueluribe_taller2.adapters.ContactsAdapter
import com.example.migueluribe_taller2.databinding.ActivityContactsBinding

class ContactsActivity : AppCompatActivity() {
    // En esta parte inicializo las variables necesarias para el uso correcto de la funcionalidad de esta interfaz.
    private lateinit var binding: ActivityContactsBinding
    private val getSimplePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            updateUI(it)
        }
    private val projection = arrayOf(
        ContactsContract.Profile._ID,
        ContactsContract.Profile.DISPLAY_NAME_PRIMARY
    )
    private lateinit var adapter: ContactsAdapter

    // Acà me encargo de que pedir permisos si es que aun no los tengo y de inicializar el adaptador para manejar
    // a toda la lista de contactos del usuario y asignarlo a la listview.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = ContactsAdapter(this, null, 0)
        binding.listContacts.adapter = adapter
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(
                    this,
                    "El permiso es necesario para desplegar la lista de todos sus contactos",
                    Toast.LENGTH_LONG
                ).show()
            }
            getSimplePermission.launch(android.Manifest.permission.READ_CONTACTS)
        } else {
            updateUI(true)
        }
    }

    // Esta función se llama cuando se obtiene una respuesta de la solicitud de permiso y recibe
    // un valor booleano permission, que indica si el permiso se ha concedido o no, si el permiso
    // se ha concedido "true", la función realiza una consulta a la base de datos
    // de contactos y actualiza la interfaz de usuario para mostrar la lista de contactos.
    private fun updateUI(permission: Boolean) {
        if (permission) {
            val cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.Contacts._ID} ASC" // Ordenar por ID de menor a mayor
            )
            adapter.changeCursor(cursor)
        }
    }
}
