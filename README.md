<h2>Wireless LED controller</h2> 

An LED controller for addressable led strips compatible with WS2812B.

 * The LED strip is controlled via an Android app.
 * The app uses a bluetooth connection to send commands to the LEDs controller.
 * The app provides a list of preprogrammed effects and custom commands that can be selected to be displayed on the LEDs.
 * The app implements a custom protocol alowing creation of new effects that can be displayed on the LED strips.
 
 Android apk: <a>https://github.com/dobrovv/LED_Controller/blob/master/Android/app/release/app-release.apk</a>
 
 
 In this project, I am using a <a href="https://www.dfrobot.com/product-1259.html">BLUNO Beetle BLE board<a> to connect the bluetooth receiver to the Android app and drive the led strips.
 The Beetle is an Arduino compatible microcontroller board with an integrated HC-05 Bluetooth Module.
 The HC-05 bluetooth module is connected to the Beetle as an USART serial device, controllable via the ```write``` and ```read``` system calls. 
 The Bluno Beetle receives commands via BLE from the Android app. The app works as a wireless controller for the LED strips using an _improved_ BLE driver.

The casing contains the Arduino board and a power bank. It can be sticked to a surface or moved around. The power source for the controller is a regular 5V power bank available in the stores. The power bank is used to power the Arduino controller and the led strip.

The main UI activity of the Android app.  

<p align="center">
<img src="https://github.com/dobrovv/LED_Controller/blob/master/Screens/0.jpg?raw=true">
</p>

Rainbow effect selected remotely from the app and displayed on the strip of 24 WS2812B LEDs.

<p align="center">
<img src="https://github.com/dobrovv/LED_Controller/blob/master/Screens/20200823_021428_2.jpg?raw=true" width="800" height="450">
</p>
