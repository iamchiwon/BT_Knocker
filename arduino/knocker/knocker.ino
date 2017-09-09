#include "bt_enabler.h"

int ledFail = 10;
int ledSuccess = 11;
int buttonSend = 6;

void setup()
{
  Serial.begin(9600);

  pinMode(ledFail, OUTPUT);
  pinMode(ledSuccess, OUTPUT);
  pinMode(buttonSend, INPUT);

  digitalWrite(ledFail, HIGH);
  digitalWrite(ledSuccess, LOW);
  setupBluetoothUntilSuccess();

  digitalWrite(ledFail, LOW);
  digitalWrite(ledSuccess, HIGH);
}

int buttonLastState = LOW;

void loop()
{
  int buttonCurrentState = digitalRead(buttonSend);

  if (buttonLastState == LOW && buttonCurrentState == HIGH) {
    //push button
    digitalWrite(ledFail, HIGH);
    sendSignal("A");
  }
  else if (buttonLastState == HIGH && buttonCurrentState == LOW) {
    //release button
    digitalWrite(ledFail, LOW);
  }

  if(BT.available()) {       
    Serial.write(BT.read());
  }
  if(Serial.available()) {         
    BT.write(Serial.read());
  }
  
  buttonLastState = buttonCurrentState;
  delay(100);
}

