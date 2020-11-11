// A basic everyday NeoPixel strip test program.

// NEOPIXEL BEST PRACTICES for most reliable operation:
// - Add 1000 uF CAPACITOR between NeoPixel strip's + and - connections.
// - MINIMIZE WIRING LENGTH between microcontroller board and first pixel.
// - NeoPixel strip's DATA-IN should pass through a 300-500 OHM RESISTOR.
// - AVOID connecting NeoPixels on a LIVE CIRCUIT. If you must, ALWAYS
//   connect GROUND (-) first, then +, then data.
// - When using a 3.3V microcontroller with a 5V-powered NeoPixel strip,
//   a LOGIC-LEVEL CONVERTER on the data line is STRONGLY RECOMMENDED.
// (Skipping these may work OK on your workbench but can fail in the field)

#include <Adafruit_NeoPixel.h>  
#ifdef __AVR__
 #include <avr/power.h> // Required for 16 MHz Adafruit Trinket
#endif

// Which pin on the Arduino is connected to the NeoPixels?
// On a Trinket or Gemma we suggest changing this to 1:
#define LED_PIN    4

// How many NeoPixels are attached to the Arduino?
#define LED_COUNT 24

// Declare our NeoPixel strip object:
Adafruit_NeoPixel strip(LED_COUNT, LED_PIN, NEO_GRB + NEO_KHZ800);
// Argument 1 = Number of pixels in NeoPixel strip
// Argument 2 = Arduino pin number (most are valid)
// Argument 3 = Pixel type flags, add together as needed:
//   NEO_KHZ800  800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
//   NEO_KHZ400  400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
//   NEO_GRB     Pixels are wired for GRB bitstream (most NeoPixel products)
//   NEO_RGB     Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
//   NEO_RGBW    Pixels are wired for RGBW bitstream (NeoPixel RGBW products)


// Declare our type of the commands received by the controller
struct CMD_STRUCT {
  char type;
  int param[4];
  bool ready;
} cmd, prev_cmd;
 


// setup() function -- runs once at startup --------------------------------

void setup() {
  strip.begin();           // INITIALIZE NeoPixel strip object (REQUIRED)
  strip.show();            // Turn OFF all pixels ASAP
  strip.setBrightness(50); // Set BRIGHTNESS to about 1/5 (max = 255)

  //Serial1.begin(38400);
  Serial.begin(115200);
  cmd.type = 0;
  cmd.ready = false;
  delay(100);
  while (!Serial) {}
  //Serial.println("Arduino is ready");
}


// loop() function -- runs repeatedly as long as board is on ---------------

void executeRemoteCmd() {
  processCMDStream();

  if(cmd.ready) {
      switch(cmd.type) {
        case 'l': {
          strip.updateLength(cmd.param[0]);
          //strip.show();
          break;
        }
        case 'p': {
          strip.setPixelColor(cmd.param[0], strip.Color(cmd.param[1], cmd.param[2], cmd.param[3]));
          strip.show();
          break;
        }
        case 'c': {
          strip.clear();
          break;
        }
        case 'm': {
          strip.setPixelColor(cmd.param[0], strip.Color(cmd.param[1], cmd.param[2], cmd.param[3]));
          break;
        }
        case 's': {
          strip.show();
          break;
        }
        case 'f': {
          strip.fill(strip.Color(cmd.param[0], cmd.param[1], cmd.param[2]));
          strip.show();
          break;
        }
        case 'b': {
          strip.setBrightness(cmd.param[0]); // Set BRIGHTNESS to about 1/5 (max = 255)
          strip.show();
          break;
        }
        case 'i': {
          break;
        }
        
        default: break;
      }

//      if (Serial) {
//        Serial.print("Command: "); Serial.print(cmd.type);
//        Serial.print(" Param 1:"); Serial.print(cmd.param[0]);
//        Serial.print(" Param 2:"); Serial.print(cmd.param[1]);
//        Serial.print(" Param 3:"); Serial.print(cmd.param[2]); 
//        Serial.print(" Param 4:"); Serial.println(cmd.param[3]);
//      }
//      Serial1.print(cmd.type);
//      Serial1.print("("); Serial1.print(cmd.param[0]);
//      Serial1.print(","); Serial1.print(cmd.param[1]);
//      Serial1.print(","); Serial1.print(cmd.param[2]); 
//      Serial1.print(","); Serial1.print(cmd.param[3]); Serial1.println(")");

      prev_cmd.type = cmd.type;
      prev_cmd.param[0] = cmd.param[0];
      prev_cmd.param[1] = cmd.param[1];
      prev_cmd.param[2] = cmd.param[2];
      prev_cmd.param[3] = cmd.param[3];
      cmd.ready = false;
    }
  
}

