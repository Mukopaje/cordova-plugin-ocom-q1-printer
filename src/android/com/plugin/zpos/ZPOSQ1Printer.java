package com.plugin.zpos;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import com.iposprinter.iposprinterservice.IPosPrinterService;
import com.iposprinter.iposprinterservice.IPosPrinterCallback;

import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.content.ServiceConnection;

import android.graphics.Bitmap;

import android.os.IBinder;

import android.util.Base64;
import android.util.Log;

import android.com.plugin.zpos.ThreadPoolManager;
import android.com.plugin.zpos.PrinterStatusReceiver;
/**
 * This class echoes a string called from JavaScript.
 */
public class ZPOSQ1Printer extends CordovaPlugin {

    private static final String TAG = "ZPOSQ1Printer";
   // private BitmapUtils bitMapUtils;
    private IPosPrinterService mIPosPrinterService;
    private PrinterStatusReceiver printerStatusReceiver = new PrinterStatusReceiver();

    /* 定义状态广播 */
    private final String PRINTER_NORMAL_ACTION = "com.iposprinter.iposprinterservice.NORMAL_ACTION";
    private final String PRINTER_PAPERLESS_ACTION = "com.iposprinter.iposprinterservice.PAPERLESS_ACTION";
    private final String PRINTER_PAPEREXISTS_ACTION = "com.iposprinter.iposprinterservice.PAPEREXISTS_ACTION";
    private final String PRINTER_THP_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.THP_HIGHTEMP_ACTION";
    private final String PRINTER_THP_NORMALTEMP_ACTION = "com.iposprinter.iposprinterservice.THP_NORMALTEMP_ACTION";
    private final String PRINTER_MOTOR_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.MOTOR_HIGHTEMP_ACTION";
    private final String PRINTER_BUSY_ACTION = "com.iposprinter.iposprinterservice.BUSY_ACTION";
    private final String PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION = "com.iposprinter.iposprinterservice.CURRENT_TASK_PRINT_COMPLETE_ACTION";
    private final String GET_CUST_PRINTAPP_PACKAGENAME_ACTION = "android.print.action.CUST_PRINTAPP_PACKAGENAME";
  
