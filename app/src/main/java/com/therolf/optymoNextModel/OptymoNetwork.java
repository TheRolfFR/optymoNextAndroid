package com.therolf.optymoNextModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@SuppressWarnings({"unused"})
public class OptymoNetwork {
    private HashMap<String, OptymoLine> lines;
    private HashMap<String, OptymoStop> stops;

    private ProgressListener networkGenerationListener = null;

    public void setNetworkGenerationListener(ProgressListener networkGenerationListener) {
        this.networkGenerationListener = networkGenerationListener;
    }

    private boolean isGenerated = false;
    private String resultJson = "";

    public boolean isGenerated() {
        return isGenerated;
    }

    public String getResultJson() {
        return resultJson;
    }

    // https://www.optymo.fr/wp-admin/admin-ajax.php?action=getLine&param=2 trace des lignes
    // https://www.optymo.fr/wp-admin/admin-ajax.php?action=getItrBus&src=itrsub/get_markers_urb.php c est pour suivre les bus
    // https://app.mecatran.com/utw/ws/gtfs/stops/belfort?includePatterns=false&apiKey=2c643c5655034f467a070f5f7028613c2a3c6c71&includeStops=true // les stops (XML)
    // https://siv.optymo.fr/passage.php?ar=technhom1utbm&type=1 (offre passage)

    public OptymoNetwork() {
        lines = new HashMap<>();
        stops = new HashMap<>();
    }

    public void begin(String stopsJson, InputStream linesXml) {
        begin(stopsJson, linesXml, false);
    }
    public void begin(String stopsJson, InputStream linesXml, boolean forceXml) {
        boolean result = decodeJSON(stopsJson);
        if(forceXml || !result) {
            generateFromXML(linesXml);
        }
    }

    /**
     * decodes the network from json
     * @param stopsJson relative path to the json
     * @return whever it worked or not
     */
    private boolean decodeJSON(String stopsJson) {
        System.out.println("decode json");
        boolean result = false;

        try {

            JSONObject jsonDecoded = new JSONObject(stopsJson);
            JSONArray array = jsonDecoded.getJSONArray("stops");

            for (int i = 0; i < array.length(); i++) {
                if(networkGenerationListener != null) {
                    networkGenerationListener.OnProgressUpdate(i, array.length(), "stop");
                }

                JSONObject stopObject = array.getJSONObject(i);
                Iterator<String> itr = stopObject.keys();

                while(itr.hasNext()) {
                    JSONObject stop = stopObject.getJSONObject(itr.next());

                    addOptymoStop(stop.getString("name"), false);
                }
            }

            JSONArray linesArray = jsonDecoded.getJSONArray("lines");
            JSONObject lineObject;
            OptymoLine createdLine;
            JSONArray stopsOfLine;
            String stopSlug;
            OptymoStop foundStop;

            for(int i = 0; i < linesArray.length(); ++i) {
                if(networkGenerationListener != null) {
                    networkGenerationListener.OnProgressUpdate(i, linesArray.length(), "line");
                }
                lineObject = linesArray.getJSONObject(i);
                createdLine = getLine("" + lineObject.getInt("number"), lineObject.getString("name"));
                stopsOfLine = lineObject.getJSONArray("stops");
                //noinspection UnusedAssignment
                foundStop = null;

                for(int a = 0; a < stopsOfLine.length(); a++) {
                    stopSlug = stopsOfLine.getString(a);
                    foundStop = getStopBySlug(stopSlug);

                    if(foundStop != null && createdLine != null) {
                        createdLine.addStopToLine(foundStop);
                    }
                }
            }

            result = true;
        } catch(JSONException e) {
            e.printStackTrace();
        }

        if(networkGenerationListener != null) {
            networkGenerationListener.OnGenerationEnd(result);
        }

        isGenerated = result;
        return result;
    }