bool checkIfPreInstalledActive(int what) {
  processCMDStream();
  if (prev_cmd.type != cmd.type || prev_cmd.type != 'i' || prev_cmd.param[0] != cmd.param[0])
    return false;
  else if (prev_cmd.param[0] != what)
    return false;
  else
    return true;
}

void loop() {
   
   executeRemoteCmd();
   
    if (prev_cmd.type == 'i'){
      if (prev_cmd.param[0] == 0) {
        colorWipe(strip.Color(255,   0,   0), 50); // Red
        colorWipe(strip.Color(  0, 255,   0), 50); // Green
        colorWipe(strip.Color(  0,   0, 255), 50); // Blue
      } else if (prev_cmd.param[0] == 1){
        theaterChase(strip.Color(127, 127, 127), 50); // White, half brightness
        theaterChase(strip.Color(127,   0,   0), 50); // Red, half brightness
        theaterChase(strip.Color(  0,   0, 127), 50); // Blue, half brightness
      } else if (prev_cmd.param[0] == 2){
         rainbow(10);  
      } else if (prev_cmd.param[0] == 3) {
        theaterChaseRainbow(10);
      } else {
        switch(prev_cmd.param[0]) {
           case 4  : {
                  // RGBLoop - no parameters
                  RGBLoop();
                  break;
                }

          case 5  : {
                      // FadeInOut - Color (red, green. blue)
                      FadeInOut(0xff, 0x00, 0x00); // red
                      FadeInOut(0xff, 0xff, 0xff); // white
                      FadeInOut(0x00, 0x00, 0xff); // blue
                      break;
                    }
                   
          case 6  : {
                      // Strobe - Color (red, green, blue), number of flashes, flash speed, end pause
                      Strobe(0xff, 0xff, 0xff, 10, 50, 1000);
                      break;
                    }
      
          case 7  : {
                      // HalloweenEyes - Color (red, green, blue), Size of eye, space between eyes, fade (true/false), steps, fade delay, end pause
                      HalloweenEyes(0xff, 0x00, 0x00,
                                    1, 4,
                                    true, random(5,50), random(50,150),
                                    random(1000, 10000));
                      break;
                    }
                   
          case 8  : {
                      // CylonBounce - Color (red, green, blue), eye size, speed delay, end pause
                      CylonBounce(0xff, 0x00, 0x00, 4, 10, 50);
                      break;
                    }
                   
          case 9  : {
                      // NewKITT - Color (red, green, blue), eye size, speed delay, end pause
                      NewKITT(0xff, 0x00, 0x00, 8, 10, 50);
                      break;
                    }
                   
          case 10  : {
                      // Twinkle - Color (red, green, blue), count, speed delay, only one twinkle (true/false)
                      Twinkle(0xff, 0x00, 0x00, 10, 100, false);
                      break;
                    }
                   
          case 11  : {
                      // TwinkleRandom - twinkle count, speed delay, only one (true/false)
                      TwinkleRandom(20, 100, false);
                      break;
                    }
                   
          case 12  : {
                      // Sparkle - Color (red, green, blue), speed delay
                      Sparkle(0xff, 0xff, 0xff, 0);
                      break;
                    }
                     
          case 13  : {
                      // SnowSparkle - Color (red, green, blue), sparkle delay, speed delay
                      SnowSparkle(0x10, 0x10, 0x10, 20, random(100,1000));
                      break;
                    }
                   
          case 14 : {
                      // Running Lights - Color (red, green, blue), wave dealy
                      RunningLights(0xff,0x00,0x00, 50);  // red
                      RunningLights(0xff,0xff,0xff, 50);  // white
                      RunningLights(0x00,0x00,0xff, 50);  // blue
                      break;
                    }
      
          case 15 : {
                      // Fire - Cooling rate, Sparking rate, speed delay
                      Fire(55,120,15);
                      break;
                    }
      
      
                    // simple bouncingBalls not included, since BouncingColoredBalls can perform this as well as shown below
                    // BouncingColoredBalls - Number of balls, color (red, green, blue) array, continuous
                    // CAUTION: If set to continuous then this effect will never stop!!!
                   
//          case 16 : {
//                      // mimic BouncingBalls
//                      byte onecolor[1][3] = { {0xff, 0x00, 0x00} };
//                      BouncingColoredBalls(1, onecolor, false);
//                      break;
//                    }
      
          case 16 : {
                      // multiple colored balls
                      byte colors[3][3] = { {0xff, 0x00, 0x00},
                                            {0xff, 0xff, 0xff},
                                            {0x00, 0x00, 0xff} };
                      BouncingColoredBalls(3, colors, true);
                      break;
                    }
      
          case 17 : {
                      // meteorRain - Color (red, green, blue), meteor size, trail decay, random trail decay (true/false), speed delay
                      meteorRain(0xff,0xff,0xff,10, 64, true, 30);
                      break;
                    }
        }
      }
    }
}


