import cs132.vapor.ast.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.HashMap;

/**
 * TranslatorVisitor represents the visitor that generates MIPS code given, the corresponding vaporm code. 
 */
public class TranslatorVisitor extends VInstr.VisitorR<LinkedList<String>, Throwable> {
	// A list of MIPS code where each String element is a line of MIPS code
	public LinkedList<String> mips= new LinkedList<>();
	private static final String DATA = ".data";
	private static final String TEXT = ".text";
	private static final String ALIGN = ".align 0";
	private static final String SYSCALL = "syscall";
	private static boolean shouldPrint = false;
	private static boolean shouldError = false;
	private static boolean shouldHeapAlloc = false;
	private static boolean oob = false;
	private static boolean _null = false;
	private static final HashMap<String, String> unaryFuncs = new HashMap<String, String>(){{
		put("PrintIntS", "_print");
		put("Error", "_error");
	}};
	private static final HashMap<String, String> binaryFuncs = new HashMap<String, String>(){{
		put("HeapAllocZ", "_heapAlloc");
		put("Add", "addu");
		put("LtS", "slti");
		put("LtSreg", "slt");
		put("MulS", "mul");
		put("Sub", "subu");
		put("Lt", "sltu");
		//put("Eq", "");
	}};


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
		return instr(in, dest, src, "");
	}

	private LinkedList<String> arithmetic(String type, String dest, String src, String amt) {
		return new LinkedList<String>(Arrays.asList(String.format(indent() + "%s %s %s %s", type, dest, src, amt)));
	}

	private LinkedList<String> jump(String type, String to) {
		return new LinkedList<String>(Arrays.asList(String.format(indent() + "%s %s", type, to)));
	}

	private LinkedList<String> callMain(String mainName) {
		LinkedList<String> mainCall = new LinkedList<>();
		indentLevel++;
		mainCall.add(indent() + "jal " + mainName);
		mainCall.addAll(instr("li", "$v0", "10"));
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
		heap.addAll(instr("li", "$v0", "9", "\t # syscall: sbrk"));
		heap.add(indent() + SYSCALL);
		heap.addAll(jump("jr", "$ra"));
		indentLevel--;
		return heap;
	}

	private LinkedList<String> startFrame(int offset) {
		LinkedList<String> frame = new LinkedList<>();
		frame.addAll(instr("sw", "$fp", "-8($sp)"));
		frame.addAll(instr("move", "$fp", "$sp"));
		frame.addAll(arithmetic("subu", "$sp", "$sp", String.valueOf(offset)));
		frame.addAll(instr("sw", "$ra", "-4($fp)"));
		return frame;
	}

	private LinkedList<String> endFrame(int offset) {
		LinkedList<String> frame = new LinkedList<>();
		frame.addAll(instr("lw", "$ra", "-4($fp)"));
		frame.addAll(instr("lw", "$fp", "-8($fp)"));
		frame.addAll(arithmetic("addu", "$sp", "$sp", String.valueOf(offset)));
		frame.addAll(jump("jr", "$ra"));
		return frame;
	}


	private boolean isRegister(String str) {
		return str.contains("$");
	}

	private LinkedList<String> end() {
		LinkedList<String> _end = new LinkedList<>();
		_end.add(indent() + DATA);
		_end.add(indent() + ALIGN);
		if(shouldPrint) {
			_end.add(indent() + "_newline: .asciiz \"\\n\"");
		}
		int strCount = 0;
		if(_null) {
			_end.add(indent() + "_str" + strCount + ": .asciiz \"null pointer\\n\"");
			strCount++;
		}
		if(oob) {
			_end.add(indent() + "_str" + strCount + ": .asciiz \"array index out of bounds\\n\"");
		}
		return _end;
	}

	private int compute(String op, String val1, String val2) {
		switch(op) {
			case "Add":
			return Integer.parseInt(val1) + Integer.parseInt(val2);
			case "Sub":
			return Integer.parseInt(val2) - Integer.parseInt(val1);
			case "MulS":
			return Integer.parseInt(val1) * Integer.parseInt(val2);
			default:
			return 0;
		}
	}


	/**
	 * Runs the actual translator that kicks of the visitor.
	 */
	public TranslatorVisitor(VaporProgram program) throws Throwable {
		// Copy over data segments
		mips.add(indent() + DATA + "\n");
		for(VDataSegment segment : program.dataSegments) {
			mips.add(indent() + segment.ident + ":");
			indentLevel++;
			// VMT methods
			for(VOperand func : segment.values) {
				mips.add(indent() + func.toString().substring(1));
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
			int offset = 8 + 4*(func.stack.local + func.stack.out);
			mips.addAll(startFrame(offset));
			LinkedList<VCodeLabel> allLabels = new LinkedList<>(Arrays.asList(func.labels));
			for(VInstr _instr : func.body) {
				while(!allLabels.isEmpty() && allLabels.peek().sourcePos.line < _instr.sourcePos.line) {
					String labelId = allLabels.pop().ident + ":";
					indentLevel--;
					mips.add(indent() + labelId);
					indentLevel++;
				}
				mips.addAll(_instr.accept(this));
			}
			mips.addAll(endFrame(offset));
			mips.add("\n");
			indentLevel--;
		}
		if(shouldPrint)
			mips.addAll(print());
		if(shouldError)
			mips.addAll(error());
		if(shouldHeapAlloc)
			mips.addAll(heapAlloc());
		mips.addAll(end());
	}

	/*********** Visitors for each instruction ***********/

	@Override
	public LinkedList<String> visit(VAssign vAssign) throws Throwable {
		LinkedList<String> assign = new LinkedList<>();
		String rhs = vAssign.source.toString();
		String lhs = vAssign.dest.toString();
		if(isRegister(rhs))
			assign.addAll(instr("move", lhs, rhs));
		else {
			String load = rhs.contains(":") ? "la" : "li";
			assign.addAll(instr(load, lhs, rhs.replaceFirst(":", "")));
		}
		return assign;
	}

	@Override
	public LinkedList<String> visit(VCall vCall) throws Throwable {
		LinkedList<String> call = new LinkedList<>();
		String addr = vCall.addr.toString();
		String jmp = isRegister(addr) ? "jalr" : "jal";
		call.addAll(jump(jmp, vCall.addr.toString().replaceFirst(":","")));
		return call;
	}

	@Override
	public LinkedList<String> visit(VBuiltIn vBuiltIn) throws Throwable {
		LinkedList<String> builtIn = new LinkedList<>();
		VVarRef dest = vBuiltIn.dest;
		String name = vBuiltIn.op.name;
		if(dest == null) {
			// Unary funcs
			String arg = vBuiltIn.args[0].toString();
			if(name.contains("Print")) {
				shouldPrint = true;
				String transfer = isRegister(arg) ? "move" : "li";
				builtIn.addAll(instr(transfer, "$a0", arg));
				builtIn.addAll(jump("jal", unaryFuncs.get(vBuiltIn.op.name)));
			} else {
				_null = _null || arg.contains("null pointer");
				oob = oob || arg.contains("out of bounds");
				shouldError = true;
				int count = _null && arg.contains("out of bounds") ? 1 : 0;
				builtIn.addAll(instr("la", "$a0", "_str" + count));
				builtIn.addAll(jump("j", unaryFuncs.get(name)));

			}
		} else {
			// Binary funcs
			String arg1 = vBuiltIn.args[0].toString();
			if(name.equals("HeapAllocZ")) {
				shouldHeapAlloc = true;
				String transfer = isRegister(arg1) ? "move" : "li";
				builtIn.addAll(instr(transfer, "$a0", arg1));
				builtIn.addAll(jump("jal", binaryFuncs.get(name)));
				builtIn.addAll(instr("move", dest.toString(), "$v0"));
			} else {
				String arg2 = vBuiltIn.args[1].toString();
				if(!isRegister(arg1) && !isRegister(arg2)) {
					int val = compute(name, arg1, arg2);
					builtIn.addAll(instr("li", dest.toString(), String.valueOf(val)));
				} else if(!isRegister(arg1)) {
					builtIn.addAll(instr("li", "$t9", arg1));
					builtIn.addAll(arithmetic(binaryFuncs.get(name), dest.toString(), "$t9", arg2));
				} else {
					if(name.equals("LtS") && isRegister(arg2)) {
						name = "LtSreg";
					}
					builtIn.addAll(arithmetic(binaryFuncs.get(name), dest.toString(), arg1, arg2));
				}
			}
		}
		return builtIn;
	}

	@Override
	public LinkedList<String> visit(VMemWrite vMemWrite) throws Throwable {
		LinkedList<String> memWrite = new LinkedList<>();
		VMemRef dest = vMemWrite.dest;
		VOperand src = vMemWrite.source;
		if(dest instanceof VMemRef.Stack) {
			// Stack
			VMemRef.Stack stack = ((VMemRef.Stack)dest);
			int idx = 4 * stack.index;
			String access = idx + "($sp)";
			if(isRegister(src.toString())) {
				memWrite.addAll(instr("sw", src.toString(), access));
			} else {
				memWrite.addAll(instr("li", "$t9", src.toString()));
				memWrite.addAll(instr("sw", "$t9", access));
			}
		} else {
			// Global
			int offset = ((VMemRef.Global)dest).byteOffset;
			String reg = ((VMemRef.Global)dest).base.toString();
			String access = offset + "(" + reg + ")";
			if(src.toString().contains(":")) {
				// Load address
				memWrite.addAll(instr("la", "$t9", src.toString().substring(1)));
				memWrite.addAll(instr("sw", "$t9", access));
			} else {
				String srcStr = src.toString();
				if(!isRegister(src.toString())) {
					memWrite.addAll(instr("li", "$t9", src.toString()));
					srcStr = "$t9";
				}
				memWrite.addAll(instr("sw", srcStr, access));
			}
		}
		return memWrite;
	}

	@Override
	public LinkedList<String> visit(VMemRead vMemRead) throws Throwable {
		LinkedList<String> memRead = new LinkedList<>();
		VVarRef dest = vMemRead.dest;
		VMemRef src = vMemRead.source;
		if(src instanceof VMemRef.Stack) {
			// Stack
			VMemRef.Stack stack = ((VMemRef.Stack)src);
			int idx = 4 * stack.index;
			String reg = stack.region.toString().contains("In") ? "($fp)" : "($sp)";
			memRead.addAll(instr("lw", dest.toString(), String.valueOf(idx) + reg));
		} else {
			// Global
			int offset = ((VMemRef.Global)src).byteOffset;
			String reg = ((VMemRef.Global)src).base.toString();
			String access = offset + "(" + reg + ")";
			memRead.addAll(instr("lw", dest.toString(), access));
		}
		return memRead;
	}
	@Override
	public LinkedList<String> visit(VBranch vBranch) throws Throwable {
		LinkedList<String> branch = new LinkedList<>();
		String target = vBranch.target.toString().substring(1);
		String cond = vBranch.value.toString();
		if(vBranch.positive) {
			// if
			branch.addAll(instr("bnez", cond, target));
		} else {
			// if0
			branch.addAll(instr("beqz", cond, target));
		}
		return branch;
	}
	@Override
	public LinkedList<String> visit(VGoto vGoto) throws Throwable {
		LinkedList<String> _goto = new LinkedList<>();
		_goto.addAll(jump("j", vGoto.target.toString().substring(1)));
		return _goto;
	}

	@Override
	public LinkedList<String> visit(VReturn vReturn) throws Throwable {
		LinkedList<String> ret = new LinkedList<>();
		return ret;
	}
}
