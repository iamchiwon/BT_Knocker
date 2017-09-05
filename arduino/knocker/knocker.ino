#include "bt_enabler.h"

void setup()
{
  Serial.begin(9600);

  if ( btSetUp() ) {
    Serial.println("OK");
  } else {
    Serial.println("FAIL");
  }
}

void loop()
{
}
