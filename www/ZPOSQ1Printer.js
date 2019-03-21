var exec = require('cordova/exec');

module.exports = {

    printerInit: function (resolve, reject) {
      exec(resolve, reject, "ZPOSQ1Printer", "printerInit", []);
    },
    
    hasPrinter: function (resolve, reject) {
      exec(resolve, reject, "ZPOSQ1Printer", "hasPrinter", []);
    },
    sendRAWData: function (base64Data, resolve, reject) {
        exec(resolve, reject, "ZPOSQ1Printer", "sendRAWData", [base64Data]);
      },
      setAlignment: function (alignment, resolve, reject) {
        exec(resolve, reject, "Printer", "setAlignment", [alignment]);
      },
      setFontName: function (typeface, resolve, reject) {
        exec(resolve, reject, "ZPOSQ1Printer", "setFontName", [typeface]);
      },
      setFontSize: function (fontSize, resolve, reject) {
        exec(resolve, reject, "ZPOSQ1Printer", "setFontSize", [fontSize]);
      },
      printTextWithFont: function (text, typeface, fontSize, resolve, reject) {
        exec(resolve, reject, "Printer", "printTextWithFont", [text, typeface, fontSize]);
      },
      printColumnsText: function (colsTextArr, colsWidthArr, colsAlign, resolve, reject) {
        exec(resolve, reject, "Printer", "printColumnsText", [colsTextArr, colsWidthArr, colsAlign]);
      },
      printBitmap: function (base64Data, width, height, resolve, reject) {
        exec(resolve, reject, "ZPOSQ1Printer", "printBitmap", [base64Data, width, height]);
      },
      printBarCode: function (barCodeData, symbology, width, height, textPosition, resolve, reject) {
        exec(resolve, reject, "ZPOSQ1Printer", "printBarCode", [barCodeData, symbology, width, height, textPosition]);
      },
      printQRCode: function (qrCodeData, moduleSize, errorLevel, resolve, reject) {
        exec(resolve, reject, "ZPOSQ1Printer", "printQRCode", [qrCodeData, moduleSize, errorLevel]);
      },
      printOriginalText: function (text, resolve, reject) {
        exec(resolve, reject, "ZPOSQ1Printer", "printOriginalText", [text]);
      },
      printString: function (text, resolve, reject) {
        exec(resolve, reject, "ZPOSQ1Printer", "printString", [text]);
      },
      printerStatusStartListener: function (onSuccess, onError) {
        exec(onSuccess, onError, "ZPOSQ1Printer", "printerStatusStartListener", []);
      },
      printerStatusStopListener: function () {
        exec(function () {}, function () {}, "ZPOSQ1Printer", "printerStatusStopListener", []);
      }

}
