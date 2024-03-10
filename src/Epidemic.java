// Epidemic.java
/* Program that will eventually develop into an epidemic simulator
 * authors Douglas W. Jones Robert S. Shipley
 * version Feb. 15, 2021
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.LinkedList;
import java.lang.Math;
import java.lang.NumberFormatException;
import java.util.Random;

/** Error reporting framework
 *  All error messages go to System.err (aka stderr, the standard error stream).
 *  Currently, this only supports fatal error reporting.
 *  Later it would be nice to have a way to report non-fatal errors.
 */
class Errors {

    /** Report a fatal error
     *  @param msg -- error message to be output
     *  This never returns, the program terminates reporting failure.
     */
    public static void fatal( String msg ) {
        System.err.println( "Epidemic: " + msg );
        System.exit( 1 );  // abnormal termination
    }
}

/** People in the simulated community each have a role
 */
class Role {

    // instance variables
    String name;      // name of this role
    float fraction;   // fraction of the population in this role
    int number;       // number of people in this role
    LinkedList<Place> placesForRoles = new LinkedList<Place>();

    // static variables used for summary of all roles
    static float sum = 0.0F; // sum of all the fractions
    static LinkedList<Role> allRoles = new LinkedList<Role>();


    /** Construct a new role by scanning an input stream
     *  @param in -- the input stream
     *  The stream must contain the role name, and the number or fraction
     *  of the population in that role.
     *  All role specifications end with a semicolon.
     */
    public Role( Scanner in ) {

        // get the name
        if (in.hasNext()) {
            name = in.next();
        } else {
            Errors.fatal( "role with no name" );
        }

        // get the number in this role or the fraction of the population
        if (in.hasNextFloat()) {
            fraction = in.nextFloat();
            sum = sum + fraction;
        } else {
            Errors.fatal( "role " + name + ": not followed by population" );
        }
        // get the places that the role can visit
        while (!in.hasNext(";")) {
            placesForRoles.add(Place.findPlace(in.next()));
        }
        // get the semicolon
        if (!in.hasNext( ";" )) {
            Errors.fatal( "role " + name + " " + fraction + ": missing ;" );
        } else {
            in.next( ";" );
        }

        // complain if the name is not unique
        if (findRole( name ) != null) {
            Errors.fatal(
                    "role " + name + " " + fraction + ": role name reused?"
            );
        }
        // force the fraction or population to be positive
        if (fraction <= 0) {
            Errors.fatal(
                    "role " + name + " " + fraction + ": negative population?"
            );
        }

        allRoles.add( this ); // include this role in the list of all roles
    }

    /** Find a role, by name
     *  @param n -- the name of the role
     *  @return the role with that name, or null if none has been defined
     */
    static Role findRole( String n ) {
        for (Role r: allRoles) {
            if (r.name.equals( n )) return r;
        }
        return null; // role not found
    }

    /** Create the total population, divided up by roles in
     *  @param population -- the total population to be created
     *  The math here divides the population in the ratio of the numbers
     *  given for each role.
     *  It is critical that this not be done until all roles are known.
     */
    static void populateRoles( int population ) {
        if (allRoles.isEmpty()) Errors.fatal( "no roles specified" );
        for (Role r: allRoles) {
            // how many people are in this role
            r.number = Math.round( (r.fraction / r.sum) * population );
            //populate roles with places
            // make that many people
            System.out.println("r.number = " + r.number);
            for (int i = 1; i <= r.number; i++) {
                int counter = 0;
                for (Place p: r.placesForRoles) {
                    Place newPlace = p.populateRoleByPlace(p);
                    r.placesForRoles.set(counter, newPlace);
                    counter++;
                }
                new Person(r, r.placesForRoles);
            }
        }
    }
}

/** People go places within their roles
 */
class Place {

    String name; //name of place
    float median; //median number of people in a place
    float scatter; //scatter or spread of people usually in a place
    int max; //max capacity
    int counter = 0; //how many people there
    //Static variable to keep track of all the places
    static LinkedList<Place> allPlaces = new LinkedList<>();

    /** Creates a new place with the same name, different hash, and possibly
     *  different max capacity
     *  @param p -- Place object that needs to be replaced with another.
     *  @return newPlace -- new Place object with different hash and max capacity
     */
    // BUG: This does not create a new Hash. All objects stay the same hash.
    // BUG: Role's aren't all updated to replacement, probably have to find a different way.
    // (Role objects still reference an old Place object after replacement)
    public static Place newPlaceSameName(Place p) {
        p.findRoom(p.median, p.scatter);
        Place newPlace = new Place(p.name, p.median, p.scatter);
        return newPlace;
    }

    /** Finds the maximum capacity of a place using log-normal distribution
     *  @param m -- median, s -- scatter
     */
    public void findRoom(float m, float s) {
        double sigma = Math.log((m + s)/m);
        Random rand = new Random();
        double logNormal = Math.exp(sigma * rand.nextGaussian()) * median;
        max = (int) Math.floor(logNormal);
    }

    public void populatePlace(Place p) {
        if (p.counter >= p.max) {
            for (Role r: Role.allRoles) {
                int counter = 0;
                for (Place pl: r.placesForRoles) {
                    if (pl.name.equals(p.name)) {
                        r.placesForRoles.set(counter, new Place(p.name, p.median, p.scatter));
                    }
                }
            }
        }
    }

