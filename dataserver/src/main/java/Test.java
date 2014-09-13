import java.io.File;
import java.io.RandomAccessFile;

/**
 * Created by criss on 14-9-9.
 */
public class Test {

    public static void main(String[]a)
    {
        RandomAccessFile raf1 = null;
        File f = new File("block");
        File[] f2 = f.listFiles();
        for(int i=0;i<f2.length;i++)
        {
            try
            {
                raf1 = new RandomAccessFile(f2[i],"rw");
                long l = raf1.readLong();
                System.out.println(f2[i].getName()+":"+l);
                /**
                 * 67435051
                 * 66885885
                 * 68747054
                 */
                //System.out.println(l+1280003+32000);

                raf1.close();
            }catch (Exception e)
            {

            }
        }
    }

}
