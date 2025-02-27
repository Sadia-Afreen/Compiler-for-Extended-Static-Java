package test;

import optim.ByteCodeOptimizer;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class ByteCodeOptimizerTest {

  class CustomClassLoader extends ClassLoader {

    public CustomClassLoader() {
      super(ByteCodeOptimizerTest.this.getClass().getClassLoader());
    }

    @SuppressWarnings("rawtypes")
    public Class loadClass(final String name, final byte[] bytecodes) {
      return defineClass(name, bytecodes, 0, bytecodes.length);
    }
  }

  static void outputBytecodes(final PrintWriter pw, final byte[] b) {
    final ClassReader cr = new ClassReader(b);
    final TraceClassVisitor tcv = new TraceClassVisitor(pw);
    cr.accept(tcv, ClassReader.SKIP_FRAMES);
    pw.flush();
  }

  @SuppressWarnings("unchecked")
  void testPass(final String filename) {
    final Object[] args = new Object[] { new String[] {} };
    try {
      final String filePath =  "./src/inputs/" + filename;

      String mainClassName = "inputs."+filename.split(".java")[0];
      System.out.println("main class:\t" + mainClassName);
      byte[] mainClassBytes = ByteCodeOptimizer.optimize(mainClassName);
      //get names of other classes
      //the file to be opened for reading
      FileInputStream fis=new FileInputStream(filePath);
      Scanner sc=new Scanner(fis);    //file to be scanned
      Map <String, byte[]> otherClasses = new HashMap<String, byte[]> ();
      //returns true if there is another line to read
      while(sc.hasNextLine())
      {
        String line = sc.nextLine().trim();
        String otherClassName = "";
        if(line.startsWith("class")){
          String[] result = line.split("\\s");
          otherClassName = "inputs."+result[1];
          System.out.println("other class:\t\t" + otherClassName);
          otherClasses.put(otherClassName, ByteCodeOptimizer.optimize(otherClassName));
        }
      }
      sc.close();     //closes the scanner

      //printing out bytecodes to stdout as strings
      final PrintWriter pw = new PrintWriter(System.out);
      ByteCodeOptimizerTest.outputBytecodes(pw, mainClassBytes);
      for (final byte[] bytecodes : otherClasses.values()) {
        ByteCodeOptimizerTest.outputBytecodes(pw, bytecodes);
      }

      final CustomClassLoader ccl = new CustomClassLoader();

      for (final String className : otherClasses.keySet()) {
        ccl.loadClass(className, otherClasses.get(className));
      }
      @SuppressWarnings("rawtypes")
      final Class c = ccl.loadClass(mainClassName, mainClassBytes);
      c.getMethod("main", new Class[] { String[].class }).invoke(null, args);
    } catch (final Exception e) {
      e.printStackTrace();
      Assert.assertTrue(e.getMessage(), false);
      throw new RuntimeException();
    }
  }

  @Test
  public void testAEEmptyTest() {
    testPass("AEEmptyTest.java");
  }

  @Test
  public void testArrayAccessVariable() {
    testPass("ArrayAccessVariable.java");
  }

  @Test
  public void testArrayCreation() {
    testPass("ArrayCreation.java");
  }

  @Test
  public void testArrayCreation2() {
    testPass("ArrayCreation2.java");
  }

  @Test
  public void testArrayIndex() {
    testPass("ArrayIndex.java");
  }

  @Test
  public void testArrayIndexConstant() {
    testPass("ArrayIndexConstant.java");
  }

  @Test
  public void testAssignNullToObject() {
    testPass("AssignNullToObject.java");
  }

  @Test
  public void testBasicTypes() {
    testPass("BasicTypes.java");
  }

  @Test
  public void testBinaryOps() {
    testPass("BinaryOps.java");
  }

  @Test
  public void testBooleanAnd() {
    testPass("BooleanAnd.java");
  }

  @Test
  public void testBooleanLiteral() {
    testPass("BooleanLiteral.java");
  }

  @Test
  public void testBooleanNot() {
    testPass("BooleanNot.java");
  }

  @Test
  public void testBooleanOr() {
    testPass("BooleanOr.java");
  }

  @Test
  public void testConditionalFalse() {
    testPass("ConditionalFalse.java");
  }

  @Test
  public void testConditionalTrue() {
    testPass("ConditionalTrue.java");
  }

  @Test
  public void testConditionalWithNull() {
    testPass("ConditionalWithNull.java");
  }

  @Test
  public void testCreateIntArrayWithIntAndIntLiteralInitializers() {
    testPass("CreateIntArrayWithIntAndIntLiteralInitializers.java");
  }

  @Test
  public void testDoWhile() {
    testPass("DoWhile.java");
  }

  @Test
  public void testDoWhileWithBooleanArrayAccessCondition() {
    testPass("DoWhileWithBooleanArrayAccessCondition.java");
  }

  @Test
  public void testFactorial() {
    testPass("Factorial.java");
  }

  @Test
  public void testFieldAccess() {
    testPass("FieldAccess.java");
  }

  @Test
  public void testFor() {
    testPass("For.java");
  }

  @Test
  public void testForBooleanArrayAccessConditional() {
    testPass("ForBooleanArrayAccessConditional.java");
  }

  @Test
  public void testForBooleanMemberAccessConditional() {
    testPass("ForBooleanMemberAccessConditional.java");
  }

  @Test
  public void testForCondOnly() {
    testPass("ForCondOnly.java");
  }

  @Test
  public void testForEmpty() {
    testPass("ForEmpty.java");
  }

  @Test
  public void testForFull() {
    testPass("ForFull.java");
  }

  @Test
  public void testForIncOrDecOnly() {
    testPass("ForIncOrDecOnly.java");
  }

  @Test
  public void testForInitOnly() {
    testPass("ForInitOnly.java");
  }

  @Test
  public void testForLoop() {
    testPass("ForLoop.java");
  }

  @Test
  public void testForMissingCond() {
    testPass("ForMissingCond.java");
  }

  @Test
  public void testForMissingIncOrDec() {
    testPass("ForMissingIncOrDec.java");
  }

  @Test
  public void testForMissingInit() {
    testPass("ForMissingInit.java");
  }

  @Test
  public void testForMultipleIncOrDecOnly() {
    testPass("ForMultipleIncOrDecOnly.java");
  }

  @Test
  public void testForMultipleInitAndIncOrDec() {
    testPass("ForMultipleInitAndIncOrDec.java");
  }

  @Test
  public void testForMultipleInitOnly() {
    testPass("ForMultipleInitOnly.java");
  }

  @Test
  public void testForwardClassTest() {
    testPass("ForwardClassTest.java");
  }

  @Test
  public void testIf() {
    testPass("If.java");
  }

  @Test
  public void testIfFalseEmpty() {
    testPass("IfFalseEmpty.java");
  }

  @Test
  public void testIfFalseSingle() {
    testPass("IfFalseSingle.java");
  }

  @Test
  public void testIfFalseSingleElseEmpty() {
    testPass("IfFalseSingleElseEmpty.java");
  }

  @Test
  public void testIfFalseSingleElseMultiple() {
    testPass("IfFalseSingleElseMultiple.java");
  }

  @Test
  public void testIfFalseSingleElseSingle() {
    testPass("IfFalseSingleElseSingle.java");
  }

  @Test
  public void testIfTrueEmpty() {
    testPass("IfTrueEmpty.java");
  }

  @Test
  public void testIfTrueEmptyElseEmpty() {
    testPass("IfTrueEmptyElseEmpty.java");
  }

  @Test
  public void testIfTrueMultiple() {
    testPass("IfTrueMultiple.java");
  }

  @Test
  public void testIfTrueSingle() {
    testPass("IfTrueSingle.java");
  }

  @Test
  public void testIfTrueSingleElseEmpty() {
    testPass("IfTrueSingleElseEmpty.java");
  }

  @Test
  public void testIfTrueSingleElseSingle() {
    testPass("IfTrueSingleElseSingle.java");
  }

  @Test
  public void testIncIntArrayAccess() {
    testPass("IncIntArrayAccess.java");
  }

  @Test
  public void testIntAdd() {
    testPass("IntAdd.java");
  }

  @Test
  public void testIntComplement() {
    testPass("IntComplement.java");
  }

  @Test
  public void testIntDivide() {
    testPass("IntDivide.java");
  }

  @Test
  public void testIntEqual() {
    testPass("IntEqual.java");
  }

  @Test
  public void testIntGreaterThan() {
    testPass("IntGreaterThan.java");
  }

  @Test
  public void testIntGreaterThanOrEqual() {
    testPass("IntGreaterThanOrEqual.java");
  }

  @Test
  public void testIntLessThan() {
    testPass("IntLessThan.java");
  }

  @Test
  public void testIntLessThanOrEqual() {
    testPass("IntLessThanOrEqual.java");
  }

  @Test
  public void testIntMultiply() {
    testPass("IntMultiply.java");
  }

  @Test
  public void testIntNegate() {
    testPass("IntNegate.java");
  }

  @Test
  public void testIntNotEqual() {
    testPass("IntNotEqual.java");
  }

  @Test
  public void testIntPlus() {
    testPass("IntPlus.java");
  }

  @Test
  public void testIntPostDecrement() {
    testPass("IntPostDecrement.java");
  }

  @Test
  public void testIntPostIncrement() {
    testPass("IntPostIncrement.java");
  }

  @Test
  public void testIntRemainder() {
    testPass("IntRemainder.java");
  }

  @Test
  public void testIntShiftLeft() {
    testPass("IntShiftLeft.java");
  }

  @Test
  public void testIntShiftRight() {
    testPass("IntShiftRight.java");
  }

  @Test
  public void testIntSubtract() {
    testPass("IntSubtract.java");
  }

  @Test
  public void testIntUnsignedShiftRight() {
    testPass("IntUnsignedShiftRight.java");
  }

  @Test
  public void testNewBasic() {
    testPass("NewBasic.java");
  }

  @Test
  public void testNewID() {
    testPass("NewID.java");
  }

  @Test
  public void testNullArgumentForIntArrayParameter() {
    testPass("NullArgumentForIntArrayParameter.java");
  }

  @Test
  public void testNullEqualsNull() {
    testPass("NullEqualsNull.java");
  }

  @Test
  public void testObjectAEqualsReturnedObjectA() {
    testPass("ObjectAEqualsReturnedObjectA.java");
  }

  @Test
  public void testParens() {
    testPass("Parens.java");
  }

  @Test
  public void testPower() {
    testPass("Power.java");
  }

  @Test
  public void testQueue() {
    testPass("Queue.java");
  }

  @Test
  public void testReturnNullFromIntArrayMethod() {
    testPass("ReturnNullFromIntArrayMethod.java");
  }

  @Test
  public void testSAExample() {
    testPass("SAExample.java");
  }

  @Test
  public void testSymbolTableTest() {
    testPass("SymbolTableTest.java");
  }

  @Test
  public void testSyntaxTorture() {
    testPass("SyntaxTorture.java");
  }

  @Test
  public void testTypesArray() {
    testPass("TypesArray.java");
  }

  @Test
  public void testTypesBasic() {
    testPass("TypesBasic.java");
  }

  @Test
  public void testTypesID() {
    testPass("TypesID.java");
  }

  @Test
  public void testUnaryOps() {
    testPass("UnaryOps.java");
  }

  @Test
  public void testWhile() {
    testPass("While.java");
  }

  @Test
  public void testOptimization1() {
    testPass("Optimization1.java");
  }

  @Test
  public void testOptimization2() {
    testPass("Optimization2.java");
  }

  @Test
  public void testOptimization3() {
    testPass("Optimization3.java");
  }

  @Test
  public void testOptimization4() {
    testPass("Optimization4.java");
  }

  @Test
  public void testOptimization5() {
    testPass("Optimization5.java");
  }


}
