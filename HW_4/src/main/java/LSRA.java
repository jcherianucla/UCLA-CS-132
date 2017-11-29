import cs132.vapor.ast.*;

import java.util.*;

/**
 * LSRA represents the Linear Scan Register Allocator, that uses a linear
 * scan allocation scheme given liveness information, run per function.
 * This utilizes caller saved registers to handle cross call variables,
 * saving cross call variables onto the caller's stack and restoring them
 * at the very end.
 */
public class LSRA {

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

    // Caller and callee registers holding $s0,..., $s7,$t0,...,$t8
    private LinkedList<String> calleeRegisters;
    private LinkedList<String> callerRegisters;
    // All variables currently active in the LSRA algorithm
    private LinkedList<VMVar> activeVars;
    // Pool of free registers pulled from caller and callee registers
    private LinkedList<String> freeRegisters = new LinkedList<>();
    // Mapping of variable to stack location
    private HashMap<VMVar, String> locations = new HashMap<>();
    // Mapping of variable to allocated register
    private HashMap<VMVar, String> registerMap = new HashMap<>();
    // Liveness analysis information
    private LivenessAnalysis analysis;
    private static final int TOTAL_REGISTERS = 17;
    private final VFunction currFunc;
    // Count of how many stack allocations we'll need - caller saved
    public int localCount = 0;
    // Count of how many out stack variables we'll need
    public int outCount = 0;
    // Final map of all variables to their registers/local stack locations
    private HashMap<String, String> allocatedMap = new HashMap<>();


    /**
     * Set up Allocator to work on current function
     * @param func The function to run on
     */
    public LSRA(VFunction func) {
        this.currFunc = func;
        calleeRegisters = initRegisters("s", 7);
        callerRegisters = initRegisters("t", 8);
    }

    /**
     * Performs liveness analysis and allocates registers according to
     * LSRA algorithm. It then sets a full mapping of variables to registers/stack.
     * @throws Throwable
     */
    public void run() throws Throwable {
        analysis = new LivenessAnalysis(this.currFunc);
        outCount = analysis.getOutCount();
        allocate();
        for(Map.Entry<VMVar, String> varToReg : registerMap.entrySet()) {
            allocatedMap.put(varToReg.getKey().id, varToReg.getValue());
        }
        for(Map.Entry<VMVar, String> varToLocal : locations.entrySet()) {
            allocatedMap.put(varToLocal.getKey().id, varToLocal.getValue());
        }
    }

    /**
     * Gives back the register/stack location the variable is mapped to
     * @param var Variable we want to retrieve
     * @return String representing the register/stack location
     */
    public String getRegister(String var) {
        return allocatedMap.get(var);
    }

    /**
     * Allocates a new stack variable, noting the increase in stack variables.
     * @return String representing the stack location
     */
    private String newStackLocation() {
        return "local[" + localCount++ + "]";
    }

    /**
     * Checks if a register is callee or caller
     * @param reg String representing the register in question
     * @return Boolean denoting caller vs callee register
     */
    private boolean isCalleeRegister(String reg) {
        return reg.contains("s");
    }

    /**
     * Assigns callee register - Caller saved therefore increase in localcount
     * @return Callee register
     */
    private String getCalleeRegister() {
        localCount++;
        return calleeRegisters.removeFirst();
    }

    /**
     * Gives back a caller register
     * @return Caller register
     */
    private String getCallerRegister() {
        return callerRegisters.removeFirst();
    }

    /**
     * The Linear Scan Register Allocation algorithm as given in
     * Section 4.1, Figure 1 in Linear Scan Register Allocation by
     * Massimiliano Poletto and Vivek Sarkar. Calculates active variables
     * and assigns them to available pool of registers, grabs a new free
     * register or spills onto the stack in the case of running out of registers.
     */
    private void allocate() {
        activeVars = new LinkedList<>();
        LinkedList<VMVar> live = new LinkedList<>(analysis.getVarMap().values());
        live.sort(new VMVar.StartComparator());
        for(VMVar var : live) {
            expireOldIntervals(var);
            if(activeVars.size() == TOTAL_REGISTERS || (var.afterCall && calleeRegisters.isEmpty())) {
                spillAtInterval(var);
            } else {
                registerMap.put(var, getFreeRegister(var.afterCall));
                activeVars.add(var);
                activeVars.sort(new VMVar.EndComparator());
            }
        }
    }
    /**
     * Go through old intervals and remove the register mappings
     * for all variables that are no longer in the current range.
     * @param inVar The variable to compare ranges to
     */
    private void expireOldIntervals(VMVar inVar) {
        activeVars.sort(new VMVar.EndComparator());
        ListIterator<VMVar> iter = activeVars.listIterator();
        while(iter.hasNext()) {
            VMVar currVar = iter.next();
            if(currVar.range.end >= inVar.range.start) {
                return;
            }
            iter.remove();
            freeRegisters.add(registerMap.get(currVar));
        }
    }

    /**
     * Spill variables onto the local stack. Either spill the last
     * variable or the variable currently being looked at based on the
     * end range. The idea is that we spill the latest variable seen so far.
     * @param inVar The variable to compare ranges to
     */
    private void spillAtInterval(VMVar inVar) {
        VMVar spill = activeVars.getLast();
        if(spill.range.end > inVar.range.end) {
            registerMap.put(inVar, registerMap.get(spill));
            locations.put(spill, newStackLocation());
            activeVars.remove(spill);
            activeVars.add(inVar);
            activeVars.sort(new VMVar.EndComparator());
        } else {
            locations.put(inVar, newStackLocation());
        }
    }

    /**
     * Retrieves the next free register. We try to get caller registers first
     * after which we default to callee registers. The special case is when
     * we have a variable that is cross call (i.e. used after a call). This
     * LSRA uses caller saved, in which case we save to callee registers.
     * @param afterCall Whether the we are looking at a variable that is cross call or not
     * @return String representing the register
     */
    private String getFreeRegister(boolean afterCall) {
        if(afterCall) {
            ListIterator<String> iter = freeRegisters.listIterator();
            while(iter.hasNext()) {
                String free = iter.next();
                if(isCalleeRegister(free)) {
                    iter.remove();
                    return free;
                }
            }
            return getCalleeRegister();
        }
        if(!freeRegisters.isEmpty()) {
            return freeRegisters.removeFirst();
        }
        if(callerRegisters.isEmpty()) {
            return getCalleeRegister();
        } else {
            return getCallerRegister();
        }
    }
}