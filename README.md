<h2>Wireless LED controller</h2> 

An LED controller for addressable led strips compatible with WS2812B.

 * The led strip is controlled via an Android app
 * The app uses bluetooth to create connection and send commands to the led controller.
 * The app provides a list of preprogrammed effects and commands that can be selected to be displayed on the leds.
 * The app supports a custom protocol to create custom effects that can be displayed on the LED strips.
 
 Android apk: <a>https://github.com/dobrovv/LED_Controller/blob/master/Android/app/release/app-release.apk</a>
 
 
 For this project, I am using a <a href="https://www.dfrobot.com/product-1259.html">BLUNO Beetle BLE board<a> to connect the bluetooth receiver to the Android and drive the led strips.
 The Beetle is an Arduino compatible board with an HC-05 Bluetooth Module installed on the board in addition to the microcontroller.
 The HC-05 bluetooth module is connected to the Beetle as a USART serial device and is controllable via the ```write``` and ```read``` system calls. 
 The Bluno Beetle receives commands via bluetooth from the Android app and works as a controller for the LED strips.

The casing contains the Arduino board and a power bank. It can be sticked to a surface or moved around. The power source for the controller is a regular 5V power bank available in the stores. The power bank is used to power the Arduino controller and the led strip.

<p align="left">
<img src="https://github.com/dobrovv/LED_Controller/blob/master/Screens/20200823_015720.jpg?raw=true" width="338" height="600">
</p>

Rainbow effect selected remotely from the app and displayed on the strip of 24 WS2812B LEDs

<p align="center">
<img src="https://github.com/dobrovv/LED_Controller/blob/master/Screens/20200823_021428_2.jpg?raw=true" width="800" height="450">
</p>

The main UI view

<p align="center">
<img src="https://github.com/dobrovv/LED_Controller/blob/master/Screens/0.jpg?raw=true">
</p>
