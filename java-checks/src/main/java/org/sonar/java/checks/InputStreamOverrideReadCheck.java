/*
 * SonarQube Java
 * Copyright (C) 2012-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.java.checks;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

@Rule(key = "S4929")
public class InputStreamOverrideReadCheck extends IssuableSubscriptionVisitor {

  private static final MethodMatchers READ_BYTES_INT_INT = MethodMatchers.create()
    .ofAnyType().names("read").addParametersMatcher("byte[]", "int", "int").build();
  private static final MethodMatchers READ_INT = MethodMatchers.create()
    .ofAnyType().names("read").addParametersMatcher("int").build();

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.singletonList(Tree.Kind.CLASS);
  }

  @Override
  public void visitNode(Tree tree) {
    ClassTree classTree = (ClassTree) tree;
    Type superType = classTree.symbol().superClass();
    IdentifierTree className = classTree.simpleName();
    if (className == null || classTree.symbol().isAbstract() || superType == null || !(superType.is("java.io.InputStream") || superType.is("java.io.FilterInputStream"))) {
      return;
    }

    Optional<MethodTree> readByteIntInt = findMethod(classTree, READ_BYTES_INT_INT);
    if (!readByteIntInt.isPresent()) {
      String message = findMethod(classTree, READ_INT)
        .filter(readIntTree -> readIntTree.block().body().isEmpty())
        .map(readIntTree -> "Provide an empty override of \"read(byte[],int,int)\" for this class as well.")
        .orElse("Provide an override of \"read(byte[],int,int)\" for this class.");
      reportIssue(className, message);
    }

  }

  private static Optional<MethodTree> findMethod(ClassTree classTree, MethodMatchers methodMatcher) {
    return classTree.members()
      .stream()
      .filter(m -> m.is(Tree.Kind.METHOD))
      .map(MethodTree.class::cast)
      .filter(methodMatcher::matches)
      .findFirst();
  }
}
