import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (context) => GuessState(),
      child: MaterialApp(
        home: MyWidget(),
        debugShowCheckedModeBanner: false,
      ),
    ),
  );
}

class MyWidget extends StatefulWidget {
  MyWidget({Key? key}) : super(key: key);

  @override
  _MyWidgetState createState() => _MyWidgetState();
}

class _MyWidgetState extends State<MyWidget> {
  TextEditingController _tec = TextEditingController();
  bool _isTextFieldEnabled = true;

  void play() {
    var guessState = Provider.of<GuessState>(context, listen: false);
    var guess = int.tryParse(_tec.text);
    if (guess != null) {
      if (guess < 0 || guess > 100) {
        print("$guess is out of bounds");
      } else {
        _tec.text = ""; // Limpia el campo de texto
        guessState.increaseCounter();
        if (guess > guessState.random) {
          guessState.setMessage("$guess is bigger");
        } else if (guess < guessState.random) {
          guessState.setMessage("$guess is smaller");
        } else {
          guessState.setMessage("$guess is CORRECT!");
          guessState.finishGame();
          _isTextFieldEnabled = false; // Desactiva la edición del campo
        }
      }
    }
  }

  void resetGame() {
    var guessState = Provider.of<GuessState>(context, listen: false);
    guessState.resetGame();
    _tec.text = ""; // Limpia el campo de texto
    _isTextFieldEnabled = true; // Habilita la edición del campo nuevamente
  }

  @override
  Widget build(BuildContext context) {
    var guessState = Provider.of<GuessState>(context);
    TextStyle messageStyle = TextStyle(
      color: guessState.finish ? Colors.green : Colors.black,
    );

    return Scaffold(
      appBar: AppBar(title: Text("Adivina el número")),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          SizedBox(height: 16),
          SizedBox(
            width: 100,
            height: 100,
            child: Image.asset("assets/logi.png"),
          ),
          SizedBox(height: 16),
          Text(
            "Enter a number 0 - 100",
            style: TextStyle(
              color: Colors.blue,
              fontSize: 20,
            ),
          ),
          SizedBox(height: 16),
          TextFormField(
            controller: _tec,
            keyboardType: TextInputType.number,
            decoration: InputDecoration(hintText: "Your number..."),
            enabled:
                _isTextFieldEnabled, // Habilita o deshabilita la edición del campo
          ),
          SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: guessState.finish ? null : play,
              child: Text("Play"),
            ),
          ),
          SizedBox(height: 16),
          Text("Message: ${guessState.message}", style: messageStyle),
          SizedBox(height: 16),
          Text("Counter: ${guessState.counter}"),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: resetGame,
        child: Icon(Icons.refresh),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.endDocked,
    );
  }
}

class GuessState extends ChangeNotifier {
  var _message = "";
  var _counter = 0;
  var _finish = false;
  var _random = Random().nextInt(101);

  String get message => _message;
  int get counter => _counter;
  bool get finish => _finish;
  int get random => _random;

  void setMessage(String msg) {
    _message = msg;
    notifyListeners();
  }

  void setCounter(int value) {
    _counter = value;
    notifyListeners();
  }

  void increaseCounter() {
    setCounter(_counter + 1);
  }

  void setRandom(int value) {
    _random = value;
    notifyListeners();
  }

  void newRandom() {
    setRandom(Random().nextInt(101));
  }

  void finishGame() {
    _finish = true;
    notifyListeners();
  }

  void resetGame() {
    setCounter(0);
    newRandom();
    setMessage("");
    _finish = false;
  }
}
