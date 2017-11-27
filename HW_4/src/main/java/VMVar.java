import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a Vapor-M Variable, which is a variable with all
 * the relevant information from Vapor code to be translated into
 * registers in Vapor-M.
 */
public class VMVar {
    public String id;
    public Interval range;
    public List<String> beforeLabels = new LinkedList<>();
    public List<String> afterLabels = new LinkedList<>();
    public boolean beforeCall = false;
    public boolean afterCall = false;

    public VMVar(String id, int start) {
        this.id = id;
        this.range = new Interval(start);
    }

    public void r(int line) {
        this.range.end = line;
        afterLabels.addAll(beforeLabels);
        beforeLabels.clear();
        if(beforeCall) {
            afterCall = true;
        }
    }

    public void w(int line) {
        this.range.end = line;
        beforeLabels.clear();
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

