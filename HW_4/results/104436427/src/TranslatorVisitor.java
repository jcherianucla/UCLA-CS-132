import cs132.vapor.ast.*;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * TranslatorVisitor represents the visitor that generates vaporm code given,
 * the corresponding vapor code. Utilizes the Register allocator for storing and
 * loading from registers or stack space.
 */
public class TranslatorVisitor extends VInstr.VisitorR<LinkedList<String>, Throwable> {

    // A list of vaporm code where each String element is a line of vaporm code
    public LinkedList<String> vaporm = new LinkedList<>();
    // Used to denote the amount of indent needed
    private static int indentLevel = 0;
    // Register allocator
    private static LSRA lsra;

    /**
     * @return String representing the relevant amount of indents.
     */
    private String indent() {
        String indent = "";
        for(int i = 0; i < indentLevel; i++)
            indent += "\t";
        return indent;
    }

    /**
     * Runs the actual translator that kicks of the visitor.
     */
    public TranslatorVisitor(VaporProgram program) throws Throwable {
        // Copy over data segments
        for(VDataSegment segment : program.dataSegments) {
            // Start of VMT
            vaporm.add(indent() + "const " + segment.ident);
            indentLevel++;
            // VMT methods
            for(VOperand func : segment.values) {
                vaporm.add(indent() + func.toString());
            }
            indentLevel--;
        }
        // Run through each function
        for(VFunction func : program.functions) {
            // Run LSRA on function
            lsra = new LSRA(func);
            lsra.run();
            // In stack only needed to be used if more than 4 arguments
            int inCount = (func.params.length - 4) > 0 ? func.params.length - 4 : 0;
            // Add function declaration
            vaporm.add(indent() + "func " + func.ident + " [in " + inCount + ", out "
                    + lsra.outCount + ", local " + lsra.localCount + "]");
            indentLevel++;
            // Caller saved locals
            for(int i = 0; i < lsra.localCount && i < 8; i++) {
                vaporm.add(indent() + "local[" + i + "] = $s" + i);
            }
            // Arguments
            for(int i = 0; i < func.params.length; i++) {
                String reg = lsra.getRegister(func.params[i].toString());
                if(i < 4) {
                    vaporm.add(indent() + reg + " = $a" + i);
                } else {
                    vaporm.add(indent() + reg + " = in[" + (i-4) + "]");
                }
            }
            LinkedList<VCodeLabel> allLabels = new LinkedList<>(Arrays.asList(func.labels));
            // Go through all instructions
            for(VInstr instr : func.body) {
                // Add any labels before the current instruction
                while(!allLabels.isEmpty() && allLabels.peek().sourcePos.line < instr.sourcePos.line) {
                    String labelId = allLabels.pop().ident;
                    indentLevel--;
                    vaporm.add(indent() + labelId + ":");
                    indentLevel++;
                }
                // Begin visitor
                vaporm.addAll(instr.accept(this));
            }
            indentLevel--;
        }
    }

    /*********** Visitors for each instruction ***********/

    @Override
    public LinkedList<String> visit(VAssign vAssign) throws Throwable {
        LinkedList<String> assign = new LinkedList<>();
        String lhsStr = vAssign.dest.toString();
        String rhsStr = vAssign.source.toString();
        String rhsVal = lsra.getRegister(rhsStr) == null ? rhsStr : lsra.getRegister(rhsStr);
        assign.add(indent() + lsra.getRegister(lhsStr) + " = " + rhsVal);
        return assign;
    }

    @Override
    public LinkedList<String> visit(VCall vCall) throws Throwable {
        LinkedList<String> call = new LinkedList<>();
        // Go through arguments to assign to argument registers/out stack
        for(int i = 0; i < vCall.args.length; i++) {
            String argument = vCall.args[i].toString();
            String rhs = lsra.getRegister(argument) == null ?
                    argument : lsra.getRegister(argument);
            if(i < 4) {
               call.add(indent() + "$a" + i + " = " + rhs);
            } else {
                call.add(indent() + "out[" + (i-4) + "] = " + rhs);
            }
        }
        String resultReg = lsra.getRegister(vCall.dest.toString());
        String addr = vCall.addr.toString();
        String funcVar = lsra.getRegister(addr) == null ? addr : lsra.getRegister(addr);
        call.add(indent() + "call " + funcVar);
        call.add(indent() + resultReg + " = $v0");
        return call;
    }

