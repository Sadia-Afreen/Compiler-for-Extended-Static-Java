package esjc.type.checker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import esjc.type.*;
import esjc.type.ArrayType;
import esjc.type.Type;
import org.eclipse.jdt.core.dom.*;

import esjc.symboltable.SymbolTable;
import esjc.symboltable.ExtendedSymbolTable;
import esjc.util.Pair;

/**
 * This class is used to type check a StaticJava {@link CompilationUnit} with a
 * given {@link SymbolTable}.
 *
 * @author <a href="mailto:robby@cis.ksu.edu">Robby</a>
 */
public class ExtendedTypeChecker extends TypeChecker {
    /**
     * The visitor for {@link ASTNode} to type check an ExtendedStaticJava
     * {@link CompilationUnit}.
     *
     * @author <a href="mailto:robby@cis.ksu.edu">Robby</a>
     */
    protected static class Visitor extends TypeChecker.Visitor {
        protected Map<String, TypeDeclaration> classMap;

        protected Map<Pair<String, String>, FieldDeclaration> fieldMap;

        protected Visitor(final TypeFactory tf, final ExtendedSymbolTable est) {
            super(tf, est);
            this.classMap = est.classMap;
            this.fieldMap = est.fieldMap;
            for (final Pair<String, String> p : est.fieldMap.keySet()) {
                final FieldDeclaration fd = est.fieldMap.get(p);
                final Type t = convertType(fd, fd.getType());
                final String className = p.first;
                final String fieldName = p.second;
                final ClassType ct = tf.getClassType(className);
                ct.fieldTypeMap.put(fieldName, t);
            }
        }

        protected void typeCheckMethodInvocation(final MethodInvocation node,
                                                 final String className, final String methodName, final Type[] argTypes,
                                                 final Method m) {
            @SuppressWarnings("rawtypes")
            final Class[] paramTypeClasses = m.getParameterTypes();
            final int numOfParams = paramTypeClasses.length;
            if (argTypes.length != numOfParams) {
                throw new Error(node, "Wrong number of arguments to invoke method \""
                        + methodName + "\" in \"" + node + "\"");
            }
            final List<Type> paramTypes = new ArrayList<>();
            for (int i = 0; i < numOfParams; i++) {
                final Type t = convertType(node, paramTypeClasses[i]);
                if ((argTypes[i] instanceof NullType) && (t instanceof NonPrimitiveType)){

                }
                else if (t != argTypes[i]) {
                    throw new Error(node, "Type mismatch the " + i + " argument in \""
                            + node + "\"");
                }
                paramTypes.add(t);
            }
            final Type returnType = convertType(node, m.getReturnType());
            if (!this.resultMethodTypeMap.containsKey(m)) {
                this.resultMethodTypeMap.put(m, new Pair<>(returnType,
                        paramTypes));
            }
            setResult(node, returnType);
        }

        protected void typeCheckMethodInvocation(final MethodInvocation node,
                                                 final String className, final String methodName, final Type[] argTypes,
                                                 final MethodDeclaration md) {
            final int numOfParams = md.parameters().size();
            if (argTypes.length != numOfParams) {
                throw new Error(node, "Wrong number of arguments to invoke method \""
                        + methodName + "\" in \"" + node + "\"");
            }
            for (int i = 0; i < numOfParams; i++) {
                final Type t = convertType(node, ((SingleVariableDeclaration) md
                        .parameters().get(i)).getType());
                if ((argTypes[i] instanceof NullType) && (t instanceof NonPrimitiveType)){

                }
                else if (t != argTypes[i]) {
                    throw new Error(node, "Type mismatch the " + i + " argument in \""
                            + node + "\"");
                }
            }
            final Type returnType = convertType(node, md.getReturnType2());
            setResult(node, returnType);
        }

