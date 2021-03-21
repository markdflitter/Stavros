package com.example.stavros;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.os.Handler;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.lang.ref.WeakReference;
import java.util.Set;




public class MainActivity extends AppCompatActivity {


    private static class myHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public myHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            final MainActivity activity = mActivity.get();

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            activity.setStatus("Connected");
                            activity.enableControls (true);
                            activity.setResponse("");
                            break;
                        case Constants.STATE_CONNECTING:
                            activity.setStatus("Connecting");
                            activity.enableControls (false);
                            activity.setResponse("");
                            break;
                        case Constants.STATE_NONE:
                            activity.setStatus("Not connected");
                            activity.enableControls (false);
                            activity.setResponse("");
                            break;
                        case Constants.STATE_ERROR:
                            activity.setStatus("Error : " + msg.getData().getString(Constants.MSG));
                            activity.enableControls (false);
                            activity.setResponse("");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    activity.setResponse(readMessage);
                    break;
            }
        }
    }


    private final static int REQUEST_ENABLE_BT = 1;

    public BluetoothAdapter bluetoothAdapter;
    private BluetoothService bluetoothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableControls (false);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {

            bluetoothService.stop();
            bluetoothService = null;
        }

}
    public void connectClicked(View view) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                if (deviceName.equals("DSD TECH HC-05")) {
                    if (bluetoothService != null) {
                        bluetoothService.stop();
                        bluetoothService = null;
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                        }
                    }

                    myHandler handler = new myHandler(this);

                    bluetoothService = new BluetoothService(handler, device);
                    bluetoothService.connect();
                }
            }
        }
    }

    public void disconnectClicked(View view) {
        if (bluetoothService != null) {
            bluetoothService.stop();
            bluetoothService = null;
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }

        setStatus ("Not connected");
        setResponse ("");
    }

    private void write(char b) {
        if (bluetoothService != null) {
            byte[] bytes = new byte[]{(byte) b};
            bluetoothService.write(bytes);
        }
    }




    public void exterminateClicked(View view) {
        write('3');
    }

    public void onOffClicked(View view) {
        Switch toggle = (Switch) findViewById(R.id.on_switch);
        if (toggle.isChecked()) {
            write('1');
        } else {
            write('0');
        }
    }

    public void setStatus(String st) {
        TextView statusBox = (TextView) findViewById(R.id.status);
        statusBox.setText(st);

        if (st.equals("Connected")) {
            write('2');

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }

            write('2');
        }
    }

    public void setResponse(String st) {
        TextView response = (TextView) findViewById(R.id.response);
        response.setText(st);
            if (st.equals("STAVROS ON\r\n") || st.equals("TAVROS ON\r\n")) {
            Switch toggle = (Switch) findViewById(R.id.on_switch);
            toggle.setChecked(true);
        } else if (st.equals("STAVROS OFF\r\n") || st.equals("TAVROS OFF\r\n")) {
            Switch toggle = (Switch) findViewById(R.id.on_switch);
            toggle.setChecked(false);
        }
    }

    public void enableControls (boolean enabled) {
        Switch toggle = (Switch) findViewById(R.id.on_switch);
        toggle.setEnabled  (enabled);

        if (!enabled)
        {
            toggle.setChecked(false);
        }

        Button exterminate = (Button) findViewById(R.id.exterminate_button);
        exterminate.setEnabled  (enabled);
    }
}
