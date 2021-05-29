/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.io.IOException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

/**
 *
 * @author Carnival stores
 */
public class LinkValidation {

    public static boolean validatesingleURL(String link){// given link as a string
        boolean valid;
        try {
            Jsoup.connect(link).get(); // try connecting to link
            valid = true;
        } catch (HttpStatusException | IllegalArgumentException ex) {  // when returns 404 page not found
            valid = false;
        }catch(UnsupportedMimeTypeException e){
            valid = true;
        }
        catch (IOException ex) { // when timeout to connect to server not found
            valid = false;
        }
        return valid; // return wether connection was successfull
    }
}