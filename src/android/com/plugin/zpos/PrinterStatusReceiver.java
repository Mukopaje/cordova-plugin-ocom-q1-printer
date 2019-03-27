package android.com.plugin.zpos;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.widget.Toast;
import android.util.Log;

import org.json.JSONObject;

import android.com.plugin.zpos.utils.HandlerUtils;

public class PrinterStatusReceiver extends BroadcastReceiver {
  private static final String TAG = "ZPOSQ1PrinterReceiver";

  private CallbackContext callbackReceive;
  private boolean isReceiving = true;
  private HandlerUtils.MyHandler handler;

  /*定义消息*/
  private final int MSG_TEST                               = 1;
  private final int MSG_IS_NORMAL                          = 2;
  private final int MSG_IS_BUSY                            = 3;
  private final int MSG_PAPER_LESS                         = 4;
  private final int MSG_PAPER_EXISTS                       = 5;
  private final int MSG_THP_HIGH_TEMP                      = 6;
  private final int MSG_THP_TEMP_NORMAL                    = 7;
  private final int MSG_MOTOR_HIGH_TEMP                    = 8;
  private final int MSG_MOTOR_HIGH_TEMP_INIT_PRINTER       = 9;


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

  public PrinterStatusReceiver() {

  }

  @Override
  public void onReceive(Context context, Intent data) {
    if (this.isReceiving && this.callbackReceive != null) {
      String action = data.getAction();
      String type = "PrinterStatus";

      JSONObject jsonObj = new JSONObject();
      try {
        jsonObj.put("type", type);
        jsonObj.put("action", action);

        if(action == null)
        {
            Log.d(TAG,"IPosPrinterStatusListener onReceive action = null");
            return;
        }
        Log.d(TAG,"IPosPrinterStatusListener action = "+action);
        if(action.equals(PRINTER_NORMAL_ACTION))
        {
            handler.sendEmptyMessageDelayed(MSG_IS_NORMAL,0);
        }
        else if (action.equals(PRINTER_PAPERLESS_ACTION))
        {
            handler.sendEmptyMessageDelayed(MSG_PAPER_LESS,0);
        }
        else if (action.equals(PRINTER_BUSY_ACTION))
        {
            handler.sendEmptyMessageDelayed(MSG_IS_BUSY,0);
        }
        else if (action.equals(PRINTER_PAPEREXISTS_ACTION))
        {
            handler.sendEmptyMessageDelayed(MSG_PAPER_EXISTS,0);
        }
        else if (action.equals(PRINTER_THP_HIGHTEMP_ACTION))
        {
            handler.sendEmptyMessageDelayed(MSG_THP_HIGH_TEMP,0);
        }
        else if (action.equals(PRINTER_THP_NORMALTEMP_ACTION))
        {
            handler.sendEmptyMessageDelayed(MSG_THP_TEMP_NORMAL,0);
        }
        else if (action.equals(PRINTER_MOTOR_HIGHTEMP_ACTION))  //此时当前任务会继续打印，完成当前任务后，请等待2分钟以上时间，继续下一个打印任务
        {
            handler.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP,0);
        }
        else
        {
            handler.sendEmptyMessageDelayed(MSG_TEST,0);
        }

        Log.i(TAG, "RECEIVED STATUS " + action);
        // Toast.makeText(webView.getContext(), "Printer Status " + action, Toast.LENGTH_LONG).show();

        PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObj);
        result.setKeepCallback(true);
        callbackReceive.sendPluginResult(result);
      } catch (Exception e) {
        Log.i(TAG, "ERROR: " + e.getMessage());
      }
    }
  }

  public void startReceiving(CallbackContext ctx) {
    this.callbackReceive = ctx;
    this.isReceiving = true;

    Log.i(TAG, "Start receiving status");
  }

  public void stopReceiving() {
    this.callbackReceive = null;
    this.isReceiving = false;

    Log.i(TAG, "Stop receiving status");
  }
}
