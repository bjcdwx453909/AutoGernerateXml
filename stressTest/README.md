WIFI自动连接：可以通过UI手动触发，或通过命令广播触发  
====
  1.拉起应用，adb shell am start com.gree.stresstest/.StatusMonitorActivity  
  2.开启wifi自动连接广播命令：adb shell am broadcast -a SERVICES.START_WIFI_MONITOR   -e ssid wifi名 -e pwd 密码  （ssid为wifissid,pwd为wifi密码，若包含空格或特殊字符用转移字符,  
  如-e ssid network\\& -e pwd abc\\*123\\&）   
  3.关闭wifi自动连接广播命令：adb shell am broadcast -a SERVICES.STOP_WIFI_MONITOR  

开启蓝牙自动连接：可以通过UI手动触发，或通过命令广播触发  
====
  1.拉起应用，adb shell am start com.gree.stresstest/.StatusMonitorActivity  
  2.开启蓝牙自动连接广播命令：adb shell am broadcast -a SERVICES.START_BLUETOOTH_MONITOR  
  3.关闭蓝牙自动连接广播命令：adb shell am broadcast -a SERVICES.STOP_BLUETOOTH_MONITOR  

开启音量自动调节：可以通过UI手动触发，或通过命令广播触发 
====
  1.拉起应用，adb shell am start com.gree.stresstest/.StatusMonitorActivity  
  2.开启自动静音广播命令：adb shell am broadcast -a SERVICES.START_VOLUME_MONITOR  -e volume 音量值（0-15）  
  3.关闭自动静音广播命令：adb shell am broadcast -a SERVICES.STOP_VOLUME_MONITOR  

开启亮度自动调节：可以通过UI手动触发，或通过命令广播触发
====
  1.拉起应用，adb shell am start com.gree.stresstest/.StatusMonitorActivity  
  2.开启自动调低亮度广播命令：adb shell am broadcast -a SERVICES.START_BRIGHTNESS_MONITOR  -e brightness 亮度值（0-255)  
  3.关闭自动调低亮度广播命令：adb shell am broadcast -a SERVICES.STOP_BRIGHTNESS_MONITOR  