/*
  // some usage examples
  // Fill along the length of the strip in various colors...
  colorWipe(strip.Color(255,   0,   0), 50); // Red
  colorWipe(strip.Color(  0, 255,   0), 50); // Green
  colorWipe(strip.Color(  0,   0, 255), 50); // Blue

  // Do a theater marquee effect in various colors...
  theaterChase(strip.Color(127, 127, 127), 50); // White, half brightness
  theaterChase(strip.Color(127,   0,   0), 50); // Red, half brightness
  theaterChase(strip.Color(  0,   0, 127), 50); // Blue, half brightness

  rainbow(10);             // Flowing rainbow cycle along the whole strip
  theaterChaseRainbow(10); // Rainbow-enhanced theaterChase variant
 */

// Parsing of the bt input stream --------------------------

void processCMDStream() {
  
  #define CMD_BUFF_SIZE 32
  static char cmdBuff[CMD_BUFF_SIZE+1]; // an array to store the received data
  
  static int ndx;       // counter for the unparsed characters inside the cmdBuff 
  static int token = 0; // counter for parsed tokens (cmd and the following params) of the command string
  static int rc;
  static int param;
  
  while (Serial.available() > 0 && !cmd.ready) {
    rc = Serial.read();
    //Serial.write(rc);

    if( token >= 5+1 ) { // too many params
        token=0; ndx=0;
        cmd.type = 0; cmd.ready = true; // mark parsing error
    }

    if( ndx >= CMD_BUFF_SIZE ) { // buffer overflow
        token=0; ndx=0;
        cmd.type = 0; cmd.ready = true; // mark parsing error
    }
    
    if (rc == '\n' || rc == '\r') {
      if (token == 0 && ndx == 0) { // end reached, no cmd
        cmd.type = 0; // parsing error
      } else if (token == 0 && ndx != 0) { // end reached, a cmd type received w/o arguments
        cmd.type = cmdBuff[0]; // read cmd type from buff
      } else { // end reached, parse final param
        param = atoi(cmdBuff); 
        cmd.param[token-1] = param;
      }
      token = 0; ndx = 0; 
      cmd.ready = true; // command is parsed, wait
    } else if (rc == '-') {
      if (token == 0 && ndx == 0) { // separator reached, nothing received
        cmd.type = 0; cmd.ready = true; // mark parsing error
        // cmd.token=0; cmd.ndx= 0; //token and ndx are already set to 0
      } else if (token == 0 && ndx != 0) { //separator reached, cmd type received
        cmd.type = cmdBuff[0]; // read cmd type from buff
        token++; ndx = 0;
      } else { //separator reached, even in the case nothing was received ( i.e ndx=0) param is zero
        param = atoi(cmdBuff); 
        cmd.param[token-1] = param;
        token++; ndx = 0;
      }
    } else {
      cmdBuff[ndx++] = rc;
      cmdBuff[ndx] = 0;
    }
  }      
}


// Some functions of our own for creating animated effects -----------------

// Fill strip pixels one after another with a color. Strip is NOT cleared
// first; anything there will be covered pixel by pixel. Pass in color
// (as a single 'packed' 32-bit value, which you can get by calling
// strip.Color(red, green, blue) as shown in the loop() function above),
// and a delay time (in milliseconds) between pixels.
void colorWipe(uint32_t color, int wait) {
  for(int i=0; i<strip.numPixels(); i++) { // For each pixel in strip...
    strip.setPixelColor(i, color);         //  Set pixel's color (in RAM)
    if (!checkIfPreInstalledActive(0)) return;
    strip.show();                          //  Update strip to match
    delay(wait);                           //  Pause for a moment
  }
}

