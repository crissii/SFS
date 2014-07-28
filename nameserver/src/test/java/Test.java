import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by criss on 14-7-4.
 */
public class Test {

    public static void main(String[]a)
    {

        Map map = new TreeMap<String, String>();

        File[] roots = File.listRoots();
        double unit = Math.pow(1024, 3);

        for (int i = 0; i < roots.length; i++) {

            String hd = roots[i].getPath();

            double freespace = roots[i].getFreeSpace() / unit;

            freespace = Math.ceil((freespace * 10)) / 10;

            map.put(hd, String.valueOf(freespace));
            System.out.println(hd);
            System.out.println(String.valueOf(freespace));

        }
    }

}
