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
package org.sonar.plugins.java.api;

import org.sonar.java.annotations.Beta;

/**
 * Implementing this interface allows a check to be executed - or not - during analysis, depending
 * of expected java version.
 * <br />
 * In order to be taken into account during analysis, the property <code>sonar.java.source</code> must be set.
 */
@Beta
public interface JavaVersionAwareVisitor {
  /**
   * Control if the check is compatible with the java version of the project being analyzed. The version used as parameter depends of the
   * property <code>sonar.java.source</code> (6 or 1.6 for java 1.6, 7 or 1.7, etc.).
   *
   * @param version The java version of the sources
   * @return true if the check is compatible with detected java version and should be executed on sources, false otherwise.
   */
  boolean isCompatibleWithJavaVersion(JavaVersion version);
}
