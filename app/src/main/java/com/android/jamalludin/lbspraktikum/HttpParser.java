package com.android.jamalludin.lbspraktikum;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.Node;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by L on 5/6/16.
 */
public class HttpParser {
    public Document dapatkanDocument(LatLng mulai,LatLng selesai) throws IOException{
        String url = "http://maps.googleapis.com/maps/api/directions/xml?origin="+mulai.latitude+","+mulai.longitude+"&destination="+selesai.latitude+","+selesai.longitude+"&sensor=false&units=metric&mode=driving";
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection)obj.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        BufferedReader buf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuffer hasil = new StringBuffer();
        while ((line = buf.readLine())!= null){
            hasil.append(line);
        }
        try{
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(hasil.toString()));
            Document document = db.parse(is);
            return document;
        }catch (Exception e){
        e.printStackTrace();
        }
        return  null;
    }
    public String getDuration(Document doc){
        NodeList n1,n2;
        String hasil;
        n1 = doc.getElementsByTagName("duration");
        org.w3c.dom.Node node = n1.item(n1.getLength()-1);
        n2 = node.getChildNodes();
        hasil = n2.item(3).getTextContent();

        return hasil;
    }
    private int getNodeIndex(NodeList nodeList,String name){
        for(int i=0;i<nodeList.getLength();i++){
            if(nodeList.item(i).getNodeName().equals(name))
                return i;
        }
        return -1;
    }
    public ArrayList<LatLng> getDirection(Document doc) {
        NodeList nl1, nl2, nl3;
        ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();
        nl1 = doc.getElementsByTagName("step");
        if (nl1.getLength() > 0) {
            for (int i = 0; i < nl1.getLength(); i++) {
                org.w3c.dom.Node node1 = nl1.item(i);
                nl2 = node1.getChildNodes();
                org.w3c.dom.Node locationNode = nl2.item(getNodeIndex(nl2, "start_location"));
                nl3 = locationNode.getChildNodes();
                org.w3c.dom.Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
                double lat = Double.parseDouble(latNode.getTextContent());
                org.w3c.dom.Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                double lng = Double.parseDouble(lngNode.getTextContent());
                listGeopoints.add(new LatLng(lat, lng));
                locationNode = nl2.item(getNodeIndex(nl2,"polyline"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3,"points"));
                ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
                for (int j = 0; j < arr.size(); j++) {
                    listGeopoints.add(new
                            LatLng(arr.get(j).latitude, arr.get(j).longitude));
                }
                locationNode = nl2.item(getNodeIndex(nl2,"end_location"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3, "lat"));
                lat = Double.parseDouble(latNode.getTextContent());
                lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                lng = Double.parseDouble(lngNode.getTextContent());
                listGeopoints.add(new LatLng(lat, lng));
            }
        }

        return  listGeopoints;
    }
    public ArrayList<LatLng> decodePoly(String encoded){
        ArrayList<LatLng> poly = new ArrayList<LatLng>(); int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len){
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5;
            }while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat; shift = 0; result = 0;
            do {
                b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }
    public String dataKampus(String alamat){
        String result = null;
        try {
            URL obj = new URL(alamat);
            HttpURLConnection connection = (HttpURLConnection)obj.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            BufferedReader buf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuffer hasil = new StringBuffer();
            while ((line = buf.readLine())!= null){
                hasil.append(line);
            }
            return hasil.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }
    public JSONObject getjsonObject(){
        try {
            JSONObject object = new JSONObject(dataKampus("http://192.168.100.10/coba/json/lokasiWebservice.php"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  null;
    }
}
