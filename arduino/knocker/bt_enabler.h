#include <SoftwareSerial.h>

const char bt_name[] = "KNOCKER_BT";
const char bt_pwd[]  = "0000";

int bTx = 2;
int bRx = 3;
SoftwareSerial BT(bTx, bRx);

bool sendMessage(String command) {
  BT.print(command);
  String result = BT.readString();
  if (result.startsWith("OK")) return true;
  return false;
}

bool btSetUp() {
  BT.begin(9600);

  String settingState;
  Serial.println("Processing...");

  settingState = "Handshake";
  if (!sendMessage("AT")) goto ERROR;

  settingState = "Set Name";
  if (!sendMessage(String("AT+NAME") + bt_name)) goto ERROR;

  settingState = "Set Password";
  if (!sendMessage(String("AT+PIN") + bt_pwd)) goto ERROR;

  // 1:1200, 2:2400, 3:4800, 4:9600, 5:19200, 6:28400, 7:57600, 8:115200
  settingState = "Set Speed";
  if (!sendMessage(String("AT+BAUD4"))) goto ERROR;

  Serial.print("BT_NAME : ");
  Serial.println(bt_name);
  Serial.print("BT_PWD  : ");
  Serial.println(bt_pwd);
  Serial.println("BT_BAUD : 9600");
  Serial.println("Setting Complete!");
  return true;

ERROR:
  Serial.println("Setting Fail : " + settingState);
  return false;
}

void setupBluetoothUntilSuccess() {
  bool btSetupSuccess = false;
  while ( !btSetupSuccess ) {
    btSetupSuccess = btSetUp();
    if (btSetupSuccess) {
      Serial.println("OK");
      break;
    }
    Serial.println("FAIL");
    delay(5000); //5s
  }
  return btSetupSuccess;
}

void sendSignal(String msg) {
  BT.println(msg);
}