// Theater-marquee-style chasing lights. Pass in a color (32-bit value,
// a la strip.Color(r,g,b) as mentioned above), and a delay time (in ms)
// between frames.
void theaterChase(uint32_t color, int wait) {
  for(int a=0; a<10; a++) {  // Repeat 10 times...
    for(int b=0; b<3; b++) { //  'b' counts from 0 to 2...
      strip.clear();         //   Set all pixels in RAM to 0 (off)
      // 'c' counts up from 'b' to end of strip in steps of 3...
      for(int c=b; c<strip.numPixels(); c += 3) {
        strip.setPixelColor(c, color); // Set pixel 'c' to value 'color'
      }
      if (!checkIfPreInstalledActive(1)) return;
      strip.show(); // Update strip with new contents
      delay(wait);  // Pause for a moment
    }
  }
}

// Rainbow cycle along whole strip. Pass delay time (in ms) between frames.
void rainbow(int wait) {
  // Hue of first pixel runs 5 complete loops through the color wheel.
  // Color wheel has a range of 65536 but it's OK if we roll over, so
  // just count from 0 to 5*65536. Adding 256 to firstPixelHue each time
  // means we'll make 5*65536/256 = 1280 passes through this outer loop:
  for(long firstPixelHue = 0; firstPixelHue < 5*65536; firstPixelHue += 256) {
    for(int i=0; i<strip.numPixels(); i++) { // For each pixel in strip...
      // Offset pixel hue by an amount to make one full revolution of the
      // color wheel (range of 65536) along the length of the strip
      // (strip.numPixels() steps):
      int pixelHue = firstPixelHue + (i * 65536L / strip.numPixels());
      // strip.ColorHSV() can take 1 or 3 arguments: a hue (0 to 65535) or
      // optionally add saturation and value (brightness) (each 0 to 255).
      // Here we're using just the single-argument hue variant. The result
      // is passed through strip.gamma32() to provide 'truer' colors
      // before assigning to each pixel:
      strip.setPixelColor(i, strip.gamma32(strip.ColorHSV(pixelHue)));
    }
    if(!checkIfPreInstalledActive(2)) return;
    strip.show(); // Update strip with new contents
    delay(wait);  // Pause for a moment
  }
}

// Rainbow-enhanced theater marquee. Pass delay time (in ms) between frames.
void theaterChaseRainbow(int wait) {
  int firstPixelHue = 0;     // First pixel starts at red (hue 0)
  for(int a=0; a<30; a++) {  // Repeat 30 times...
    for(int b=0; b<3; b++) { //  'b' counts from 0 to 2...
      strip.clear();         //   Set all pixels in RAM to 0 (off)
      // 'c' counts up from 'b' to end of strip in increments of 3...
      for(int c=b; c<strip.numPixels(); c += 3) {
        // hue of pixel 'c' is offset by an amount to make one full
        // revolution of the color wheel (range 65536) along the length
        // of the strip (strip.numPixels() steps):
        int      hue   = firstPixelHue + c * 65536L / strip.numPixels();
        uint32_t color = strip.gamma32(strip.ColorHSV(hue)); // hue -> RGB
        strip.setPixelColor(c, color); // Set pixel 'c' to value 'color'
      }
      if (!checkIfPreInstalledActive(3)) return;
      strip.show();                // Update strip with new contents
      delay(wait);                 // Pause for a moment
      firstPixelHue += 65536 / 90; // One cycle of color wheel over 90 frames
    }
  }
}

// *************************
// ** LEDEffect Functions **
// ** https://www.tweaking4all.com/hardware/arduino/arduino-all-ledstrip-effects-in-one/
// *************************

void RGBLoop(){
  for(int j = 0; j < 3; j++ ) {
    // Fade IN
    for(int k = 0; k < 256; k++) {
      switch(j) {
        case 0: setAll(k,0,0); break;
        case 1: setAll(0,k,0); break;
        case 2: setAll(0,0,k); break;
      }
      if (!checkIfPreInstalledActive(4)) return;
      strip.show();
      delay(3);
    }
    // Fade OUT
    for(int k = 255; k >= 0; k--) {
      switch(j) {
        case 0: setAll(k,0,0); break;
        case 1: setAll(0,k,0); break;
        case 2: setAll(0,0,k); break;
      }
      if (!checkIfPreInstalledActive(4)) return;
      strip.show();
      delay(3);
    }
  }
}