        @Override
        public boolean visit(final InfixExpression node) {
            node.getLeftOperand().accept(this);
            final Type lhsType = getResult();
            node.getRightOperand().accept(this);
            final Type rhsType = getResult();
            final InfixExpression.Operator op = node.getOperator();
            if ((op == InfixExpression.Operator.TIMES)
                    || (op == InfixExpression.Operator.DIVIDE)
                    || (op == InfixExpression.Operator.REMAINDER)
                    || (op == InfixExpression.Operator.PLUS)
                    || (op == InfixExpression.Operator.MINUS)) {
                if (lhsType != this.tf.Int) {
                    throw new Error(node,
                            "Expecting an int type expression as the left-hand operand of \""
                                    + op + "\" in \"" + node + "\"");
                }
                if (rhsType != this.tf.Int) {
                    throw new Error(node,
                            "Expecting an int type expression as the right-hand operand of \""
                                    + op + "\" in \"" + node + "\"");
                }
                setResult(node, this.tf.Int);
            } else if ((op == InfixExpression.Operator.LESS)
                    || (op == InfixExpression.Operator.GREATER)
                    || (op == InfixExpression.Operator.LESS_EQUALS)
                    || (op == InfixExpression.Operator.GREATER_EQUALS)) {
                if (lhsType != this.tf.Int) {
                    throw new Error(node,
                            "Expecting an int type expression as the left-hand operand of \""
                                    + op + "\" in \"" + node + "\"");
                }
                if (rhsType != this.tf.Int) {
                    throw new Error(node,
                            "Expecting an int type expression as the right-hand operand of \""
                                    + op + "\" in \"" + node + "\"");
                }
                setResult(node, this.tf.Boolean);
            } else if ((op == InfixExpression.Operator.CONDITIONAL_AND)
                    || (op == InfixExpression.Operator.CONDITIONAL_OR)) {
                if (lhsType != this.tf.Boolean) {
                    throw new Error(node,
                            "Expecting a boolean type expression as the left-hand operand of \""
                                    + op + "\" in \"" + node + "\"");
                }
                if (rhsType != this.tf.Boolean) {
                    throw new Error(node,
                            "Expecting a boolean type expression as the right-hand operand of \""
                                    + op + "\" in \"" + node + "\"");
                }
                setResult(node, this.tf.Boolean);
            } else if ((op == InfixExpression.Operator.EQUALS)
                    || (op == InfixExpression.Operator.NOT_EQUALS)) {
                if ((lhsType != rhsType) && !(((lhsType instanceof ClassType) && rhsType == this.tf.Null)
                        || ((rhsType instanceof ClassType) && lhsType == this.tf.Null)
                        || ((lhsType instanceof ArrayType) && rhsType == this.tf.Null)
                        || ((rhsType instanceof ArrayType) && lhsType == this.tf.Null)
                        )){
                    throw new Error(node, "Type mismatch in \"" + node + "\": " + lhsType
                            + " " + op + " " + rhsType);
                }
                setResult(node, this.tf.Boolean);
            }

            else if ((op == InfixExpression.Operator.LEFT_SHIFT)
                    || (op == InfixExpression.Operator.RIGHT_SHIFT_SIGNED)
                    || (op == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED)) {
                if (lhsType != rhsType) {
                    throw new Error(node, "Type mismatch in \"" + node + "\": " + lhsType
                            + " " + op + " " + rhsType);
                }
                setResult(node, this.tf.Int);
            } else {
                throw new Error(node, "Unexpected InfixExpression: \'" + node + "\'");
            }

            return false;
        }

        @Override
        public boolean visit(final Assignment node) {
            node.getLeftHandSide().accept(this);
            final Type lhsType = getResult();
            node.getRightHandSide().accept(this);
            final Type rhsType = getResult();
            if ((lhsType != rhsType) && ((lhsType instanceof ClassType) && rhsType == this.tf.Null)){

            }
            else if (lhsType != rhsType) {
                throw new Error(node, "Type mismatch in \"" + node + "\": " + lhsType
                        + " = " + rhsType);
            }
            // no need to set the type result for assignments since
            // assignments in StaticJava are statements,
            // i.e., they are evaluated for their side-effects.
            return false;
        }

