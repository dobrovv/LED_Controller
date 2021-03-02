<h2>Wireless LED controller</h2> 

An LED controller for addressable led strips compatible with WS2812B.

 * The led strip is controlled via an Android app
 * The app uses bluetooth to create connection and send commands to the led controller.
 * The app provides a list of effects and commands that can be selected to display on the leds.
 * Also the app supports creation of custom effects that can be displayed on the led strip.
 
 Android apk: <a>https://github.com/dobrovv/LED_Controller/blob/master/Android/app/release/app-release.apk</a>
 
 
 I am using a BLUNO Beetle BLE board to connect the bluetooth receiver to the Android and drive the led strip.
 The Beetle is an Arduino compatible board with an HC-05 Bluetooth Module installed on the board in addition to the microcontroller.
 The HC-05 bluetooth module is connected to the Beetle as a serial device and is accessed via the standart serial interface writes and reads. 
 The Bluno Beetle receives commands via bluetooth from the Android app and works as a controller for the led strip.


<img src="https://github.com/dobrovv/LED_Controller/blob/master/Screens/20200810_001049.jpg?raw=true" width="450" height="800">

The casing contains the Arduino board and a power bank. It can be sticked to a surface or it can be moved around. The power source for the controller is a regular 5V power bank available in the stores. The power bank is used to power the Arduino controller and the led strip.

<img src="https://github.com/dobrovv/LED_Controller/blob/master/Screens/20200823_015720.jpg?raw=true" width="450" height="800">

Rainbow effect selected remotely from the app and displayed on the strip of 24 WS2812B LEDs

<img src="https://github.com/dobrovv/LED_Controller/blob/master/Screens/20200823_021428.jpg?raw=true" width="450" height="800">

