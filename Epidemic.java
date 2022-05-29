// Epidemic.java
/* Program that will eventually develop into an epidemic simulator
 * up to MP2: author Douglas W. Jones
 * version Feb. 15, 2021
 */
/* author Robert S. Shipley
 * version Mar. 14, 2021
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.LinkedList;
import java.lang.Math;
import java.lang.NumberFormatException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Epidemic {
    /** Read the details of the model from an input stream
     *  @param in -- the stream
     *  Identifies the keywords population, role, etc and farms out the
     *  work for most of these to the classes that construct model parts.
     *  The exception (for now) is the total population.
     */
    //Needs update for infected stuff
    private static void buildModel(MyScanner in ) {
        int pop = 0; // the population of the model, 0 = uninitialized
        while ( in .hasNext()) { // scan the input file
            // each item begins with a keyword
            String keyword = in .next();
            Pattern getWord = Pattern.compile("[a-zA-Z]*");
            Matcher matchLetters = getWord.matcher(keyword);
            String keywordGetter = "";
            // -- may have to incorporate some way of keeping track of
            // what places get what times (probably hash)
            while (matchLetters.find()) {
                keywordGetter += matchLetters.group();
            }
            keyword = keywordGetter.toLowerCase();
            if ("population".equals(keyword)) {
                String popString = in.next();
                Pattern popPat = Pattern.compile("[0-9]+");
                Matcher popMat = popPat.matcher(popString);
                String temp = "";
                while (popMat.find()) {
                    temp = popMat.group();
                }
                if (temp.equals("")) {
                    Errors.fatal("population not followed by integer");
                } else {
                    if (pop != 0) {
                        Errors.fatal("population specified more than once");
                    }
                    pop = Integer.parseInt(temp);
                    if (pop <= 0) {
                        Errors.fatal("population " + pop + ": not positive");
                    }
                }
                    Pattern semi = Pattern.compile("[\\;]");
                    Matcher mSemi = semi.matcher(popString);
                    String hold = "";
                    while (mSemi.find()) {
                        if (mSemi.group().equals(";")) {
                            hold += mSemi.group();
                        }
                    }
                    if (!hold.equals(";")) {
                       if (!in.hasNext(";")) {
                           Errors.fatal("population " + pop + ": missing ;");
                       } else {
                           in.next();
                       }
                    }
            } else if ("role".equals(keyword)) {
                new Role( in );
            } else if ("place".equals(keyword)) {
                new Place( in );
            } else { // none of the above
                Errors.fatal("not a keyword: " + keyword);
            }
        }
        if (pop == 0) Errors.fatal("population not specified");
        Role.populateRoles(pop);
        Person.shuffle();
    }
    /** The main method
     *  @param args -- the command line arguments
     *  Most of this code is entirely about command line argument processing.
     *  It calls buildModel and will eventuall also start the simulation.
     */
    public static void main(String[] args) throws FileNotFoundException {

//        if (args.length < 1) Errors.fatal( "missing file name" );
//        if (args.length > 1) Errors.fatal( "too many arguments: "
//         + args[1] );
        try {
            buildModel(new MyScanner(new File("testfile2.txt")));
            // BUG:  Simulate based on model just built?
            Person.printAll(); // BUG:  In the long run, this is just for debug
        } catch (FileNotFoundException e) {
            Errors.fatal("could not open file: " + args[0]);
        }
    }
}
// class for catching errors
class Errors {
    /** Report a fatal error
     *  @param msg -- error message to be output
     *  This never returns, the program terminates reporting failure.
     */
    public static void fatal(String msg) {
        System.err.println("Epidemic: " + msg);
        System.exit(1); // abnormal termination
    }
}
/**  Random wrapper class MyRandom
 *   Creates a static random seed for the entire project
 */
