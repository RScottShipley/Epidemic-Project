import java.lang.Cloneable;

public class TestingObjects implements Cloneable {
    int intTest;
    String stringTest;
    int[] intArrayTest;

    public TestingObjects(int n, String s, int[] nArray) {
        intTest = n;
        stringTest = s;
        intArrayTest = nArray;
    }

    public TestingObjects() {
        intTest = 0;
        stringTest = "";
        intArrayTest = null;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void main(String args[]) throws CloneNotSupportedException {
        int[] n = {1,2,3,4,5};
        TestingObjects first = new TestingObjects(5, "test", n);
        TestingObjects second = new TestingObjects();
        System.out.println("First object: " + first);
        System.out.println("Second object: " + second);
        System.out.println("Changing objects to see if anything happens to hash");
        TestingObjects third = new TestingObjects(first.intTest, first.stringTest, first.intArrayTest);
        System.out.println("Third object (copied first): " + third);
        TestingObjects fourth = (TestingObjects) second.clone();
        System.out.println("Fourth object (cloned second): " + fourth);
    }


}
