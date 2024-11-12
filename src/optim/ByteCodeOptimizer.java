package optim;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.ASM9;

public class ByteCodeOptimizer {
    public static void main(String[] args) throws IOException {
        ClassReader cr = new ClassReader("inputs.Optimization4");
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ClassVisitor(ASM9, cw){};
        cr.accept(cv,0);
        FileOutputStream stream = new FileOutputStream("./out/inputs/Optimization4.class");
        stream.write(cw.toByteArray());
    }

    public static byte[] optimize(String fileName) throws IOException {
        ClassReader cr = new ClassReader(fileName);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        //default visitor - uncomment it to see
        //the default bytecode produced by ASM
//        ClassVisitor cv = new ClassVisitor(ASM9, cw) {};
        ClassVisitor cv = new Optimizer1(cw);
//        ClassVisitor cv = new Optimizer2(cw);
//        ClassVisitor cv = new Optimizer3(cw);
//        ClassVisitor cv = new Optimizer4(cw);
//        ClassVisitor cv = new Optimizer5(cw);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}
