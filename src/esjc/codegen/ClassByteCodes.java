package esjc.codegen;


/**
 * This class is used to hold the result of the {@link ByteCodeGenerator} (when
 * the generation is succesful).
 * 
 * @author <a href="mailto:robby@ksu.edu">Robby</a>
 */
public class ClassByteCodes {
  /**
   * Holds the name of the main class.
   */
  public final String mainClassName;

  /**
   * Holds the byte array representation of the main class.
   */
  public final byte[] mainClassBytes;

  /**
   * Constructs the class byte codes.
   * 
   * @param mainClassName
   *          The name of the main class.
   * @param mainClassBytes
   *          The byte array representation of the main class.
   */
  public ClassByteCodes(final String mainClassName,
      final byte[] mainClassBytes) {
    assert (mainClassName != null) && (mainClassBytes != null);
    this.mainClassName = mainClassName;
    this.mainClassBytes = mainClassBytes;
  }
}
