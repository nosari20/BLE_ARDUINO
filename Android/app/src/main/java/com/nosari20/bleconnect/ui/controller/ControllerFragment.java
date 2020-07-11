package com.nosari20.bleconnect.ui.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.nosari20.bleconnect.R;
import com.nosari20.bleconnect.widgets.JoystickWidget;

public class ControllerFragment extends Fragment {

    private ControllerViewModel controllerViewModel;

    private JoystickWidget lr_joystick, fb_joystick;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        controllerViewModel = new ControllerViewModel();

        View root = inflater.inflate(R.layout.fragment_controller, container, false);




        // Forward/backward
        fb_joystick = (JoystickWidget) root.findViewById(R.id.fb_joystick);
        JoystickWidget.IsJoystickChanged fb_isJoystickChanged = new JoystickWidget.IsJoystickChanged() {
            @Override
            public boolean compare(double old_angle, int old_power, JoystickWidget.Direction old_direction, double new_angle, int new_power, JoystickWidget.Direction new_direction) {
                if(old_direction == JoystickWidget.Direction.LEFT || old_direction == JoystickWidget.Direction.RIGHT) return true;
                if(Math.abs(old_angle) <= 90 &&  Math.abs(new_angle) > 90) return true;

                if(Math.abs(old_angle) >= 90 &&  Math.abs(new_angle) < 90) return true;

                if(old_power >= 50 &&  new_power < 50) return true;
                if(old_power <= 50 &&  new_power > 50) return true;

                return false;
            }
        };
        fb_joystick.setIsJoystickChanged(fb_isJoystickChanged);
        fb_joystick.setOnJoystickMoveListener(new JoystickWidget.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(double angle, int power, JoystickWidget.Direction direction) {
                if(direction == JoystickWidget.Direction.NONE){
                    controllerViewModel.send("S");
                }else{
                    if(
                            Math.abs(angle) <= 90
                    ){
                        controllerViewModel.send("F");
                    }else{
                        controllerViewModel.send("B");
                    }

                }

            }
        }, JoystickWidget.DEFAULT_LOOP_INTERVAL);


        // left/right
        lr_joystick = (JoystickWidget) root.findViewById(R.id.lr_joystick);
        JoystickWidget.IsJoystickChanged lr_isJoystickChanged = new JoystickWidget.IsJoystickChanged() {
            @Override
            public boolean compare(double old_angle, int old_power, JoystickWidget.Direction old_direction, double new_angle, int new_power, JoystickWidget.Direction new_direction) {

                if(old_direction == JoystickWidget.Direction.FRONT || old_direction == JoystickWidget.Direction.BOTTOM) return true;
                if(old_angle <= 0 &&  new_angle > 0) return true;
                if(old_angle >= 0 &&  new_angle < 0) return true;

                if(old_power >= 50 &&  new_power < 50) return true;
                if(old_power <= 50 &&  new_power > 50) return true;

                return false;
            }
        };

        lr_joystick.setIsJoystickChanged(lr_isJoystickChanged);
        lr_joystick.setOnJoystickMoveListener(new JoystickWidget.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(double angle, int power, JoystickWidget.Direction direction) {
                if(direction == JoystickWidget.Direction.NONE){
                    controllerViewModel.send("S");
                }else{
                    if(angle > 0){
                        controllerViewModel.send("R");
                    }else{
                        controllerViewModel.send("L");
                    }
                }

            }
        }, JoystickWidget.DEFAULT_LOOP_INTERVAL);




        return root;
    }
}
