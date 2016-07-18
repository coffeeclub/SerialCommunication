package coffeeclub.serialcommunication.serial;

import coffeeclub.serialcommunication.common.SerialDataListener;
import coffeeclub.serialcommunication.common.SerialQueue;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import java.io.IOException;
import java.util.*;

/**
 * Created by coffeeclub on 3/12/14.
 * serial common class ,please open() before use and close() after use
 */
public class SerialComm {

    private SerialQueue serialQueue;
    private SerialPort serialPort;
    private SerialWrite serialWrite;
    private Thread read;
    private List<SerialDataListener> dataListeners;
    private String device;
    private static Map<String, SerialComm> allSerialComms = new HashMap<String, SerialComm>();
    static final Logger logger= LogManager.getLogger(SerialComm.class.getName());

    public static SerialComm getInstance(String device) {
        if (allSerialComms.containsKey(device)) {
            return allSerialComms.get(device);
        } else {
            SerialComm instance = new SerialComm(device);
            allSerialComms.put(device, instance);
            return instance;
        }
    }

    private SerialComm(String device) {
        this.device = device;
    }

    public void open() throws Exception {
        open(115200);
    }

    public void open(int baudRate) throws Exception {
        dataListeners = new ArrayList<SerialDataListener>();
        openPort(baudRate);
        serialQueue = new SerialQueue();
        read = new Thread(new SerialRead(serialQueue, serialPort, dataListeners));
        read.start();
        serialWrite = new SerialWrite(serialPort.getOutputStream());
    }

    public void close() {
        try {
            serialWrite.close();
            SerialRead.close();
            read.interrupt();
            read.join();
        } catch (Exception e) {
            logger.error("close: " + e.getMessage());
        }

        serialPort.close();
    }

    private void openPort(int baudRate) throws Exception {
        final int timeout = 3 * 1000;
        Enumeration en = CommPortIdentifier.getPortIdentifiers();
        while (en.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) en.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                logger.info(portId.getName());
            }
        }
        CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(this.device);

        serialPort = (SerialPort) portId.open("test", timeout);
        serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    }

    public void sendCommand(String command) throws IOException,InterruptedException {
        serialWrite.sendCommand(command);
        Thread.sleep(300);
    }

    public String getReadMessage() {
        if (serialQueue.getSize() == 0) {
            return null;
        }
        return serialQueue.deQueue();
    }

    public void addSerialDataListener(SerialDataListener listener, String name) throws TooManyListenersException {

        if (dataListeners.size() > 10) {
            throw new TooManyListenersException("Too many serial Data Listener Exception");
        }
        dataListeners.add(listener);
        logger.info("A listener join: " + name);
    }

    public void clearSerialQueue(){
        this.serialQueue.clear();
    }
}
