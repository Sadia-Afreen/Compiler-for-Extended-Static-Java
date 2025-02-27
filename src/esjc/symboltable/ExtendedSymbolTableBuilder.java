package esjc.symboltable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

import esjc.util.Pair;

/**
 * This class is used to build symbol table for an ExtendedStaticJava
 * {@link CompilationUnit}. Note that the algorithm assumes that the JDT AST
 * tree was built using the {@link esjc.ast.ExtendedStaticJavaASTBuilder}. That is, it
 * assumes certain structures on the AST, e.g., a class does not have an
 * instance method.
 * 
 * @author <a href="mailto:robby@cis.ksu.edu">Robby</a>
 */
public class ExtendedSymbolTableBuilder extends SymbolTableBuilder {
  /**
   * The visitor for {@link ASTNode} to resolve symbols.
   * 
   * @author <a href="mailto:robby@cis.ksu.edu">Robby</a>
   */
  private static class Visitor extends SymbolTableBuilder.Visitor {
    private Map<String, TypeDeclaration> classMap = new HashMap<>();

    private Map<Pair<String, String>, FieldDeclaration> fieldMap = new HashMap<>();

    /**
     * Unlink references and clear maps/collections.
     */
    @Override
    protected void dispose() {
      super.dispose();
    }

    /**
     * Determines whether a {@link List} of {@link Modifier}s has a public
     * modifier.
     * 
     * @param modifiers
     *        the list of modifiers
     *
     * @return True, if the {@link List} contains a public modifier.
     */
    private boolean hasPublicModifier(final List modifiers) {
      for (final Object o : modifiers) {
        if ((o instanceof Modifier)
            && (((Modifier) o).getKeyword() == ModifierKeyword.PUBLIC_KEYWORD)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean visit(final ArrayCreation node) {
      // Note that we don't visit the ArrayCreation's type
      // because we want visit(SimpleName) to resolve variable references
      // instead of method names.
      for (final Object o : node.dimensions()) {
        ((ASTNode) o).accept(this);
      }
      if (node.getInitializer() != null) {
        node.getInitializer().accept(this);
      }
      return false;
    }

    @Override
    public boolean visit(final ClassInstanceCreation node) {
      // Note that we don't visit the ClassInstanceCreation's name
      // because we want visit(SimpleName) to resolve variable references
      // instead of method names.
      Type x = node.getType();
      this.result.put(node, classMap.get(x.toString()));
      return false;
    }

    @Override
    public boolean visit(final FieldAccess node) {
      // Note that we don't visit the FieldAccess's name
      // because we want visit(SimpleName) to resolve variable references
      // instead of method names.
      node.getExpression().accept(this);
      return false;
    }

    @Override
    public boolean visit(final TypeDeclaration node) {
      if (hasPublicModifier(node.modifiers())) { // use super's visit method because this node is the main class
        return super.visit(node);
      }

      final String className = node.getName().getIdentifier();
      if (this.classMap.containsKey(className)) {
        throw new Error(node, "Error in class declaration '" + className
            + "' : the class name has been used in:\n"
            + this.classMap.get(className));
      }
      this.classMap.put(className, node);

      for (final Object o : node.bodyDeclarations()) {
        // simple class declaration only has field declarations
        final FieldDeclaration fd = (FieldDeclaration) o;

        final VariableDeclarationFragment vdf = (VariableDeclarationFragment) fd
            .fragments().get(0);
        final String name = vdf.getName().getIdentifier();
        final Pair<String, String> p = new Pair<>(className, name);
        if (this.fieldMap.containsKey(p)) {
          throw new Error(fd, "Error in field declaration '" + name
              + "' : the field name has been used in:\n"
              + this.fieldMap.get(new Pair<>(className, name)));
        }
        this.fieldMap.put(p, fd);
      }
      return false;
    }
  }

  /**
   * Builds a {@link ExtendedSymbolTable} for the given ExtendedStaticJava
   * {@link CompilationUnit}.
   * 
   * @param cu
   *          The {@link CompilationUnit}.
   * @return The {@link ExtendedSymbolTable} for the given
   *         {@link CompilationUnit}.
   * @throws Error
   *           If the builder encounter unresolvable symbol.
   */
  public static ExtendedSymbolTable build(final CompilationUnit cu)
      throws Error {
    assert cu != null;

    final Visitor v = new Visitor();
    cu.accept(v);
    final ExtendedSymbolTable result = new ExtendedSymbolTable(v.result,
        v.classMap, v.fieldMap);
    v.dispose();
    return result;
  }

  /**
   * Declared as protected to disallow creation of this object outside from the
   * methods of this class.
   */
  protected ExtendedSymbolTableBuilder() {
    super();
  }
}