        @Override
        public boolean visit(final ExpressionStatement node) {
            final Expression e = node.getExpression();
            e.accept(this);
            if (e instanceof Assignment) {
                // assignment should not have a resulting type.
                assert getResult() == null;
            } else if (node.getExpression() instanceof MethodInvocation) {
                // method invocation's result can be any type (including void)
                // so we can ignore it.
                getResult();
            } else if (node.getExpression() instanceof PostfixExpression) {
                node.getExpression().accept(this);
                if (getResult() != this.tf.Int) {
                    throw new Error(node,
                            "Expecting an int type expression");
                }
            } else {
                throw new Error(node, "Unexpected SimpleName: \'" + node + "\'");
            }
            return false;
        }

        @Override
        public boolean visit(final PrefixExpression node) {
            node.getOperand().accept(this);
            final Type t = getResult();
            final PrefixExpression.Operator op = node.getOperator();
            if ((op == PrefixExpression.Operator.PLUS)
                    || (op == PrefixExpression.Operator.MINUS)
                    || (op == PrefixExpression.Operator.COMPLEMENT)) {
                if (t != this.tf.Int) {
                    throw new Error(node,
                            "Expecting an int type expression as the operand of \"" + op
                                    + "\" in \"" + node + "\"");
                }
                setResult(node, this.tf.Int);
            } else if (op == PrefixExpression.Operator.NOT) {
                if (t != this.tf.Boolean) {
                    throw new Error(node,
                            "Expecting a boolean type expression as the operand of \"" + op
                                    + "\" in \"" + node + "\"");
                }
                setResult(node, this.tf.Boolean);
            } else {
                throw new Error(node, "Unexpected PrefixExpression: \'" + node + "\'");
            }
            return false;
        }

        @Override
        public boolean visit(final ClassInstanceCreation node) {
            final Type t = convertType(node, node.getType());
            setResult(node, t);
            this.symbolMap.put(node, this.classMap.get(node.getType().toString()));
            return false;
        }

        @Override
        public boolean visit(final ConditionalExpression node) {
            if (node.getExpression() != null) {
                node.getExpression().accept(this);
                if (getResult() != this.tf.Boolean) {
                    throw new Error(node, "Condition not of type boolean: " + node);
                }
            }
            node.getThenExpression().accept(this);
            final Type thenExp = getResult();

            node.getElseExpression().accept(this);
            final Type elseExp = getResult();

            if (thenExp != elseExp) {
                if (!((thenExp instanceof ClassType && elseExp == this.tf.Null)
                        || (elseExp instanceof ClassType && thenExp == this.tf.Null)
                        || (thenExp instanceof ArrayType && elseExp == this.tf.Null)
                        || (elseExp instanceof ArrayType && thenExp == this.tf.Null))) {
                    throw new Error(node, "Conditional Then and Else expression type mismatch: " + node);
                }
            }
            if (thenExp == this.tf.Null)
                setResult(node, elseExp);
            else if (elseExp == this.tf.Null)
                setResult(node, thenExp);
            else
                setResult(node, thenExp);
            return false;
        }

        @Override
        public boolean visit(final FieldAccess node) {
            node.getExpression().accept(this);
            final ClassType exp = (ClassType) getResult();
            final Type id = exp.fieldTypeMap.get(node.getName().toString());
            final Pair<String, String> key = new Pair<>(exp.toString(), node.getName().toString());
            final FieldDeclaration fd = this.fieldMap.get(key);

            setResult(node, id);
            this.symbolMap.put(node, fd);
            return false;
        }

