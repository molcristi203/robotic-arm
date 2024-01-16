#include <SoftwareSerial.h>
#include <Servo.h>
#include <UTFT.h>
#include <URTouch.h>

Servo grip;
Servo pitch;
Servo roll;
Servo elbow;
Servo shoulder;
Servo waist;

boolean sliderPressed = false;

SoftwareSerial btSerial(10, 11); //10 = RX on board, 11 = TX on board

UTFT myGLCD(ILI9341_8,38,39,40,41);
URTouch myTouch(6, 5, 4, 3, 2);

extern uint8_t BigFont[];

String dataString = "";
boolean stringComplete;

void setup() {
  // put your setup code here, to run once:
  btSerial.begin(9600);
  Serial.begin(9600);
  btSerial.listen();
  dataString.reserve(100);
  
  myGLCD.InitLCD(PORTRAIT);
  myGLCD.clrScr();
  myGLCD.setColor(255, 255, 255);
  myGLCD.setFont(BigFont);
  
  grip.attach(14);
  grip.write(45);
  pitch.attach(15);
  pitch.write(90);
  roll.attach(16);
  roll.write(90);
  elbow.attach(17);
  elbow.write(90);
  shoulder.attach(18);
  shoulder.write(110);
  waist.attach(19);
  waist.write(90);

  myTouch.InitTouch(PORTRAIT);
  myTouch.setPrecision(PREC_MEDIUM);

  drawSliders();
}

void loop() {
  while (btSerial.available() > 0) {
    char data = btSerial.read();
    if (data != '\n') {
      dataString += data;
    }
    else {
      stringComplete = true;
    }
  }

  if (stringComplete) {
    Serial.println(dataString);
    
    String motor = dataString.substring(0, 2);
    int angle = dataString.substring(3, dataString.length()).toInt();

    //Serial.print(motor);
    //Serial.println(angle);

    if (motor == "m1")
    {
      grip.write(angle);
      drawSlider2(10, 230, 25, 45, 0, 180, angle);
    }
    else if (motor == "m2")
    {
      drawSlider2(10, 230, 70, 90, 0, 180, angle);
      pitch.write(angle);
    }
    else if (motor == "m3")
    {
      drawSlider2(10, 230, 115, 135, 0, 180, angle);
      roll.write(angle);
    }
    else if (motor == "m4")
    {
      drawSlider2(10, 230, 160, 180, 0, 180, angle);
      elbow.write(angle);
    }
    else if (motor == "m5")
    {
      drawSlider2(10, 230, 205, 225, 0, 180, angle);
      shoulder.write(angle);
    }
    else if (motor == "m6")
    {
      drawSlider2(10, 230, 250, 270, 0, 180, angle);
      waist.write(angle);
    }
    
    stringComplete = false;
    dataString = "";
  }

  if (myTouch.dataAvailable()) {
    myTouch.read();
    int x = myTouch.getX();
    int y = myTouch.getY();

    if (x > 10 && x < 230 && y > 25 && y < 45) {
      int angle = drawSlider(10, 230, 25, 45, 0, 180, x);
      grip.write(angle);
    }

    if (x > 10 && x < 230 && y > 70 && y < 90) {
      int angle = drawSlider(10, 230, 70, 90, 0, 180, x);
      pitch.write(angle);
    }

    if (x > 10 && x < 230 && y > 115 && y < 135) {
      int angle = drawSlider(10, 230, 115, 135, 0, 180, x);
      roll.write(angle);
    }

    if (x > 10 && x < 230 && y > 160 && y < 180) {
      int angle = drawSlider(10, 230, 160, 180, 0, 180, x);
      elbow.write(angle);
    }

    if (x > 10 && x < 230 && y > 205 && y < 225) {
      int angle = drawSlider(10, 230, 205, 225, 0, 180, x);
      shoulder.write(angle);
    }

    if (x > 10 && x < 230 && y > 250 && y < 270) {
      int angle = drawSlider(10, 230, 250, 270, 0, 180, x);
      sliderPressed = true;
      waist.write(angle);
    }
  }
  else
  {
    if (sliderPressed) {
      sliderPressed = false;
      waist.write(90);
      drawSlider(10, 230, 250, 270, 0, 180, 120);
    }
  }
}

int drawSlider(int minX, int maxX, int minY, int maxY, int minValue, int maxValue, int value) {
  myGLCD.setColor(VGA_BLACK);
  myGLCD.fillRect(minX, minY, maxX, maxY);
  myGLCD.setColor(VGA_WHITE);
  myGLCD.drawRect(minX, minY, maxX, maxY);
  
  myGLCD.fillRect(max(value - 4, minX), minY, min(value + 4, maxX), maxY);

  return map(value, minX, maxX, minValue, maxValue);
}

void drawSlider2(int minX, int maxX, int minY, int maxY, int minValue, int maxValue, int angle) {
  myGLCD.setColor(VGA_BLACK);
  myGLCD.fillRect(minX, minY, maxX, maxY);
  myGLCD.setColor(VGA_WHITE);
  myGLCD.drawRect(minX, minY, maxX, maxY);

  int value = map(angle, minValue, maxValue, minX, maxX);
  
  myGLCD.fillRect(max(value - 4, minX), minY, min(value + 4, maxX), maxY);
}

void drawSliders() {
  myGLCD.print("Grip", 10, 5);
  drawSlider(10, 230, 25, 45, 0, 180, 60);
  
  myGLCD.print("Wrist Pitch", 10, 50);
  drawSlider(10, 230, 70, 90, 0, 180, 120);
  
  myGLCD.print("Wrist Roll", 10, 95);
  drawSlider(10, 230, 115, 135, 0, 180, 120);

  myGLCD.print("Elbow", 10, 140);
  drawSlider(10, 230, 160, 180, 0, 180, 120);

  myGLCD.print("Shoulder", 10, 185);
  drawSlider(10, 230, 205, 225, 0, 180, 120);

  myGLCD.print("Waist", 10, 230);
  drawSlider(10, 230, 250, 270, 0, 180, 120);
}
