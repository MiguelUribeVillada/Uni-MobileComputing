package com.example.migueluribe_taller2

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.example.migueluribe_taller2.databinding.ActivityCameraGalleryBinding
import java.io.File
import java.io.IOException

class CameraGalleryActivity : AppCompatActivity() {
    //Acà me encarguè de inicializar variables necesarias para el resto de la interfaz.
    private lateinit var binding: ActivityCameraGalleryBinding
    private var cameraUri: Uri? = null
    val getContentGallery = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        { uri ->
            if (uri != null) {
                loadImage(uri)
            }
        }
    )
    val getContentCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            if (cameraUri != null) {
                loadImage(cameraUri!!)
            }
        }
    }

    // Acà me encargo de la inicialización de la actividad como tal, inflando la interfaz de usuario,
    // configurando botones y preparando la actividad para responder a las acciones del usuario,
    // como el hecho de seleccionar imágenes de la galería o tomar fotos desde la cámara.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Por si quiere seleccionar una imagen desde galeria.
        binding.galleryButton.setOnClickListener {
            getContentGallery.launch("image/*")
        }
        // Por si quiere es tomar una imagen desde su camara.
        binding.cameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                try {
                    val file = createImageFile()
                    cameraUri = FileProvider.getUriForFile(baseContext, baseContext.packageName + ".fileprovider", file)
                    getContentCamera.launch(cameraUri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                requestCameraPermission()
            }
        }
    }

    // Esta función  hice que verifique si la aplicación tiene permiso para acceder a la cámara del
    // dispositivo y retorna "true" si el permiso está concedido y "false" si no lo está.
    private fun checkCameraPermission(): Boolean {
        val cameraPermission = android.Manifest.permission.CAMERA
        val granted = PackageManager.PERMISSION_GRANTED
        return ContextCompat.checkSelfPermission(this, cameraPermission) == granted
    }

    // Esta función solicita permiso para acceder a la cámara del dispositivo y si el permiso
    // no está concedido, se llama a esta función para solicitarlo.
    private fun requestCameraPermission() {
        val cameraPermission = android.Manifest.permission.CAMERA
        val requestCode = 123 // Este es un valor que segun investiguè es ùnico y no es necesario cambiarlo.
        requestPermissions(arrayOf(cameraPermission), requestCode)
    }

    // Este método maneja la respuesta de la solicitud de permisos y verifica si el permiso de la
    // cámara fue concedido y, si es así, intenta abrir la cámara para tomar una foto.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    val file = createImageFile()
                    cameraUri = FileProvider.getUriForFile(baseContext, baseContext.packageName + ".fileprovider", file)
                    getContentCamera.launch(cameraUri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Esta función crea un archivo de imagen temporal en el almacenamiento externo del dispositivo y
    // el nombre del archivo se genera con una marca de tiempo única (dia y hora como toca hacer en el taller).
    private fun createImageFile(): File {
        val timeStamp = System.currentTimeMillis().toString()
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    // Esta función corrige la orientación de una imagen cargada desde la galería o la cámara
    // y lee la información de orientación de los metadatos EXIF de la imagen y la rota en consecuencia.
    private fun fixImageOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
        val orientationColumnIndex = ExifInterface.TAG_ORIENTATION
        // Acà se obtiene la informaciòn de rotaciòn de la imagen tomada.
        val inputStream = contentResolver.openInputStream(uri)
        val exif = inputStream?.let { ExifInterface(it) }
        val orientation = exif?.getAttributeInt(orientationColumnIndex, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Esta función toma una URI que apunta a una imagen y carga esa imagen desde el almacenamiento
    // del dispositivo y despues la ajusta a su tamaño a un valor fijo en píxeles y corrige su orientación
    // antes de mostrarla en una ImageView en la interfaz de usuario.
    private fun loadImage(uri: Uri) {
        val imageStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(imageStream)
        // Establece un tamaño fijo en píxeles para la ImageView como se menciona en el taller que es necesario
        val desiredWidthInPixels = 1000
        val desiredHeightInPixels = 1000
        val layoutParams = binding.image.layoutParams
        layoutParams.width = desiredWidthInPixels
        layoutParams.height = desiredHeightInPixels
        binding.image.layoutParams = layoutParams
        // Acà corrijo la orientación de la imagen
        val fixedBitmap = fixImageOrientation(uri, bitmap)
        binding.image.setImageBitmap(fixedBitmap)
    }
}
