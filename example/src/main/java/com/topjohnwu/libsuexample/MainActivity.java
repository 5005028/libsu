package com.topjohnwu.libsuexample;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private TextView console;
    private EditText input;
    private ScrollView sv;
    private Shell.Output output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        console = findViewById(R.id.console);
        input = findViewById(R.id.cmd_input);
        sv = findViewById(R.id.sv);

        Button sync_cmd = findViewById(R.id.sync_cmd);
        Button async_cmd = findViewById(R.id.async_cmd);
        Button close_shell = findViewById(R.id.close_shell);
        Button sync_script = findViewById(R.id.sync_script);
        Button async_script = findViewById(R.id.async_script);
        Button clear = findViewById(R.id.clear);

        // Run the shell command in the input box synchronously
        sync_cmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shell.sh(input.getText().toString()).to(output).exec();
                input.setText("");
            }
        });

        // Run the shell command in the input box asynchronously.
        // Also demonstrates that Async.Callback works
        async_cmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shell.sh(input.getText().toString())
                        .to(output)
                        .onResult(new Shell.ResultCallback() {
                            @Override
                            public void onResult(Shell.Output out) {
                                Log.d(ExampleApp.TAG, "async_cmd_result");
                            }
                        }).enqueue();
                input.setText("");
            }
        });

        // Closing a shell is always synchronous
        close_shell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Shell.getShell().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Load a script from raw resources synchronously
        sync_script.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shell.sh(getResources().openRawResource(R.raw.info)).to(output).exec();
            }
        });

        // Load a script from raw resources asynchronously
        async_script.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shell.sh(getResources().openRawResource(R.raw.count))
                        .to(output)
                        .onResult(new Shell.ResultCallback() {
                            @Override
                            public void onResult(Shell.Output out) {
                                Log.d(ExampleApp.TAG, "async_script_result");
                            }
                        }).enqueue();
                }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output.getOut().clear();
            }
        });

        /* Create a CallbackList to update the UI with Shell output
         * Here I demonstrate 2 ways to implement a CallbackList
         * Use either ContainerCallbackList or StringBuilderCallbackList
         * Both implementation will have the same result
         */
        output = new Shell.Output(new ContainerCallbackList(new ArrayList<String>()));
    }

    private class ContainerCallbackList extends CallbackList<String> {

        private ContainerCallbackList(List<String> l) {
            super(l);
        }

        @Override
        public void onAddElement(String s) {
            console.setText(TextUtils.join("\n", this));
            sv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sv.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }, 10);
        }

        @Override
        public void clear() {
            super.clear();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    console.setText("");
                }
            });
        }
    }

    private class StringBuilderCallbackList extends CallbackList<String> {

        private StringBuilder builder;

        private StringBuilderCallbackList() {
            builder = new StringBuilder();
        }

        @Override
        public void onAddElement(String s) {
            builder.append(s).append('\n');
            console.setText(builder);
            sv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sv.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }, 10);
        }

        @Override
        public synchronized void clear() {
            builder = new StringBuilder();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    console.setText("");
                }
            });
        }
    }
}
