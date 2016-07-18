package coffeeclub.serialcommunication.serial;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by coffeeclub on 3/12/14.
 * write command to serial port
 */
public class SerialWrite {

    OutputStream output;

    public SerialWrite(OutputStream outputStream) {
        output = outputStream;
    }

    public void sendCommand(String comm) throws IOException {
        System.out.println("command:" + comm);
        for (byte b : comm.getBytes()) {
            output.write(b);
            output.flush();
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                //TODO:
            }
        }
    }

    public void close() throws IOException {
        output.close();
    }

}
