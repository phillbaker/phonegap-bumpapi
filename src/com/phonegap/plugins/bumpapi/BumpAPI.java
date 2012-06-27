package com.phonegap.plugins.bumpapi;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.bump.api.IBumpAPI;
import com.bump.api.BumpAPIIntents;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

public class BumpAPI extends Plugin {

    private IBumpAPI api;
    private long currChannelID;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.i("FooBar", "onServiceConnected");
            api = IBumpAPI.Stub.asInterface(binder);
            try {
                api.configure("XXXXXXXXX",
                              "FooBar");
            } catch (RemoteException e) {
                Log.w("FooBar", e);
            }
            Log.d("FooBar", "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            api = null;
            Log.d("FooBar", "Service disconnected");
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            try {
                if (action.equals(BumpAPIIntents.DATA_RECEIVED)) {
                    String user = api.userIDForChannelID(intent.getLongExtra("channelID", 0));
                    String data = new String(intent.getByteArrayExtra("data"));
                    
                    Log.i("FooBar", "Received data from: " + user); 
                    Log.i("FooBar", "Data: " + data);
                    
                    sendJavascript("window.plugins.bumpAPI.options.reciever('" + user + "','" + data + "');");
                } 
                else if (action.equals(BumpAPIIntents.MATCHED)) {
                    long channelID = intent.getLongExtra("proposedChannelID", 0); 
                    Log.i("FooBar", "Matched with: " + api.userIDForChannelID(channelID));
                    api.confirm(channelID, true);
                    Log.i("FooBar", "Confirm sent");
                } 
                else if (action.equals(BumpAPIIntents.CHANNEL_CONFIRMED)) {
                    currChannelID = intent.getLongExtra("channelID", 0);
                    String user = api.userIDForChannelID(currChannelID);
                    Log.i("FooBar", "Channel confirmed with " + user);
                    
                    //e.g. of sending data on channel confirm
                    //api.send(currChannelID, "Hello, world!".getBytes());
                    sendJavascript("window.plugins.bumpAPI.options.sender({confirmed: '" + user + "');");
                    sendJavascript("window.plugins.bumpAPI.options.reciever('" + user + "','');");
                } 
                else if (action.equals(BumpAPIIntents.NOT_MATCHED)) {
                    Log.i("FooBar", "Not matched.");
                } 
                else if (action.equals(BumpAPIIntents.CONNECTED)) {
                    Log.i("FooBar", "Connected to Bump...");
                    api.enableBumping();
                }
                else if (action.equals(BumpAPIIntents.DISCONNECTED)) {
                    Log.i("FooBar", "Disconnected.");
                    currChannelID = -1;
                }
                else if (action.equals(BumpAPIIntents.BUMPED)) {
                    Log.i("FooBar", "Bumped.");
                }
            } catch (RemoteException e) {}
        } 
    };
    
    /**
     * 
     * @param action One of 'construct', 'send', 'destroy', 'debug'.
     * @param data Ignored except for 'send', when it should be a JSON array, with 1 element, the string to send.
     * @param callbackId
     */
    @Override
    public PluginResult execute(String action, JSONArray data, String callbackId) {
        //no result as default
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        //do something based on "action"
        if (action.equals("construct")) {
            this.ctx.bindService(
                new Intent(IBumpAPI.class.getName()),
                connection, 
                Context.BIND_AUTO_CREATE
            );

            IntentFilter filter = new IntentFilter();
            filter.addAction(BumpAPIIntents.CHANNEL_CONFIRMED);
            filter.addAction(BumpAPIIntents.DATA_RECEIVED);
            filter.addAction(BumpAPIIntents.NOT_MATCHED);
            filter.addAction(BumpAPIIntents.MATCHED);
            filter.addAction(BumpAPIIntents.CONNECTED);
            //filter.addAction(BumpAPIIntents.BUMPED);
            //filter.addAction(BumpAPIIntents.DISCONNECTED);
            this.ctx.registerReceiver(receiver, filter);
            
            result = new PluginResult(Status.OK);
        }
        else if (action.equals("send")) {
            if(!data.isNull(0)) {
                try {
                    api.send(currChannelID, data.getString(0).getBytes());
                } 
                catch (RemoteException e) {} 
                catch (JSONException e) {
                    Log.d("FooBar", "Got JSON Exception " + e.getMessage());
                    result = new PluginResult(Status.JSON_EXCEPTION);
                }
            }
        }
        else if (action.equals("enable")) {
            try {
                api.enableBumping();
            } 
            catch (RemoteException e) {}
        }
        else if (action.equals("disable")) {
            try {
                api.disableBumping();
            } 
            catch (RemoteException e) {}
        }
        else if (action.equals("destroy")) {
            if(connection != null) {
                this.ctx.unbindService(connection);
                this.ctx.unregisterReceiver(receiver);
            }
            
            result = new PluginResult(Status.OK);
        }
        else if (action.equals("debug")) {
            try {
                api.simulateBump();
            } catch (RemoteException e) {}
        }
        
        return result;
    }

}
