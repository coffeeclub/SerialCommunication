package coffeeclub.serialcommunication.serial;

import coffeeclub.serialcommunication.common.SerialQueue;
import coffeeclub.serialcommunication.common.SerialDataListener;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Created by coffeeclub on 3/12/14.
 * read serial port data
 */
public class SerialRead implements Runnable, SerialPortEventListener {

    SerialQueue serialQueue;
    InputStream input;
    volatile static int eof;
    byte[] tmpData;
    SerialPort serialPort;
    volatile boolean dataAvailable = false;
    volatile List<SerialDataListener> dataListeners;
    static final Logger logger = LogManager.getLogger(SerialRead.class.getName());

    public SerialRead(SerialQueue queue, SerialPort serialPort, List<SerialDataListener> listeners) {
        serialQueue = queue;
        tmpData = new byte[1024];
        eof = 1;
        this.serialPort = serialPort;
        dataListeners = listeners;
    }

    public void run() {
        try {
            input = serialPort.getInputStream();
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            logger.error("run: " + e);
        }
        while (eof != 0) {

//            try {                        //other method
//                if (input.available() > 0) {
//                    dataCount = input.read(tmpData);
//                    if (dataCount > 0) {
//                        String mess = new String(tmpData, 0, dataCount);
//                        serialQueue.inQueue(mess);
//                        System.out.print(mess);
//                    }
//                }
//            } catch (Exception e) {
//            }

            if (dataAvailable) {
                try {
                    readData();
                } catch (Exception e) {
                    logger.error("readData: " + e);
                }
                dataAvailable = false;
            }
            synchronized (this) {
                if (!dataAvailable) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
        try {
            input.close();
        } catch (IOException e) {
            logger.error("input close: " + e);
        }
    }

    private void readData() throws IOException {
        int dataCount;
        while (input.available() > 0) {
            dataCount = input.read(tmpData);
            if (dataCount > 0) {
                String mess = new String(tmpData, 0, dataCount);
                serialQueue.inQueue(mess);
                System.out.print(mess);
            }
        }
    }

    public static void close() {
        eof = 0;
    }

    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            dataAvailable = true;
            synchronized (this) {
                notifyAll();
            }
            notifyDataListeners();
        }
    }

    private void notifyDataListeners() {
        Iterator<SerialDataListener> iterator = dataListeners.listIterator();
        while (iterator.hasNext()) {
            iterator.next().notifyData();
        }
    }
}
