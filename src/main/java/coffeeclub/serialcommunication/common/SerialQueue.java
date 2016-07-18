package coffeeclub.serialcommunication.common;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by coffeeclub on 3/12/14.
 */
public class SerialQueue {

    ConcurrentLinkedQueue<String> serialData;
    Integer size = 0;
    final int maxLength = 1024 * 1024;

    public SerialQueue() {
        serialData = new ConcurrentLinkedQueue<String>();
    }

    public void inQueue(String data) {
        synchronized (size) {
            if (size >= maxLength) {
                serialData.clear();
                size = 0;
            }
            serialData.offer(data);
            size += data.length();
        }
    }

    public String deQueue() {
        String data = serialData.poll();
        synchronized (size) {
            size -= data.length();
        }
        return data;
    }

    public int getSize() {
        synchronized (size) {
            return size;
        }
    }

    public void clear(){
        serialData.clear();
    }
}
