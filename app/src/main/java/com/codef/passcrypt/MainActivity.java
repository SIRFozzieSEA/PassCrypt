package com.codef.passcrypt;


import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import java.lang.reflect.Field;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    // https://guides.codepath.com/android/using-the-recyclerview
    // https://developer.android.com/training/basics/network-ops/xml#java

    ArrayList<PassCryptEntry> passCryptEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadResourceMap();

        RecyclerView rvPassCrypt = (RecyclerView) findViewById(R.id.rvPassCrypt);

        try {
            passCryptEntries = LoadText(R.raw.site_passwords_secure);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PassCryptAdapter adapter = new PassCryptAdapter(this, loadResourceMap(), passCryptEntries);
        rvPassCrypt.setAdapter(adapter);
        rvPassCrypt.setLayoutManager(new LinearLayoutManager(this));

    }

    public HashMap<String, Integer> loadResourceMap() {
        HashMap<String, Integer> resourceMap = new HashMap<>();
        Resources resources = getResources();
        String packageName = getPackageName();
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int resId = resources.getIdentifier(field.getName(), "raw", packageName);
                String resourceName = resources.getResourceEntryName(resId);
                resourceMap.put(resourceName, resId);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
        return resourceMap;
    }

    public ArrayList<PassCryptEntry> LoadText(int resourceId) throws ParserConfigurationException, IOException, SAXException {

        ArrayList<PassCryptEntry> returnPassCryptEntries = new ArrayList<>();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(this.getResources().openRawResource(resourceId));
        Element root = doc.getDocumentElement();

        NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) child;
                String nodeName = element.getNodeName();
                NodeList testList = element.getElementsByTagName("URL");
                if (testList.getLength() > 0) {
                    String url = element.getElementsByTagName("URL").item(0).getTextContent();
                    returnPassCryptEntries.add(new PassCryptEntry(nodeName, url));
                }
            }
        }

        return returnPassCryptEntries;
    }


}