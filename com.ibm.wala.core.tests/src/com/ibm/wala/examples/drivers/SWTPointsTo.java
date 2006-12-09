/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.examples.drivers;

import java.util.Properties;

import org.eclipse.jface.window.ApplicationWindow;

import com.ibm.wala.analysis.pointers.BasicHeapGraph;
import com.ibm.wala.ecore.java.scope.EJavaAnalysisScope;
import com.ibm.wala.emf.wrappers.EMFScopeWrapper;
import com.ibm.wala.emf.wrappers.JavaScopeUtil;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.InferGraphRootsImpl;
import com.ibm.wala.util.io.CommandLine;
import com.ibm.wala.util.warnings.WalaException;
import com.ibm.wala.util.warnings.WarningSet;
import com.ibm.wala.viz.SWTTreeViewer;

/**
 * 
 * This application is a WALA client: it invokes an SWT TreeViewer to visualize
 * a Points-To solution
 * 
 * @author sfink
 */
public class SWTPointsTo {

  /**
   * Usage: SWTPointsTo -appJar [jar file name] The "jar file name" should be
   * something like "c:/temp/testdata/java_cup.jar"
   * 
   * @param args
   * @throws WalaException 
   */
  public static void main(String[] args) throws WalaException {
    Properties p = CommandLine.parse(args);
    GVCallGraph.validateCommandLine(p);
    run(p.getProperty("appJar"));
  }

  /**
   * @param appJar
   *          should be something like "c:/temp/testdata/java_cup.jar"
   */
  public static ApplicationWindow run(String appJar) {

    try {
      Graph<Object> g = buildPointsTo(appJar);

      // create and run the viewer
      final SWTTreeViewer v = new SWTTreeViewer();
      v.setGraphInput(g);
      v.setRootsInput(InferGraphRootsImpl.inferRoots(g));
      v.run();
      return v.getApplicationWindow();

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static Graph<Object> buildPointsTo(String appJar) throws WalaException {
    EJavaAnalysisScope escope = JavaScopeUtil.makeAnalysisScope(appJar);

    // generate a DOMO-consumable wrapper around the incoming scope object
    EMFScopeWrapper scope = EMFScopeWrapper.generateScope(escope);
    

    // TODO: return the warning set (need a CAPA type)
    // invoke DOMO to build a DOMO class hierarchy object
    WarningSet warnings = new WarningSet();
    ClassHierarchy cha = ClassHierarchy.make(scope, warnings);

    Entrypoints entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha);
    AnalysisOptions options = new AnalysisOptions(scope, entrypoints);

    // //
    // build the call graph
    // //
    com.ibm.wala.ipa.callgraph.CallGraphBuilder builder = Util.makeZeroCFABuilder(options, cha, scope, warnings, null, null);
    CallGraph cg = builder.makeCallGraph(options);
    PointerAnalysis pointerAnalysis = builder.getPointerAnalysis();
    return new BasicHeapGraph(pointerAnalysis,cg);
  }
}