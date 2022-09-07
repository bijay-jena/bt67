package com.bt67;

import java.util.UUID;

public interface Constants {
    static final String TAG = "bt";
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    static final String APP_NAME = "transfer";
    static final UUID MY_UUID = UUID.fromString("e890b1c4-2da3-11ed-a261-0242ac120002");
}
