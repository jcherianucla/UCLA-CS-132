import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.parser.VaporParser;

import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.util.Arrays;

import static java.lang.System.in;

public class V2VM {
    
    public static void main (String [] args) throws Throwable {
        Op[] ops = {
                Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS, Op.PrintIntS,
                Op.HeapAllocZ, Op.Error
        };
        boolean allowLocals = true;
        String[] registers = null;
        boolean allowStack = false;

        VaporProgram program;
        try {
            program = VaporParser.run(new InputStreamReader(in), 1, 1,
                    Arrays.asList(ops), allowLocals, registers, allowStack);
        } catch (ProtocolException e) {
            return;
        }
    }

}
