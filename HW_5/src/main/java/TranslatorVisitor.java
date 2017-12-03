import cs132.vapor.ast.*;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * TranslatorVisitor represents the visitor that generates MIPS code given, the corresponding vaporm code. 
 */
public class TranslatorVisitor extends VInstr.VisitorR<LinkedList<String>, Throwable> {
	// A list of MIPS code where each String element is a line of MIPS code
	public LinkedList<String> mips= new LinkedList<>();
	private static final String DATA = ".data";
	private static final String TEXT = ".text";
	private static final String SYSCALL = "syscall";
	private static boolean shouldPrint = false;
	private static boolean shouldError = false;
	private static boolean shouldHeapAlloc = false;

	// Used to denote the amount of indent needed
	private static int indentLevel = 0;
	/**
	 * @return String representing the relevant amount of indents.
	 */
	private String indent() {
		String indent = "";
		for(int i = 0; i < indentLevel; i++)
			indent += "\t";
		return indent;
	}

	private LinkedList<String> instr(String in, String dest, String src, String comment) {
		return new LinkedList<String>(Arrays.asList(String.format(indent() + "%s %s %s %s", in, dest, src, comment)));
	}

	private LinkedList<String> instr(String in, String dest, String src) {
		return instr(in, src, dest, "");
	}

	private LinkedList<String> arithmetic(String type, String dest, String src, int amt) {
		return new LinkedList<String>(Arrays.asList(String.format(indent() + "%s %s %s %d", type, dest, src, amt)));
	}

	private LinkedList<String> jump(String type, String to) {
		return new LinkedList<String>(Arrays.asList(String.format(indent() + "%s %s", type, to)));
	}

	private LinkedList<String> callMain(String mainName) {
		LinkedList<String> mainCall = new LinkedList<>();
		indentLevel++;
		mainCall.add(indent() + "jal " + mainName);
		mainCall.add(indent() + "$v0 10");
		mainCall.add(indent() + SYSCALL + "\n");
		indentLevel--;
		return mainCall;
	}

	private LinkedList<String> print() {
		LinkedList<String> print = new LinkedList<>();
		print.add("_print:");
		indentLevel++;
		print.addAll(instr("li", "$v0", "1", "\t # syscall: print integer"));
		print.add(indent() + SYSCALL);
		print.addAll(instr("la", "$a0", "_newline"));
		print.addAll(instr("li", "$v0", "4", "\t # syscall: print string"));
		print.add(indent() + SYSCALL);
		print.addAll(jump("jr", "$ra"));
		indentLevel--;
		return print;
	}

	private LinkedList<String> error() {
		LinkedList<String> error = new LinkedList<>();
		error.add("_error:");
		indentLevel++;
		error.addAll(instr("li", "$v0", "4", "\t # syscall: print string"));
		error.add(indent() + SYSCALL);
		error.addAll(instr("li", "$v0", "10", "\t # syscall: exit"));
		error.add(indent() + SYSCALL);
		indentLevel--;
		return error;
	}

	private LinkedList<String> heapAlloc() {
		LinkedList<String> heap = new LinkedList<>();
		heap.add("_heapAlloc:");
		indentLevel++;
		heap.addAll(instr("l1", "$v0", "9", "\t # syscall: sbrk"));
		heap.add(indent() + SYSCALL);
		heap.addAll(jump("jr", "$ra"));
		indentLevel--;
		return heap;
	}

	private LinkedList<String> startFrame(int offset) {
		LinkedList<String> frame = new LinkedList<>();
		frame.addAll(instr("sw", "$fp", "-8($sp)"));
		frame.addAll(instr("move", "$fp", "$sp"));
		frame.addAll(arithmetic("subu", "$sp", "$sp", offset));
		frame.addAll(instr("sw", "$ra", "-4($fp)"));
		return frame;
	}

	/**
	 * Runs the actual translator that kicks of the visitor.
	 */
	public TranslatorVisitor(VaporProgram program) throws Throwable {
		// Copy over data segments
		mips.add(indent() + DATA + "\n");
		for(VDataSegment segment : program.dataSegments) {
			mips.add(indent() + segment.ident);
			indentLevel++;
			// VMT methods
			for(VOperand func : segment.values) {
				mips.add(indent() + func.toString());
			}
			indentLevel--;
		}
		// Source code
		mips.add(indent() + TEXT + "\n");
		// Initialization
		mips.addAll(callMain(program.functions[0].ident));

		// Run through each function
		for(VFunction func : program.functions) {
			mips.add(indent() + func.ident + ":");
			indentLevel++;
			mips.addAll(startFrame(8 + 4*(func.stack.local + func.stack.out)));
			LinkedList<VCodeLabel> allLabels = new LinkedList<>(Arrays.asList(func.labels));
			for(VInstr _instr : func.body) {
				while(!allLabels.isEmpty() && allLabels.peek().sourcePos.line < _instr.sourcePos.line) {
					String labelId = allLabels.pop().ident + ":";
					indentLevel--;
					mips.add(indent() + labelId);
					indentLevel++;
				}
				//mips.addAll(_instr.accept(this));
			}
			indentLevel--;
		}
		if(shouldPrint)
			mips.addAll(print());
		if(shouldError)
			mips.addAll(error());
		if(shouldHeapAlloc)
			mips.addAll(heapAlloc());
	}

	/*********** Visitors for each instruction ***********/

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
