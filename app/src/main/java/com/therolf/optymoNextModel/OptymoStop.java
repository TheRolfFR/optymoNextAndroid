package com.therolf.optymoNextModel;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;

@SuppressWarnings({"unused", "WeakerAccess"})
public class OptymoStop implements Comparable<OptymoStop> {
    private String slug;
    private String name;
    private OptymoLine[] lines;

    @SuppressWarnings("WeakerAccess")
    public OptymoStop(String slug, String name) {
        this.slug = slug;
        this.name = name;
        this.lines = new OptymoLine[0];
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public OptymoLine[] getLines() {
        return lines;
    }

    public void addLineToStop(OptymoLine line) {
        OptymoLine[] newTable = new OptymoLine[lines.length+1];
        System.arraycopy(lines, 0, newTable, 0, lines.length);
        newTable[lines.length] = line;

        lines = newTable;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int compareTo(OptymoStop o) {
        return slug.compareTo(o.slug);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OptymoStop) {
            OptymoStop other = (OptymoStop) obj;
            return other.getSlug().equals(this.getSlug()) && this.getName().equals(other.getName());
        }
        return super.equals(obj);
    }

    public OptymoDirection[] getAvailableDirections() {
        return getAvailableDirections(0);
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    public OptymoDirection[] getAvailableDirections(int lineFilter) {
        OptymoDirection[] result = new OptymoDirection[0];

        org.jsoup.nodes.Document doc;
        Elements errorTitle, directions, lines;
        doc = null;
        try {
            doc = Jsoup.connect("https://siv.optymo.fr/passage.php?ar=" + this.getSlug() + "&type=1").get();
        } catch (IOException ignored) {}

        if(doc != null) {
            errorTitle = doc.getElementsByTag("h3");
            if(errorTitle.size() == 0) {

                lines = doc.getElementsByClass("f1");
                directions = doc.getElementsByClass("f2");

                HashMap<String, OptymoDirection> resultMap = new HashMap<>();

                for(int directionIndex = 0; directionIndex < directions.size(); directionIndex++) {
                    if((lineFilter == 0 || lineFilter == Integer.parseInt(lines.get(directionIndex).text())) && !resultMap.containsKey(directions.get(directionIndex).text()) && !directions.get(directionIndex).text().startsWith("Dépôt")) {
                        resultMap.put(
                                directions.get(directionIndex).text(),
                                new OptymoDirection(
                                        Integer.parseInt(lines.get(directionIndex).text()),
                                        directions.get(directionIndex).text(),
                                        new String(this.name),
                                        new String(this.slug)
                                )
                        );
                    }
                }

                result = resultMap.values().toArray(new OptymoDirection[0]);
            } else {
                System.err.println("stop not found");
            }
        } else {
            System.err.println("cannot access page");
        }

        return result;
    }

    public OptymoNextTime[] getNextTimes() throws IOException {
        return OptymoNetwork.getNextTimes(this.slug, 0);
    }

    public OptymoNextTime[] getNextTimes(int lineFilter) throws IOException {
        return OptymoNetwork.getNextTimes(this.slug, lineFilter);
    }

    public static String nameToSlug(String stopName) {
        return Normalizer.normalize(stopName, Normalizer.Form.NFD).replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }
}