    /**
     * JSON generation in an attriibute from XML
     * @param linesXml raw XML document in an InputStream
     */
    private void generateFromXML(InputStream linesXml) {
        System.out.println("decode xml");
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(linesXml);

            NodeList names = document.getElementsByTagName("name");
            Elements lines, directions;
            String cleanedName, name;

            //noinspection RedundantCast
            JSONStringer stringer = (JSONStringer) new JSONStringer()
                    .object()
                    .key("stops")
                        .array();

            System.err.println(names.getLength());

            for(int i = 0; i < names.getLength(); i++) {
                name = names.item(i).getTextContent();
                cleanedName = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^A-Za-z0-9]", "").toLowerCase();

                org.jsoup.nodes.Document doc = null;
                try {
                    doc = Jsoup.connect("https://siv.optymo.fr/passage.php?ar=" + cleanedName + "&type=1").get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(doc != null && doc.getElementsByTag("h3").size() == 0) {
                    if(networkGenerationListener != null) {
                        networkGenerationListener.OnProgressUpdate(i, names.getLength(), "stop");
                    }
                    stringer
                            .object()
                            .key(cleanedName)
                            .object()
                            .key("name").value(name)
                            .key("directions").array();

                    lines = doc.getElementsByClass("f1");
                    directions = doc.getElementsByClass("f2");

                    for(int a  = 0; a < lines.size(); ++a) {
                        stringer.value(lines.get(a).text() + " - " + directions.get(a).text());
                    }

                    stringer.endArray()
                            .endObject()
                            .endObject();
                    addOptymoStop(name);
                }
            }

            stringer.endArray();
            stringer.key("lines").array();

            OptymoLine[] myLines = getLines();
            OptymoStop[] lineStops;

            for(OptymoLine line : myLines) {
                stringer.object()
                        .key("number").value(line.getNumber())
                        .key("name").value(line.getName())
                        .key("stops").array();

                lineStops = line.getStops();

                for(OptymoStop stop : lineStops) {
                    stringer.value(stop.getSlug());
                }

                stringer.endArray().endObject();
            }

            isGenerated = true;
            resultJson  = stringer.endArray().endObject().toString();
        } catch (ParserConfigurationException | IOException | SAXException | JSONException e) {
            e.printStackTrace();
        }

        if(networkGenerationListener != null) {
            networkGenerationListener.OnGenerationEnd(!resultJson.equals(""));
        }
    }

    private void addOptymoStop(String stopName) {
        this.addOptymoStop(stopName, false);
    }

    /**
     * Method that adds the stops to the stops list and the given line
     * @param stopName the name of the stops
     */
    @SuppressWarnings("SameParameterValue")
    private void addOptymoStop(String stopName, boolean fromJSON) {
        String cleanedName = Normalizer.normalize(stopName, Normalizer.Form.NFD).replaceAll("[^A-Za-z0-9]", "").toLowerCase();

        org.jsoup.nodes.Document doc = null;
        try {
            doc = Jsoup.connect("https://siv.optymo.fr/passage.php?ar=" + cleanedName + "&type=1").get(); // get the page
        } catch (IOException ignored) {}
        if(!fromJSON && doc != null && doc.getElementsByTag("h3").size() == 0) { // if not error page
            OptymoStop newStop = new OptymoStop(cleanedName, stopName); // create a new stop
            stops.put(cleanedName, newStop); // add the stop to the global list

            // add the new lines to the global list
            Elements lines = doc.getElementsByClass("f1"), directions = doc.getElementsByClass("f2");
            for(int i = 0; i < lines.size(); ++i) {
                OptymoLine l = getLine(lines.get(i).text(), directions.get(i).text());
                if(l != null) {
                    l.addStopToLine(newStop); // add the stop to the line
                }
            }
        }
    }

    /**
     * Add or get a line given its lin number and direction
     * @param lineNumber the number of the line
     * @param lineDirection the direction of the line
     * @return the given line
     */
    private OptymoLine getLine(String lineNumber, String lineDirection) {
        String key =  lineNumber + lineDirection; // create the key

        if(lines.containsKey(key)) { // if the line already exists
            return lines.get(key); // get the line
        } else { // else
            if(!lineDirection.equals("Dépôt")) {
                OptymoLine newLine = new OptymoLine(Integer.parseInt(lineNumber), lineDirection); // create it
                lines.put(key, newLine); // and add it to the global list

                return newLine; // then return the added line
            }
        }

        return null;
    }

    /**
     * Returns all the stops of the network ordered by slug
     * @return the ordered array of stops
     */
    public OptymoStop[] getStops() {
        ArrayList<OptymoStop> list = new ArrayList<>(this.stops.values());
        Collections.sort(list);
        return list.toArray(new OptymoStop[0]);
    }


    /**
     * Returns all the lines of the network ordered by number and name
     * @return the ordered array of lines
     */
    public OptymoLine[] getLines() {
        ArrayList<OptymoLine> list = new ArrayList<>(this.lines.values());
        Collections.sort(list);
        return list.toArray(new OptymoLine[0]);
    }

    /**
     * Method returning a stop for a given slug
     * @param slug the given slug of the stop
     * @return a stop or null
     */
    public OptymoStop getStopBySlug(String slug) {
        OptymoStop result = null, tmp;
        int i = 0;
        Object[] keys = stops.keySet().toArray();

        while (result == null && i < keys.length) {
            //noinspection SuspiciousMethodCalls
            tmp = stops.get(keys[i]);
            if (tmp != null && tmp.getSlug().equals(slug)) {
                result = tmp;
            }

            ++i;
        }

        return result;
    }

    public interface ProgressListener {
        void OnProgressUpdate(int current, int total, String message);
        void OnGenerationEnd(boolean returnValue);
    }
}
