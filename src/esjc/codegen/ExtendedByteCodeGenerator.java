package esjc.codegen;

import java.util.HashMap;
import java.util.Map;

import esjc.type.BooleanType;
import esjc.type.IntType;
import esjc.type.Type;
import org.eclipse.jdt.core.dom.*;


import esjc.symboltable.ExtendedSymbolTable;
import esjc.type.checker.ExtendedTypeTable;
import esjc.util.Pair;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.DUP;

/**
 * This class is used to translate an ExtendedStaticJava {@link CompilationUnit}
 * to {@link ExtendedClassByteCodes} that represent a Java 1.5 class files.
 * 
 * @author <a href="mailto:robby@cis.ksu.edu">Robby</a>
 */
public class ExtendedByteCodeGenerator extends ByteCodeGenerator {
  /**
   * The visitor for {@link ASTNode} to generate bytecodes.
   * 
   * @author <a href="mailto:robby@cis.ksu.edu">Robby</a>
   */
  protected static class Visitor extends ByteCodeGenerator.Visitor {
    public
    Map<String, byte[]> otherClasses = new HashMap<String, byte[]>();

    protected Map<String, TypeDeclaration> classMap;

    protected Map<Pair<String, String>, FieldDeclaration> fieldMap;

    protected Visitor(final ExtendedSymbolTable st,
         final ExtendedTypeTable tt) {
      super(st, tt);
      this.classMap = st.classMap;
      this.fieldMap = st.fieldMap;
    }


    // Postfix Expression (Inc-Dec)
    @Override
    public boolean visit(PostfixExpression node){
      Expression e = node.getOperand();
      if (e instanceof SimpleName){
        SimpleName sn = (SimpleName) e;
        String name = sn.getIdentifier();
        int indexVar = localIndexMap.get(name);
        int incVal = 1;
        if(node.getOperator() == PostfixExpression.Operator.DECREMENT){
          incVal = -1;
        }
        mv.visitIincInsn(indexVar, incVal);
      }
      else {
        System.out.println(e);
        throw new Error(node, "cannot handle it yet");
      }
      return false;
    }

    //Do-While Statement
    @Override
    public boolean visit(DoStatement node){
      Label enterLoop = new Label();
      mv.visitLabel(enterLoop);
      node.getBody().accept(this);
      node.getExpression().accept(this);
      mv.visitJumpInsn(Opcodes.IFNE, enterLoop);
      return false;
    }

    //Field Access
    @Override
    public boolean visit(FieldAccess node){
      node.getExpression().accept(this);
      FieldDeclaration fd = (FieldDeclaration) symbolMap.get(node);
      String owner = ((TypeDeclaration) fd.getParent()).getName().getIdentifier();
      Type t = typeMap.get(node);
      mv.visitFieldInsn(Opcodes.GETFIELD, owner, node.getName().getIdentifier(), convertType(t));
      return false;
    }

    //New
    @Override
    public boolean visit(ClassInstanceCreation node){
      Type t = this.typeMap.get(node);
      this.mv.visitTypeInsn(Opcodes.NEW, t.name);
      this.mv.visitInsn(DUP);
      this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, t.name, "<init>",
              "()V", false);
      return false;
    }

    //Array Access
    @Override
    public boolean visit(final ArrayAccess node) {
      Type t = this.typeMap.get(node);
      Expression exp = node.getArray();

      if (node.getArray() instanceof MethodInvocation) {
        node.getArray().accept(this);
      } else {
        this.mv.visitVarInsn(Opcodes.ALOAD,
                this.localIndexMap.get(exp.toString()));
      }
      node.getIndex().accept(this);
      if (t instanceof IntType) {
        this.mv.visitInsn(Opcodes.IALOAD);
      } else if (t instanceof BooleanType) {
        this.mv.visitInsn(Opcodes.BALOAD);
      } else {
        this.mv.visitInsn(Opcodes.AALOAD);
      }
      return false;
    }

