/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.io.IOException;
import static java.lang.Integer.max;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Carnival stores
 */
public class SubLinksValidation extends LinkValidation {

    static int valid = 0, invalid = 0; // number of valid and invalid links
    static int maxThreads = 0; // max number of threads active
    private static String text; // text to be appended to output

    // arraylist to keep track of already checked urls
    @SuppressWarnings("FieldMayBeFinal")
    private static ArrayList<String> visited = new ArrayList<>();

    public static void validateURL(Element link, int currentdepth) throws IOException, InterruptedException {
        if (validatesingleURL(link.attr("abs:href"))) { // check f this url is valid
            text = "Valid link:" + link.attr("abs:href") + "\nText: " + link.text() + "\n\n";
            update(text, true); // append url and text to output
            if (currentdepth == ThreadCreation.getMaxdepth()) { // if we reached required depth return
                return;
            }

            Document doc = Jsoup.connect(link.attr("abs:href")).get();
            Elements elements = doc.select("a[href]"); // get all links on page

            for (Element element : elements) { // for each link
                String x = element.attr("abs:href"); // get link as string
                if (visited.contains(x)) { // if already checked continue
                    continue;
                }
                visited.add(x); // if not checked add to checked and enqueue
                ThreadCreation.enqueue(element, currentdepth + 1);
            }
        } else { // if link is invalid just add to output
            text = "invalid link:" + link.attr("abs:href") + "\nText: " + link.text() + "\n\n";
            update(text, false);
        }

    }

    // overloaded method becaus in the very first link we will not
    // have an element object so we will need to get elements from a link as a string
    public static void validateURL(String link, int currentdepth) throws IOException, InterruptedException {
        if (LinkValidation.validatesingleURL(link)) {
            text = "Valid link:" + link + "\n\n";
            update(text, true);
            try {
                Document doc = Jsoup.connect(link).get();
                Elements elements = doc.select("a[href]");
                if (currentdepth == ThreadCreation.getMaxdepth()) {
                    return;
                }
                for (Element element : elements) {
                    String x = element.attr("abs:href");
                    if (visited.contains(x)) {
                        continue;
                    }
                    visited.add(x);
                    ThreadCreation.enqueue(element, currentdepth + 1);
                }
            } catch (UnsupportedMimeTypeException e) {
            }

        } else {
            text = "invalid link:" + link + "\n\n";
            update(text, false);
        }

    }

    // synchronized method to append text to frame and increment no of links
    // and also update the no of threads
    synchronized private static void update(String s, boolean valid) {
        ThreadCreation.getFrame().append(s);
        if (valid) {
            SubLinksValidation.valid++;
        } else {
            invalid++;
        }

        int threads = Thread.activeCount();
        maxThreads = max(threads, maxThreads);
        ThreadCreation.getFrame().update(SubLinksValidation.valid, invalid, threads, maxThreads);
    }

}
