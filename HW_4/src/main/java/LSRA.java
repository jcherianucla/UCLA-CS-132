import cs132.vapor.ast.*;

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

    private static LinkedList<String> calleeRegisters;
    private static LinkedList<String> callerRegisters;
    private static LinkedList<VMVar> activeVars;
    private static LinkedList<String> freeRegisters = new LinkedList<>();
    private static HashMap<VMVar, String> locations = new HashMap<>();
    private static HashMap<VMVar, String> registerMap = new HashMap<>();
    private static LinkedHashMap<String, VMVar> varMap = new LinkedHashMap<>();
    private static final int TOTAL_REGISTERS = 17;
    private final VFunction currFunc;
    public int localCount = 0;
    public int outCount = 0;
    private static HashMap<String, String> allocatedMap = new HashMap<>();


    public LSRA(VFunction func) {
        this.currFunc = func;
        calleeRegisters = initRegisters("s", 7);
        callerRegisters = initRegisters("t", 8);
    }

    public void run() throws Throwable {
        runAnalysis();
        allocate();
        for(Map.Entry<VMVar, String> varToReg : registerMap.entrySet()) {
            allocatedMap.put(varToReg.getKey().id, varToReg.getValue());
        }
        for(Map.Entry<VMVar, String> varToLocal : locations.entrySet()) {
            allocatedMap.put(varToLocal.getKey().id, varToLocal.getValue());
        }
    }

    public String getRegister(String var) {
        return allocatedMap.get(var);
    }

    private String newStackLocation() {
        return "local[" + localCount++ + "]";
    }

    private boolean isCalleeRegister(String reg) {
        return reg.contains("s");
    }

    private boolean isVariable(Node node) {
        if(node == null)
            return false;
        if(node instanceof VOperand) {
            return node instanceof VVarRef.Local;
        } else if(node instanceof VMemRef) {
            return node instanceof VMemRef.Global;
        }
        return false;
    }

    private String getCalleeRegister() {
        localCount++;
        return calleeRegisters.removeFirst();
    }

    private String getCallerRegister() {
        return callerRegisters.removeFirst();
    }

    private void readVariable(String varId, int pos) {
        VMVar var = varMap.get(varId);
        if(var != null) {
            var.r(pos);
        }
    }

    private void writeVariable(String varId, int pos) {
        VMVar var = varMap.get(varId);
        if(var != null) {
            var.w(pos);
        } else {
            varMap.put(varId, new VMVar(varId, pos));
        }
    }

    private void updateLabels(String label, int pos) {
        for(VMVar currVar : varMap.values()) {
            if(currVar.afterLabels.contains(label)) {
                currVar.range.end = pos;
                if(currVar.beforeCall)
                    currVar.afterCall = true;
            }
        }
    }

    /**
     * The Linear Scan Register Allocation algorithm as given in
     * Section 4.1, Figure 1 in Linear Scan Register Allocation by
     * Massimiliano Poletto and Vivek Sarkar.
     */
    private void allocate() {
        activeVars = new LinkedList<>();
        LinkedList<VMVar> live = new LinkedList<>(varMap.values());
        live.sort(new VMVar.StartComparator());
        for(VMVar var : live) {
            expireOldIntervals(var);
            if(activeVars.size() >= TOTAL_REGISTERS || (var.afterCall && calleeRegisters.isEmpty())) {
                spillAtInterval(var);
            } else {
                registerMap.put(var, getFreeRegister(var.afterCall));
                activeVars.add(var);
            }
        }

    }

    private void expireOldIntervals(VMVar inVar) {
        activeVars.sort(new VMVar.EndComparator());
        ListIterator<VMVar> iter = activeVars.listIterator();
        while(iter.hasNext()) {
            VMVar currVar = iter.next();
            if(currVar.range.end < inVar.range.start) {
               freeRegisters.add(registerMap.get(currVar));
               iter.remove();
            }
        }
    }

    private void spillAtInterval(VMVar inVar) {
        VMVar spill = activeVars.getLast();
        if(spill.range.end >= inVar.range.end) {
            registerMap.put(inVar, registerMap.get(spill));
            locations.put(spill, newStackLocation());
            activeVars.remove(spill);
            activeVars.add(inVar);
            activeVars.sort(new VMVar.EndComparator());
        } else {
            locations.put(inVar, newStackLocation());
        }
    }

    private String getFreeRegister(boolean afterCall) {
        String free;
        if(afterCall) {
            ListIterator<String> iter = freeRegisters.listIterator();
            while(iter.hasNext()) {
                free = iter.next();
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

    private void runAnalysis() throws Throwable {
        // Look through the parameters of current function
        for(VVarRef param : currFunc.params) {
            String varId = param.toString();
            varMap.put(varId, new VMVar(varId, param.sourcePos.line));
        }
        LinkedList<VCodeLabel> allLabels = new LinkedList<>(Arrays.asList(currFunc.labels));
        // Run through body of function
        for(VInstr instr : currFunc.body) {
            while(!allLabels.isEmpty() && allLabels.peek().sourcePos.line < instr.sourcePos.line) {
                String labelId = allLabels.pop().toString();
                for(VMVar currVar : varMap.values())
                    currVar.beforeLabels.add(labelId);
            }
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
        int line = vCall.sourcePos.line;
        // Go through arguments
        for(VOperand arg : vCall.args) {
            if(isVariable(arg)) {
                readVariable(arg.toString(), line);
            }
        }
        // Does the callee need to spill into out stack
        if(vCall.args.length > 4) {
            outCount = vCall.args.length - 4;
        }

        // All variables so far have been before the call
        for(VMVar currVar : varMap.values())
            currVar.beforeCall = true;

        VOperand lhs = vCall.dest;
        VAddr<VFunction> rhs = vCall.addr;
        if(isVariable(lhs)) {
            writeVariable(lhs.toString(), line);
        }
        readVariable(rhs.toString(), line);
    }

    @Override
    public void visit(VBuiltIn vBuiltIn) throws Throwable {
        int line = vBuiltIn.sourcePos.line;
        for(VOperand arg : vBuiltIn.args) {
            if(isVariable(arg))
                readVariable(arg.toString(), line);
        }
        VOperand lhs = vBuiltIn.dest;
        if(isVariable(lhs))
            writeVariable(lhs.toString(), line);
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

    private String extractLabel(VBranch node) {
        return node.target.getTarget().ident.replaceFirst(":","");
    }
    private String extractLabel(VAddr node) {
        return node.toString().replaceFirst(":","");
    }

    @Override
    public void visit(VBranch vBranch) throws Throwable {
        int line = vBranch.sourcePos.line;
        String label = extractLabel(vBranch);
        updateLabels(label, line);
        VOperand cond = vBranch.value;
        readVariable(cond.toString(), line);
    }

    @Override
    public void visit(VGoto vGoto) throws Throwable {
        int line = vGoto.sourcePos.line;
        String label = extractLabel(vGoto.target);
        updateLabels(label, line);
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
