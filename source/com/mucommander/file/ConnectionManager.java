
package com.mucommander.file;

import java.util.Hashtable;
import java.util.Enumeration;

public class ConnectionManager {

    private static ConnectionManager ConnectionManager = new ConnectionManager();
    private static Hashtable entries;

    private ConnectionManager() {
        entries = new Hashtable();
    }

    public static void put(String path, Object connectionObject) {
        entries.put(path, connectionObject);
    }

    public static Object get(String path) {
        Object connection = entries.get(path);
        if(connection!=null)
            return connection;

        Enumeration keys = entries.keys();
        String key;
        Object bestConnection = null;
        int max = -1;
        int length;
        while (keys.hasMoreElements()) {
            key = (String)keys.nextElement();
            if (path.startsWith(key)) {
                length = key.length();
                if (length>max) {
                    max = length;
                    bestConnection = entries.get(key);
                }
            }
        }
        return bestConnection;
    }

    public static void remove(String path) {
        entries.remove(path);
    }
}