void FadeInOut(byte red, byte green, byte blue){
  float r, g, b;
     
  for(int k = 0; k < 256; k=k+1) {
    r = (k/256.0)*red;
    g = (k/256.0)*green;
    b = (k/256.0)*blue;
    setAll(r,g,b);
    if (!checkIfPreInstalledActive(5)) return;
    strip.show();
  }
     
  for(int k = 255; k >= 0; k=k-2) {
    r = (k/256.0)*red;
    g = (k/256.0)*green;
    b = (k/256.0)*blue;
    setAll(r,g,b);
    if (!checkIfPreInstalledActive(5)) return;
    strip.show();
  }
}

void Strobe(byte red, byte green, byte blue, int StrobeCount, int FlashDelay, int EndPause){
  for(int j = 0; j < StrobeCount; j++) {
    setAll(red,green,blue);
    if (!checkIfPreInstalledActive(6)) return;
    strip.show();
    delay(FlashDelay);
    setAll(0,0,0);
    if (!checkIfPreInstalledActive(6)) return;
    strip.show();
    delay(FlashDelay);
  }
 
 delay(EndPause);
}

void HalloweenEyes(byte red, byte green, byte blue,
                   int EyeWidth, int EyeSpace,
                   boolean Fade, int Steps, int FadeDelay,
                   int EndPause){
  //randomSeed(analogRead(0));
 
  int i;
  int StartPoint  = random( 0, strip.numPixels() - (2*EyeWidth) - EyeSpace );
  int Start2ndEye = StartPoint + EyeWidth + EyeSpace;
 
  for(i = 0; i < EyeWidth; i++) {
    setPixel(StartPoint + i, red, green, blue);
    setPixel(Start2ndEye + i, red, green, blue);
  }
  if (!checkIfPreInstalledActive(7)) return;
  strip.show();
 
  if(Fade==true) {
    float r, g, b;
 
    for(int j = Steps; j >= 0; j--) {
      r = j*(red/Steps);
      g = j*(green/Steps);
      b = j*(blue/Steps);
     
      for(i = 0; i < EyeWidth; i++) {
        setPixel(StartPoint + i, r, g, b);
        setPixel(Start2ndEye + i, r, g, b);
      }
      if (!checkIfPreInstalledActive(7)) return;
      strip.show();
      delay(FadeDelay);
    }
  }

  if (!checkIfPreInstalledActive(8)) return;
  setAll(0,0,0); // Set all black
 
  delay(EndPause);
}

void CylonBounce(byte red, byte green, byte blue, int EyeSize, int SpeedDelay, int ReturnDelay){

  for(int i = 0; i < strip.numPixels()-EyeSize-2; i++) {
    setAll(0,0,0);
    setPixel(i, red/10, green/10, blue/10);
    for(int j = 1; j <= EyeSize; j++) {
      setPixel(i+j, red, green, blue);
    }
    setPixel(i+EyeSize+1, red/10, green/10, blue/10);
    if (!checkIfPreInstalledActive(8)) return;
    strip.show();
    delay(SpeedDelay);
  }

  delay(ReturnDelay);

  for(int i = strip.numPixels()-EyeSize-2; i > 0; i--) {
    setAll(0,0,0);
    setPixel(i, red/10, green/10, blue/10);
    for(int j = 1; j <= EyeSize; j++) {
      setPixel(i+j, red, green, blue);
    }
    setPixel(i+EyeSize+1, red/10, green/10, blue/10);
    if (!checkIfPreInstalledActive(8)) return;
    strip.show();
    delay(SpeedDelay);
  }
 
  delay(ReturnDelay);
}

