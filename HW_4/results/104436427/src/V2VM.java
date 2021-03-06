import cs132.util.ProblemException;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.parser.VaporParser;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;

import static java.lang.System.in;

public class V2VM {

    /**
     * Runs through all the vaporm code and prints them.
     */
    public static void printVaporM(LinkedList<String> vaporm) {
        for(String line : vaporm) {
            System.out.println(line);
        }
    }

    public static void main (String [] args) throws Throwable {
        Op[] ops = {
                Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS, Op.PrintIntS,
                Op.HeapAllocZ, Op.Error
        };
        boolean allowLocals = true;
        String[] registers = null;
        boolean allowStack = false;

        VaporProgram program;
        // Parse program
        try {
            program = VaporParser.run(new InputStreamReader(in), 1, 1,
                    Arrays.asList(ops), allowLocals, registers, allowStack);
        } catch (ProblemException e) {
            return;
        }
        // Translate the vapor to vaporm
        TranslatorVisitor translator = new TranslatorVisitor(program);
        // Output vaporm
        printVaporM(translator.vaporm);
    }

}