    @Override
    public LinkedList<String> visit(VBuiltIn vBuiltIn) throws Throwable {
        LinkedList<String> builtIn = new LinkedList<>();
        String args = "";
        int argSize = vBuiltIn.args.length;
        // Build up argument string
        for(int i = 0; i < argSize; i++) {
            String curr = vBuiltIn.args[i].toString();
            String arg = lsra.getRegister(curr) == null ? curr : lsra.getRegister(curr);
            args += arg + ((i < argSize - 1) ? " " : "");
        }
        VVarRef dest = vBuiltIn.dest;
        String lhs = "";
        // Left hand side only needed if an assignment Op
        if(dest != null) {
            lhs = lsra.getRegister(vBuiltIn.dest.toString()) + " = ";
        }
        builtIn.add(indent() + lhs + vBuiltIn.op.name + "(" + args + ")");
        return builtIn;
    }

    @Override
    public LinkedList<String> visit(VMemWrite vMemWrite) throws Throwable {
        LinkedList<String> memWrite = new LinkedList<>();
        VMemRef.Global dest = (VMemRef.Global)vMemWrite.dest;
        String lhsReg = lsra.getRegister(dest.base.toString());
        String srcStr = vMemWrite.source.toString();
        String rhsVal = lsra.getRegister(srcStr) == null ? srcStr : lsra.getRegister(srcStr);
        String offset = dest.byteOffset == 0 ? "" : "+" + String.valueOf(dest.byteOffset);
        memWrite.add(indent() + "[" + lhsReg + offset + "] = " + rhsVal);
        return memWrite;
    }

    @Override
    public LinkedList<String> visit(VMemRead vMemRead) throws Throwable {
        LinkedList<String> memRead = new LinkedList<>();
        String lhsReg = lsra.getRegister(vMemRead.dest.toString());
        VMemRef.Global src = (VMemRef.Global)vMemRead.source;
        String srcStr = src.base.toString();
        String rhsVal = lsra.getRegister(srcStr) == null ? srcStr : lsra.getRegister(srcStr);
        String offset = src.byteOffset == 0 ? "" : "+" + String.valueOf(src.byteOffset);
        memRead.add(indent() + lhsReg + " = [" + rhsVal + offset + "]");
        return memRead;
    }

    @Override
    public LinkedList<String> visit(VBranch vBranch) throws Throwable {
        LinkedList<String> branch = new LinkedList<>();
        String cond = lsra.getRegister(vBranch.value.toString());
        branch.add(indent() + "if" + (vBranch.positive ? "" : "0") + cond + " goto " + vBranch.target.toString());
        return branch;
    }

    @Override
    public LinkedList<String> visit(VGoto vGoto) throws Throwable {
        LinkedList<String> _goto = new LinkedList<>();
        _goto.add(indent() + "goto " + vGoto.target.toString());
        return _goto;
    }

    @Override
    public LinkedList<String> visit(VReturn vReturn) throws Throwable {
        LinkedList<String> ret = new LinkedList<>();
        // Assign value back to return register
        if(vReturn.value != null) {
            String retVal = vReturn.value.toString();
            String val = lsra.getRegister(retVal) == null ? retVal : lsra.getRegister(retVal);
            ret.add(indent() + "$v0 = " + val);
        }
        // Reassign local stack values to callee saved registers
        for (int i = 0; i < lsra.localCount && i < 8; i++) {
            ret.add(indent() + "$s" + i + " = local[" + i + "]");
        }
        ret.add(indent() + "ret");
        return ret;
    }
}
