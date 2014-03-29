package pt.up.fe.sdis.proj1.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import pt.up.fe.sdis.proj1.utils.Pair;

public class BackupSystemConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private BackupSystemConfiguration(String configFilePath) {
        _configFile = configFilePath;
        _modified = true;
    }
    
    public void save() {
        if (!_modified) return;
        
        try {
            FileOutputStream fileOut = new FileOutputStream(_configFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
            _modified = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void setConfigFile(String configFilePath) { 
        _configFile = configFilePath;
    }
    
    public static BackupSystemConfiguration loadFromFile(File configFile) {
        if (!configFile.exists()) {
            return new BackupSystemConfiguration(configFile.getAbsolutePath());
        } else {
            try {
                FileInputStream fileIn = new FileInputStream(configFile);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                BackupSystemConfiguration result = (BackupSystemConfiguration) in.readObject();
                in.close();
                fileIn.close();
                result.setConfigFile(configFile.getAbsolutePath());
                return result;
            } catch (Exception e) {
                return new BackupSystemConfiguration(configFile.getAbsolutePath());
            }
        }
    }
    
    public static BackupSystemConfiguration loadFromFile(String configFilePath) {
        File configFile = new File(configFilePath);
        return loadFromFile(configFile);
    }
    
    public Pair<String, Integer> getMC() {
        return _mc;
    }

    public void setMC(Pair<String, Integer> _mc) {
        this._mc = _mc;
        _modified = true;
    }

    public Pair<String, Integer> getMDB() {
        return _mdb;
    }

    public void setMDB(Pair<String, Integer> _mdb) {
        this._mdb = _mdb;
        _modified = true;
    }

    public Pair<String, Integer> getMDR() {
        return _mdr;
    }

    public void setMDR(Pair<String, Integer> _mdr) {
        this._mdr = _mdr;
        _modified = true;
    }

    public String getWorkingDir() {
        return _workingDir;
    }

    public void setWorkingDir(String _workingDir) {
        this._workingDir = _workingDir;
        _modified = true;
    }

    public Integer getDefaultReplicationDeegree() {
        return _defaultReplicationDeegree;
    }

    public void setDefaultReplicationDeegree(Integer _defaultReplicationDeegree) {
        this._defaultReplicationDeegree = _defaultReplicationDeegree;
        _modified = true;
    }

    public Long getAvailableSpace() {
        return _availableSpace;
    }

    public void setAvailableSpace(Long _availableSpace) {
        this._availableSpace = _availableSpace;
        _modified = true;
    }

    public Integer getProtocolVersion() {
        return _protocolVersion;
    }

    public void setProtocolVersion(Integer _protocolVersion) {
        this._protocolVersion = _protocolVersion;
        _modified = true;
    }

    public Integer getRestorePort() {
        return _restorePort;
    }

    public void setRestorePort(Integer _restorePort) {
        this._restorePort = _restorePort;
        _modified = true;
    }

    private Pair<String, Integer> _mc;
    private Pair<String, Integer> _mdb;
    private Pair<String, Integer> _mdr;
    private String _workingDir = new File("workDir").getAbsolutePath();
    private Integer _defaultReplicationDeegree = 1;
    private Long _availableSpace = 0L;
    private Integer _protocolVersion = 1;
    private Integer _restorePort = 11094;
    
    private transient boolean _modified = false;
    private transient String _configFile;
}
