package com.mareschema.dmparmproject;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//Class that given an open BT Socket will
//Open, manage and close the data Stream from the Arduino BT device
public class ConnectedThread extends Thread {

    private static final String TAG = "FrugalLogs";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String valueRead;
    private String valueWriiten;

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
        //Input and Output streams members of the class
        //We wont use the Output stream of this project
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public String getValueRead(){
        return valueRead;
    }

    public String getValueWriiten(){
        return valueWriiten;
    }

    public void run() {

        byte[] buffer = new byte[1024];
        int bytes = 0;

        while(true){
            try{
                bytes = mmInStream.read(buffer);
                String valueRead = new String(buffer, 0, bytes);
            } catch (IOException exception) {
                Log.e(TAG, "Error reading from the input stream", exception);
                break;
            }
        }
    }

    public void sendCommandToArduino(String command){
        try{
            mmOutStream.write(command.getBytes());
            valueWriiten = command;
        } catch (IOException exception){
            Log.e(TAG, "Error writing to the output stream", exception);

        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}