class MyRandom extends Random {
    //Static random seed for entire project
    public static final MyRandom stream = new MyRandom();
    //Creates a random from super class Random
    private MyRandom() {
        super();
    }
    //returns the static random seed for use
    public static MyRandom stream() {
        return stream;
    }
    //add distributions that weren't built in
    //not used but could be useful in future
    public double nextExponential(double mean) {
        return mean * -Math.log(this.nextDouble());
    }
}
//MyScanner Class to condense things being passed into Role, Place, etc.
class MyScanner {
    Scanner sc; //wrapper scanner
    public MyScanner(File f) throws FileNotFoundException {
        sc = new Scanner(f);
    }
    //Methods to inherit from Scanner
    public boolean hasNext() {
        return sc.hasNext();
    }
    public boolean hasNextFloat() {
        return sc.hasNextFloat();
    }
    public boolean hasNext(String s) {
        return sc.hasNext(s);
    }
    public String next() {
        return sc.next();
    }
    public String nextLine() {
        return sc.nextLine();
    }
    public String next(String lookup) {
        return sc.next(lookup);
    }
    public boolean hasNextInt() {
        return sc.hasNextInt();
    }
    public int nextInt() {
        return sc.nextInt();
    }
    //new methods to Scanner
    //uses Scanner.next if it has a next or returns error message
    public String getNext(String errorMessage) {
        if (sc.hasNext()) {
            return sc.next();
        } else {
            Errors.fatal(errorMessage);
        }
        return null;
    }
    //uses Scanner.nextFloat if it has a float next
    // or returns error message
    public float getNextFloat(String errorMessage) {
        if (sc.hasNextFloat()) {
            return sc.nextFloat();
        } else {
            Errors.fatal(errorMessage);
        }
        return 0;
    }
    public int getNextInt(String errorMessage) {
        if (sc.hasNextInt()) {
            return sc.nextInt();
        } else {
            Errors.fatal(errorMessage);
        }
        return 0;
    }
    //uses Scanner.next(String) for next matching String or
    // returns error message
    public void getNextLiteral(String literal, String errorMessage) {
        if (sc.hasNext(literal)) {
            sc.next(literal);
        } else {
            Errors.fatal(errorMessage);
        }
    }
    /** uses a Matcher to find the string that matches the output
     *  if not found (should be if it is being used) then returns blank
     *  returns the String that matches the pattern
     */
    public String getNextPattern(Pattern p, String check, String eMessage) {
        Matcher m = p.matcher(check);
        String output = "";
        while (m.find()) {
            if (!m.group().equals("")) {
                output = m.group();
            }
        }
        if (!output.equals("")) {
            return output;
        } else {
            Errors.fatal(eMessage);
        }
        return output;
    }
}

/** Health class -
 *  Keeps track of what people are uninfected, latent, symptomatic,
 *  asymptomatic, bedridden, recovered, and dead. Creates a correlation between
 *  Person and that object's "health".
 *  Note: dead does not need to be taken as a variable, we can get that number
 *  from taking the number that are alive at the end and subtracting that from
 *  the total Person population.
 */
class Health {
    //Boolean variables for whether they meet one of these conditions
    boolean uninfected;
    boolean latent;
    boolean symptomatic;
    boolean asymptomatic;
    boolean bedridden;
    boolean recovered;
    /** Static linked list for keeping track of how many people are in each
     *  state above, as well as how many are dead, i.e. deleted people
     */
    static LinkedList<Integer> healthStats = new LinkedList<Integer>();
}

/** InfectedStats class -
 *  Sets the statistics for each of the variables in Health, and how likely
 *  a person can go from one status to another with the passage of a day.
 */
class InfectedStats {
    //Need variables for medians and scatters for: latency, asymptomatic
    //Need medians, scatters, and probability of recovery for: symtomatic, bedridden
    int initialInfected;
    double medLat;
    double scatLat;
    double medAsymp;
    double scatAsymp;
    double medSymp;
    double scatSymp;
    double recSymp;
    double medBed;
    double scatBed;
    double recBed;

