package com.sunsharing.directMan;

import com.alibaba.fastjson.JSONObject;
import com.sunsharing.component.utils.base.StringUtils;
import com.sunsharing.sfs.common.utils.Path;
import com.sunsharing.sfs.requestapi.Request;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by criss on 14-8-23.
 */
public class File implements Serializable{

    long serialVersionUID = 1L;

    boolean direct;

    List<File> files = new ArrayList<File>();

    String source_name="";

    long lastModify=0;

    String size="";

    File parent;

    String dfs_name="";

    private File(){

    }

    public static File getRoot()
    {
        return root;
    }

    static File root = new File();

    public static void initRoot()
    {
        java.io.File fileDir = new java.io.File(Path.getContextRealPath()+"dirfilename");
        java.io.File dataFile=new java.io.File(Path.getContextRealPath()+"data");
        System.out.println(dataFile.getAbsolutePath());
        if(fileDir.exists())
        {
            String sourcename = "";
            FileInputStream read = null;
            FileOutputStream out = null;
            if(dataFile.exists())
            {
                dataFile.delete();
            }
            try
            {
                byte[] array = new byte[1024];
                read = new FileInputStream(fileDir);
                int len = read.read(array);
                sourcename = new String(array,0,len);
                out = new FileOutputStream(dataFile);
                Request request = new Request();
                request.read(sourcename,out,"192.168.0.169",1320,false);
            }catch (Exception e)
            {
                e.printStackTrace();
            }finally {
                if(read!=null)
                {
                    try
                    {
                        read.close();
                    }catch (Exception e)
                    {

                    }
                }
                if(out!=null)
                {
                    try
                    {
                        out.close();
                    }catch (Exception e)
                    {

                    }
                }
            }

            FileInputStream input = null;
            try
            {
                input = new FileInputStream(dataFile);
                ObjectInputStream input2 = new ObjectInputStream(input);
                root = (File)input2.readObject();

                initIndex(root);

                System.out.println("-------加载索引成功--------");

            }catch (Exception e)
            {
                e.printStackTrace();
            }finally {
                if(input!=null)
                {
                    try
                    {
                        input.close();
                    }catch (Exception e)
                    {

                    }
                }
            }

        }
    }

    public static void initIndex(File f)
    {
//        if(StringUtils.isBlank(f.source_name))
//        {
//            return;
//        }
        FullText.add(f);
        for(File f2:f.files)
        {
            initIndex(f2);
        }
    }

    public synchronized  void getDirs(File f,List<String> dirs,String basePath)
    {
        if(f == null)
        {
            f = root;
        }
        for(int i=0;i<f.files.size();i++)
        {
            if(f.files.get(i).direct)
            {
                dirs.add(basePath+"/"+f.files.get(i).source_name);
                getDirs(f.files.get(i),dirs,basePath+"/"+f.files.get(i).source_name);
            }
        }
    }

    public synchronized List<File> getChilden(String filePath)
    {
        if("root".equals(filePath))
        {
            return root.files;
        }else
        {
            filePath = filePath.replaceAll("\\\\","/");
            String [] array = filePath.split("/");
            File f = root;

            for(int i=0;i<array.length;i++)
            {
                if(!StringUtils.isBlank(array[i]))
                {
                    boolean isExist = false;
                    for(File tmp:f.files)
                    {
                        if(tmp.source_name.equals(array[i]))
                        {
                            f = tmp;
                            isExist = true;
                            break;
                        }
                    }
                    if(!isExist)
                    {
                        return new ArrayList<File>();
                    }else
                    {
                        if(i==array.length-1)
                        {
                            return f.files;
                        }
                    }

                }
            }

           return new ArrayList<File>();

        }
    }

