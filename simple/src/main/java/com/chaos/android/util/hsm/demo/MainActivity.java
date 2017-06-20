/*
 * Copyright 2017 Chaos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chaos.android.util.hsm.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLog = (TextView) findViewById(R.id.log);

        LogWriter writer = new LogWriterImpl();
        Hsm1 hsm = Hsm1.makeHsm1(writer);
        synchronized (hsm) {
            hsm.sendMessage(hsm.obtainMessage(hsm.CMD_1));
            hsm.sendMessage(hsm.obtainMessage(hsm.CMD_2));
            try {
                // wait for the messages to be handled
                hsm.wait();
            } catch (InterruptedException e) {
                Log.e(hsm.getName(), "exception while waiting " + e.getMessage());
                writer.write("exception while waiting " + e.getMessage());
            }
        }
    }

    private class LogWriterImpl implements LogWriter {
        @Override
        public void write(CharSequence log) {
            mLog.append(log);
            mLog.append("\n");
        }
    }
}
