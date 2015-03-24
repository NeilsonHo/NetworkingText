package net.learn2develop.NetworkingText;

import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.provider.DocumentsContract;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.AsyncTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.learn2develop.Networking.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class NetworkingActivity extends Activity {
    String name="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //note-> scroll down for more code
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final EditText et = (EditText)findViewById(R.id.editText);
        Button b1 = (Button) findViewById(R.id.button);


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = et.getText().toString();
                new AccessWebServiceTask().execute(name);
            }
        });
        // ---access a Web Service∫ using GET---

    }
    //this function from the example opens and check if the urlString exists
    private InputStream OpenHttpConnection(String urlString)
            throws IOException {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
        return in;
    }

    //xmlParser function
    private String xmlParser(String word) throws XmlPullParserException{
        //ignore any list and Word objects
        InputStream in = null;
        String s1=word+"\n";
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        //List<Word> defList = null;
        try {
            in = OpenHttpConnection("http://services.aonaware.com/DictService/DictService.asmx/Define?word="
                    + word);
            xpp.setInput(in, null);
            int eventType = xpp.getEventType();

            //basically the eventType parses everything in the document from top to bottom
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //Word temp = new Word();
                //String tagName = xpp.getName();
                if (eventType == XmlPullParser.START_DOCUMENT) {

                }
                else if (eventType == XmlPullParser.END_DOCUMENT) {

                }
                else if (eventType == XmlPullParser.START_TAG) {
                    //this block checks if the eventType = to any <start tag>
                    //also the following the if checks eventType equals to the following tags
                    /*
                        if (xpp.getName().equalsIgnoreCase("word")){
                        if(xpp.next() == XmlPullParser.TEXT) {
                            //temp.set_id(xpp.getText());
                            s1+=xpp.getText()+"\n";
                        }
                    }
                    else if (xpp.getName().equalsIgnoreCase("wordDefinition")){
                        if(xpp.next() == XmlPullParser.TEXT) {
                            //temp.setDefinition(xpp.getText());
                            s1+=xpp.getText();
                        }
                    }
                    */
                }
                else if (eventType == XmlPullParser.END_TAG) {

                }
                else if (eventType == XmlPullParser.TEXT) {
                    //this blocks takes everything between the start and end tag
                    s1+=xpp.getText();
                }
                eventType = xpp.next();
            }

        } catch (IOException e) {
            Log.d("NetworkingActivity", e.getLocalizedMessage());
        }
        //for(int i=0;i<=defList.size();i++) {
        //}
        //s1 = defList.get(0).getDefinition();
        return s1;//returns the string
    }

    //DOM parser
    private String WordDefinition(String word) {
        InputStream in = null;
        String strDefinition = "";
        try {
            in = OpenHttpConnection("http://services.aonaware.com/DictService/DictService.asmx/Define?word="
                    + word);
            Document doc = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder db;
            try {
                db = dbf.newDocumentBuilder();
                doc = db.parse(in);
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            doc.getDocumentElement().normalize();

            // ---retrieve all the <Definition> elements---
            NodeList definitionElements = doc
                    .getElementsByTagName("Definition");

            // ---iterate through each <Definition> elements---
            for (int i = 0; i < definitionElements.getLength(); i++) {
                Node itemNode = definitionElements.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    // ---convert the Definition node into
                    // an Element---
                    Element definitionElement = (Element) itemNode;

                    // ---get all the <WordDefinition>
                    // elements under
                    // the <Definition> element---
                    NodeList wordDefinitionElements = (definitionElement)
                            .getElementsByTagName("WordDefinition");

                    strDefinition = "";
                    // ---iterate through each
                    // <WordDefinition> elements---
                    for (int j = 0; j < wordDefinitionElements
                            .getLength(); j++) {
                        // ---convert a <WordDefinition>
                        // node into an Element---
                        Element wordDefinitionElement = (Element) wordDefinitionElements
                                .item(j);

                        // ---get all the child nodes
                        // under the
                        // <WordDefinition> element---
                        NodeList textNodes = ((Node) wordDefinitionElement)
                                .getChildNodes();

                        strDefinition += ((Node) textNodes
                                .item(0))
                                .getNodeValue()
                                + ". \n";
                    }//end of for loop j
                }//end of if
            }//end of for loop i
        } catch (IOException e1) {
            Log.d("NetworkingActivity", e1.getLocalizedMessage());
        }
        // ---return the definitions of the word---
        return strDefinition;
    }//end of WordDefinition function

    private class AccessWebServiceTask extends
            AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls)  {
            //this block is for xmlParser
            String str="";
            try {
                str=xmlParser(urls[0]);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return str;
            //this line is for DOM parser
            //return WordDefinition(urls[0]);
        }

        protected void onPostExecute(String result) {
            TextView tv = (TextView) findViewById(R.id.textView2);
            tv.setText(result);
        }
    }//end of AccessWebServiceTask class

}