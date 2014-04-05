import com.funtowiczmo.tftp.ui.TFTPWindow;

/**
 * Created by Morgan on 05/04/2014.
 */
public class Main {
    public static void main(String[] args) {
        if(args.length == 0){
            launchUI();
        }else{
            checkArgs(args);
        }
    }

    private static void launchUI() {
        new TFTPWindow().setVisible(true);
    }

    private static void checkArgs(String[] args) {

    }
}
