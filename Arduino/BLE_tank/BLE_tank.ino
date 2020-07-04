
// BEAU RATE
#define BAUD_RATE 115200
// END OF LINE CHAR
#define EOL '\n'


#include <AFMotor.h>

AF_DCMotor MotorR1(2);
AF_DCMotor MotorR2(1);
AF_DCMotor MotorL1(3);
AF_DCMotor MotorL2(4);






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



  // MOTORS CONFIGURATION
  Serial.println("Configuring motors");
  MotorR1.setSpeed(255);
  MotorR2.setSpeed(255);
  MotorL1.setSpeed(255);
  MotorL2.setSpeed(255);



  Serial.println("Setup done");

}



void forward() {
  MotorR1.run(FORWARD);
  MotorR2.run(FORWARD);
  MotorL1.run(FORWARD);
  MotorL2.run(FORWARD);
}

void backward() {
  MotorR1.run(BACKWARD);
  MotorR2.run(BACKWARD);
  MotorL1.run(BACKWARD);
  MotorL2.run(BACKWARD);
}

void stop() {
  MotorR1.run(RELEASE);
  MotorR2.run(RELEASE);
  MotorL1.run(RELEASE);
  MotorL2.run(RELEASE);
}

void right() {
  MotorR1.run(BACKWARD);
  MotorR2.run(BACKWARD);
  MotorL1.run(FORWARD);
  MotorL2.run(FORWARD);
}

void left() {
  MotorR1.run(FORWARD);
  MotorR2.run(FORWARD);
  MotorL1.run(BACKWARD);
  MotorL2.run(BACKWARD);
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

      if(command == "F") {
        forward();
      }
      else if (command == "B"){
        backward();
      }
      else if (command == "R"){
        right();
      }
      else if (command == "L"){
        left();
      }
      else if (command == "S"){
        stop();
      }
    
    command = "";
  }

}