void NewKITT(byte red, byte green, byte blue, int EyeSize, int SpeedDelay, int ReturnDelay){
  RightToLeft(red, green, blue, EyeSize, SpeedDelay, ReturnDelay);
  LeftToRight(red, green, blue, EyeSize, SpeedDelay, ReturnDelay);
  OutsideToCenter(red, green, blue, EyeSize, SpeedDelay, ReturnDelay);
  CenterToOutside(red, green, blue, EyeSize, SpeedDelay, ReturnDelay);
  LeftToRight(red, green, blue, EyeSize, SpeedDelay, ReturnDelay);
  RightToLeft(red, green, blue, EyeSize, SpeedDelay, ReturnDelay);
  OutsideToCenter(red, green, blue, EyeSize, SpeedDelay, ReturnDelay);
  CenterToOutside(red, green, blue, EyeSize, SpeedDelay, ReturnDelay);
}

// used by NewKITT
void CenterToOutside(byte red, byte green, byte blue, int EyeSize, int SpeedDelay, int ReturnDelay) {
  for(int i =((strip.numPixels()-EyeSize)/2); i>=0; i--) {
    setAll(0,0,0);
   
    setPixel(i, red/10, green/10, blue/10);
    for(int j = 1; j <= EyeSize; j++) {
      setPixel(i+j, red, green, blue);
    }
    setPixel(i+EyeSize+1, red/10, green/10, blue/10);
   
    setPixel(strip.numPixels()-i, red/10, green/10, blue/10);
    for(int j = 1; j <= EyeSize; j++) {
      setPixel(strip.numPixels()-i-j, red, green, blue);
    }
    setPixel(strip.numPixels()-i-EyeSize-1, red/10, green/10, blue/10);
    if (!checkIfPreInstalledActive(9)) return;
    strip.show();
    delay(SpeedDelay);
  }
  delay(ReturnDelay);
}

// used by NewKITT
void OutsideToCenter(byte red, byte green, byte blue, int EyeSize, int SpeedDelay, int ReturnDelay) {
  for(int i = 0; i<=((strip.numPixels()-EyeSize)/2); i++) {
    setAll(0,0,0);
   
    setPixel(i, red/10, green/10, blue/10);
    for(int j = 1; j <= EyeSize; j++) {
      setPixel(i+j, red, green, blue);
    }
    setPixel(i+EyeSize+1, red/10, green/10, blue/10);
   
    setPixel(strip.numPixels()-i, red/10, green/10, blue/10);
    for(int j = 1; j <= EyeSize; j++) {
      setPixel(strip.numPixels()-i-j, red, green, blue);
    }
    setPixel(strip.numPixels()-i-EyeSize-1, red/10, green/10, blue/10);
    if (!checkIfPreInstalledActive(9)) return;
    strip.show();
    delay(SpeedDelay);
  }
  delay(ReturnDelay);
}

// used by NewKITT
void LeftToRight(byte red, byte green, byte blue, int EyeSize, int SpeedDelay, int ReturnDelay) {
  for(int i = 0; i < strip.numPixels()-EyeSize-2; i++) {
    setAll(0,0,0);
    setPixel(i, red/10, green/10, blue/10);
    for(int j = 1; j <= EyeSize; j++) {
      setPixel(i+j, red, green, blue);
    }
    setPixel(i+EyeSize+1, red/10, green/10, blue/10);
    if (!checkIfPreInstalledActive(9)) return;
    strip.show();
    delay(SpeedDelay);
  }
  delay(ReturnDelay);
}

// used by NewKITT
void RightToLeft(byte red, byte green, byte blue, int EyeSize, int SpeedDelay, int ReturnDelay) {
  for(int i = strip.numPixels()-EyeSize-2; i > 0; i--) {
    setAll(0,0,0);
    setPixel(i, red/10, green/10, blue/10);
    for(int j = 1; j <= EyeSize; j++) {
      setPixel(i+j, red, green, blue);
    }
    setPixel(i+EyeSize+1, red/10, green/10, blue/10);
    if (!checkIfPreInstalledActive(9)) return;
    strip.show();
    delay(SpeedDelay);
  }
  delay(ReturnDelay);
}

void Twinkle(byte red, byte green, byte blue, int Count, int SpeedDelay, boolean OnlyOne) {
  setAll(0,0,0);
 
  for (int i=0; i<Count; i++) {
     setPixel(random(strip.numPixels()),red,green,blue);
     if (!checkIfPreInstalledActive(10)) return;
     strip.show();
     delay(SpeedDelay);
     if(OnlyOne) {
       setAll(0,0,0);
     }
   }
 
  delay(SpeedDelay);
}

