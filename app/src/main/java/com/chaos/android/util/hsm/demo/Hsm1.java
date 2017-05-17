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

import android.os.Message;
import android.util.Log;

import com.chaos.android.util.hsm.State;
import com.chaos.android.util.hsm.StateMachine;

class Hsm1 extends StateMachine {

    public static final int CMD_1 = 1;
    public static final int CMD_2 = 2;
    public static final int CMD_3 = 3;
    public static final int CMD_4 = 4;
    public static final int CMD_5 = 5;

    private LogWriter mLogWriter;

    public static Hsm1 makeHsm1(LogWriter writer) {
        Log.d("hsm1", "makeHsm1 E");
        writer.write("makeHsm1 E");
        Hsm1 sm = new Hsm1("hsm1", writer);
        sm.start();
        writer.write("makeHsm1 X");
        Log.d("hsm1", "makeHsm1 X");
        return sm;
    }

    Hsm1(String name, LogWriter writer) {
        super(name);
        mLogWriter = writer;
        log("ctor E");
        // Add states, use indentation to show hierarchy
        addState(mP1);
        addState(mS1, mP1);
        addState(mS2, mP1);
        addState(mP2);
        // Set the initial state
        setInitialState(mS1);
        log("ctor X");
    }

    class P1 extends State {
        @Override
        public void enter() {
            log("mP1.enter");
        }

        @Override
        public boolean processMessage(Message message) {
            boolean retVal;
            log("mP1.processMessage what = " + message.what);
            switch (message.what) {
                case CMD_2:
                    // CMD_2 will arrive in mS2 before CMD_3
                    sendMessage(obtainMessage(CMD_3));
                    deferMessage(message);
                    transitionTo(mS2);
                    retVal = HANDLED;
                    break;
                default:
                    // Any message we don't understand in this state invokes unhandledMessage
                    retVal = NOT_HANDLED;
                    break;
            }
            return retVal;
        }

        @Override
        public void exit() {
            log("mP1.exit");
        }
    }

    class S1 extends State {
        @Override
        public void enter() {
            log("mS1.enter");
        }

        @Override
        public boolean processMessage(Message message) {
            log("S1.processMessage what = " + message.what);
            if (message.what == CMD_1) {
                // Transition to ourself to show that enter/exit is called
                transitionTo(mS1);
                return HANDLED;
            } else {
                // Let parent process all other messages
                return NOT_HANDLED;
            }
        }

        @Override
        public void exit() {
            log("mS1.exit");
        }
    }

    class S2 extends State {
        @Override
        public void enter() {
            log("mS2.enter");
        }

        @Override
        public boolean processMessage(Message message) {
            boolean retVal;
            log("mS2.processMessage what = " + message.what);
            switch (message.what) {
                case (CMD_2):
                    sendMessage(obtainMessage(CMD_4));
                    retVal = HANDLED;
                    break;
                case (CMD_3):
                    deferMessage(message);
                    transitionTo(mP2);
                    retVal = HANDLED;
                    break;
                default:
                    retVal = NOT_HANDLED;
                    break;
            }
            return retVal;
        }

        @Override
        public void exit() {
            log("mS2.exit");
        }
    }

    class P2 extends State {
        @Override
        public void enter() {
            log("mP2.enter");
            sendMessage(obtainMessage(CMD_5));
        }

        @Override
        public boolean processMessage(Message message) {
            log("P2.processMessage what = " + message.what);
            switch (message.what) {
                case (CMD_3):
                    break;
                case (CMD_4):
                    break;
                case (CMD_5):
                    transitionToHaltingState();
                    break;
            }
            return HANDLED;
        }

        @Override
        public void exit() {
            log("mP2.exit");
        }
    }

    @Override
    protected void onHalting() {
        log("halting");
        synchronized (this) {
            this.notifyAll();
        }
    }

    P1 mP1 = new P1();
    S1 mS1 = new S1();
    S2 mS2 = new S2();
    P2 mP2 = new P2();

    @Override
    protected void log(String s) {
        super.log(s);
        mLogWriter.write(s);
    }
}