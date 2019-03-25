package com.plugin.zpos;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import com.iposprinter.iposprinterservice.IPosPrinterService;
import com.iposprinter.iposprinterservice.IPosPrinterCallback;
import android.os.Message;
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

import android.com.plugin.zpos.utils.HandlerUtils;
import android.com.plugin.zpos.utils.BytesUtil;
/**
 * This class echoes a string called from JavaScript.
 */
public class ZPOSQ1Printer extends CordovaPlugin {

    private static final String TAG = "ZPOSQ1Printer";
   // private BitmapUtils bitMapUtils;
    private IPosPrinterService mIPosPrinterService;
    private PrinterStatusReceiver printerStatusReceiver = new PrinterStatusReceiver();
    private IPosPrinterCallback callback = null;
    private HandlerUtils.MyHandler handler;

    private final int PRINTER_NORMAL = 0;
    private final int PRINTER_PAPERLESS = 1;
    private final int PRINTER_THP_HIGH_TEMPERATURE = 2;
    private final int PRINTER_MOTOR_HIGH_TEMPERATURE = 3;
    private final int PRINTER_IS_BUSY = 4;
    private final int PRINTER_ERROR_UNKNOWN = 5;
    private int printerStatus = 0;

   
  
    private final int MSG_TEST                               = 1;
    private final int MSG_IS_NORMAL                          = 2;
    private final int MSG_IS_BUSY                            = 3;
    private final int MSG_PAPER_LESS                         = 4;
    private final int MSG_PAPER_EXISTS                       = 5;
    private final int MSG_THP_HIGH_TEMP                      = 6;
    private final int MSG_THP_TEMP_NORMAL                    = 7;
    private final int MSG_MOTOR_HIGH_TEMP                    = 8;
    private final int MSG_MOTOR_HIGH_TEMP_INIT_PRINTER       = 9;

    /*循环打印类型*/
    private final int  MULTI_THREAD_LOOP_PRINT  = 1;
    private final int  INPUT_CONTENT_LOOP_PRINT = 2;
    private final int  DEMO_LOOP_PRINT          = 3;
    private final int  PRINT_DRIVER_ERROR_TEST  = 4;
    private final int  DEFAULT_LOOP_PRINT       = 0;

    //循环打印标志位
    private       int  loopPrintFlag            = DEFAULT_LOOP_PRINT;
    private       byte loopContent              = 0x00;
    private       int  printDriverTestCount     = 0;

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

