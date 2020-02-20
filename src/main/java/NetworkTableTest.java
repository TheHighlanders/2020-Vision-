import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;


public class NetworkTableTest{
    NetworkTableEntry Testpi;
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable table;
    double yt = 0;
    int trials = 15;
    public NetworkTableTest(){
        table = inst.getTable("RaspberryCheesecake");
        Testpi = table.getEntry("Testpi");
        inst.startServer();
    }


    public void Test(){
    for(int i = 0;i > trials; i ++){
        Testpi.setDouble(yt);
        yt += 0.1; 
        System.out.println("Test is commencing");
    }
    }
    
}
