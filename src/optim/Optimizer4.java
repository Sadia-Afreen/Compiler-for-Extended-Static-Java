package optim;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import javax.naming.BinaryRefAddr;

import static org.objectweb.asm.Opcodes.*;

public class Optimizer4 extends ClassVisitor {
    public Optimizer4(ClassVisitor cv) {
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
        protected final static int SEEN_ILOAD_ILOAD = 2;
        protected final static int SEEN_ALOAD = 3;
        protected final static int SEEN_ALOAD_GETFIELD = 5;
        protected final static int SEEN_ALOAD_GETFIELD_ALOAD = 6;
        protected final static int SEEN_ALOAD_GETFIELD_ALOAD_GETFIELD = 7;
        protected int state;
        protected int x;
        protected String fieldOwner;
        protected String fieldName;
        protected String fieldDesc;


        public ReplaceWithDup(MethodVisitor mv) {
            super(ASM9, mv);
        }

        public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
            visitInsn();
            mv.visitFrame(type, numLocal, local, numStack, stack);

        }

        @Override
        public void visitInsn(int opcode) {
            visitInsn();
            mv.visitInsn(opcode);
        }

        protected void visitInsn() {
            switch (state) {
                case SEEN_ILOAD_ILOAD:
                case SEEN_ALOAD_GETFIELD_ALOAD_GETFIELD: {
                    mv.visitInsn(DUP);
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
                    if (opcode == ALOAD) {
                        state = SEEN_ALOAD;
                        x = varIndex;
                        mv.visitVarInsn(opcode, varIndex);
                        return;
                    }
                    break;
                }
                case SEEN_ILOAD: {
                    if (opcode == ILOAD && varIndex == x) {
                        state = SEEN_ILOAD_ILOAD;
                        visitInsn();
                        return;
                    }
                    else if (opcode == ILOAD && varIndex != x) {
                        state = SEEN_ILOAD;
                        x = varIndex;
                        mv.visitVarInsn(opcode, varIndex);
                        return;
                    }
                    break;
                }
                case SEEN_ALOAD: {
                    state = SEEN_ALOAD;
                    x = varIndex;
                    mv.visitVarInsn(opcode, varIndex);
                    return;
                }
                case SEEN_ALOAD_GETFIELD: {
                    if (opcode == ALOAD && varIndex == x) {
                        state = SEEN_ALOAD_GETFIELD_ALOAD;
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
            switch (state) {
                case SEEN_ALOAD: {
                    if (opc == GETFIELD) {
                        state = SEEN_ALOAD_GETFIELD;
                        fieldOwner = owner;
                        fieldName = name;
                        fieldDesc = desc;
                        mv.visitFieldInsn(opc, owner, name, desc);
                        return;
                    }
                    break;
                }
                case SEEN_ALOAD_GETFIELD_ALOAD: {
                    if (opc == GETFIELD && owner.equals(fieldOwner) && name.equals(fieldName) && desc.equals(fieldDesc)){
                        state = SEEN_ALOAD_GETFIELD_ALOAD_GETFIELD;
                        visitInsn();
                        return;
                    }
                    break;
                }
            }
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
    }
}
