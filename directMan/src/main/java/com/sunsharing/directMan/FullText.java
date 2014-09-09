package com.sunsharing.directMan;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunsharing.component.utils.base.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.wltea.analyzer.lucene.IKQueryParser;
import org.wltea.analyzer.lucene.IKSimilarity;

/**
   * Created by criss on 14-9-7.
 */
public class FullText {

    public static void main(String []a) throws Exception
    {
//        Analyzer ikAnalyzer = new IKAnalyzer();
//        System.out.println("======中文=======IKAnalyzer======分词=======");
//        showToken(ikAnalyzer, "中文何鑫");
//
//        Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_30);
//        System.out.println("=====一元========StandardAnalyzer=====分词========");
//        showToken(standardAnalyzer, "中文何鑫");
        //add();

    }

    public static JSONArray  fullText(String keyword)
    {
        try
        {
        IndexReader reader = IndexReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new IKSimilarity());
        //String keyWords = "何鑫";

        Query query = IKQueryParser.parse("source_name", keyword);
        TopDocs topDocs = searcher.search(query, 100);
        System.out.println(topDocs.totalHits);

        ScoreDoc[] score = topDocs.scoreDocs;
            JSONArray array = new JSONArray();
        if(score!=null)
        {
            for(int i=0;i<score.length;i++)
            {
                ScoreDoc sdoc = score[i];
                Document doc = searcher.doc(sdoc.doc);
                JSONObject tmp = new JSONObject();
                tmp.put("direct",new Boolean(doc.get("direct")));
                tmp.put("source_name",doc.get("source_name"));
                tmp.put("dfs_name",doc.get("dfs_name"));
                tmp.put("last_modify", new Long(doc.get("lastModify")));
                tmp.put("size", doc.get("size"));
                tmp.put("path", doc.get("path"));
                array.add(tmp);
            }
        }
        return array;



        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    static    RAMDirectory directory = new RAMDirectory();

    public static void add(File f)
    {
        if(StringUtils.isBlank(f.source_name))
        {
            return;
        }
        IndexWriter indexWriter = null;
        try
        {


            //File INDEX_DIR = new File("E:\\temp\\index");

        Analyzer analyzer = new IKAnalyzer();

        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_33, analyzer);
         indexWriter = new IndexWriter(directory, iwc);

            //String str = "你好俊杰，这是成功的开始！加油！";

        boolean direct = f.direct;
        Document doc = new Document();
        doc.add(new Field("direct",direct+"",Field.Store.YES,Field.Index.NO));


        //Document doc2 = new Document();
        doc.add(new Field("source_name", f.source_name + "", Field.Store.YES, Field.Index.ANALYZED));
        //indexWriter.addDocument(doc2);

        System.out.println(f.source_name+":"+f.lastModify+"......");
        //Document doc3 = new Document();
        doc.add(new Field("lastModify", f.lastModify + "", Field.Store.YES, Field.Index.NO));
        //indexWriter.addDocument(doc3);

        //Document doc5 = new Document();
        doc.add(new Field("dfs_name", f.dfs_name + "", Field.Store.YES, Field.Index.NO));
        //indexWriter.addDocument(doc5);


        //Document doc4 = new Document();
        if(StringUtils.isBlank(f.size))
        {
            doc.add(new Field("size", "", Field.Store.YES, Field.Index.NO));
        }else
        {
            doc.add(new Field("size", f.size + "", Field.Store.YES, Field.Index.NO));
        }

        //indexWriter.addDocument(doc4);


        List list = new ArrayList();
        getPath(f,list);
        String path = "";
        for(int i=list.size()-1;i>=0;i--)
        {
            path+=list.get(i)+"/";
        }
        if(path.length()>0)
        {
            path = path.substring(0,path.length()-1);
        }

        System.out.println(path);
        //Document doc6 = new Document();
        doc.add(new Field("path", path, Field.Store.YES, Field.Index.NO));
        //indexWriter.addDocument(doc6);

            indexWriter.addDocument(doc);

        System.out.println("add index。。。。。。。。。。。");
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            if(indexWriter!=null)
            {
                try
                {
                    indexWriter.close();
                }catch (Exception e)
                {

                }
            }

        }

//        IndexReader reader = IndexReader.open(directory);
//        IndexSearcher searcher = new IndexSearcher(reader);
//        searcher.setSimilarity(new IKSimilarity());
//        String keyWords = "何鑫";
//
//        Query query = IKQueryParser.parse("contents", keyWords);
//        TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
//        System.out.println(topDocs.totalHits);

    }

    public static void getPath(File f,List<String> list)
    {
        list.add(f.source_name);
        while(f.parent!=null)
        {
            list.add(f.parent.source_name);
            f = f.parent;
        }
    }

    /**
               * 分词及打印分词结果的方法
            indexWriter.close();  * @param analyzer     分词器名称
     * @param text         要分词的字符串
     * @throws IOException 抛出的异常
     */
    public static void showToken(Analyzer analyzer, String text) throws IOException {

        Reader reader = new StringReader(text);
        TokenStream stream = (TokenStream)analyzer.tokenStream("", reader);
        //添加工具类  注意：以下这些与之前lucene2.x版本不同的地方
        TermAttribute termAtt  = (TermAttribute)stream.addAttribute(TermAttribute.class);
        OffsetAttribute offAtt  = (OffsetAttribute)stream.addAttribute(OffsetAttribute.class);
        // 循环打印出分词的结果，及分词出现的位置
        while(stream.incrementToken()){
            System.out.print(termAtt.term() + "|("+ offAtt.startOffset() + " " + offAtt.endOffset()+")");
        }
        System.out.println();
    }

}
