## Android Hierarchical State Machine

The state machine defined here is a hierarchical state machine which processes messages and can have states arranged hierarchically.

## Dependency

``` Groovy
repositories {
    maven {
        jcenter()
    }
}

dependencies {
   compile 'com.chaos.android.util:hsm:1.0.0'
}
```

## Usage

### Step 1:

Define `State` and implements `processMessage` method:

``` java
class P1 extends State {
    @Override
    public void enter() {
        log("mP1.enter");
    }

    @Override
    public boolean processMessage(Message message) {
        boolean retVal;
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
        switch (message.what) {
            case (CMD_2):
                sendMessage(obtainMessage(CMD_4));
                retVal = HANDLED;
                break;
            case (CMD_3):
                transitionToHaltingState();
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
```

> Note: if a child state is unable to handle a message it may have the message processed by its parent by returning false or `NOT_HANDLED`.

### Step 2:

Define a `StateMachine` and add your state hierarchy:

``` java
class Hsm1 extends StateMachine {

    public static final int CMD_1 = 1;
    public static final int CMD_2 = 2;
    public static final int CMD_3 = 3;

    P1 mP1 = new P1();
    S1 mS1 = new S1();
    S2 mS2 = new S2();
    P2 mP2 = new P2();

    Hsm1(String name) {
        super(name);
        // Add states, use indentation to show hierarchy
        addState(mP1);
        addState(mS1, mP1);
        addState(mS2, mP1);
        // Set the initial state
        setInitialState(mS1);
    }

    @Override
    protected void onHalting() {
        log("onHalting");
    }
}
```

### Step 3:

Start the `StateMachine`:

``` java
public static Hsm1 makeHsm1() {
    Hsm1 sm = new Hsm1("hsm1");
    sm.start();
    return sm;
}
```

### Step 4 (Optional):

Send message to the `StateMachine`:

``` java
hsm.sendMessage(hsm.obtainMessage(Hsm1.CMD_1));
```

More information: [javadoc](https://chaosleong.github.io/AndroidStateMachine/).

## License


    Copyright 2017 Chaos
    Copyright (C) 2009 The Android Open Source Project

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.