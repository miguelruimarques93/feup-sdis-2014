package pt.up.fe.sdis.proj1.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
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
        return MINIMUM_PORT >= 0 && MAXIMUM_PORT <= 65535;
    }
    
    public static final int MINIMUM_PORT = 0;
    public static final int MAXIMUM_PORT = 65535;

    public static InetAddress[] getPossibleInterfaces() {
        try {
            ArrayList<InetAddress> result = new ArrayList<InetAddress>();
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                if (intf.supportsMulticast())
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress ipAddr = enumIpAddr.nextElement();
                        if (!ipAddr.isLoopbackAddress() && ipAddr instanceof Inet4Address) {
                            result.add(ipAddr);
                        }
                    }
            }

            InetAddress[] resultArray = new InetAddress[result.size()];
            result.toArray(resultArray);
            return resultArray;
        } catch (SocketException e) {
            return null;
        }
    }
}
