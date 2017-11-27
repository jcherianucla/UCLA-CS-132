import cs132.vapor.ast.*;

import java.util.Arrays;
import java.util.LinkedList;

public class TranslatorVisitor extends VInstr.VisitorR<LinkedList<String>, Throwable> {

    public LinkedList<String> vaporm = new LinkedList<>();
    private static int indentLevel = 0;
    private static LSRA lsra;

    private String indent() {
        String indent = "";
        for(int i = 0; i < indentLevel; i++)
            indent += "\t";
        return indent;
    }

    public TranslatorVisitor(VaporProgram program) throws Throwable {
        // Copy over data segments
        for(VDataSegment segment : program.dataSegments) {
            vaporm.add(indent() + "const " + segment.ident);
            indentLevel++;
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
            int inCount = (func.params.length - 4) > 0 ? func.params.length - 4 : 0;
            // Add function declaration
            vaporm.add(indent() + "func " + func.ident + " [in " + inCount + ", out "
                    + lsra.outCount + ", local " + lsra.localCount + "]");
            indentLevel++;
            // Caller saved locals
            for(int i = 0; i < lsra.localCount; i++) {
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
            boolean shouldIndent = false;
            // Go through all instructions
            for(VInstr instr : func.body) {
                // Add any labels before the current instruction
                while(!allLabels.isEmpty() && allLabels.peek().sourcePos.line < instr.sourcePos.line) {
                    String labelId = allLabels.pop().ident;
                    vaporm.add(indent() + labelId + ":");
                    shouldIndent = true;
                }
                if (shouldIndent)
                    indentLevel++;
                //vaporm.addAll(instr.accept(this));
                if (shouldIndent)
                    indentLevel--;
                shouldIndent = false;
            }
            indentLevel--;
        }
    }

    @Override
    public LinkedList<String> visit(VAssign vAssign) throws Throwable {
        return null;
    }

    @Override
    public LinkedList<String> visit(VCall vCall) throws Throwable {
        return null;
    }

    @Override
    public LinkedList<String> visit(VBuiltIn vBuiltIn) throws Throwable {
        return null;
    }

    @Override
    public LinkedList<String> visit(VMemWrite vMemWrite) throws Throwable {
        return null;
    }

    @Override
    public LinkedList<String> visit(VMemRead vMemRead) throws Throwable {
        return null;
    }

    @Override
    public LinkedList<String> visit(VBranch vBranch) throws Throwable {
        return null;
    }

    @Override
    public LinkedList<String> visit(VGoto vGoto) throws Throwable {
        return null;
    }

    @Override
    public LinkedList<String> visit(VReturn vReturn) throws Throwable {
        return null;
    }
}
