/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.io.IOException;
import static java.lang.Integer.max;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Element;

public class ThreadCreation extends Thread {

    private Element link; // to store element ater dequeing
    private int id; // id of current thread
    private int depth; // depth of element after dequeing
    private static int maxdepth; // depth entered by user
    private static ViewResults frame; // output frame
    private static long startTime; // start time for program
    private static long endTime; // execution time for program
    @SuppressWarnings("FieldMayBeFinal")
    private static boolean[] allDone = new boolean[10]; // boolean array to check if all threads are done
    @SuppressWarnings("FieldMayBeFinal")
    private static Queue<Pair> urls = new LinkedList<>(); // queue to keep urls to check
    private long startThread; // start time for thread
    private long endThread; // execution time for thread


    public static int getMaxdepth() { // returns depth entered by user
        return maxdepth;
    }

    @Override
    public void run() {
        startThread = System.nanoTime(); // start thread time
        while (!syncedEmpty()) { // while queue is not empty
            Pair d = dequeue(); // dequeue pair
            link = (Element)d.getKey(); // link element gets stored in element
            depth = (int) d.getValue(); // depth gets stored in int
            try {
                SubLinksValidation.validateURL(link, depth); // run checker
            } catch (IOException | InterruptedException ex) { // exception handling
                Logger.getLogger(ThreadCreation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        endThread = System.nanoTime() - startThread; // end thread time
        frame.setTime(Thread.currentThread().getName(), endThread); // set thread time on frame
        check(this.id); // check if other threads are done
        
    }
    
    // method to check if all threads are done
    synchronized private static void check(int id){ 
        allDone[id] = true; // set given thread as done
        for(boolean done: allDone){ // check if all other threads are done
            if(!done)
                return;
        }
        end(); // if all are done end program
    }

    // synchronized method to enqueue
    public static synchronized void enqueue(Element link, int depth) {
        urls.add(new Pair<>(link, depth));
    }

    // synchronized method to dequeue
    public static synchronized Pair dequeue() {
        return urls.poll();
    }

    // init method to start the program
    public static void init(String link, int depth) throws IOException { // given url to check and depth required
        try {
            maxdepth = depth; // set max depth
            startTime = System.nanoTime(); // start program timer
            frame = new ViewResults(); // create output frame
            Jsoup.connect(link); // check if any exceptions given at first link
            getFrame().setVisible(true); // set visible
            SubLinksValidation.validateURL(link, 0); // validate first link and get links inside
            for (int i = 0; i < 10; i++) { // create 10 threads
                ThreadCreation t = new ThreadCreation();
                t.setName("Thread "+i); // set thread name
                t.id = i; // set object id
                t.start();
            }
        } catch (IOException | InterruptedException | IllegalArgumentException ex) {
            throw new IOException(); // throwing new exception if url entered isnt valid
        }
    }
    
    synchronized private static void end(){
        endTime = System.nanoTime() - startTime; // set execution time
        int threads = Thread.activeCount(); // get no of threads
        int max = max(threads, SubLinksValidation.maxThreads); // get max no of threads
        
        // update frame
        frame.update(SubLinksValidation.valid, SubLinksValidation.invalid, threads, max, endTime);
    }
    
    // synchronized method to check if queue is empty
    synchronized public static boolean syncedEmpty(){
        return urls.isEmpty();
    }

    /**
     * @return the frame
     */
    public static ViewResults getFrame() {
        return frame;
    }

}
