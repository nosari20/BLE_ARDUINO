
// BEAU RATE
#define BAUD_RATE 115200
// END OF LINE CHAR
#define EOL '\n'






void setup() {
  Serial.begin(BAUD_RATE);

  
  Serial.println("Setup starting");


  // BLUETOOTH CONFIGURATION
  Serial.println("Configuring Bluetooth device");
  delay(500);
  Serial.write("+++");
  delay(2000);
  Serial.write("AT+SETTING=DEFAULT\r\n");
  delay(1000);
  Serial.write("AT+NAME=BT_UNO\r\n");
  delay(1000);
  Serial.write("AT+EXIT\r\n");
  delay(1000);



  // PIN CONFIGURATION
  Serial.println("Configuring pins");
  pinMode(13, OUTPUT);
  digitalWrite(13, HIGH);



  Serial.println("Setup done");

}





// BLUETOOTH CHAR INPUT
char bluetooth_char = 0;
// BLUETOOTH LINE INPUT
String bluetooth_line = "";
// COMMAND
String command = "";

void loop() {


  // HANDLE INPUT
  if(Serial.available()){
    // Read bluetooth serial and store the value in bluetooth_char
    bluetooth_char = Serial.read();
    // Check if EOL
    if(bluetooth_char != EOL) {
      // Add bluetooth_char to bluetooth_line
      bluetooth_line = bluetooth_line + bluetooth_char;
      // end loop function
      return;
    }else{
      // Store bluetooth_line in command
      command = bluetooth_line;
      // Reset bluetooth_line
      bluetooth_line = "";
    }
  }

  // HANDLE COMMAND
  if(command == ""){
    return;
  }else{
    
    Serial.println("Command : " + command);

    if (command == "UP") {
      digitalWrite(13, HIGH);
    } else if (command == "DOWN") {
      digitalWrite(13, LOW);
    }
    
    command = "";
  }

}