    //Assignment
    @Override
    public boolean visit(final Assignment node) {
      final ASTNode lhsNode = node.getLeftHandSide();
      if (lhsNode instanceof ArrayAccess) {
        final ArrayAccess lhsAA = (ArrayAccess) lhsNode;
        Type t = this.typeMap.get(lhsAA);
        this.mv.visitVarInsn(Opcodes.ALOAD,
                this.localIndexMap.get(lhsAA.getArray().toString()));
        lhsAA.getIndex().accept(this);
        node.getRightHandSide().accept(this);
        if (t instanceof IntType) {
          this.mv.visitInsn(Opcodes.IASTORE);
        } else if (t instanceof BooleanType) {
          this.mv.visitInsn(Opcodes.BASTORE);
        } else {
          this.mv.visitInsn(Opcodes.AASTORE);
        }
      }
      else if (lhsNode instanceof FieldAccess) {
        final FieldAccess lhsFA = (FieldAccess) lhsNode;
        lhsFA.getExpression().accept(this);
        node.getRightHandSide().accept(this);
        final Type t = this.typeMap.get(lhsFA.getExpression());
        this.mv.visitFieldInsn(Opcodes.PUTFIELD, t.name, lhsFA
                .getName().getIdentifier(), convertType(this.typeMap
                .get(lhsFA)));
      } else {
        node.getRightHandSide().accept(this);
        final String varName = ((SimpleName) lhsNode).getIdentifier();
        final Object lhsDecl = this.symbolMap.get(lhsNode);
        if (lhsDecl instanceof FieldDeclaration) {
          final FieldDeclaration fd = (FieldDeclaration) lhsDecl;
          final String className = ((TypeDeclaration) fd.getParent())
                  .getName().getIdentifier();
          this.mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
                  varName, convertType(this.typeMap.get(fd)));
        } else {
          Type t = this.typeMap.get(lhsDecl);
          if (t instanceof BooleanType || t instanceof IntType) {
            this.mv.visitVarInsn(Opcodes.ISTORE, this.localIndexMap
                    .get(varName));
          } else {
            this.mv.visitVarInsn(Opcodes.ASTORE, this.localIndexMap
                    .get(varName));
          }
        }
      }
      return false;
    }

    //Array Creation
    @Override
    public boolean visit(final ArrayCreation node) {

      Type t = this.typeMap.get(node);
      esjc.type.ArrayType arr = (esjc.type.ArrayType) t;
//      ArrayType arr = (ArrayType) t;
      Type base = arr.baseType;

      if (node.dimensions() != null) {
        for (int i = 0; i < node.dimensions().size(); i++) {
          ((ASTNode) node.dimensions().get(0)).accept(this);
          if (base instanceof IntType) {
            this.mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
          } else if (base instanceof BooleanType) {
            this.mv.visitIntInsn(Opcodes.NEWARRAY,
                    Opcodes.T_BOOLEAN);
          } else {
            this.mv.visitTypeInsn(Opcodes.ANEWARRAY, base.name);
          }
        }
      }
      if (node.getInitializer() != null) {
        final int initSize = node.getInitializer().expressions().size();
        generateIntConst(initSize);
        if (base instanceof IntType) {
          this.mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
        } else if (base instanceof BooleanType) {
          this.mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
        } else {
          this.mv.visitTypeInsn(Opcodes.ANEWARRAY, base.toString());
        }
        for (int i = 0; i < initSize; i++) {
          this.mv.visitInsn(Opcodes.DUP);
          generateIntConst(i);
          ((ASTNode) node.getInitializer().expressions().get(i))
                  .accept(this);
          if (base instanceof IntType) {
            this.mv.visitInsn(Opcodes.IASTORE);
          } else if (base instanceof BooleanType) {
            this.mv.visitInsn(Opcodes.BASTORE);
          } else {
            this.mv.visitInsn(Opcodes.AASTORE);
          }
        }
      }
      return false;
    }

    private boolean hasMainMethod(MethodDeclaration[] methods) {
        for (MethodDeclaration method : methods) {
            if (method.toString().contains("main"))
                return true;
        }
      return false;
    }

    //Type Declaration
    @Override
    public boolean visit(final TypeDeclaration node) {
      if (hasMainMethod(node.getMethods()) == true) {
        this.mainClassName = node.getName().getIdentifier();
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES
                | ClassWriter.COMPUTE_MAXS);
        this.cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC
                        + Opcodes.ACC_SUPER, this.mainClassName, null,
                "java/lang/Object", null);
        this.cw.visitSource(null, null);
        generateConstructor(this.mainClassName);
        for (final Object o : node.bodyDeclarations()) {
          ((ASTNode) o).accept(this);
        }
        this.cw.visitEnd();
        this.mainClassBytes = this.cw.toByteArray();
        this.cw = null;
      }
      else {
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES
                | ClassWriter.COMPUTE_MAXS);
        this.cw.visit(Opcodes.V1_5, 0, node.getName().getIdentifier(),
                null, "java/lang/Object", null);
        this.cw.visitSource(null, null);
        generateConstructor(node.getName().getIdentifier());
        for (final Object o : node.bodyDeclarations()) {
          ((ASTNode) o).accept(this);
        }
        this.cw.visitEnd();
        this.otherClasses.put(node.getName().getIdentifier(),
                this.cw.toByteArray());
        this.cw = null;
      }

      return false;
    }

    //Null Literal
    @Override
    public boolean visit(final NullLiteral node) {
      this.mv.visitInsn(Opcodes.ACONST_NULL);
      return false;
    }

    //Return
    @Override
    public boolean visit(final ReturnStatement node) {
      final Expression exp = node.getExpression();
      if (exp == null)
        this.mv.visitInsn(Opcodes.RETURN);
      else {
        Type t = this.typeMap.get(exp);
        exp.accept(this);
        // primitive return
        if (t instanceof IntType || t instanceof BooleanType)
          this.mv.visitInsn(Opcodes.IRETURN);
          // everything else
        else
          this.mv.visitInsn(Opcodes.ARETURN);
      }

      return false;
    }

    //Infix extended
    @Override
    public boolean visit(final InfixExpression node) {
      final InfixExpression.Operator op = node.getOperator();
      node.getLeftOperand().accept(this);
      final Type t = this.typeMap.get(node.getLeftOperand());
      if (op == InfixExpression.Operator.CONDITIONAL_AND) {
        this.mv.visitInsn(Opcodes.DUP);
        final Label falseLabel = new Label();
        this.mv.visitJumpInsn(Opcodes.IFEQ, falseLabel);
        this.mv.visitInsn(Opcodes.POP);
        node.getRightOperand().accept(this);
        this.mv.visitLabel(falseLabel);
      } else if (op == InfixExpression.Operator.CONDITIONAL_OR) {
        this.mv.visitInsn(Opcodes.DUP);
        final Label trueLabel = new Label();
        this.mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
        this.mv.visitInsn(Opcodes.POP);
        node.getRightOperand().accept(this);
        this.mv.visitLabel(trueLabel);
      } else {
        node.getRightOperand().accept(this);
        if (op == InfixExpression.Operator.PLUS) {
          this.mv.visitInsn(Opcodes.IADD);
        } else if (op == InfixExpression.Operator.MINUS) {
          this.mv.visitInsn(Opcodes.ISUB);
        } else if (op == InfixExpression.Operator.TIMES) {
          this.mv.visitInsn(Opcodes.IMUL);
        } else if (op == InfixExpression.Operator.DIVIDE) {
          this.mv.visitInsn(Opcodes.IDIV);
        } else if (op == InfixExpression.Operator.REMAINDER) {
          this.mv.visitInsn(Opcodes.IREM);
        } else if (op == InfixExpression.Operator.GREATER) {
          generateRelationalCode(Opcodes.IF_ICMPGT);
        } else if (op == InfixExpression.Operator.GREATER_EQUALS) {
          generateRelationalCode(Opcodes.IF_ICMPGE);
        } else if (op == InfixExpression.Operator.LESS) {
          generateRelationalCode(Opcodes.IF_ICMPLT);
        } else if (op == InfixExpression.Operator.LESS_EQUALS) {
          generateRelationalCode(Opcodes.IF_ICMPLE);
        }
        else if (op == InfixExpression.Operator.EQUALS
                && !(t instanceof IntType || t instanceof BooleanType)) {
          generateRelationalCode(Opcodes.IF_ACMPEQ);
        } else if (op == InfixExpression.Operator.EQUALS) {
          generateRelationalCode(Opcodes.IF_ICMPEQ);
        } else if (op == InfixExpression.Operator.NOT_EQUALS
                && !(t instanceof IntType || t instanceof BooleanType)) {
          generateRelationalCode(Opcodes.IF_ACMPNE);
        } else if (op == InfixExpression.Operator.NOT_EQUALS) {
          generateRelationalCode(Opcodes.IF_ICMPNE);
        }
        else if (op == InfixExpression.Operator.LEFT_SHIFT) {
          this.mv.visitInsn(Opcodes.ISHL);
        } else if (op == InfixExpression.Operator.RIGHT_SHIFT_SIGNED) {
          this.mv.visitInsn(Opcodes.ISHR);
        } else if (op == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
          this.mv.visitInsn(Opcodes.IUSHR);
        }
      }
      return false;
    }

    //Prefix Expression
    @Override
    public boolean visit(final PrefixExpression node) {
      node.getOperand().accept(this);
      final PrefixExpression.Operator op = node.getOperator();
      if (op == PrefixExpression.Operator.PLUS) {
        // do nothing
      } else if (op == PrefixExpression.Operator.MINUS) {
        this.mv.visitInsn(Opcodes.INEG);
      } else if (op == PrefixExpression.Operator.NOT) {
        final Label falseLabel = new Label();
        final Label endLabel = new Label();
        this.mv.visitJumpInsn(Opcodes.IFEQ, falseLabel);
        this.mv.visitInsn(Opcodes.ICONST_0);
        this.mv.visitJumpInsn(Opcodes.GOTO, endLabel);
        this.mv.visitLabel(falseLabel);
        this.mv.visitInsn(Opcodes.ICONST_1);
        this.mv.visitLabel(endLabel);
      }
      // Added complement
      else if (op == PrefixExpression.Operator.COMPLEMENT) {
        this.mv.visitInsn(Opcodes.ICONST_M1);
        this.mv.visitInsn(Opcodes.IXOR);
      }
      return false;
    }

    //For
    @Override
    public boolean visit(final ForStatement node) {
      Label loop = new Label();
      if (!node.initializers().isEmpty()) {
        for (int i = 0; i < node.initializers().size(); i++)
          ((ASTNode) node.initializers().get(i)).accept(this);
      }
      if (node.getExpression() != null && node.getBody() != null) {
        Label end = new Label();
        this.mv.visitJumpInsn(Opcodes.GOTO, end);
        this.mv.visitLabel(loop);

        node.getBody().accept(this);
        if (node.updaters() != null) {
          for (int i = 0; i < node.updaters().size(); i++)
            ((ASTNode) node.updaters().get(i)).accept(this);
        }
        this.mv.visitLabel(end);
        if (node.getExpression() != null) {
          node.getExpression().accept(this);
          this.mv.visitJumpInsn(Opcodes.IFNE, loop);
        }
        else {
          this.mv.visitJumpInsn(Opcodes.GOTO, loop);
        }
      } else {
        this.mv.visitLabel(loop);
        node.getBody().accept(this);
        if (node.updaters() != null) {
          for (int i = 0; i < node.updaters().size(); i++)
            ((ASTNode) node.updaters().get(i)).accept(this);
        }
        if (node.getExpression() != null) {
          node.getExpression().accept(this);
          this.mv.visitJumpInsn(Opcodes.IFNE, loop);
        }
        else {
          this.mv.visitJumpInsn(Opcodes.GOTO, loop);
        }
      }
      return false;
    }

    @Override
    protected void dispose() {
      super.dispose();
      this.classMap = null;
      this.fieldMap = null;
    }

  }


  /**
   * Generates a {@link ExtendedClassByteCodes} that represents the class files
   * for the given ExtendedStaticJava {@link CompilationUnit} with the given
   * {@link ExtendedSymbolTable} and {@link ExtendedTypeTable}.
   * 
   * @param cu
   *          The StaticJava {@link CompilationUnit}.
   * @param est
   *          The {@link ExtendedSymbolTable} of the {@link CompilationUnit}.
   * @param ett
   *          The {@link ExtendedTypeTable} of the {@link CompilationUnit}.
   * @return The {@link ExtendedClassByteCodes} that represents the class files
   *         for the given ExtendedStaticJava {@link CompilationUnit} with the
   *         given {@link ExtendedSymbolTable} and {@link ExtendedTypeTable}.
   * @throws ByteCodeGenerator.Error
   *           If the generator encounter unexpected error.
   */
  public static ExtendedClassByteCodes generate( final CompilationUnit cu,
      final ExtendedSymbolTable est,
      final ExtendedTypeTable ett) throws ByteCodeGenerator.Error {
    assert (cu != null) && (est != null) && (ett != null);

    final Visitor v = new Visitor(est, ett);
    cu.accept(v);
    final ExtendedClassByteCodes result = new ExtendedClassByteCodes(
        v.mainClassName, v.mainClassBytes, v.otherClasses);
    v.dispose();
    return result;
  }

  /**
   * Declared as protected to disallow creation of this object outside from the
   * methods of this class.
   */
  protected ExtendedByteCodeGenerator() {
  }
}
