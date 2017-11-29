import cs132.vapor.ast.*;

import java.util.*;

/**
 * LivenessAnalysis represents the liveness analysis information for a given
 * function. The algorithm is extremely basic and does not rely on a CFG. It
 * just naively runs through each instruction updating the end location of 
 * each variable that has been seen so far (or creates a new variable).
 * The key is in calculating end range of cross call variables. To do this,
 * we store state in each variable determining if its been used after a call
 * and if its being used in a label.
 */
public class LivenessAnalysis extends VInstr.Visitor<Throwable> {
	
	// Mapping of variable name to VMVar representation in order
	private LinkedHashMap<String, VMVar> varMap = new LinkedHashMap<>();
	private int outCount = 0;

	/**
	 * Run the liveness analysis on the current function by looking through
	 * each parameter and instruction, mapping variables to VMVars.
	 * @param func The function to run analysis on
	 */
	public LivenessAnalysis(VFunction func) throws Throwable {
		// Look through the parameters of current function
        for(VVarRef param : func.params) {
            String varId = param.toString();
            varMap.put(varId, new VMVar(varId, param.sourcePos.line));
        }
        LinkedList<VCodeLabel> allLabels = new LinkedList<>(Arrays.asList(func.labels));
        // Run through body of function
        for(VInstr instr : func.body) {
            while(!allLabels.isEmpty() &&
                    allLabels.peek().sourcePos.line < instr.sourcePos.line) {
                String labelId = allLabels.pop().ident;
                for(VMVar currVar : varMap.values())
                    currVar.beforeLabels.add(labelId);
            }
            instr.accept(this);
        }
	}

	/**
	 * @return Mapping of variable name to VMVar representation.
	 */ 
	public LinkedHashMap<String, VMVar> getVarMap() {
		return this.varMap;
	}

	/**
	 * @return Overall out stack count calculated.
	 */
	public int getOutCount() {
		return this.outCount;
	}

	/**
     * Checks if a node is a valid variable, which is either a local or instance variable
     * @param node The Vapor Node in question
     * @return Whether the node is a local or global variable
     */
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

	/**
     * Performs a read operation on the variable if it exists
     * @param varId The variable name we are trying to search on
     * @param pos The new line position for the end range of the variable
     */
    private void readVariable(String varId, int pos) {
        VMVar var = varMap.get(varId);
        if(var != null) {
            var.r(pos);
        }
    }

	/**
     * Performs a write operation on the variable, which involves creating
     * the variable if it does not exist or to update it.
     * @param varId The variable name we are trying to search on
     * @param pos The new line position for the end range of the variable
     */
    private void writeVariable(String varId, int pos) {
        VMVar var = varMap.get(varId);
        if(var != null) {
            var.w(pos);
        } else {
            varMap.put(varId, new VMVar(varId, pos));
        }
    }

	/**
     * For cross call variables we need to look through all variables currently
     * seen and check if the current label is one they occur after. If so, the
     * technical end of the variable is during this branch/goto on the label.
     * @param label The label we are searching for
     * @param pos The new line position for the end range of the variable
     */
    private void updateLabels(String label, int pos) {
        for(VMVar currVar : varMap.values()) {
            if(currVar.afterLabels.contains(label)) {
                currVar.range.end = pos;
                currVar.afterCall = currVar.beforeCall;
            }
        }
    }

    /**
     * Extracts the label name from a branch.
     * @param node The VBranch node
     * @return String version of the label
     */
    private String extractLabel(VBranch node) {
        return node.target.getTarget().ident.replaceFirst(":","");
    }

    /**
     * Extracts the label name from a address.
     * @param node The VAddr node
     * @return String version of the label
     */
    private String extractLabel(VAddr node) {
        return node.toString().replaceFirst(":","");
    }

    /*********** Visitors for each instruction ***********/

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
        VOperand lhs = vCall.dest;
        VAddr<VFunction> rhs = vCall.addr;

        readVariable(rhs.toString(), line);
        // The callee need to spill into out stack
        if(vCall.args.length > 4) {
            outCount = vCall.args.length - 4;
        }

        // All variables so far have been before the call
        for(VMVar currVar : varMap.values())
            currVar.beforeCall = true;

        if(isVariable(lhs)) {
            writeVariable(lhs.toString(), line);
        }

    }

    @Override
    public void visit(VBuiltIn vBuiltIn) throws Throwable {
        int line = vBuiltIn.sourcePos.line;
        // Go through arguments
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
        // Memory destinations already have a register mapping, so just read
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

    @Override
    public void visit(VBranch vBranch) throws Throwable {
        int line = vBranch.sourcePos.line;
        String label = extractLabel(vBranch);
        updateLabels(label, line);
        // Update variable used as conditional
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