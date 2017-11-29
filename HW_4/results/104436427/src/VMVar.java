import java.util.*;

/**
 * Represents a Vapor-M Variable, which is a variable with all
 * the relevant information from Vapor code to be translated into
 * registers in Vapor-M.
 */
public class VMVar {
    // Name of the variable
    public String id;
    // Live range of the variable
    public Interval range;
    // All labels that the variable is under so far
    public Set<String> beforeLabels = new HashSet<>();
    // All the labels where the variable has been used after
    public Set<String> afterLabels = new HashSet<>();
    // Whether a call has occured after variable read
    public boolean beforeCall = false;
    // Whether a variable has been read after a call
    public boolean afterCall = false;

    public VMVar(String id, int start) {
        this.id = id;
        this.range = new Interval(start);
    }

    public void r(int line) {
        this.range.end = line;
        afterLabels.addAll(beforeLabels);
        // No need to keep prior labels
        beforeLabels.clear();
        // Denotes whether the variable has been read after a call
        afterCall = beforeCall;
    }

    public void w(int line) {
        this.range.end = line;
        // No need to keep prior labels
        beforeLabels.clear();
    }

    /**
     * Pretty printing for debugging purposes.
     */
    public void print() {
        System.out.println("Var Name:" + id);
        System.out.println("Range: Start: " + range.start + " End: " + range.end);
        System.out.println(beforeLabels);
        System.out.println(afterLabels);
        System.out.println("Cross call: " + afterCall);
    }

    /**
     * Represents a range object with a start and end position.
     */
    public static class Interval {
        int start, end;
        public Interval(int line) {
            start = line;
            end = line;
        }
    }

    /**
     * Sorts an interval in ascending order based on its start time.
     */
    public static class StartComparator implements Comparator<VMVar> {
        @Override
        public int compare(VMVar var1, VMVar var2) {
            return var1.range.start - var2.range.start;
        }
    }

    /**
     * Sorts an interval in ascending order based on its end time.
     */
    public static class EndComparator implements Comparator<VMVar> {
        @Override
        public int compare(VMVar var1, VMVar var2) {
            return var1.range.end - var2.range.end;
        }
    }

}

