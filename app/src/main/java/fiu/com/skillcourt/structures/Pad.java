package fiu.com.skillcourt.structures;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import fiu.com.skillcourt.services.ConnectionService;

/**
 * Created by Joshua Mclendon on 2/2/18.
 */

public class Pad {

    private static final String TAG = Pad.class.getName();
    private Socket mSocket;
    private BufferedReader mInput;
    private OutputStreamWriter mOutput;
    private String uuid;
    private String hitColor = "";
    private boolean alive = true;
    private ConnectionService mConnectionService;
    private int CurrentStatus;
    private int Order;
    //Timer variable
    //ReplyFlag

    public Pad(){

    }
    public Pad(Socket socket, ConnectionService connectionService) {
        mSocket = socket;
        mConnectionService = connectionService;
    }

    public enum COMMANDS {
        ALIVE("-1"),
        CONNECT("1"),
        START_GAME("2"),
        LIGHT_UP("3"),
        LIGHT_OFF("4");

        private String comm;

        COMMANDS(String comm) {
            this.comm = comm;
        }

        public String getCommand() {
            return comm;
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    public String getUuid() {
        return uuid;
    }

    public int getCurrentStatus() {
        return CurrentStatus;
    }

    public int getOrder() {
        return Order;
    }

    public void setOrder(int orderToSet) {
        this.Order = orderToSet;
    }

    public void connect() {
        Log.i(TAG, "calling connect");
        Log.i(TAG, "getting buffer reader " + mSocket.isConnected());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mInput = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    mOutput = new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8");
                    sendMessage(COMMANDS.CONNECT.getCommand());
                    Log.i(TAG, "starting thread connect " + mSocket.isConnected());
                    //while(mSocket.isConnected() && replyFlag)git
                    while (mSocket.isConnected()) {

                        String data = mInput.readLine();

                        if (data.length() > 1) {
                                uuid = data;
                                Log.i(TAG, "UUID received " + uuid);
                            }
                            if (data.equalsIgnoreCase("-1")) {
                                alive = true;
                            } else if (data.equalsIgnoreCase("0")) {
                                Log.i(TAG, "Shutdown");
                                shutDown();
                                Thread.currentThread().interrupt();
                            } else if (data.equalsIgnoreCase("1")) {

                                Log.i(TAG, "Connected received");
                                mConnectionService.broadcast(Pad.this, "PAD_CONNECTION_CHANGE");
                                //handshakePollingService();
                            } else if (data.equalsIgnoreCase("3") || data.equalsIgnoreCase(hitColor)) {

                                Log.i(TAG, "Hit received");
                                mConnectionService.broadcast(Pad.this, "PAD_HIT");
                            }
                            Log.i(TAG + " Input Thread", "Received: " + data);


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    shutDown();
                    Thread.currentThread().interrupt();
                }
            }

        }).start();
    }

    public void handshakePollingService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Start Timer var

                while (alive) {
                    sendMessage(COMMANDS.ALIVE.getCommand());
                    long startTime = System.currentTimeMillis();
                    alive = false;
                    while (10 * 1000 < System.currentTimeMillis() - startTime) {

                    }

                }
                shutDown();
                Thread.currentThread().interrupt();

            }

        }).start();

    }

    private void sendMessage(String message) {
        try {
            if (mSocket.isConnected()) {
                Log.i(TAG, "sending message");
                mOutput.write(message, 0, message.length());
                mOutput.flush();
            }
        } catch (Exception e) {
            Log.i(TAG, "send message error");
            e.printStackTrace();
        }
    }

    public void shutDown() {
        try {

            if (mSocket.isConnected()) {
                mInput.close();
                mOutput.close();
                mSocket.close();
            }

        } catch (Exception e) {
            Log.i(TAG, "shut down error");
            e.printStackTrace();
        }

        if (mConnectionService.getActivePads().contains(Pad.this)) {
            mConnectionService.removeActivePad(Pad.this);
        }

    }

    public void turnOn() {
        sendMessage(COMMANDS.LIGHT_UP.getCommand());
    }

    public void turnOn(String color) {
        sendMessage(color);
    }

    public void turnOff() {
        sendMessage(COMMANDS.LIGHT_OFF.getCommand());
    }

    public void startGame() {
        sendMessage(COMMANDS.START_GAME.getCommand());
    }
}