    private ServiceConnection connectService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIPosPrinterService = IPosPrinterService.Stub.asInterface(service);
            // setButtonEnable(true);
            Toast.makeText(webView.getContext(), "Service connected " + mIPosPrinterService, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIPosPrinterService = null;
        }
    };


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
      super.initialize(cordova, webView);
  
      Toast.makeText(webView.getContext(), "Initialization Statrted " + mIPosPrinterService, Toast.LENGTH_LONG).show();
      Context applicationContext = this.cordova.getActivity().getApplicationContext();
  
      // bitMapUtils = new BitmapUtils(applicationContext);
  
      Intent intent = new Intent();
      intent.setPackage("com.iposprinter.iposprinterservice");
      intent.setAction("com.iposprinter.iposprinterservice.IPosPrintService");
      //startService(intent);
      
        applicationContext.startService(intent);
        applicationContext.bindService(intent, connectService, Context.BIND_AUTO_CREATE);
      //注册打印机状态接收器
      IntentFilter printerStatusFilter = new IntentFilter();
      printerStatusFilter.addAction(PRINTER_NORMAL_ACTION);
      printerStatusFilter.addAction(PRINTER_PAPERLESS_ACTION);
      printerStatusFilter.addAction(PRINTER_PAPEREXISTS_ACTION);
      printerStatusFilter.addAction(PRINTER_THP_HIGHTEMP_ACTION);
      printerStatusFilter.addAction(PRINTER_THP_NORMALTEMP_ACTION);
      printerStatusFilter.addAction(PRINTER_MOTOR_HIGHTEMP_ACTION);
      printerStatusFilter.addAction(PRINTER_BUSY_ACTION);
      printerStatusFilter.addAction(GET_CUST_PRINTAPP_PACKAGENAME_ACTION);

      applicationContext.registerReceiver(printerStatusReceiver, printerStatusFilter);

     

      Toast.makeText(webView.getContext(), "Initialization Statrted " + mIPosPrinterService, Toast.LENGTH_LONG).show();

    }

    @Override
  public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
    if (action.equals("printerInit")) {
      printerInit(callbackContext);
      return true;
    }  else if (action.equals("hasPrinter")) {
        hasPrinter(callbackContext);
        return true;
      }else if (action.equals("sendRAWData")) {
        sendRAWData(data.getString(0), callbackContext);
        return true;
    } else if (action.equals("setAlignment")) {
        setAlignment(data.getInt(0), callbackContext);
        return true;
    } else if (action.equals("setFontName")) {
        setFontName(data.getString(0), callbackContext);
        return true;
    } else if (action.equals("setFontSize")) {
        setFontSize((int) data.getDouble(0), callbackContext);
        return true;
    } else if (action.equals("printTextWithFont")) {
        printTextWithFont(data.getString(0), data.getString(1), (int) data.getDouble(2), callbackContext);
        return true;
    } else if (action.equals("printColumnsText")) {
        printColumnsText(data.getJSONArray(0), data.getJSONArray(1), data.getJSONArray(2), callbackContext);
        return true;
    }
    // } else if (action.equals("show")) {
    //     show(data.getString(0), callbackContext);
    //     return true;
    // } else if (action.equals("printBarCode")) {
    //     printBarCode(data.getString(0), data.getInt(1), data.getInt(2), data.getInt(1), data.getInt(2),
    //             callbackContext);
    //     return true;
    // } else if (action.equals("printQRCode")) {
    //     printQRCode(data.getString(0), data.getInt(1), data.getInt(2), callbackContext);
    //     return true;
    // } 
    else if (action.equals("printOriginalText")) {
        printOriginalText(data.getString(0), callbackContext);
        return true;
    } else if (action.equals("printString")) {
        printString(data.getString(0), callbackContext);
        return true;
    } else if (action.equals("printerStatusStartListener")) {
        printerStatusStartListener(callbackContext);
        return true;
    } else if (action.equals("printerStatusStopListener")) {
        printerStatusStopListener();
        return true;
    }


    return false;
  }

  

  public void printerInit(final CallbackContext callbackContext) {
    final IPosPrinterService printerService = mIPosPrinterService;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
      @Override
      public void run() {
        try {
          printerService.printerInit(new IPosPrinterCallback.Stub() {
            @Override
            public void onRunResult(boolean isSuccess) {
              if (isSuccess) {
                callbackContext.success("");
              } else {
                callbackContext.error(isSuccess + "");
              }
            }

            @Override
            public void onReturnString(String result) {
              callbackContext.success(result);
            }

            @Override
            public void onRaiseException(int code, String msg) {
              callbackContext.error(msg);
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
          Log.i(TAG, "ERROR: " + e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  public void hasPrinter(final CallbackContext callbackContext) {
    try {
      callbackContext.success(hasPrinter());
    } catch (Exception e) {
      Log.i(TAG, "ERROR: " + e.getMessage());
      callbackContext.error(e.getMessage());
    }
  }

  private int hasPrinter() {
    final IPosPrinterService printerService = mIPosPrinterService;
    final boolean hasPrinterService = printerService != null;
    return hasPrinterService ? 1 : 0;
  }

  
  public void sendRAWData(String base64EncriptedData, final CallbackContext callbackContext) {
    final IPosPrinterService printerService = mIPosPrinterService;
    final byte[] d = Base64.decode(base64EncriptedData, Base64.DEFAULT);
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
        @Override
        public void run() {
            try {
                printerService.printRawData(d, new IPosPrinterCallback.Stub() {
                    @Override
                    public void onRunResult(boolean isSuccess) {
                        if (isSuccess) {
                            callbackContext.success("");
                        } else {
                            callbackContext.error(isSuccess + "");
                        }
                    }

                    @Override
                    public void onReturnString(String result) {
                        callbackContext.success(result);
                    }

                    @Override
                    public void onRaiseException(int code, String msg) {
                        callbackContext.error(msg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "ERROR: " + e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
    });
}

public void setAlignment(int alignment, final CallbackContext callbackContext) {
    final IPosPrinterService printerService = mIPosPrinterService;
    final int align = alignment;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
        @Override
        public void run() {
            try {
                printerService.setPrinterPrintAlignment(align, new IPosPrinterCallback.Stub() {
                    @Override
                    public void onRunResult(boolean isSuccess) {
                        if (isSuccess) {
                            callbackContext.success("");
                        } else {
                            callbackContext.error(isSuccess + "");
                        }
                    }

                    @Override
                    public void onReturnString(String result) {
                        callbackContext.success(result);
                    }

                    @Override
                    public void onRaiseException(int code, String msg) {
                        callbackContext.error(msg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "ERROR: " + e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
    });
}

public void setFontName(String typeface, final CallbackContext callbackContext) {
    final IPosPrinterService printerService = mIPosPrinterService;
    final String tf = typeface;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
        @Override
        public void run() {
            try {
                printerService.setPrinterPrintFontType(tf, new IPosPrinterCallback.Stub() {
                    @Override
                    public void onRunResult(boolean isSuccess) {
                        if (isSuccess) {
                            callbackContext.success("");
                        } else {
                            callbackContext.error(isSuccess + "");
                        }
                    }

                    @Override
                    public void onReturnString(String result) {
                        callbackContext.success(result);
                    }

                    @Override
                    public void onRaiseException(int code, String msg) {
                        callbackContext.error(msg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "ERROR: " + e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
    });
}

public void setFontSize(int fontsize, final CallbackContext callbackContext) {
    final IPosPrinterService printerService = mIPosPrinterService;
    final int fs = fontsize;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
        @Override
        public void run() {
            try {
                printerService.setPrinterPrintFontSize(fs, new IPosPrinterCallback.Stub() {
                    @Override
                    public void onRunResult(boolean isSuccess) {
                        if (isSuccess) {
                            callbackContext.success("");
                        } else {
                            callbackContext.error(isSuccess + "");
                        }
                    }

                    @Override
                    public void onReturnString(String result) {
                        callbackContext.success(result);
                    }

                    @Override
                    public void onRaiseException(int code, String msg) {
                        callbackContext.error(msg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "ERROR: " + e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
    });
}

public void printTextWithFont(String text, String typeface, int fontsize, final CallbackContext callbackContext) {
    final IPosPrinterService printerService = mIPosPrinterService;
    final String txt = text;
    final String tf = typeface;
    final int fs = fontsize;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
        @Override
        public void run() {
            try {
                printerService.printSpecifiedTypeText(txt, tf, fs, new IPosPrinterCallback.Stub() {
                    @Override
                    public void onRunResult(boolean isSuccess) {
                        if (isSuccess) {
                            callbackContext.success("");
                        } else {
                            callbackContext.error(isSuccess + "");
                        }
                    }

                    @Override
                    public void onReturnString(String result) {
                        callbackContext.success(result);
                    }

                    @Override
                    public void onRaiseException(int code, String msg) {
                        callbackContext.error(msg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "ERROR: " + e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
    });
}

public void printColumnsText(JSONArray colsTextArr, JSONArray colsWidthArr, JSONArray colsAlign,
        final CallbackContext callbackContext) {

    final IPosPrinterService printerService = mIPosPrinterService;
    final String[] clst = new String[colsTextArr.length()];
    for (int i = 0; i < colsTextArr.length(); i++) {
        try {
            clst[i] = colsTextArr.getString(i);
        } catch (JSONException e) {

            clst[i] = "-";
            Log.i(TAG, "ERROR TEXT: " + e.getMessage());
        }
    }
    final int[] clsw = new int[colsWidthArr.length()];
    for (int i = 0; i < colsWidthArr.length(); i++) {
        try {
            clsw[i] = colsWidthArr.getInt(i);
        } catch (JSONException e) {
            clsw[i] = 1;
            Log.i(TAG, "ERROR WIDTH: " + e.getMessage());
        }
    }
    final int[] clsa = new int[colsAlign.length()];
    for (int i = 0; i < colsAlign.length(); i++) {
        try {
            clsa[i] = colsAlign.getInt(i);
        } catch (JSONException e) {
            clsa[i] = 0;
            Log.i(TAG, "ERROR ALIGN: " + e.getMessage());
        }
    }
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
        @Override
        public void run() {
            try {
                printerService.printColumnsText(clst, clsw, clsa, 0, new IPosPrinterCallback.Stub() {
                    @Override
                    public void onRunResult(boolean isSuccess) {
                        if (isSuccess) {
                            callbackContext.success("");
                        } else {
                            callbackContext.error(isSuccess + "");
                        }
                    }

                    @Override
                    public void onReturnString(String result) {
                        callbackContext.success(result);
                    }

                    @Override
                    public void onRaiseException(int code, String msg) {
                        callbackContext.error(msg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "ERROR: " + e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
    });
}

public void printOriginalText(String text, final CallbackContext callbackContext) {
    final IPosPrinterService printerService = mIPosPrinterService;
    final String txt = text;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
        @Override
        public void run() {
            try {
                printerService.printText(txt, new IPosPrinterCallback.Stub() {
                    @Override
                    public void onRunResult(boolean isSuccess) {
                        if (isSuccess) {
                            callbackContext.success("");
                        } else {
                            callbackContext.error(isSuccess + "");
                        }
                    }

                    @Override
                    public void onReturnString(String result) {
                        callbackContext.success(result);
                    }

                    @Override
                    public void onRaiseException(int code, String msg) {
                        callbackContext.error(msg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "ERROR: " + e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
    });
}

public void printString(String message, final CallbackContext callbackContext) {
    final IPosPrinterService printerService = mIPosPrinterService;
    final String msgs = message;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
        @Override
        public void run() {
            try {
                printerService.printText(msgs, new IPosPrinterCallback.Stub() {
                    @Override
                    public void onRunResult(boolean isSuccess) {
                        if (isSuccess) {
                            callbackContext.success("");
                        } else {
                            callbackContext.error(isSuccess + "");
                        }
                    }

                    @Override
                    public void onReturnString(String result) {
                        callbackContext.success(result);
                    }

                    @Override
                    public void onRaiseException(int code, String msg) {
                        callbackContext.error(msg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "ERROR: " + e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
    });
}

public void printerStatusStartListener(final CallbackContext callbackContext) {
    final PrinterStatusReceiver receiver = printerStatusReceiver;
    receiver.startReceiving(callbackContext);
}

public void printerStatusStopListener() {
    final PrinterStatusReceiver receiver = printerStatusReceiver;
    receiver.stopReceiving();
}

}
