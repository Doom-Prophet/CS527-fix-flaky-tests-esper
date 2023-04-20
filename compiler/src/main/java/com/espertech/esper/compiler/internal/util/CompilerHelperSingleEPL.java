/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.compiler.internal.util;

import com.espertech.esper.common.client.soda.ClassProvidedExpression;
import com.espertech.esper.common.client.soda.EPStatementObjectModel;
import com.espertech.esper.common.internal.compile.stage1.Compilable;
import com.espertech.esper.common.internal.compile.stage1.spec.SelectClauseStreamSelectorEnum;
import com.espertech.esper.common.internal.compile.stage1.spec.StatementSpecRaw;
import com.espertech.esper.common.internal.compile.stage1.specmapper.StatementSpecMapEnv;
import com.espertech.esper.common.internal.compile.stage1.specmapper.StatementSpecMapper;
import com.espertech.esper.common.internal.compile.stage2.StatementSpecCompileException;
import com.espertech.esper.common.internal.compile.stage3.StatementCompileTimeServices;
import com.espertech.esper.common.internal.epl.classprovided.compiletime.ClassProvidedPrecompileResult;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;
import com.espertech.esper.common.internal.util.ValidationException;
import com.espertech.esper.compiler.client.option.InlinedClassInspectionContext;
import com.espertech.esper.compiler.client.option.InlinedClassInspectionOption;
import com.espertech.esper.compiler.internal.generated.EsperEPL2GrammarParser;
import com.espertech.esper.compiler.internal.parse.*;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.Tree;
import org.codehaus.janino.util.ClassFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.espertech.esper.common.internal.epl.classprovided.compiletime.ClassProvidedPrecompileUtil.compileClassProvided;

public class CompilerHelperSingleEPL {

    private final static ParseRuleSelector EPL_PARSE_RULE;
    private final static Logger log = LoggerFactory.getLogger(CompilerHelperSingleEPL.class);

    static {
        EPL_PARSE_RULE = new ParseRuleSelector() {
            public Tree invokeParseRule(EsperEPL2GrammarParser parser) throws RecognitionException {
                return parser.startEPLExpressionRule();
            }
        };
    }

    protected static CompilerHelperSingleResult parseCompileInlinedClassesWalk(Compilable compilable, InlinedClassInspectionOption inlinedClassConsumer, StatementCompileTimeServices compileTimeServices)
        throws StatementSpecCompileException {
        CompilerHelperSingleResult result;
        try {
            if (compilable instanceof CompilableEPL) {
                CompilableEPL compilableEPL = (CompilableEPL) compilable;

                // parse
                ParseResult parseResult = parse(compilableEPL.getEpl());

                // compile application-provided classes (both create-class as well as just class-keyword)
                ClassProvidedPrecompileResult classesInlined = compileAddExtensions(parseResult.getClasses(), compilable, inlinedClassConsumer, compileTimeServices);

                // walk - this may use the new classes already such as for extension-single-row-function
                StatementSpecRaw raw = walk(parseResult, compilableEPL.getEpl(), compileTimeServices.getStatementSpecMapEnv());
                result = new CompilerHelperSingleResult(raw, classesInlined);
            } else if (compilable instanceof CompilableSODA) {
                EPStatementObjectModel soda = ((CompilableSODA) compilable).getSoda();

                // compile application-provided classes (both create-class as well as just class-keyword)
                ClassProvidedPrecompileResult classesInlined;
                if ((soda.getClassProvidedExpressions() != null && !soda.getClassProvidedExpressions().isEmpty()) || soda.getCreateClass() != null) {
                    List<String> classTexts = new ArrayList<>();
                    if (soda.getClassProvidedExpressions() != null) {
                        for (ClassProvidedExpression inlined : soda.getClassProvidedExpressions()) {
                            classTexts.add(inlined.getClassText());
                        }
                    }
                    if (soda.getCreateClass() != null) {
                        classTexts.add(soda.getCreateClass().getClassProvidedExpression().getClassText());
                    }
                    classesInlined = compileAddExtensions(classTexts, compilable, inlinedClassConsumer, compileTimeServices);
                } else {
                    classesInlined = ClassProvidedPrecompileResult.EMPTY;
                }

                // map from soda to raw
                StatementSpecRaw raw = StatementSpecMapper.map(soda, compileTimeServices.getStatementSpecMapEnv());
                result = new CompilerHelperSingleResult(raw, classesInlined);
            } else {
                throw new IllegalStateException("Unrecognized compilable " + compilable);
            }
        } catch (StatementSpecCompileException ex) {
            throw ex;
        } catch (Throwable t) {
            throw new StatementSpecCompileException("Exception processing statement: " + t.getMessage(), t, compilable.toEPL());
        }
        return result;
    }

    public static StatementSpecRaw parseWalk(String epl, StatementSpecMapEnv mapEnv)
        throws StatementSpecCompileException {
        ParseResult parseResult = parse(epl);
        return walk(parseResult, epl, mapEnv);
    }

    private static ClassProvidedPrecompileResult compileAddExtensions(List<String> classes, Compilable compilable, InlinedClassInspectionOption option, StatementCompileTimeServices compileTimeServices) throws StatementSpecCompileException {
        Consumer<Object> classFileConsumer = null;
        if (option != null) {
            classFileConsumer = compilerResult -> {
                ClassFile[] files = ((List<ClassFile>) compilerResult).toArray(new ClassFile[0]);
                option.visit(new InlinedClassInspectionContext(files));
            };
        }

        ClassProvidedPrecompileResult classesInlined;
        try {
            classesInlined = compileClassProvided(classes, classFileConsumer, compileTimeServices, null);
            // add inlined classes including create-class
            compileTimeServices.getClassProvidedClasspathExtension().add(classesInlined.getClasses(), classesInlined.getBytes());
        } catch (ExprValidationException ex) {
            throw new StatementSpecCompileException(ex.getMessage(), ex, compilable.toEPL());
        }
        return classesInlined;
    }

    private static StatementSpecRaw walk(ParseResult parseResult, String epl, StatementSpecMapEnv mapEnv)
        throws StatementSpecCompileException {
        Tree ast = parseResult.getTree();

        SelectClauseStreamSelectorEnum defaultStreamSelector = StatementSpecMapper.mapFromSODA(mapEnv.getConfiguration().getCompiler().getStreamSelection().getDefaultStreamSelector());
        EPLTreeWalkerListener walker = new EPLTreeWalkerListener(parseResult.getTokenStream(), defaultStreamSelector, parseResult.getScripts(), parseResult.getClasses(), mapEnv);

        try {
            ParseHelper.walk(ast, walker, epl, epl);
        } catch (ASTWalkException | ValidationException ex) {
            throw new StatementSpecCompileException(ex.getMessage(), ex, epl);
        } catch (RuntimeException ex) {
            String message = "Invalid expression encountered";
            throw new StatementSpecCompileException(getNullableErrortext(message, ex.getMessage()), ex, epl);
        }

        if (log.isDebugEnabled()) {
            ASTUtil.dumpAST(ast);
        }

        return walker.getStatementSpec();
    }

    private static ParseResult parse(String epl)
        throws StatementSpecCompileException {
        return ParseHelper.parse(epl, epl, true, EPL_PARSE_RULE, true);
    }

    private static String getNullableErrortext(String msg, String cause) {
        if (cause == null) {
            return msg;
        } else {
            return cause;
        }
    }
}