void TwinkleRandom(int Count, int SpeedDelay, boolean OnlyOne) {
  setAll(0,0,0);
 
  for (int i=0; i<Count; i++) {
     setPixel(random(strip.numPixels()),random(0,255),random(0,255),random(0,255));
     if (!checkIfPreInstalledActive(11)) return;
     strip.show();
     delay(SpeedDelay);
     if(OnlyOne) {
       setAll(0,0,0);
     }
   }
 
  delay(SpeedDelay);
}

void Sparkle(byte red, byte green, byte blue, int SpeedDelay) {
  int Pixel = random(strip.numPixels());
  setPixel(Pixel,red,green,blue);
  if (!checkIfPreInstalledActive(12)) return;
  strip.show();
  delay(SpeedDelay);
  setPixel(Pixel,0,0,0);
}

void SnowSparkle(byte red, byte green, byte blue, int SparkleDelay, int SpeedDelay) {
  setAll(red,green,blue);
 
  int Pixel = random(strip.numPixels());
  setPixel(Pixel,0xff,0xff,0xff);
  strip.show();
  delay(SparkleDelay);
  setPixel(Pixel,red,green,blue);
  if (!checkIfPreInstalledActive(13)) return;
  strip.show();
  delay(SpeedDelay);
}

void RunningLights(byte red, byte green, byte blue, int WaveDelay) {
  int Position=0;
 
  for(int i=0; i<strip.numPixels()*2; i++)
  {
      Position++; // = 0; //Position + Rate;
      for(int i=0; i<strip.numPixels(); i++) {
        // sine wave, 3 offset waves make a rainbow!
        //float level = sin(i+Position) * 127 + 128;
        //setPixel(i,level,0,0);
        //float level = sin(i+Position) * 127 + 128;
        setPixel(i,((sin(i+Position) * 127 + 128)/255)*red,
                   ((sin(i+Position) * 127 + 128)/255)*green,
                   ((sin(i+Position) * 127 + 128)/255)*blue);
      }
      if (!checkIfPreInstalledActive(14)) return;
      strip.show();
      delay(WaveDelay);
  }
}

void Fire(int Cooling, int Sparking, int SpeedDelay) {
  static byte heat[32];
  int cooldown;
 
  // Step 1.  Cool down every cell a little
  for( int i = 0; i < strip.numPixels() && i < 32; i++) {
    cooldown = random(0, ((Cooling * 10) / strip.numPixels()) + 2);
   
    if(cooldown>heat[i]) {
      heat[i]=0;
    } else {
      heat[i]=heat[i]-cooldown;
    }
  }
 
  // Step 2.  Heat from each cell drifts 'up' and diffuses a little
  for( int k= strip.numPixels() - 1; k >= 2; k--) {
    heat[k] = (heat[k - 1] + heat[k - 2] + heat[k - 2]) / 3;
  }
   
  // Step 3.  Randomly ignite new 'sparks' near the bottom
  if( random(255) < Sparking ) {
    int y = random(7);
    heat[y] = heat[y] + random(160,255);
    //heat[y] = random(160,255);
  }

  // Step 4.  Convert heat to LED colors
  for( int j = 0; j < strip.numPixels(); j++) {
    setPixelHeatColor(j, heat[j] );
  }
  if (!checkIfPreInstalledActive(15)) return;
  strip.show();
  delay(SpeedDelay);
}

void setPixelHeatColor (int Pixel, byte temperature) {
  // Scale 'heat' down from 0-255 to 0-191
  byte t192 = round((temperature/255.0)*191);
 
  // calculate ramp up from
  byte heatramp = t192 & 0x3F; // 0..63
  heatramp <<= 2; // scale up to 0..252
 
  // figure out which third of the spectrum we're in:
  if( t192 > 0x80) {                     // hottest
    setPixel(Pixel, 255, 255, heatramp);
  } else if( t192 > 0x40 ) {             // middle
    setPixel(Pixel, 255, heatramp, 0);
  } else {                               // coolest
    setPixel(Pixel, heatramp, 0, 0);
  }
}

