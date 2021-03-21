package com.example.stavros;

import java.util.UUID;

public interface Constants {
    String APP_NAME = "Stavros Controller";

    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;

    int STATE_NONE = 0;
    int STATE_ERROR = 1;
    int STATE_CONNECTING = 2;
    int STATE_CONNECTED = 3;

    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    String MSG = "msg";
}
