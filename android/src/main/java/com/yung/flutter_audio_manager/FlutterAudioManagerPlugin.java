package com.yung.flutter_audio_manager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FlutterAudioManagerPlugin
 * https://developer.android.com/reference/android/media/AudioManager#setCommunicationDevice(android.media.AudioDeviceInfo)
 */
public class FlutterAudioManagerPlugin implements FlutterPlugin, MethodCallHandler {
    private static MethodChannel channel;
    private static AudioManager audioManager;
    private static Context activeContext;
    private List<AudioDeviceInfo> devices;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_audio_manager");
        channel.setMethodCallHandler(new FlutterAudioManagerPlugin());
//        AudioChangeReceiver receiver = new AudioChangeReceiver(listener);
//        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        activeContext = flutterPluginBinding.getApplicationContext();
//        activeContext.registerReceiver(receiver, filter);
        // Get an AudioManager instance
        audioManager = (AudioManager) activeContext.getSystemService(Context.AUDIO_SERVICE);
    }

//  public static void registerWith(Registrar registrar) {
//    channel = new MethodChannel(registrar.messenger(), "flutter_audio_manager");
//    channel.setMethodCallHandler(new FlutterAudioManagerPlugin());
//    AudioChangeReceiver receiver = new AudioChangeReceiver(listener);
//    IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
//    activeContext = registrar.activeContext();
//    activeContext.registerReceiver(receiver, filter);
//    audioManager = (AudioManager) activeContext.getSystemService(Context.AUDIO_SERVICE);
//  }

//    static AudioEventListener listener = new AudioEventListener() {
//        @Override
//        public void onChanged() {
//            channel.invokeMethod("inputChanged", 1);
//        }
//    };

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getCurrentOutput":
                result.success(getCurrentOutput());
                break;
            case "getAvailableInputs":
                result.success(getAvailableInputs());
                break;
            case "changeToReceiver":
                result.success(changeToReceiverV2());
                break;
            case "changeToSpeaker":
                result.success(changeToSpeakerV2());
                break;
            case "changeToHeadphones":
                result.success(changeToHeadphones());
                break;
            case "changeToBluetooth":
                result.success(changeToBluetooth());
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private Boolean changeToSpeakerV2() {
        devices = audioManager.getAvailableCommunicationDevices();
        if (devices == null || devices.isEmpty()) {
            return false;
        }
        AudioDeviceInfo speakerDevice = null;
        for (AudioDeviceInfo device : devices) {
            if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                speakerDevice = device;
                break;
            }
        }
        if (speakerDevice != null) {
            // Turn speakerphone ON.
            boolean result = audioManager.setCommunicationDevice(speakerDevice);
            Log.d("dashu","changeToSpeakerV2,speakerDevice="+speakerDevice+",result="+result);
            return result;
//          if (!result) {
            // Handle error.
//          }
            // Turn speakerphone OFF.
//          audioManager.clearCommunicationDevice();
        }
        return false;
    }

    private Boolean changeToReceiverV2() {
        Log.d("dashu","changeToReceiverV2= clear");
        audioManager.clearCommunicationDevice();
        return true;
    }

    private Boolean changeToReceiver() {
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);
        audioManager.setSpeakerphoneOn(false);
//        listener.onChanged();
        return true;
    }

    private Boolean changeToSpeaker() {
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);
        audioManager.setSpeakerphoneOn(true);
//        listener.onChanged();
        return true;
    }

    private Boolean changeToHeadphones() {
        return changeToReceiverV2();
    }

    private Boolean changeToBluetooth() {
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.startBluetoothSco();
        audioManager.setBluetoothScoOn(true);
//        listener.onChanged();
        return true;
    }

    private List<String> getCurrentOutput() {
        List<String> info = new ArrayList();
        if (audioManager.isSpeakerphoneOn()) {
            info.add("Speaker");
            info.add("2");
        } else if (audioManager.isBluetoothScoOn()) {
            info.add("Bluetooth");
            info.add("4");
        } else if (audioManager.isWiredHeadsetOn()) {
            info.add("Headset");
            info.add("3");
        } else {
            info.add("Receiver");
            info.add("1");
        }
        return info;
        // if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // MediaRouter mr = (MediaRouter)
        // activeContext.getSystemService(Context.MEDIA_ROUTER_SERVICE);
        // MediaRouter.RouteInfo routeInfo =
        // mr.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO);
        // Log.d("aaa", "getCurrentOutput:
        // "+audioManager.isSpeakerphoneOn()+audioManager.isWiredHeadsetOn()+audioManager.isSpeakerphoneOn());
        // info.add(routeInfo.getName().toString());
        // info.add(_getDeviceType(routeInfo.getDeviceType()));
        // } else {
        // info.add("unknow");
        // info.add("0");
        // }
        // return info;
    }

    private List<List<String>> getAvailableInputs() {
        List<List<String>> list = new ArrayList();
        list.add(Arrays.asList("Receiver", "1"));
        if (audioManager.isWiredHeadsetOn()) {
            list.add(Arrays.asList("Headset", "3"));
        }
        if (audioManager.isBluetoothScoOn()) {
            list.add(Arrays.asList("Bluetooth", "4"));
        }
        return list;
    }

    private String _getDeviceType(int type) {
        Log.d("type", "type: " + type);
        switch (type) {
            case 3:
                return "3";
            case 2:
                return "2";
            case 1:
                return "4";
            default:
                return "0";
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        // 切换到正常的模式
        if (channel != null) {
            channel.setMethodCallHandler(null);
            channel = null;
        }
    }
}
