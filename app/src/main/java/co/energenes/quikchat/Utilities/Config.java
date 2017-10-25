package co.energenes.quikchat.Utilities;

/**
 * Created by rfkamd on 7/20/2017.
 */

public class Config {

    public static String HOST = "74.208.157.203";//"192.168.1.104";//
    public static String PORT = "3000";

    public static String BASE_URL = "http://"+HOST+":"+PORT+"/api/";

    public static String SOCKET_ADDRESS = "http://" + Config.HOST + (":" + Config.PORT + "/");

    public static boolean SHOW_NOTIFICATION = true;
}