    public InfectedStats(MyScanner in) {
        Pattern p = Pattern.compile("[0-9]*[\\.]*[0-9]+");
        String hold = in.nextLine();
        while (!hold.matches("(.*)end(.*)\\;")) {
            hold += " " + in.next();
        }
        int semiCounter = 0;
        for (char c: hold.toCharArray()) {
            if (c == ';') {
                semiCounter++;
            }
        }
        if (semiCounter != 6) {
            Errors.fatal("Infected Statistics is missing a semicolon");
        }
        String[] holdArray = hold.split(";");



    }

}

//Time class creates objects of time relative to places roles go
class Time {
    float startTime; //First entered time
    float endTime; //Second entered time
    // linked list of all times, could be useful in a lookup
    static LinkedList<Time> allTimes = new LinkedList<Time>();
    /** Time object constructor
     * @param times - String array of the times,
     *              Created by a split between the hyphen
     */
    public Time(String[] times) {
        startTime = Float.parseFloat(times[0]);
        endTime = Float.parseFloat(times[1]);
        allTimes.add(this);
    }
    /** Time object's toString
     *  creates a string up to parameters of expected output
     * @return - expected string output
     */
    public String toString() {
        return "(" + startTime + "-" + endTime + ")";
    }
}
// Role class populates the people with roles
class Role {
    float fraction; //fraction of the population in this role
    String name; //name of the role
    int number; //number of people in this role
    boolean semiCheck = false; //check for semicolon
    String posErrMes = "";
    //keeps track of places role goes (by name)
    LinkedList < Place > allPlacesRoleGoes = new LinkedList < Place > ();
    /** Collection for Time objects they will be places, the first place in
     *  allPlacesRoleGoes is considered a default place, where any unaccounted
     *  time, it is assumed they are there. So, the length of this list
     *  will be 1 less than the length of allPlacesRoleGoes
     */
    LinkedList< Time > timesForPlaces = new LinkedList< Time > ();
    //static variables used for summary of all roles
    static float sum = 0.0F; //sum of all fractions
    static LinkedList < Role > allRoles = new LinkedList < Role > ();

