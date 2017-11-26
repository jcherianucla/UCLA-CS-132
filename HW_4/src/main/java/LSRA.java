import cs132.vapor.ast.*;

import java.text.ParseException;
import java.util.*;

public class LSRA extends VInstr.Visitor<Throwable> {

    /**
     * Initializes a set of registers based on the type of register,
     * and how many registers are needed.
     * @param type The type of a register, varies by caller and callee.
     * @param amt The number of registers, varies by caller and callee.
     * @return List of created registers as Strings.
     */
    private LinkedList<String> initRegisters(String type, int amt) {
        LinkedList<String> regs = new LinkedList<>();
        for(int i = 0; i <= amt; i++) {
            regs.add("$" + type + i);
        }
        return regs;
    }

    /**
     * Represents a Vapor-M Variable, which is a variable with all
     * the relevant information from Vapor code to be translated into
     * registers in Vapor-M.
     */
    public static class VMVar {
        public String id;
        public Interval range;
        public boolean inCall = false;
        public boolean existsAfterCall = false;

        public VMVar(String id, int start) {
            this.id = id;
            this.range = new Interval(start);
        }

        public void r(int line) {
            range.end = line;
            if(inCall)
                existsAfterCall = true;
        }

        public void w(int line) {
            range.end = line;
        }
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
    public class StartComparator implements Comparator<VMVar> {
        @Override
        public int compare(VMVar var1, VMVar var2) {
            return var1.range.start - var2.range.start;
        }
    }

    /**
     * Sorts an interval in ascending order based on its end time.
     */
    public class EndComparator implements Comparator<VMVar> {
        @Override
        public int compare(VMVar var1, VMVar var2) {
            return var1.range.end - var2.range.end;
        }
    }

    private static LinkedList<String> calleeRegisters;
    private static LinkedList<String> callerRegisters;
    private static LinkedList<VMVar> activeVars;
    private static LinkedList<String> freeRegisters = new LinkedList<>();
    private static HashMap<VMVar, String> locations = new HashMap<>();
    private static HashMap<VMVar, String> registerMap = new HashMap<>();
    private static LinkedHashMap<String, VMVar> varMap = new LinkedHashMap<>();
    private static final int TOTAL_REGISTERS = 17;
    private final VFunction currFunc;
    private static int calleeRegisterCount = 0;


    public LSRA(VFunction func) {
        this.currFunc = func;
        calleeRegisters = initRegisters("s", 7);
        callerRegisters = initRegisters("t", 8);
    }

    private String newStackLocation() {
        return "local[" + calleeRegisterCount++ + "]";
    }

    private boolean isCalleeRegister(String reg) {
        return reg.contains("s");
    }

    private boolean isVariable(Node node) {
        if(node instanceof VOperand) {
            return node instanceof VVarRef.Local;
        } else if(node instanceof VMemRef) {
            return node instanceof VMemRef.Global;
        }
        return false;
    }

    private String getCalleeRegister() {
        calleeRegisterCount++;
        return calleeRegisters.removeFirst();
    }

    private String getCallerRegister() {
        return callerRegisters.removeFirst();
    }

    public void readVariable(String varId, int pos) {
        VMVar var = varMap.get(varId);
        if(var != null) {
            var.r(pos);
        }
    }

    public void writeVariable(String varId, int pos) {
        VMVar var = varMap.get(varId);
        if(var != null) {
            var.w(pos);
        } else {
            varMap.put(varId, new VMVar(varId, pos));
        }
    }

    /**
     * The Linear Scan Register Allocation algorithm as given in
     * Section 4.1, Figure 1 in Linear Scan Register Allocation by
     * Massimiliano Poletto and Vivek Sarkar.
     */
    public void allocate() {
        activeVars = new LinkedList<>();
        LinkedList<VMVar> live = new LinkedList<>(varMap.values());
        live.sort(new StartComparator());
        for(VMVar var : live) {
            expireOldIntervals(var);
            if(activeVars.size() >= TOTAL_REGISTERS || (var.existsAfterCall && calleeRegisters.isEmpty())) {
                spillAtInterval(var);
            } else {
                registerMap.put(var, getFreeRegister(var.existsAfterCall));
                activeVars.add(var);
            }
        }

    }

