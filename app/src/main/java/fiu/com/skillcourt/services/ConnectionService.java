package fiu.com.skillcourt.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import fiu.com.skillcourt.connection.NsdConnection;
import fiu.com.skillcourt.structures.Pad;


/**
 * Created by Joshua Mclendon on 9/24/2017.
 */

public class ConnectionService extends Service {

    private static final String TAG = ConnectionService.class.getName();
    public NsdConnection nsdConnection;
    private ServerThread mServerThread;
    private ServerSocket mServerSocket;
    private int mLocalPort;
    private int mPadsConnected = 0;
    private List<Pad> activePads = new CopyOnWriteArrayList<>();
    private IBinder mBinder = new LocalBinder();



    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {

        //Create and start the thread that'll handle creating a server socket and registering nsd
        mServerThread = new ServerThread();
        mServerThread.setName("ServerThread");
        mServerThread.start();

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        mServerThread.interrupt();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //broadcast a total disconnect
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    public List<Pad> getActivePads() {
        return activePads;
    }

    public void addActivePad(Pad pad) {
        activePads.add(pad);
        mPadsConnected++;
    }

    public void removeActivePad(Pad pad) {
        activePads.remove(pad);
        mPadsConnected--;
    }

    public int getPadsConnected() {
        return mPadsConnected;
    }

    public int getLocalPort() {
        return mLocalPort;
    }

    public void setLocalPort(int localPort) {
        mLocalPort = localPort;
    }

    public void broadcast(Pad pad, String intent) {
        Intent i = new Intent();
        if (intent.equals("PAD_CONNECTION_CHANGE")) {
            Log.i(TAG, "broadcasting connection change");
            addActivePad(pad);

        } else if (intent.equals("PAD_HIT")) {
            i.putExtra("PAD_HIT_UUID", pad.getUuid());
            Log.i(TAG, "Pad " + pad.getUuid() + " hit " + mPadsConnected);
        }

        i.setAction(intent);
        sendBroadcast(i);
    }

    public void swap(int index1, int index2) {
        Collections.swap(activePads, index1, index2);
        Log.i(TAG, "Swapped from " + index1 + " to " + index2);
    }

    //So activities can access the methods inside this service
    public class LocalBinder extends Binder {
        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    private class ServerThread extends Thread {


        @Override
        public void interrupt() {
            super.interrupt();
            for (Pad pad : activePads) {
                pad.shutDown();
            }
            activePads.clear();
            nsdConnection.tearDown();
            Log.i(TAG, "Closed nsd broadcast");
            try {
                mServerSocket.close();
                Log.i(TAG, "Closed server socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                // Since discovery will happen via Nsd, we don't need to care which port is
                // used. Just grab an available one and advertise it via Nsd.
                ServerSocket serverSocket = new ServerSocket(0);
                mServerSocket = serverSocket;
                nsdConnection = new NsdConnection(getApplicationContext());
                setLocalPort(serverSocket.getLocalPort());
                nsdConnection.registerService(mLocalPort);

                //Continuously look for clients to communicate to
                while (!Thread.currentThread().isInterrupted()) {
                    Log.i(TAG, "ServerSocket Created, awaiting connection");
                    Socket socket = serverSocket.accept();
                    Log.i(TAG, "Connection accepted " + socket.getInetAddress());
                    Pad pad = new Pad(socket, ConnectionService.this);
                    pad.connect();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating ServerSocket: ", e);
                e.printStackTrace();
                interrupt();
            } catch (Exception e) {
                e.printStackTrace();
                interrupt();
            }
        }
    }
}