        @Override
        public boolean visit(final NullLiteral node) {
            setResult(node, this.tf.Null);
            return false;
        }
        @Override
        public boolean visit(final ArrayCreation node) {
            ArrayInitializer arrayinit = node.getInitializer();
            if (arrayinit != null) {
                List<Expression> expressions = arrayinit.expressions();
                Type element = convertType(node, node.getType().getElementType());
                for (int index = 0; index < expressions.size(); index++) {
                    Expression exp = expressions.get(index);
                    exp.accept(this);
                    Type t = getResult();
                    if (t != element) {
                        if (!(element instanceof ClassType && exp instanceof NullLiteral)) {
                            throw new Error(node, "Array initialization error: " + node);
                        }
                    }
                }
            }

            List<Expression> content = node.dimensions();
            for (int i = 0; i < content.size(); i++) {
                Expression exp = content.get(i);
                exp.accept(this);

                Type t = getResult();
                if (t != tf.Int) {
                    throw new Error(node, "Array size type mismatch: " + node.dimensions());
                }
            }

            Type t = convertType(node, node.getType());
            setResult(node, t);
            return false;
        }

        @Override
        public boolean visit(final ArrayAccess node) {
            node.getIndex().accept(this);
            if (getResult() != this.tf.Int) {
                throw new Error(node, "Array Index not of type int: " + node);
            }
            node.getArray().accept(this);
            setResult(node, ((ArrayType)getResult()).baseType);
            return false;
        }

        @Override
        public boolean visit(final ReturnStatement node) {
            final Expression exp = node.getExpression();

            if ((this.methodReturnType == this.tf.Void) && (exp != null)) {
                throw new Error(node, "Return type should be void: " + node);
            } else if ((this.methodReturnType != this.tf.Void) && (exp == null)) {
                throw new Error(node, "Missing return statement: " + node);
            } else if ((this.methodReturnType != this.tf.Void) && (exp != null)) {
                exp.accept(this);
                final Type t = getResult();
                if (t != this.methodReturnType
                        && !(((this.methodReturnType instanceof ArrayType) && t == this.tf.Null)
                        || ((this.methodReturnType instanceof ClassType) && t == this.tf.Null))) {
                    throw new Error(node, "Incorrect return type: " + node);
                }
            }
            return false;
        }


        @Override
        protected Type convertType(final ASTNode node,
                                   final org.eclipse.jdt.core.dom.Type t) {
            if (t instanceof SimpleType) {
                final SimpleType st = (SimpleType) t;
                final String name = st.getName().getFullyQualifiedName();
                if (this.classMap.containsKey(name)) {
                    return this.tf.getClassType(name);
                }
            }
            return super.convertType(node, t);
        }

        @Override
        protected void dispose() {
            super.dispose();

            this.classMap = null;
            this.fieldMap = null;
        }
    }



    /**
     * Type checks an ExtendedStaticJava {@link CompilationUnit} with the given
     * {@link ExtendedSymbolTable} and the given {@link TypeFactory}. It also
     * resolves {@link MethodInvocation} of library call (and put its mapping in
     * the {@link ExtendedSymbolTable}).
     *
     * @param tf          The {@link TypeFactory}.
     * @param cu          The StaticJava {@link CompilationUnit}.
     * @param symbolTable The {@link ExtendedSymbolTable} of the {@link CompilationUnit}
     * @return The {@link ExtendedTypeTable}.
     * @throws Error If the type checker encounter type error in the
     *               {@link CompilationUnit}.
     */
    public static ExtendedTypeTable check(final TypeFactory tf,
                                          final CompilationUnit cu,
                                          final ExtendedSymbolTable symbolTable) throws Error {
        assert (tf != null) && (cu != null) && (symbolTable != null);

        final Visitor v = new Visitor(tf, symbolTable);
        cu.accept(v);
        final ExtendedTypeTable result = new ExtendedTypeTable(v.resultTypeMap,
                v.resultMethodTypeMap);
        v.dispose();
        return result;
    }

    /**
     * Declared as protected to disallow creation of this object outside from the
     * methods of this class.
     */
    protected ExtendedTypeChecker() {

    }




}