    public void expireOldIntervals(VMVar inVar) {
        activeVars.sort(new EndComparator());
        for(VMVar currVar: activeVars) {
            if(currVar.range.end >= inVar.range.start)
                return;
            activeVars.remove(currVar);
            freeRegisters.add(registerMap.get(currVar));
        }
    }

    public void spillAtInterval(VMVar inVar) {
        VMVar spill = activeVars.getLast();
        if(spill.range.end >= inVar.range.end) {
            registerMap.put(inVar, registerMap.get(spill));
            locations.put(spill, newStackLocation());
            activeVars.remove(spill);
            activeVars.add(inVar);
            activeVars.sort(new EndComparator());
        } else {
            locations.put(inVar, newStackLocation());
        }
    }

    public String getFreeRegister(boolean aliveAfterCall) {
        String free;
        if(aliveAfterCall) {
            for(String reg : freeRegisters) {
                if(isCalleeRegister(reg)) {
                    free = reg;
                    freeRegisters.remove(reg);
                    return free;
                }
            }
            return getCalleeRegister();
        }
        for(String reg : freeRegisters) {
            free = reg;
            freeRegisters.remove(reg);
            return free;
        }
        if(callerRegisters.isEmpty()) {
            return getCalleeRegister();
        } else {
            return getCallerRegister();
        }
    }

    public void runAnalysis() throws Throwable {
        // Look through the parameters of current function
        for(VVarRef param : currFunc.params) {
            String varId = param.toString();
            varMap.put(varId, new VMVar(varId, param.sourcePos.line));
        }
        // Run through body of function
        for(VInstr instr : currFunc.body) {
            instr.accept(this);
        }
    }

    @Override
    public void visit(VAssign vAssign) throws Throwable {
        int line = vAssign.sourcePos.line;
        VOperand lhs = vAssign.dest;
        VOperand rhs = vAssign.source;
        if(isVariable(rhs)) {
            readVariable(rhs.toString(), line);
        }
        // Left hand side must be a variable, therefore update it
        writeVariable(lhs.toString(), line);
    }

    @Override
    public void visit(VCall vCall) throws Throwable {

    }

    @Override
    public void visit(VBuiltIn vBuiltIn) throws Throwable {

    }

    @Override
    public void visit(VMemWrite vMemWrite) throws Throwable {
        int line = vMemWrite.sourcePos.line;
        VMemRef lhs = vMemWrite.dest;
        VOperand rhs = vMemWrite.source;
        if(isVariable(rhs)) {
            readVariable(rhs.toString(), line);
        }
        if(isVariable(lhs)) {
            readVariable(((VMemRef.Global)lhs).base.toString(), line);
        } else {
            throw new Throwable();
        }
    }

    @Override
    public void visit(VMemRead vMemRead) throws Throwable {
        int line = vMemRead.sourcePos.line;
        VVarRef lhs = vMemRead.dest;
        VMemRef rhs = vMemRead.source;
        if(isVariable(rhs)) {
            readVariable(((VMemRef.Global)rhs).base.toString(), line);
        }
        if(isVariable(lhs)) {
            writeVariable(lhs.toString(), line);
        }
    }

    @Override
    public void visit(VBranch vBranch) throws Throwable {

    }

    @Override
    public void visit(VGoto vGoto) throws Throwable {

    }

    @Override
    public void visit(VReturn vReturn) throws Throwable {
        int line = vReturn.sourcePos.line;
        VOperand val = vReturn.value;
        if(isVariable(val)) {
            readVariable(val.toString(), line);
        }
    }
}