    /** Constructor dedicated to creating a new place with the same name
     *  @param n -- name, m -- median, s -- scatter, num -- max capacity
     */
    public Place(String n, float m, float s) {
        name = n;
        counter = 0;
        median = m;
        scatter = s;
        findRoom(m, s);
    }

    public Place(Scanner in) {
        //Take the scanner and insert the information into the linked list
        if (in.hasNext()) {
            name = in.next();
        } else {
            Errors.fatal("place with no name");
        }
        //Get the median and scatter for this population in this place
        if (in.hasNextFloat()) {
            median = in.nextFloat();
        } else {
            Errors.fatal("place " + name + ": has no median");
        }
        if (in.hasNextFloat()) {
            scatter = in.nextFloat();
        } else {
            Errors.fatal("place " + name + " " + median + ": has no scatter");
        }
        //Get the semicolon
        if (in.hasNext(";")) {
            in.next(";");
        } else {
            Errors.fatal("place " + name + " " + median + " " + scatter + ": has no ;");
        }
        //Force the median to be positive
        if (median <= 0) {
            Errors.fatal("place " + name + " " + median + " " + scatter + ": median is negative");
        }
        //Complain if name is not unique
        if (findPlace(name) != null) {
            Errors.fatal("place " + name + " " + median + " " + scatter + ": name is reused?");
        }
        //Force scatter to be positive
        if (scatter < 0) {
            Errors.fatal("place " + name + " " + median + " " + scatter + ": scatter is negative");
        }
        this.findRoom(median, scatter);
        allPlaces.add(this);
    }

    /** Find a place, by name
     *  @param n -- the name of the place
     *  @return the place with that name, or null if none has been defined
     */
    static Place findPlace(String n) {
        for (Place p: allPlaces) {
            if (p.name.equals(n)) {
                return p;
            }
        }
        return null;
    }

    /** Populate roles, by places, and change the place if max capacity
     *  @param p -- object Place
     *  @return the place, or a new place with the same name
     */
    static Place populateRoleByPlace(Place p) {
        if (p.counter < p.max) {
            p.counter++;
            System.out.println("Place " + p.name + " now has " + p.counter + " people in it");
            System.out.println("Place " + p.name + " has a max capacity of " + p.max);
            return p;
        } else {
            Place newPlace = new Place(p.name, p.median, p.scatter);
            newPlace.findRoom(newPlace.median, newPlace.scatter);
            return newPlace;
        }
    }
}

/** People are the central actors in the simulation
 */
class Person {
    // instance variables
    Role role;      // role of this person
    LinkedList<Place> places = new LinkedList<Place>();

    // static variables used for all people
    static LinkedList<Person> allPeople = new LinkedList<Person>();

    /** Construct a new person to perform some role
     *  @param r -- the role, p -- linked list of places for roles
     */
    public Person( Role r, LinkedList<Place> p ) {
        role = r;
        places = p;

        allPeople.add( this ); // include this person in the list of all
    };

    /** Print out the entire population
     *  This is needed only in the early stages of debugging
     *  and obviously useless for large populations
     */
    public static void printAll() {
        for (Person p: allPeople) {
            String placeString = ""; //Added for loop to combine places for each role.
            for (Place pl: p.places) {
                placeString = placeString + pl.name + " " + pl.toString() + " ";
            }
            System.out.println( p.toString() + " " + p.role.name + " " + placeString);
        }
    }
}

/** The main class
 *  This class should never be instantiated.
 *  All methods here are static and all but the main method are private.
 */
public class Epidemic {

    /** Read the details of the model from an input stream
     *  @param in -- the stream
     *  Identifies the keywords population, role, etc and farms out the
     *  work for most of these to the classes that construct model parts.
     *  The exception (for now) is the total population.
     */
    private static void buildModel( Scanner in ) {
        int pop = 0; // the population of the model, 0 = uninitialized

        while ( in.hasNext() ) { // scan the input file

            // each item begins with a keyword
            String keyword = in.next();
            if ("population".equals( keyword )) {
                if (!in.hasNextInt()) {
                    Errors.fatal( "population not followed by integer" );
                } else {
                    if (pop != 0) {
                        Errors.fatal( "population specified more than once" );
                    }
                    pop = in.nextInt();
                    if (pop <= 0) {
                        Errors.fatal( "population " + pop + ": not positive" );
                    }
                    if (!in.hasNext( ";" )) {
                        Errors.fatal( "population " + pop + ": missing ;" );
                    } else {
                        in.next( ";" );
                    }
                }
            } else if ("role".equals( keyword )) {
                System.out.println("role here");
                new Role( in );
            } else if ("place".equals( keyword ) ) {
                new Place( in );
                System.out.println("here");
            } else { // none of the above
                Errors.fatal( "not a keyword: " + keyword );
            }
        }

        if (pop == 0) Errors.fatal( "population not specified" );
        Role.populateRoles( pop );
    }

    /** The main method
     *  @param args -- the command line arguments
     *  Most of this code is entirely about command line argument processing.
     *  It calls buildModel and will eventuall also start the simulation.
     */
    public static void main( String[] args ) {

        //if (args.length < 1) Errors.fatal( "missing file name" );
        //if (args.length > 1) Errors.fatal( "too many arguments: " + args[1] );
        try {
            buildModel( new Scanner( new File( "testfile.txt" ) ) );
            // BUG:  Simulate based on model just built?
            Person.printAll(); // BUG:  In the long run, this is just for debug
        } catch ( FileNotFoundException e ) {
            Errors.fatal( "could not open file: " + args[0] );
        }
    }
}
