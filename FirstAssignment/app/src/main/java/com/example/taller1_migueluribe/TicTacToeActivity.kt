package com.example.taller1_migueluribe;

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.taller1_migueluribe.databinding.ActivityTicTacToeBinding

class TicTacToeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTicTacToeBinding
    // Variable para hacer luego retrasar un momento a la alerta.
    private val handler = Handler()
    // Acá estoy probando los lateint para declarar variables que luego inicializaré.
    private lateinit var buttons: Array<Button>
    private lateinit var currentPlayer: String
    private lateinit var gameBoard: Array<String>
    private lateinit var player1TextView: TextView
    private lateinit var player2TextView: TextView
    private var gameEnded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicTacToeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        buttons = arrayOf( // Acá le introduzco al arreglo de botones los 9 botones creados en la pantalla principal.
            findViewById(R.id.button1), findViewById(R.id.button2), findViewById(R.id.button3), findViewById(R.id.button4), findViewById(R.id.button5), findViewById(R.id.button6), findViewById(R.id.button7), findViewById(R.id.button8), findViewById(R.id.button9))
        reinicioColores() // Acá les pongo por defecto este color a todos los botones.
        player1TextView = findViewById(R.id.player1TextView)
        player2TextView = findViewById(R.id.player2TextView)
        currentPlayer = "X"
        gameBoard = Array(9) { "" }
        updatePlayerTurn()
        for (button in buttons) {
            button.setOnClickListener { onButtonClick(it) } }
        findViewById<Button>(R.id.newGameButton).setOnClickListener { newGame() } //Acá va escuchando cuando el usuario desee crear una nueva partida.
        gameEnded = false
    }
    // Función para ir comprobando el ganador por cada oprimida de botón.
    private fun onButtonClick(view: View) {
        if (gameEnded) return // No hacer nada si el juego ha terminado.
        val button = view as Button
        val index = buttons.indexOf(button)
        if (gameBoard[index].isEmpty()) {
            gameBoard[index] = currentPlayer
            button.text = currentPlayer
            if (checkWin(currentPlayer)) {
                gameEnded = true // Acá ya el juego ha terminado (Hace un 3 en linea un jugador).
                showWinnerAlert(currentPlayer)
            } else if (isBoardFull()) {
                gameEnded = true // Acá ya el juego ha terminado (Se llena el tablero sin ganador).
                showDrawAlert()
            } else {
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                updatePlayerTurn()
            }
        }
    }
    // Función para ir comprobando las posibles 9 victorias (Por filas, columnas y diagonales).
    private fun checkWin(player: String): Boolean {
        // Por Filas.
        for (i in 0..6 step 3) {
            if (gameBoard[i] == player && gameBoard[i + 1] == player && gameBoard[i + 2] == player) {
                colorVictoria(listOf(i, i + 1, i + 2))
                return true
            }
        }
        // Por Columnas.
        for (i in 0..2) {
            if (gameBoard[i] == player && gameBoard[i + 3] == player && gameBoard[i + 6] == player) {
                colorVictoria(listOf(i, i + 3, i + 6))
                return true
            }
        }
        // Por los dos diagonales.
        if (gameBoard[0] == player && gameBoard[4] == player && gameBoard[8] == player) {
            colorVictoria(listOf(0, 4, 8))
            return true
        }
        if (gameBoard[2] == player && gameBoard[4] == player && gameBoard[6] == player) {
            colorVictoria(listOf(2, 4, 6))
            return true
        }
        return false //Cuando es llena el tablero y es un empate.
    }
    // Para comprobar que el tablero no este lleno aún, de lo contrario demuestra que es empate.
    private fun isBoardFull(): Boolean {
        return gameBoard.all { it.isNotEmpty() }
    }
    //Función para ir actualizando las 2 textviews de cada turno (indica el turno de cada jugador).
    private fun updatePlayerTurn() {
        player1TextView.text = "Jugador #1 [X]"
        player2TextView.text = "Jugador #2 [O]"
        if (currentPlayer == "X") {
            player1TextView.text = "Turno del Jugador #1!"
        } else {
            player2TextView.text = "Turno del Jugador #2!"
        }
    }
    // Función para reiniciar el tablero del arreglo con los 9 botones.
    private fun newGame() {
        gameEnded = false // Reiniciar el juego, permitiendo que se opriman botones nuevamente
        reinicioColores() // Restablecer el color de fondo de los botones
        for (i in 0 until gameBoard.size) {
            gameBoard[i] = ""
            buttons[i].text = ""
        }
        currentPlayer = "X"
        updatePlayerTurn()
    }
    // Funciones para generar las aleertas del posible fin del jueg (Ganador#1/Ganador#2/Empate)
    private fun showWinnerAlert(winner: String) {
        val message = if (winner == "X") "Jugador #1 esta vez gana!" else "Jugador #2 esta vez gana!"
        handler.postDelayed({
            AlertDialog.Builder(this)
                .setTitle("FIN DEL JUEGO!")
                .setMessage(message)
                .setPositiveButton("¿Nueva Partida?") { _, _ -> newGame() } // Por lo que leí esto es una función anonima y ya que no hacemos uso de sus 2 parametros, entonces solo ponemos guiones.
                .setCancelable(false)
                .show()
        }, 900)
    }
    // Función para mostrar una alerta cuando es un empate.
    private fun showDrawAlert() {
        handler.postDelayed({
            AlertDialog.Builder(this)
                .setTitle("FIN DEL JUEGO")
                .setMessage("Que reñido! Fue un empate!")
                .setPositiveButton("¿Nueva Partida?") { _, _ -> newGame() } //Por lo que leí esto es una función anonima y ya que no hacemos uso de sus 2 parametros, entonces solo ponemos guiones.
                .setCancelable(false)
                .show()
        }, 900)
    }
    // Para cambiarle el color a los botones ganadores.
    private fun colorVictoria(winningPositions: List<Int>) {
        for (position in winningPositions) {
            buttons[position].setBackgroundColor(ContextCompat.getColor(this, R.color.colorVictoria))
        }
    }
    // Para reiniciar el color por defecto de los colores luego de cambiarles el color de una victoria.
    private fun reinicioColores() {
        for (button in buttons) {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPorDefecto))
        }
    }
}