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

    /* 定义消息 */
    private final int MSG_TEST = 1;
    private final int MSG_IS_NORMAL = 2;
    private final int MSG_IS_BUSY = 3;
    private final int MSG_PAPER_LESS = 4;
    private final int MSG_PAPER_EXISTS = 5;
    private final int MSG_THP_HIGH_TEMP = 6;
    private final int MSG_THP_TEMP_NORMAL = 7;
    private final int MSG_MOTOR_HIGH_TEMP = 8;
    private final int MSG_MOTOR_HIGH_TEMP_INIT_PRINTER = 9;

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
            int res = 0;
            try {
                
                if (action == PRINTER_NORMAL_ACTION) {
                    res = 0;
                } else if (action == PRINTER_PAPERLESS_ACTION) {
                    res = 1;
                } else if (action == PRINTER_PAPEREXISTS_ACTION) {
                    res = 2;
                } else if (action == PRINTER_THP_HIGHTEMP_ACTION) {
                    res = 3;
                } else if (action == PRINTER_THP_NORMALTEMP_ACTION) {
                    res = 4;
                } else if (action == PRINTER_MOTOR_HIGHTEMP_ACTION) {
                    res = 5;
                } else if (action == PRINTER_BUSY_ACTION) {
                    res = 6;
                } else if (action == PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION) {
                    res = 7;
                } else if (action == GET_CUST_PRINTAPP_PACKAGENAME_ACTION) {
                    res = 8;
                }

                jsonObj.put("type", type);
                jsonObj.put("action", res);

                Log.i(TAG, "RECEIVED STATUS " + action);

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
