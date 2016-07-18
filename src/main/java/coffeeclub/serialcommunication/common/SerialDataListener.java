package coffeeclub.serialcommunication.common;

/**
 * Created by coffeeclub on 3/13/14.
 * please implements this interface and call serialcomm.addSerialDataListener. when the serial read available data the method notifydata() will be called
 */
public interface SerialDataListener {

    public void notifyData();   //this method will be call when data available

}
