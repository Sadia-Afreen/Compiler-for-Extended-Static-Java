package esjc.codegen;

import java.util.Map;

import esjc.util.Util;

public class ExtendedClassByteCodes extends ClassByteCodes {
  public final Map<String, byte[]> otherClasses;

  protected ExtendedClassByteCodes(final String mainClassName,
       final byte[] mainClassBytes,
      final Map<String, byte[]> otherClasses) {
    super(mainClassName, mainClassBytes);
    assert Util.checkNonNullElements(otherClasses);
    this.otherClasses = otherClasses;
  }
}
