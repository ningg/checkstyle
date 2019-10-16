////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2019 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks.modifier;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;

/**
 * <p>
 * Checks for implicit modifiers on nested types in classes.
 * </p>
 * <p>
 * This check is effectively the opposite of
 * <a href="https://checkstyle.org/config_modifier.html#RedundantModifier">RedundantModifier</a>.
 * It checks the modifiers on nested types in classes, ensuring that certain modifiers are
 * explicitly specified even though they are actually redundant.
 * </p>
 * <p>
 * Nested enums and interfaces within a class are always {@code static} and as such the compiler
 * does not require the {@code static} modifier. This check provides the ability to enforce that
 * the {@code static} modifier is explicitly coded and not implicitly added by the compiler.
 * </p>
 * <pre>
 * public final class Person {
 *   enum Age {  // violation
 *     CHILD, ADULT
 *   }
 * }
 * </pre>
 * <p>
 * Rationale for this check: Nested enums and interfaces are treated differently from nested
 * classes as they are only allowed to be {@code static}. Developers should not need to remember
 * this rule, and this check provides the means to enforce that the modifier is coded explicitly.
 * </p>
 * <ul>
 * <li>
 * Property {@code violateImpliedStaticOnNestedEnum} - Control whether to enforce that
 * {@code static} is explicitly coded on nested enums in classes.
 * Default value is {@code true}.
 * </li>
 * <li>
 * Property {@code violateImpliedStaticOnNestedInterface} - Control whether to enforce that
 * {@code static} is explicitly coded on nested interfaces in classes.
 * Default value is {@code true}.
 * </li>
 * </ul>
 * <p>
 * This example checks that all implicit modifiers on nested interfaces and enums are
 * explicitly specified in classes.
 * </p>
 * <p>
 * Configuration:
 * </p>
 * <pre>
 * &lt;module name="ClassMemberImpliedModifier" /&gt;
 * </pre>
 * <p>
 * Code:
 * </p>
 * <pre>
 * public final class Person {
 *   static interface Address1 {  // valid
 *   }
 *
 *   interface Address2 {  // violation
 *   }
 *
 *   static enum Age1 {  // valid
 *     CHILD, ADULT
 *   }
 *
 *   enum Age2 {  // violation
 *     CHILD, ADULT
 *   }
 * }
 * </pre>
 * @since 8.16
 */
@StatelessCheck
public class ClassMemberImpliedModifierCheck
    extends AbstractCheck {

    /**
     * A key is pointing to the warning message text in "messages.properties" file.
     */
    public static final String MSG_KEY = "class.implied.modifier";

    /** Name for 'static' keyword. */
    private static final String STATIC_KEYWORD = "static";

    /**
     * Control whether to enforce that {@code static} is explicitly coded
     * on nested enums in classes.
     */
    private boolean violateImpliedStaticOnNestedEnum = true;

    /**
     * Control whether to enforce that {@code static} is explicitly coded
     * on nested interfaces in classes.
     */
    private boolean violateImpliedStaticOnNestedInterface = true;

    /**
     * Setter to control whether to enforce that {@code static} is explicitly coded
     * on nested enums in classes.
     * @param violateImplied
     *        True to perform the check, false to turn the check off.
     */
    public void setViolateImpliedStaticOnNestedEnum(boolean violateImplied) {
        violateImpliedStaticOnNestedEnum = violateImplied;
    }

    /**
     * Setter to control whether to enforce that {@code static} is explicitly coded
     * on nested interfaces in classes.
     * @param violateImplied
     *        True to perform the check, false to turn the check off.
     */
    public void setViolateImpliedStaticOnNestedInterface(boolean violateImplied) {
        violateImpliedStaticOnNestedInterface = violateImplied;
    }

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {
            TokenTypes.INTERFACE_DEF,
            TokenTypes.ENUM_DEF,
        };
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ScopeUtil.isInClassBlock(ast) || ScopeUtil.isInEnumBlock(ast)) {
            final DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
            switch (ast.getType()) {
                case TokenTypes.ENUM_DEF:
                    if (violateImpliedStaticOnNestedEnum
                            && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) == null) {
                        log(ast, MSG_KEY, STATIC_KEYWORD);
                    }
                    break;
                case TokenTypes.INTERFACE_DEF:
                    if (violateImpliedStaticOnNestedInterface
                            && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) == null) {
                        log(ast, MSG_KEY, STATIC_KEYWORD);
                    }
                    break;
                default:
                    throw new IllegalStateException(ast.toString());
            }
        }
    }

}