    public synchronized void addFile(String dfsname,String filePath,long lastModify,String size)
    {
        filePath = filePath.replaceAll("\\\\","/");
        if(filePath.endsWith("/"))
        {
            filePath = filePath.substring(0,filePath.length()-1);
        }
        if(filePath.startsWith("/"))
        {
            filePath = filePath.substring(1);
        }
        String [] array = filePath.split("/");
        File f = root;

        for(int i=0;i<array.length;i++)
        {
            if(!StringUtils.isBlank(array[i]))
            {
                List<File> list = f.files;
                boolean isexit = false;
                for(File tmp:list)
                {
                    if(tmp.source_name.equals(array[i]) && tmp.direct)
                    {
                        f = tmp;
                        isexit = true;

                        if(i == array.length-1)
                        {
                            //文件
                            f.lastModify = lastModify;
                            f.dfs_name = dfsname;
                        }

                        break;
                    }
                }
                if(!isexit)
                {
                    if(i == array.length-1)
                    {
                        File newDir = new File();
                        boolean update = false;
                        for(File tmp:f.files)
                        {
                            if(tmp.source_name.equals(array[i]))
                            {
                                newDir = tmp;
                                update = true;
                                break;
                            }
                        }
                        newDir.source_name = array[i];
                        newDir.direct = false;
                        newDir.parent = f;
                        newDir.dfs_name = dfsname;
                        newDir.lastModify = lastModify;
                        newDir.size = size;
                        if(!update)
                        {
                            f.files.add(newDir);
                            FullText.add(newDir);
                        }
                        f = newDir;
                    }else
                    {
                        File newDir = new File();
                        newDir.source_name = array[i];
                        newDir.direct = true;
                        newDir.parent = f;
                        f.files.add(newDir);
                        FullText.add(newDir);
                        f = newDir;
                    }
                }
            }
        }
        //sync();

    }

    public synchronized void addDirect(String filePath)
    {
        filePath = filePath.replaceAll("\\\\","/");
        if(filePath.endsWith("/"))
        {
            filePath = filePath.substring(0,filePath.length()-1);
        }
        if(filePath.startsWith("/"))
        {
            filePath = filePath.substring(1);
        }

        String [] array = filePath.split("/");
        File f = root;

        for(int i=0;i<array.length;i++)
        {
            if(!StringUtils.isBlank(array[i]))
            {
                List<File> list = f.files;
                boolean isexit = false;
                for(File tmp:list)
                {
                    if(tmp.source_name.equals(array[i]) && tmp.direct)
                    {
                        f = tmp;
                        isexit = true;
                        break;
                    }
                }
                if(!isexit)
                {
                    File newDir = new File();
                    newDir.source_name = array[i];
                    newDir.direct = true;
                    newDir.parent = f;
                    f.files.add(newDir);
                    f = newDir;
                }
            }
        }

        //sync();
    }



    public void sync()
    {
        String filePath = Path.getContextRealPath()+"dirfilename";
        String uuid = StringUtils.generateUUID();
        java.io.File dataFile=new java.io.File(Path.getContextRealPath()+uuid);


        FileOutputStream out = null;
        try
        {
            dataFile.createNewFile();
            out = new FileOutputStream(Path.getContextRealPath()+uuid);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(root);
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            if(out!=null)
            {
                try
                {
                    out.close();
                }catch (Exception e)
                {

                }
            }
        }

        if(!new java.io.File(filePath).exists())
        {
            Request request = new Request();
            String sfsname = request.addFile(dataFile,50*1024*1024,"192.168.0.169",1320,30000);
            try
            {
                out = new FileOutputStream(filePath);
                out.write(sfsname.getBytes("UTF-8"));
            }catch (Exception e)
            {
                e.printStackTrace();
            }finally {
                if(out!=null)
                {
                    try
                    {
                        out.close();
                    }catch (Exception e)
                    {

                    }
                }
                dataFile.delete();
            }
        }else
        {
            FileInputStream read = null;
            String sourcename = "";
            try
            {
                byte[] array = new byte[1024];
                read = new FileInputStream(filePath);
                int len = read.read(array);
                sourcename = new String(array,0,len);
                Request request = new Request();
                request.updateFile(dataFile, sourcename, "192.168.0.169", 1320, 20000);
            }catch (Exception e)
            {
                e.printStackTrace();
            }finally {
                if(read!=null)
                {
                    try
                    {
                        read.close();
                    }catch (Exception e)
                    {

                    }
                }
                dataFile.delete();
            }
        }
    }

    public static void main(String []a)
    {
        File f = new File();
        f.source_name = "1";
        f.direct = true;

        File f2 = new File();
        f2.source_name = "2";
        f2.direct = true;

        File f3 = new File();
        f3.source_name = "3";
        f3.direct = true;

        f.files.add(f2);
        f2.files.add(f3);
        File.root = f;

        List<String> arr = new ArrayList<String>();

        File.root.getDirs(null,arr,"");
        for(int i=0;i<arr.size();i++)
        {
            System.out.println(arr.get(i));
        }
    }

}