    /** Role constructor for looking at the roles and summarizing data
     * @param in -- Scanner that is looking at the file
     */
    public Role(MyScanner in ) {
        //Start an error string that gets put together with each scan
        posErrMes = "role ";
        //get the name of the role
        String hold = in.getNext("role with no name");
        Pattern numCheck = Pattern.compile("[0-9]*");
        Matcher numMatch = numCheck.matcher(hold);
        String posNum = "";
        while (numMatch.find()) {
            posNum += numMatch.group();
        }
        if (!posNum.isEmpty()) {
            Errors.fatal(posErrMes + " " + hold
                    + ": has a number in it");
        }
        name = hold;
        posErrMes += name;
        //get the fraction of the role
        fraction = in.getNextFloat(posErrMes + " not followed by population");
        sum += fraction;
        posErrMes += " " + fraction;
        //Account for the places that the role can go (at least 1 place needed)
        int counter = 0;
        String place = in.next();
        allPlacesRoleGoes.add(Place.findPlace(place)); //default place
        posErrMes += " " + place;
        counter++;
        // hold for what we are looking at
        String statement = in.next();
        String[] stateArr;
        // if the name of the place is connected to the time
        if (statement.contains("(")) {
            String statePlaceName = "";
            //grab the name for the place
            for (int i = 0; i < statement.lastIndexOf("("); i++) {
                statePlaceName += statement.charAt(i);
            }
            allPlacesRoleGoes.add(Place.findPlace(statePlaceName));
            posErrMes += " " + statePlaceName;
            counter++;
            statement = statement.replace(statePlaceName, "");
            if (!statement.contains(")") && !statement.contains(";")) {
                statement += in.nextLine();
                if (!statement.contains(")")) {
                    Errors.fatal(posErrMes + ": missing end parenthesis");
                }
            } else if (!statement.contains(")")) {
                Errors.fatal(posErrMes + ": missing end parenthesis");
            }
            stateArr = findTime(in, statement);
            timesForPlaces.add(new Time(stateArr));
            //going off assumption that all but default places have time
            //if the place is not connected to the time
        } else if (!statement.contains("(")) {
            allPlacesRoleGoes.add(Place.findPlace(statement));
            posErrMes += " " + statement;
            counter++;
            statement = in.nextLine();
            if (!statement.contains("(") || !statement.contains(")")) {
                Errors.fatal(posErrMes + ": missing parentheses");
            }
            stateArr = findTime(in, statement);
            timesForPlaces.add(new Time(stateArr));
        }
        if (!semiCheck) {
            Errors.fatal(posErrMes + " missing semicolon");
        }
        allRoles.add(this);
    }
    /** findTime -- finds the Times the role will be in a place
     * @param in -- MyScanner object to be passed to semiCheck method
     * @param statement -- The times for the places to be trimmed for use
     * @return -- Array of the time's for that place
     */
    public String[] findTime(MyScanner in, String statement) {
        posErrMes += " " + statement;
        if (!statement.contains("-")) {
            Errors.fatal(posErrMes + ": missing hyphen (-)");
        }
        Pattern p = Pattern.compile("[\\-]*[0-9]*[\\.]*[0-9]*[\\s]*[\\-]" +
                "[\\s]*[\\-]*[0-9]*[\\.]*[0-9]*");
        Matcher m = p.matcher(statement);
        semiCheck(in, statement);
        String hold = "";
        while (m.find()) {
            hold += m.group();
        }
        hold = hold.replaceAll(" ", "");
        // see how many "-" there are: > 1 then error (negative time)
        int counter = 0;
        String temp = hold;
        temp = temp.replaceAll("[0-9]*[\\.]*[0-9*]", "");
        while (!temp.isEmpty()) {
            temp = temp.replaceFirst("-", "");
            counter++;
        }
        if (counter > 1) {
            Errors.fatal(hold + ": has a negative time");
        }
        return hold.split("\\-");
    }
    /** semiCheck -- Checks for a semicolon in the statement
     * @param in -- MyScanner object being used in constructor
     * @param statement -- String that is being checked for a semicolon
     * @change semiCheck -- changes semiCheck to true if semicolon is present
     * @return -- trimmed statement to exclude semicolon
     */
    public void semiCheck(MyScanner in, String statement) {
        if (statement.contains(";")) {
            semiCheck = true;
        } else if (in.hasNext(";")) {
            in.next();
            semiCheck = true;
            posErrMes += " " + ";";
        }
    }
    /** findRole -- finds the role by name
     * @param name -- the name of the role being looked for
     * @return r -- the role object that was looked for,
     *  null -- if no object was found by that name.
     */
    public Role findRole(String name) {
        for (Role r: allRoles) {
            if (name.equals(r.name)) {
                return r;
            }
        }
        return null;
    }
    /** populateRoles -- populates people with roles
     * @param population -- the population number being looked at
     */
    static void populateRoles(int population) {
        if (allRoles.isEmpty()) Errors.fatal("no roles specified");
        for (Role r: allRoles) {
            // how many people are in this role
            r.number = Math.round((r.fraction / r.sum) * population);
            // make that many people
            for (int i = 1; i <= r.number; i++) {
                new Person(r);
            }
        }
    }
}
/** This class populates places with people, changing where the people are
 * with different Place objects. Uses a "rotating" LinkedList with almost
 * identical place objects replacing one's at max capacity.
 */
class Place {
    String name;
    float median;
    float scatter;
    int max; //Max capacity of that place
    int number;

    static LinkedList < Place > allPlaces = new LinkedList < Place > ();

