package optim;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class Optimizer5 extends ClassVisitor {
    public Optimizer5(ClassVisitor cv) {
        super(ASM9, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv;

        mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new ReplaceWithDup(mv);
        }
        return mv;
    }


    private class ReplaceWithDup extends MethodVisitor {
        protected final static int SEEN_NOTHING = 0;
        protected final static int SEEN_ILOAD = 1;
        protected final static int SEEN_ILOAD_ICONST = 2;
        protected final static int SEEN_ILOAD_ICONST_IFICMPNE = 3;
        protected int state;
        protected int x;
        protected Label lbl;
        protected String typeOfLocal;

        public ReplaceWithDup(MethodVisitor mv) {
            super(ASM9, mv);
        }

        public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
            visitInsn();
            mv.visitFrame(type, numLocal, local, numStack, stack);
        }

        @Override
        public void visitInsn(int opcode) {
            switch (state) {
                case SEEN_ILOAD: {
                    if (opcode == ICONST_1){
                        state = SEEN_ILOAD_ICONST;
                        return;
                    }
                    break;
                }
            }
            visitInsn();
            mv.visitInsn(opcode);
        }

        protected void visitInsn() {
            switch (state) {
                case SEEN_ILOAD_ICONST_IFICMPNE: {
                    mv.visitJumpInsn(IFEQ, lbl);
                    break;
                }
            }
            state = SEEN_NOTHING;
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            visitInsn();
            mv.visitIntInsn(opcode, operand);
        }

        @Override
        public void visitVarInsn(int opcode, int varIndex) {
            switch (state) {
                case SEEN_NOTHING: {
                    if (opcode == ILOAD) {
                        state = SEEN_ILOAD;
                        x = varIndex;
                        mv.visitVarInsn(opcode, varIndex);
                        return;
                    }
                    break;
                }
            }
            visitInsn();
            mv.visitVarInsn(opcode, varIndex);
        }

        @Override
        public void visitTypeInsn(int opcode, String desc) {
            visitInsn();
            mv.visitTypeInsn(opcode, desc);
        }

        @Override
        public void visitFieldInsn(int opc, String owner, String name, String desc) {
            visitInsn();
            mv.visitFieldInsn(opc, owner, name, desc);
        }

        @Override
        public void visitMethodInsn(int opc, String owner, String name, String desc) {
            visitInsn();
            mv.visitMethodInsn(opc, owner, name, desc);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            visitInsn();
            mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            switch (state) {
                case SEEN_ILOAD_ICONST: {
                    if (opcode == IF_ICMPNE) {
                        state = SEEN_ILOAD_ICONST_IFICMPNE;
                        lbl = label;
                        visitInsn();
                        return;
                    }
                    break;
                }
            }
            visitInsn();
            mv.visitJumpInsn(opcode, label);
        }

        public void visitLabel(Label label) {
            visitInsn();
            mv.visitLabel(label);
        }

        public void visitLdcInsn(Object cst) {
            visitInsn();
            mv.visitLdcInsn(cst);
        }

        public void visitIincInsn(int var, int increment) {
            visitInsn();
            mv.visitIincInsn(var, increment);
        }

        public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
//            System.out.println("Local variable: Name=" + name + ", Type=" + descriptor + ", Index=" + index);
            visitInsn();
            mv.visitLocalVariable(name, descriptor, signature, start, end, index);
        }
    }
}
