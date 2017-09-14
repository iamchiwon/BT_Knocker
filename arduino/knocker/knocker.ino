#include "bt_enabler.h"

int ledFail = 10;
int ledSuccess = 11;
int buttonSend = 6;
int speaker = 8;

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

  if (BT.available()) {
    int r = BT.read();
    Serial.println(r);
    alarm();
  }

  buttonLastState = buttonCurrentState;
  delay(100);
}

const int alarmHerz = 440;
const int alarmDelay = 3000;
void alarm() {
  tone(speaker, alarmHerz, alarmDelay);
}