    /** Place constructor that takes in file input and summarizes data
     * @param in - Scanner that is running through the testfile
     */
    public Place(MyScanner in ) {
        //Create error string that's put together with each check
        String posErrMes = "place ";
        //get name
        String tempHold = in.getNext("place with no name").toLowerCase();
        Pattern numCheck = Pattern.compile("[0-9]*");
        Matcher numMatch = numCheck.matcher(tempHold);
        String posNumbers = "";
        while (numMatch.find()) {
            posNumbers += numMatch.group();
        }
        if (!posNumbers.isEmpty()) {
            Errors.fatal(posErrMes + " " + tempHold
                    + ": has numbers in it");
        }
        name = tempHold;
        posErrMes = posErrMes + name;
        //grab next item
        String hold = in.next();
        //get median using pattern ("[0-9]+["."]*)
        Pattern medPat = Pattern.compile("([\\-]*[0-9]*[\\.]*[0-9]+)");
        median = Float.parseFloat(in.getNextPattern(medPat, hold,
                posErrMes + " median is not defined"));
        posErrMes += " " + median;
        //force median to be positive
        if (median <= 0) {
            Errors.fatal(posErrMes + " median is not positive");
        }
        //grab next item
        hold = in.next();
        //get scatter using pattern
        Pattern scPat = Pattern.compile("([\\-]*[0-9]*[\\.]*[0-9]+)");
        scatter = Float.parseFloat(in.getNextPattern(scPat, hold,
                posErrMes + " scatter is not defined"));
        posErrMes += " " + scatter;
        //get semicolon
        Matcher m = Pattern.compile("([;])").matcher(hold);
        String temp = "";
        while (m.find()) {
            if (!m.group().equals("")) {
                temp += m.group();
            }
        }
        if (!temp.equals(";")) {
           //get next item
           hold = in.next();
        }
        String semi = in.getNextPattern(Pattern.compile("([;])"), hold,
                posErrMes + " missing ;");
        posErrMes += " ;";
        //force scatter to be positive
        if (scatter < 0) {
            Errors.fatal(posErrMes + " scatter is not positive");
        }
        posErrMes += " ;";
        //force place to be unique
        if (findPlace(name) != null) {
            Errors.fatal(posErrMes + " is not unique");
        }
        // find the max capacity
        findMax(median, scatter);
        number = 0;
        allPlaces.add(this); //adds this to list of places
    }

