package pt.up.fe.sdis.proj1.utils;

import java.util.regex.Pattern;

public class NetworkUtils {
    public static boolean isIPAddress(String str) {
        Pattern ipPattern = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
        boolean bool = ipPattern.matcher(str).matches();
        if (bool) {
            String[] ipAry = str.split("\\.");
            for (int i = 0; i < ipAry.length; i++) {
                int value = Integer.parseInt(ipAry[i]);
                if ((value < 0) || (value > 255)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isValidPort(String str) {
        int port;
        try {
            port = Integer.parseInt(str);
            return isValidPort(port);
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    
    public static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }
}
