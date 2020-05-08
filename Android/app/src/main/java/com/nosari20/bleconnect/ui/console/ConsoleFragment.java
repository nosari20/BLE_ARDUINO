package com.nosari20.bleconnect.ui.console;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.nosari20.bleconnect.R;

public class ConsoleFragment extends Fragment {

    private ConsoleViewModel consoleViewModel;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        consoleViewModel = new ConsoleViewModel();

        View root = inflater.inflate(R.layout.fragment_console, container, false);

        final TextView consoleOutput = root.findViewById(R.id.console_output);
        consoleViewModel.getConsoleOutput().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                consoleOutput.setText(s);
            }
        });

        final EditText consoleInput = root.findViewById(R.id.console_input);
        consoleInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        final Button consoleSend = root.findViewById(R.id.console_send);

        final ScrollView scrollView = root.findViewById(R.id.scroll_output);
        consoleSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consoleViewModel.send(consoleInput.getText().toString());
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });


        return root;
    }
}