void BouncingColoredBalls(int BallCount, byte colors[][3], boolean continuous) {
  float Gravity = -9.81;
  int StartHeight = 1;
 
  float Height[BallCount];
  float ImpactVelocityStart = sqrt( -2 * Gravity * StartHeight );
  float ImpactVelocity[BallCount];
  float TimeSinceLastBounce[BallCount];
  int   Position[BallCount];
  long  ClockTimeSinceLastBounce[BallCount];
  float Dampening[BallCount];
  boolean ballBouncing[BallCount];
  boolean ballsStillBouncing = true;
 
  for (int i = 0 ; i < BallCount ; i++) {  
    ClockTimeSinceLastBounce[i] = millis();
    Height[i] = StartHeight;
    Position[i] = 0;
    ImpactVelocity[i] = ImpactVelocityStart;
    TimeSinceLastBounce[i] = 0;
    Dampening[i] = 0.90 - float(i)/pow(BallCount,2);
    ballBouncing[i]=true;
  }

  while (ballsStillBouncing) {
    for (int i = 0 ; i < BallCount ; i++) {
      TimeSinceLastBounce[i] =  millis() - ClockTimeSinceLastBounce[i];
      Height[i] = 0.5 * Gravity * pow( TimeSinceLastBounce[i]/1000 , 2.0 ) + ImpactVelocity[i] * TimeSinceLastBounce[i]/1000;
 
      if ( Height[i] < 0 ) {                      
        Height[i] = 0;
        ImpactVelocity[i] = Dampening[i] * ImpactVelocity[i];
        ClockTimeSinceLastBounce[i] = millis();
 
        if ( ImpactVelocity[i] < 0.01 ) {
          if (continuous) {
            ImpactVelocity[i] = ImpactVelocityStart;
          } else {
            ballBouncing[i]=false;
          }
        }
      }
      Position[i] = round( Height[i] * (strip.numPixels() - 1) / StartHeight);
    }

    ballsStillBouncing = false; // assume no balls bouncing
    for (int i = 0 ; i < BallCount ; i++) {
      setPixel(Position[i],colors[i][0],colors[i][1],colors[i][2]);
      if ( ballBouncing[i] ) {
        ballsStillBouncing = true;
      }
    }
    if (!checkIfPreInstalledActive(16)) return;
    strip.show();
    setAll(0,0,0);
  }
}

void meteorRain(byte red, byte green, byte blue, byte meteorSize, byte meteorTrailDecay, boolean meteorRandomDecay, int SpeedDelay) {  
  setAll(0,0,0);
 
  for(int i = 0; i < 2*strip.numPixels(); i++) {
   
   
    // fade brightness all LEDs one step
    for(int j=0; j<strip.numPixels(); j++) {
      if( (!meteorRandomDecay) || (random(10)>5) ) {
        fadeToBlack(j, meteorTrailDecay );        
      }
    }
   
    // draw meteor
    for(int j = 0; j < meteorSize; j++) {
      if( ( i-j <strip.numPixels()) && (i-j>=0) ) {
        setPixel(i-j, red, green, blue);
      }
    }

    if (!checkIfPreInstalledActive(17)) return;
    strip.show();
    delay(SpeedDelay);
  }
}

// used by meteorrain
void fadeToBlack(int ledNo, byte fadeValue) {
 #ifdef ADAFRUIT_NEOPIXEL_H
    // NeoPixel
    uint32_t oldColor;
    uint8_t r, g, b;
    int value;
   
    oldColor = strip.getPixelColor(ledNo);
    r = (oldColor & 0x00ff0000UL) >> 16;
    g = (oldColor & 0x0000ff00UL) >> 8;
    b = (oldColor & 0x000000ffUL);

    r=(r<=10)? 0 : (int) r-(r*fadeValue/256);
    g=(g<=10)? 0 : (int) g-(g*fadeValue/256);
    b=(b<=10)? 0 : (int) b-(b*fadeValue/256);
   
    strip.setPixelColor(ledNo, r,g,b);
 #endif
 #ifndef ADAFRUIT_NEOPIXEL_H
   // FastLED
   leds[ledNo].fadeToBlackBy( fadeValue );
 #endif  
}

// *** REPLACE TO HERE ***



// ***************************************
// ** FastLed/NeoPixel Common Functions **
// ***************************************


// Set a LED color (not yet visible)
void setPixel(int Pixel, byte red, byte green, byte blue) {
   strip.setPixelColor(Pixel, strip.Color(red, green, blue));
}

// Set all LEDs to a given color and apply it (visible)
void setAll(byte red, byte green, byte blue) {
  strip.fill(strip.Color(red, green, blue));
  strip.show();
}
