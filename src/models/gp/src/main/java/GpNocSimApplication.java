import gpApp.gui.GpNoCSimRunable;

public class GpNocSimApplication {
    public static void main(String[] args) {
        Thread runnable = new Thread(new GpNoCSimRunable(args[0], args[1]));
        runnable.start();
    }
}
