package com.honeywellscannerreader;

import androidx.annotation.NonNull;
import android.util.Log;
import android.os.Build;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.bridge.Callback;

import com.honeywell.aidc.*;

@ReactModule(name = HoneyWellScannerReaderModule.NAME)
public class HoneyWellScannerReaderModule extends ReactContextBaseJavaModule
    implements AidcManager.CreatedCallback, BarcodeReader.BarcodeListener {
  public static final String NAME = "HoneyWellScannerReader";

  private static BarcodeReader barcodeReader;
  private AidcManager manager;
  private Callback onReadCallback;

  public HoneyWellScannerReaderModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a * b);
  }

  @ReactMethod
  public void initAPI(Promise promise) {
    printDebugLog("initAPI - Start");
    try {
      scannerServiceAvailable();
      AidcManager.create(getCurrentActivity(), HoneyWellScannerReaderModule.this);
      promise.resolve("Initialized");
    } catch (PackageManager.NameNotFoundException ex) {
      printDebugLog("error in initAPI PackageManager.NameNotFoundException");
      printDebugLog(ex.getMessage());
      promise.reject("Error in initAPI", "Honeywell scanner service not available", ex);
    }
    catch (RuntimeException ex) {
      printDebugLog("error in initAPI RuntimeException");
      printDebugLog(ex.getMessage());
      promise.reject("Error in initAPI", ex);
    }
    printDebugLog("initAPI - End");
  }

  @ReactMethod
  public void activateReader(Callback readCallBack) {
    printDebugLog("activateReader - Start");
    onReadCallback = readCallBack;
    if (barcodeReader != null) {
      try {
        barcodeReader.softwareTrigger(true);
      } catch (ScannerNotClaimedException e) {
        printDebugLog(e.getMessage());
        e.printStackTrace();
      } catch (ScannerUnavailableException e) {
        printDebugLog(e.getMessage());
        e.printStackTrace();
      }
    } else {
      printDebugLog("barcodeReader is null");
    }
    printDebugLog("activateReader - end");
  }

  @ReactMethod
  public void deactivateReader() {
    printDebugLog("deactivateReader - Start");
    if (barcodeReader != null) {
      // close BarcodeReader to clean up resources.
      barcodeReader.close();
      barcodeReader = null;
    }
    printDebugLog("deactivateReader - End");
  }

  @ReactMethod
  public void setReadCallBack(Callback readCallBack) {
    printDebugLog("setReadCallBack - Start");
    onReadCallback = readCallBack;
    printDebugLog("setReadCallBack - end");
  }

  @Override
  public void onCreated(AidcManager aidcManager) {
    printDebugLog("onCreated - Start");
    try {
      manager = aidcManager;
      barcodeReader = manager.createBarcodeReader();

      // set the trigger mode to automatic control
      try {
        if (barcodeReader != null) {
          printDebugLog("barcodeReader not claimed in onCreated()");
          barcodeReader.claim();
        }
        barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
            BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
        // apply settings
        // barcodeReader.setProperty(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
        // barcodeReader.setProperty(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);

      } catch (UnsupportedPropertyException e) {
        e.printStackTrace();
      } catch (ScannerUnavailableException e) {
        e.printStackTrace();
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
    // register bar code event listener
    barcodeReader.addBarcodeListener(HoneyWellScannerReaderModule.this);
    printDebugLog("onCreated - End");
  }

  // @Override
  // protected void onCreate(Bundle savedInstanceState) {
  // super.onCreate(savedInstanceState);

  // // create the AidcManager providing a Context and a
  // // CreatedCallback implementation.
  // AidcManager.create(this, new AidcManager.CreatedCallback() {
  // @Override
  // public void onCreated(AidcManager aidcManager) {
  // manager = aidcManager;
  // barcodeReader = manager.createBarcodeReader();
  // try {
  // if (barcodeReader != null) {
  // Log.d("honeywellscanner: ", "barcodereader not claimed in OnCreate()");
  // barcodeReader.claim();
  // }
  // // apply settings
  // /*
  // * barcodeReader.setProperty(BarcodeReader.PROPERTY_CODE_39_ENABLED, false);
  // * barcodeReader.setProperty(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
  // *
  // * // set the trigger mode to automatic control
  // * barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
  // * BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
  // * } catch (UnsupportedPropertyException e) {
  // * Toast.makeText(MainActivity.this, "Failed to apply properties",
  // * Toast.LENGTH_SHORT).show();
  // */
  // } catch (ScannerUnavailableException e) {
  // e.printStackTrace();
  // throw (e);
  // }
  // // register bar code event listener
  // barcodeReader.addBarcodeListener(this);
  // }
  // });
  // //ActivitySetting();
  // }

  // @Override
  // public void onStop() {
  // super.onStop();
  // if (barcodeReader != null)
  // barcodeReader.release();
  // }

  // @Override
  // protected void onDestroy() {
  // super.onDestroy();

  // if (barcodeReader != null) {
  // // close BarcodeReader to clean up resources.
  // barcodeReader.close();
  // barcodeReader = null;
  // }

  // if (manager != null) {
  // // close AidcManager to disconnect from the scanner service.
  // // once closed, the object can no longer be used.
  // manager.close();
  // }
  // }

  @Override
  public void onFailureEvent(BarcodeFailureEvent arg0) {
    // TODO Auto-generated method stub
    try {
      barcodeReader.softwareTrigger(false);
    } catch (ScannerNotClaimedException e) {
      e.printStackTrace();
    } catch (ScannerUnavailableException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onBarcodeEvent(final BarcodeReadEvent event) {
    printDebugLog("onBarcodeEvent - start");
    try {
      barcodeReader.softwareTrigger(false);
    } catch (ScannerNotClaimedException e) {
      e.printStackTrace();
    } catch (ScannerUnavailableException e) {
      e.printStackTrace();
    }

    String barcodeData = event.getBarcodeData();
    printDebugLog("onBarcodeEvent - barcodeData: " + barcodeData);
    if(onReadCallback != null){
    onReadCallback.invoke(barcodeData);
    }
    else{
      printDebugLog("onBarcodeEvent - onReadCallback is null");
    }

    printDebugLog("onBarcodeEvent - end");
  }

  private void printDebugLog(String text) {
    Log.d(this.getClass().getSimpleName(), text);
  }

  private void scannerServiceAvailable () throws PackageManager.NameNotFoundException {
    PackageManager pm = getReactApplicationContext().getPackageManager();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    pm.getPackageInfo("com.honeywell.barcode", PackageManager.PackageInfoFlags.of(0));
    } else {
    pm.getPackageInfo("com.honeywell.barcode", 0);
    }
  }

}
