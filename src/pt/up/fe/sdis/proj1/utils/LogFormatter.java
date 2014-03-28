package pt.up.fe.sdis.proj1.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    @Override
    public String format(LogRecord arg0) {
        // TODO Auto-generated method stub
        return _dateFormat.format(new Date(arg0.getMillis())) + " - " + arg0.getMessage();
    }
    
    private static SimpleDateFormat _dateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

}
