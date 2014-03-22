package pt.up.fe.sdis.proj1;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.MyFile;

import com.almworks.sqlite4java.SQLiteException;

public class Main {    
    public static void main(String[] args) throws SQLiteException, IOException, NoSuchAlgorithmException, InterruptedException {
        File file = new File("data.db");
        //file.createNewFile();
        
        BackupSystem.Files bf = new BackupSystem.Files(new File("data.db"));
        
        MyFile mfile = new MyFile("192.168.0.197",
                "[Raghu_Ramakrishnan,_Johannes_Gehrke]_Database_Man(BookFi.org).pdf");

//        System.out.println("Before file: " + bf);
//
//        bf.addFile(mfile);
//        System.out.println("Before chunks: " + bf);
//
//        for (int i = 0; i <= 10; ++i) {
//            bf.addChunk(mfile, i);
//        }
//
//        System.out.println("Before Ips: " + bf);
//
        String[] ips = { "/192.168.199", "/192.168.0.197", "/192.168.0.198" };
//
//        for (int i = 0; i <= 10; ++i) {
//            for (String ip : ips)
//                bf.addPeer(mfile, i, ip);
//        }
//
//        System.out.println(bf);
        
//        System.out.println("Before delete ips - " + bf);
//        
//        for (int i = 0; i <= 10; ++i) {
//            for (String ip : ips)
//                bf.removePeer(mfile, i, ip);
//        }
        
//        System.out.println("Before remove chunks: " + bf);
//
//        for (int i = 0; i <= 10; ++i) {
//            bf.removeChunk(mfile, i);
//        }
//        
        
//        System.out.println("Before remove file: " + bf);
//        
        bf.removeFile(mfile);
        
        System.out.println(bf);
        
        bf.dispose();
        
    }
}