    private HandlerUtils.IHandlerIntent iHandlerIntent = new HandlerUtils.IHandlerIntent()
    {
        @Override
        public void handlerIntent(Message msg)
        {
            switch (msg.what)
            {
                case MSG_TEST:
                    break;
                case MSG_IS_NORMAL:
                    if(getPrinterStatus() == PRINTER_NORMAL)
                    {
                        // loopPrint(loopPrintFlag);
                    }
                    break;
                case MSG_IS_BUSY:
                    Toast.makeText(webView.getContext(), "Printing...", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_PAPER_LESS:
                    // loopPrintFlag = DEFAULT_LOOP_PRINT;
                    Toast.makeText(webView.getContext(), "Printer Out of paper", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_PAPER_EXISTS:
                    Toast.makeText(webView.getContext(), "Paper available", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_THP_HIGH_TEMP:
                    Toast.makeText(webView.getContext(), "Printer high temp", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_MOTOR_HIGH_TEMP:
                    // loopPrintFlag = DEFAULT_LOOP_PRINT;
                    Toast.makeText(webView.getContext(), "Printer motor high temp", Toast.LENGTH_SHORT).show();
                    handler.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP_INIT_PRINTER, 180000);  //马达高温报警，等待3分钟后复位打印机
                    break;
                case MSG_MOTOR_HIGH_TEMP_INIT_PRINTER:
                    printerInit2();
                    break;
                default:
                    break;
            }
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

      // Initialize the call back function
      // theCallback();

      Toast.makeText(webView.getContext(), "Initialization Statrted " + mIPosPrinterService, Toast.LENGTH_LONG).show();

    }
    
    @Override
  public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
    if (action.equals("printerInit")) {
        // if (getPrinterStatus() == PRINTER_NORMAL)                  
            printerInit(callbackContext);
      return true;
    }  else if (action.equals("hasPrinter")) {
        hasPrinter(callbackContext);
        return true;
      } else if (action.equals("sendRAWData")) {
        sendRAWData(data.getString(0), callbackContext);
        return true;
    } else if (action.equals("printerRunPaper")) {
        printerRunPaper(data.getInt(0), callbackContext);
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
       // if (getPrinterStatus() == PRINTER_NORMAL) 
        printOriginalText(data.getString(0), callbackContext);
        return true;
    } else if (action.equals("printString")) {
       // if (getPrinterStatus() == PRINTER_NORMAL) 
        printString(data.getString(0), callbackContext);
        return true;
    } else if (action.equals("printerStatusStartListener")) {
        printerStatusStartListener(callbackContext);
        return true;
    } else if (action.equals("printerStatusStopListener")) {
        printerStatusStopListener();
        return true;
    } else if (action.equals("printKoubeiBill")) {
       // if (getPrinterStatus() == PRINTER_NORMAL) 
        printKoubeiBill(callbackContext);
        return true;
      }


    return false;
  }

  
  public int getPrinterStatus(){
    final IPosPrinterService printerService = mIPosPrinterService;
    Log.i(TAG,"***** printerStatus"+printerStatus);
    try{
        printerStatus = printerService.getPrinterStatus();
    }catch (Exception e){
        Log.i(TAG, "ERROR: " + e.getMessage());
        Toast.makeText(webView.getContext(), "Printer Status " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
    Log.i(TAG,"#### printerStatus"+printerStatus);
    Toast.makeText(webView.getContext(), "Printer Status " + printerStatus, Toast.LENGTH_LONG).show();
   return  printerStatus;
}


/**
     * 打印机走纸
     */
    public void printerRunPaper(final int lines, final CallbackContext callbackContext)
    {
        final IPosPrinterService printerService = mIPosPrinterService;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try{
                    printerService.printerFeedLines(lines, new IPosPrinterCallback.Stub() {
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
                } catch (Exception e){
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
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

  public void printerInit2(){
    final IPosPrinterService printerService = mIPosPrinterService;
    ThreadPoolManager.getInstance().executeTask(new Runnable() {
        @Override
        public void run() {
            try{
                printerService.printerInit(new IPosPrinterCallback.Stub() {
                    @Override
                    public void onRunResult(boolean isSuccess) {
                      if (isSuccess) {
                        Log.i(TAG, "Success: " + "Printer initialized");
                      } else {
                        Log.i(TAG, "Success Error: " + "Printer initialization error");
                      }
                    }
        
                    @Override
                    public void onReturnString(String result) {
                        Log.i(TAG, "Success: " + result);
                    }
        
                    @Override
                    public void onRaiseException(int code, String msg) {
                        Log.i(TAG, "Error: " + msg);
                    }
                  });
               
            }catch (Exception e){
                Log.i(TAG, "ERROR: " + e.getMessage());
            }
        }
    });
}

  private int hasPrinter() {
    getPrinterStatus();
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

                performPrint();
            } catch (Exception e) {
               
                Log.i(TAG, "ERROR: " + e.getMessage());
                callbackContext.error(e.getMessage());
            }
        }
    });
}



public void printKoubeiBill(final CallbackContext callbackContext)
    {
        final IPosPrinterService printerService = mIPosPrinterService;
        ThreadPoolManager.getInstance().executeTask(new Runnable()
        {

            @Override
            public void run()
            {
                try {
                    printerService.printSpecifiedTypeText("   ZPOS\n", "ST", 48, new IPosPrinterCallback.Stub() {
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
                   
                      printerService.printerPerformPrint(160,new IPosPrinterCallback.Stub() {
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
                }catch (Exception e){
                    Log.i(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }


    public void printTable()
    {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try{
                    mIPosPrinterService.setPrinterPrintAlignment(0,callback);
                    mIPosPrinterService.setPrinterPrintFontSize(24,callback);
                    String[] text = new String[4];
                    int[] width = new int[] { 8, 6, 6, 7 };
                    int[] align = new int[] { 0, 2, 2, 2 }; // 左齐,右齐,右齐,右齐
                    text[0] = "Item";
                    text[1] = "Qty";
                    text[2] = "Price";
                    text[3] = "Total";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "Milk 500mls";
                    text[1] = "4";
                    text[2] = "12.00";
                    text[3] = "48.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "Coca cola 500mls";
                    text[1] = "10";
                    text[2] = "4.00";
                    text[3] = "40.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "Fanta 500mls"; // 文字超长,换行
                    text[1] = "100";
                    text[2] = "16.00";
                    text[3] = "1600.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "Cheese 50g";
                    text[1] = "10";
                    text[2] = "4.00";
                    text[3] = "40.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 0,callback);
                    mIPosPrinterService.printBlankLines(1, 16, callback);

                    mIPosPrinterService.setPrinterPrintAlignment(1,callback);
                    mIPosPrinterService.setPrinterPrintFontSize(24,callback);
                    text = new String[3];
                    width = new int[] { 8, 6, 7 };
                    align = new int[] { 0, 2, 2 };
                    text[0] = "菜品";
                    text[1] = "数量";
                    text[2] = "金额";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "草莓酸奶布甸";
                    text[1] = "4";
                    text[2] = "48.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "酸奶水果夹心面包B";
                    text[1] = "10";
                    text[2] = "40.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "酸奶水果布甸香橙软桃蛋糕"; // 文字超长,换行
                    text[1] = "100";
                    text[2] = "1600.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "酸奶水果夹心面包";
                    text[1] = "10";
                    text[2] = "40.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 0,callback);
                    mIPosPrinterService.printBlankLines(1, 16, callback);

                    mIPosPrinterService.setPrinterPrintAlignment(2,callback);
                    mIPosPrinterService.setPrinterPrintFontSize(16,callback);
                    text = new String[4];
                    width = new int[] { 10, 6, 6, 8 };
                    align = new int[] { 0, 2, 2, 2 }; // 左齐,右齐,右齐,右齐
                    text[0] = "名称";
                    text[1] = "数量";
                    text[2] = "单价";
                    text[3] = "金额";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "草莓酸奶A布甸";
                    text[1] = "4";
                    text[2] = "12.00";
                    text[3] = "48.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "酸奶水果夹心面包B";
                    text[1] = "10";
                    text[2] = "4.00";
                    text[3] = "40.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "酸奶水果布甸香橙软桃蛋糕"; // 文字超长,换行
                    text[1] = "100";
                    text[2] = "16.00";
                    text[3] = "1600.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 1,callback);
                    text[0] = "酸奶水果夹心面包";
                    text[1] = "10";
                    text[2] = "4.00";
                    text[3] = "40.00";
                    mIPosPrinterService.printColumnsText(text, width, align, 0,callback);
                    mIPosPrinterService.printBlankLines(1, 8, callback);

                    mIPosPrinterService.printerPerformPrint(160,callback);

                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void performPrint()
    {
        final IPosPrinterService printerService = mIPosPrinterService;
        ThreadPoolManager.getInstance().executeTask(new Runnable()
        {

            @Override
            public void run()
            {
                try {
                   
                      printerService.printerPerformPrint(160,new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                          if (isSuccess) {
                            Log.i(TAG, "Success: " + "");
                          } else {
                            Log.i(TAG, "Success: " + "");
                          }
                        }
            
                        @Override
                        public void onReturnString(String result) {
                            Log.i(TAG, "Success: " + result);
                        }
            
                        @Override
                        public void onRaiseException(int code, String msg) {
                            Log.i(TAG, "ERROR: " + msg);
                        }
                      });
                }catch (Exception e){
                    Log.i(TAG, "ERROR: " + e.getMessage());
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