    /**Constructor that replaces place with identical but new Place object
     * Could also possibly have new max. (creates a rotating list)
     * @param p -- place object that has hit capacity
     */
    public Place(Place p) {
        name = p.name;
        median = p.median;
        scatter = p.scatter;
        findMax(median, scatter);
        number = 0;
        int counter = 0;
        for (Place pl: allPlaces) {
            if (p.name.equals(pl.name)) {
                allPlaces.set(counter, this);
            }
            counter++;
        }
    }
    /** findMax -- finds the maximum capacity of a place
     * @param m -- median for finding the max capacity of a place
     * @param s -- scatter for finding the max capacity of a place
     */
    public void findMax(float m, float s) {
        double sigma = Math.log((m + s) / m);
        Random rand = new Random();
        double logNormal = Math.exp(sigma * rand.nextGaussian()) * median;
        max = (int) Math.floor(logNormal);
        if (max == 0) {
            max = 1;
        }
    }
    /** findPlace -- finds a place object by that name
     * @param n -- String for the name of object to be found
     * @return p -- returns the place if found, null -- returns null if
     * not found
     */
    public static Place findPlace(String n) {
        for (Place p: allPlaces) {
            if (p.name.equals(n)) {
                return p;
            }
        }
        return null;
    }
}
class Person {
    // instance variables
    Role role; // role of this person
    //unique list of places per person
    LinkedList < Place > uniquePlaces = new LinkedList < Place > ();
    // static variables used for all people
    static LinkedList < Person > allPeople = new LinkedList < Person > ();
    /** Construct a new person to perform some role
     *  @param r -- the role, p -- linked list of places for roles
     */
    public Person(Role r) {
        role = r;
        /** For loop goes through the places that an instance of
         *  role goes, if that place is at max capacity, it changes
         *  the role's list and inserts a new place.
         */
        int counter = 0;
        for (Place p: r.allPlacesRoleGoes) {
            //BUG: For some reason there is a null place being made
            //     Did not have time to find where and why.
            //     Will find before next MP.
            if (p != null) {
                if (p.number >= p.max) {
                    Place temp = new Place(p);
                    uniquePlaces.add(temp);
                    r.allPlacesRoleGoes.set(counter, temp);
                    temp.number += 1;
                } else {
                    uniquePlaces.add(p);
                    p.number += 1;
                }
                counter++;
            }
        }
        allPeople.add(this); // include this person in the list of all
    }
    /** Shuffles the place objects for all people
     *  Uses changePlaceRandomly to randomly switch
     *  the place objects.
     */
    public static void shuffle() {
        /** Nested for loop for going through all people and switching
         *  their place objects. Uses an if statement to ascertain place.
         *  if statements for roles as well, if they are in the same role:
         *  not same places
         */
        for (int i = 0; i < allPeople.size(); i++) {
            for (int j = i + 1; j < allPeople.size(); j++) {
                //check role (see if it is the same)
                Person check1 = allPeople.get(i);
                Person check2 = allPeople.get(j);
                if (check1.role.name.equals(check2.role.name)) {
                    //check to see if they are in the same places (most likely)
                    for (Place p: check1.uniquePlaces) {
                        for (Place t: check2.uniquePlaces) {
                            if (p.equals(t)) {
                                //change p (check1's place) with random person
                                changePlaceRandomly(i);
                            }
                        }
                    }
                }
            }
        }
    }
    /** Method for changing a person's place(s) by index, with a random
     *  Person in the list. This creates a randomization of place objects
     *  for the people.
     *  @param index -- the index of the person object that is having
     *                      it's place(s) changed.
     */
    public static void changePlaceRandomly(int index) {
        //get a random from MyRandom
        int rand = MyRandom.stream().nextInt(allPeople.size());
        //Person whose place(s) is changing
        Person changing = allPeople.get(index);
        //Temp list for holding the random person's list (for ease of access)
        LinkedList < Place > hold = allPeople.get(rand).uniquePlaces;
        //iterate through both people's lists
        for (int i = 0; i < changing.uniquePlaces.size(); i++) {
            Place check1 = changing.uniquePlaces.get(i);
            for (int j = 0; j < hold.size(); j++) {
                Place check2 = hold.get(j);
                //make sure the name's are the same of the Place objects
                if (check1.name.equals(check2.name)) {
                    //make sure they are not the same object
                    if (!check1.equals(check2)) {
                        //switch the two places
                        changing.uniquePlaces.set(i, check2);
                        allPeople.get(rand).uniquePlaces.set(j, check1);
                    }
                }
            }
        }
    }
    /** Print out the entire population
     *  This is needed only in the early stages of debugging
     *  and obviously useless for large populations
     */
    public static void printAll() {
        for (Person p: allPeople) {
            //Time is 1 object shorter than roles due to defaulting first place
            //Concatenates the places the people go into a string
            System.out.println(p.toString() + " " + p.role.name);
            for (int i = 0; i < p.uniquePlaces.size(); i++) {
                if (i - 1 >= 0) {
                    System.out.println(p.uniquePlaces.get(i).name + " "
                            + p.uniquePlaces.get(i) + " "
                            + p.role.timesForPlaces.get(i-1).toString());
                } else {
                    System.out.println(p.uniquePlaces.get(i).name + " "
                            + p.uniquePlaces.get(i));
                }
            }
        }
    }
}