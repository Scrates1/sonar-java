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

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.java.model.LineUtils;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.EmptyStatementTree;
import org.sonar.plugins.java.api.tree.ImportClauseTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "S00103", repositoryKey = "squid")
@Rule(key = "S103")
public class TooLongLineCheck extends IssuableSubscriptionVisitor {

  private static final int DEFAULT_MAXIMUM_LINE_LENGTH = 120;

  @RuleProperty(
      key = "maximumLineLength",
      description = "The maximum authorized line length.",
      defaultValue = "" + DEFAULT_MAXIMUM_LINE_LENGTH)
  int maximumLineLength = DEFAULT_MAXIMUM_LINE_LENGTH;

  private final Set<Integer> ignoredLines = new HashSet<>();

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.emptyList();
  }

  @Override
  public void setContext(JavaFileScannerContext context) {
    ignoredLines.clear();
    ignoreLines(context.getTree());
    super.setContext(context);
    visitFile();
  }

  private void ignoreLines(CompilationUnitTree tree) {
    List<ImportClauseTree> imports = tree.imports();
    if (!imports.isEmpty()) {
      int start = getLine(imports.get(0), true);
      int end = getLine(imports.get(imports.size() - 1), false);
      for (int i = start; i <= end; i++) {
        ignoredLines.add(i);
      }
    }
  }

  private static int getLine(ImportClauseTree importClauseTree, boolean fromStart) {
    if (importClauseTree.is(Tree.Kind.IMPORT)) {
      if (fromStart) {
        return LineUtils.startLine(((ImportTree) importClauseTree).importKeyword());
      } else {
        return LineUtils.startLine(((ImportTree) importClauseTree).semicolonToken());
      }
    }
    return LineUtils.startLine(((EmptyStatementTree) importClauseTree).semicolonToken());
  }

  private void visitFile() {
    List<String> lines = context.getFileLines();
    for (int i = 0; i < lines.size(); i++) {
      if (!ignoredLines.contains(i + 1)) {
        String origLine = lines.get(i);
        if (origLine.length() > maximumLineLength && removeIgnoredPatterns(origLine).length() > maximumLineLength) {
          addIssue(i + 1, MessageFormat.format("Split this {0} characters long line (which is greater than {1} authorized).", origLine.length(), maximumLineLength));
        }
      }
    }
  }

  private static String removeIgnoredPatterns(String line) {
    if (!line.matches("\\s*(?:\\*|//).*")) return line;
    return line
      // @see <a href="http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javadoc.html#link">@link ...</a>
      .replaceFirst("\\{@link [^}]+\\}\\s*", "")
      // @see <a href="http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javadoc.html#see">@see reference</a>
      .replaceFirst("@see .+", "");
  }
}
