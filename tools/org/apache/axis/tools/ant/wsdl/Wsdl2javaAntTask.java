/*
 * Copyright 2001-2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis.tools.ant.wsdl;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.axis.enum.Scope;
import org.apache.axis.utils.DefaultAuthenticator;
import org.apache.axis.utils.ClassUtils;
import org.apache.axis.wsdl.toJava.Emitter;
import org.apache.axis.wsdl.toJava.FactoryProperty;
import org.apache.axis.wsdl.toJava.NamespaceSelector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;

/*
 * IMPORTANT: see Java2WsdlAntTask on how to javadoc this task and rebuild
 * the task documentation afterwards
 *
 */

/**
 * Create Java classes from local or remote WSDL.
 * Mappings from namespaces to packages can be provided as nested &lt;mapping&gt;
 * elements.
 * <p>
 * Proxy settings are taken from the java runtime settings of http.ProxyHost,
 * http.ProxyPort, etc. The Ant task &lt;setProxy&gt; can set these.
 * As well as the nested mapping elements, this task uses  the file
 * <tt>NStoPkg.properties</tt> in the project base directory
 * for namespace mapping
 * <p>
 * This task does no dependency checking; files are generated whether they
 * need to be or not. The exception to this is the Impl class, which is
 * not overwritten if it exists. This is a safety measure. However, all other
 * classes are generated overwriting anything that exists.
 * <p>
 * The safe way to use this task is to have it generate the java source in
 * a build directory, then have a &lt;copy&gt task selectively copy the
 * files you need into a safe location. Again, copying into the source tree
 * is dangerous, but a separate build/src tree is safe. Then include this
 * separate tree in the &lt;javac&gt; task's src attribute to include it in the
 * build. Implement your own implementation classes of the server stub and the
 * test cases using the generated templates.
 * If you want to add methods to autogenerated data types, consider subclassing
 * them, or write helper classes.
 * <p>
 * Tip: if you &lt;get&gt; the wsdl, and use the &lt;filesmatch&gt; condition
 * to compare the fetched wsdl with a catched copy, you can make the target that
 * calls the axis-wsd2ljava task conditional on the WSDL having changed. This stops
 * spurious code regeneration and follow-on rebuilds across the java source tree.
 * @ant.task category="axis" name="axis-wsdl2java"
 * @author Davanum Srinivas (dims@yahoo.com)
 * @author steve loughran
 */
public class Wsdl2javaAntTask extends Task
{
    private boolean verbose = false;
    private boolean debug = false;
    private boolean quiet = false;
    private boolean server = false;
    private boolean skeletonDeploy = false;
    private boolean testCase = false;
    private boolean noImports = false;
    private boolean all = false;
    private boolean helperGen = false;
    private boolean noWrapped = false;
    //private boolean jaxrpc11mappings = false;
    private boolean allowInvalidURL = false;
    private String factory = null;
    private HashMap namespaceMap = new HashMap();
    private String output = ".";
    private String protocolHandlerPkgs = "";
    private String deployScope = "";
    private String url = "";
    private String typeMappingVersion = TypeMappingVersionEnum.DEFAULT_VERSION;
    private long timeout = 45000;
    private File namespaceMappingFile = null;
    private MappingSet mappings = new MappingSet();
    private String username = null;
    private String password = null;
    private Path classpath = null;
    private List nsIncludes = new ArrayList();
    private List nsExcludes = new ArrayList();
    private List properties = new ArrayList();
	private String implementationClassName = null;

    /**
     * do we print a stack trace when something goes wrong?
     */
    private boolean printStackTraceOnFailure = true;
    /**
     * what action to take when there was a failure and the source was some
     * URL
     */
    private boolean failOnNetworkErrors = false;

    public Wsdl2javaAntTask() {
    }

    /**
     * validation code
     * @throws  BuildException  if validation failed
     */
    protected void validate()
            throws BuildException {
        if (url == null || url.length() == 0) {
            throw new BuildException("No url specified");
        }
        if (timeout < -1) {
            throw new BuildException("negative timeout supplied");
        }
        File outdir = new File(output);
        if (!outdir.isDirectory() || !outdir.exists()) {
            throw new BuildException("output directory is not valid");
        }
        if (quiet) {
            if (verbose) {
                throw new BuildException("quiet and verbose options are " +
                                         "exclusive");
            }
            if (debug) {
                throw new BuildException("quiet and debug options are " +
                                         "exclusive");
            }
        }
    }

    /**
     * trace out parameters
     * @param logLevel to log at
     * @see org.apache.tools.ant.Project#log
     */
    public void traceParams(int logLevel) {
        log("Running Wsdl2javaAntTask with parameters:", logLevel);
        log("\tverbose:" + verbose, logLevel);
        log("\tdebug:" + debug, logLevel);
        log("\tquiet:" + quiet, logLevel);
        log("\tserver-side:" + server, logLevel);
        log("\tskeletonDeploy:" + skeletonDeploy, logLevel);
        log("\thelperGen:" + helperGen, logLevel);
        log("\tfactory:" + factory, logLevel);
        log("\tnsIncludes:" + nsIncludes, logLevel);
        log("\tnsExcludes:" + nsExcludes, logLevel);
        log("\tfactoryProps:" + properties, logLevel);
        log("\ttestCase:" + testCase, logLevel);
        log("\tnoImports:" + noImports, logLevel);
        log("\tNStoPkg:" + namespaceMap, logLevel);
        log("\toutput:" + output, logLevel);
        log("\tprotocolHandlerPkgs:" + protocolHandlerPkgs, logLevel);
        log("\tdeployScope:" + deployScope, logLevel);
        log("\tURL:" + url, logLevel);
        log("\tall:" + all, logLevel);
        log("\ttypeMappingVersion:" + typeMappingVersion, logLevel);
        log("\ttimeout:" + timeout, logLevel);
        log("\tfailOnNetworkErrors:" + failOnNetworkErrors, logLevel);
        log("\tprintStackTraceOnFailure:" + printStackTraceOnFailure, logLevel);
        log("\tnamespaceMappingFile:" + namespaceMappingFile, logLevel);
        log("\tusername:" + username, logLevel);
        log("\t:password" + password, logLevel);
        log("\t:noWrapped" + noWrapped, logLevel);
        //log("\t:jaxrpc11mappings" + jaxrpc11mappings, logLevel);
        log("\t:allowInvalidURL" + allowInvalidURL, logLevel);
		log("\t:implementationClassName" + implementationClassName, logLevel);
        log("\t:classpath" + classpath, logLevel);
        traceNetworkSettings(logLevel);
    }

    /**
     * The method executing the task
     * @throws  BuildException  if validation or execution failed
     */
    public void execute() throws BuildException {
        //before we get any further, if the user didnt spec a namespace mapping
        //file, we load in the default

        traceParams(Project.MSG_VERBOSE);
        validate();
        try {
            // Instantiate the emitter
            Emitter emitter = createEmitter();

            //extract the scope
            Scope scope = Scope.getScope(deployScope, null);
            if (scope != null) {
                emitter.setScope(scope);
            } else if (deployScope.length() == 0
                    || "none".equalsIgnoreCase(deployScope)) {
                /* leave default (null, or not-explicit) */;
            } else {
                log("Unrecognized scope:  " + deployScope + ".  Ignoring it.", Project.MSG_VERBOSE);
            }

            //do the mappings, with namespaces mapped as the key
            mappings.execute(this, namespaceMap, false);
            if (!namespaceMap.isEmpty()) {
                emitter.setNamespaceMap(namespaceMap);
            }
            emitter.setTestCaseWanted(testCase);
            emitter.setHelperWanted(helperGen);
            if (factory != null) {
                emitter.setFactory(factory);
            }
            emitter.setNamespaceIncludes(nsIncludes);
            emitter.setNamespaceExcludes(nsExcludes);
            emitter.setProperties(properties);
            emitter.setImports(!noImports);
            emitter.setAllWanted(all);
            emitter.setOutputDir(output);
            emitter.setServerSide(server);
            emitter.setSkeletonWanted(skeletonDeploy);
            emitter.setVerbose(verbose);
            emitter.setDebug(debug);
            emitter.setQuiet(quiet);
            emitter.setTypeMappingVersion(typeMappingVersion);
            emitter.setNowrap(noWrapped);
            //emitter.setUseJaxRPC11Mappings(jaxrpc11mappings);
            emitter.setAllowInvalidURL(allowInvalidURL);
            if (namespaceMappingFile != null) {
                emitter.setNStoPkg(namespaceMappingFile.toString());
            }
			emitter.setTimeout(timeout);
			emitter.setImplementationClassName(implementationClassName);

            Authenticator.setDefault(new DefaultAuthenticator(username, password));
            if (classpath != null) {
                AntClassLoader cl = new AntClassLoader(
                        getClass().getClassLoader(),
                        getProject(),
                        classpath,
                        false);
                log("Using CLASSPATH " + cl.getClasspath(),
                        Project.MSG_VERBOSE);
                ClassUtils.setDefaultClassLoader(cl);
            }

            log("WSDL2Java " + url, Project.MSG_INFO);
            try {
                emitter.run(url);
            } catch (Throwable e) {
                if (url.startsWith("http://")) {
                    // What we have is either a network error or invalid XML -
                    // the latter most likely an HTML error page.  This makes
                    // it impossible to continue with the test, so we stop here
                    if (!failOnNetworkErrors) {
                        // test mode, issue a warning, and return without
                        //reporting a fatal error.
                        log(e.toString(), Project.MSG_WARN);
                        return;
                    } else {
                        //in 'consumer' mode, bail out with the URL
                        throw new BuildException("Could not build " + url, e);
                    }
                } else {
                    throw e;
                }
            }
        } catch (BuildException b) {
            //we rethrow this immediately; but need to catch it to stop it being
            //mistaken for a throwable.
            throw b;
        } catch (Throwable t) {
            if (printStackTraceOnFailure) {
                traceParams(Project.MSG_INFO);
                t.printStackTrace();
            }
            //now throw an exception that includes the error text of the caught exception.
            throw new BuildException("WSDL processing error for "
                    + url +" :\n "+t.getMessage() , t);
        }

    }

    /**
     *  flag for verbose output; default=false
     *
     *@param  verbose  The new verbose value
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     *  flag for debug output; default=false
     *
     *@param  debug  The new debug value
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     *  flag for quiet output; default=false
     *
     *@param  quiet  The new quiet value
     */
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    /**
     *  emit server-side bindings for web service; default=false
     */
    public void setServerSide(boolean parameter) {
        this.server = parameter;
    }

    /**
     * deploy skeleton (true) or implementation (false) in deploy.wsdd.
     * Default is false.  Assumes server-side="true".
     */
    public void setSkeletonDeploy(boolean parameter) {
        this.skeletonDeploy = parameter;
    }

    /**
     * flag for automatic Junit testcase generation
     * default is false
     */
    public void setTestCase(boolean parameter) {
        this.testCase = parameter;
    }

    /**
     * Turn on/off Helper class generation;
     * default is false
     */
    public void setHelperGen(boolean parameter) {
        this.helperGen = parameter;
    }

    /**
     * name of the Java2WSDLFactory class for
     * extending WSDL generation functions
     */
    public void setFactory(String parameter) {
        this.factory = parameter;
    }

    /**
     * only generate code for the immediate WSDL document,
     * and not imports; default=false;
     */
    public void setNoImports(boolean parameter) {
        this.noImports = parameter;
    }

    /**
     * output directory for emitted files
     */
    public void setOutput(File parameter) throws BuildException {
        try {
            this.output = parameter.getCanonicalPath();
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
    }

    /**
     * append any protocol handler pkgs specified with the task
     */
    public void setProtocolHandlerPkgs(String handlerPkgs) {
        String currentPkgs = System.getProperty("java.protocol.handler.pkgs");
        String newPkgs = null;

        if (currentPkgs == null)
            newPkgs = handlerPkgs;
        else
        // append to the existing list
            newPkgs = currentPkgs + "|" + handlerPkgs;

        System.setProperty("java.protocol.handler.pkgs", newPkgs);
    }

    /**
     * add scope to deploy.xml: "Application", "Request", "Session"
     * optional;
     */
    public void setDeployScope(String scope) {
        this.deployScope = scope;
    }
/*
    //unused till we can somehow get ant to be case insensitive when handling enums
    public void setDeployScope(DeployScopeEnum scope) {
        this.deployScope = scope.getValue();
    }
*/
    /**
     * URL to fetch and generate WSDL for.
     * Can be remote or a local file.
     */
    public void setURL(String parameter) {
        this.url = parameter;
    }

    /**
     * flag to generate code for all elements, even unreferenced ones
     * default=false;
     */
    public void setAll(boolean parameter) {
        this.all = parameter;
    }

    /**
     *  the default type mapping registry to use. Either 1.1 or 1.2.
     * Default is 1.1
     * @param parameter new version
     */
    public void setTypeMappingVersion(TypeMappingVersionEnum parameter) {
        this.typeMappingVersion = parameter.getValue();
    }

    /**
     * timeout in milliseconds for URL retrieval; default is 45 seconds.
     * Set this to -1 to disable timeouts altogether: other negative values
     * are not allowed)
     */
    public void setTimeout(long parameter) {
        this.timeout = parameter;
    }

    /**
     * add a mapping of namespaces to packages
     */
    public void addMapping(NamespaceMapping mapping) {
        mappings.addMapping(mapping);
    }

    /**
     * add a mapping of namespaces to packages
     */
    public void addMappingSet(MappingSet mappingset) {
        mappings.addMappingSet(mappingset);
    }

    /**
     * set the mapping file. This is a properties file of
     * package=namespace order. Optional, default is to look for
     * a file called NStoPkg.properties in the project directory.
     * @param namespaceMappingFile
     */
    public void setNamespaceMappingFile(File namespaceMappingFile) {
        this.namespaceMappingFile = namespaceMappingFile;
    }

    /**
     * valid deploy scopes for the task
     */
    /*
    public static class DeployScopeEnum extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[]{"Application", "Request", "Session","none"};
        }

    }
    */

    /**
     * should the task fail the build if there is a network error?
     * optional: defaults to false
     * @param failOnNetworkErrors
     */
    public void setFailOnNetworkErrors(boolean failOnNetworkErrors) {
        this.failOnNetworkErrors = failOnNetworkErrors;
    }

    /**
     * should we print a stack trace on failure?
     * Optional, default=true.
     * @param printStackTraceOnFailure
     */
    public void setPrintStackTraceOnFailure(boolean printStackTraceOnFailure) {
        this.printStackTraceOnFailure = printStackTraceOnFailure;
    }

    /**
     * set any username required for BASIC authenticated access to the WSDL;
     * optional.
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * set any password required for BASIC authenticated access to the WSDL;
     * optional; only used if username is set
     * @param password
     * @see #username
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Set the noWrapped flag.
     * @param noWrapped
     */
    public void setNoWrapped(boolean noWrapped) {
        this.noWrapped = noWrapped;
    }

    /**
     * Set the jaxrpc11mappings flag.
     */
/*
    public void setJaxrpc11mappings(boolean b) {
        this.jaxrpc11mappings = b;
    }
*/

    /**
     * Set the allowInvalidURL flag.
     */
    public void setAllowInvalidUrl(boolean b) {
        this.allowInvalidURL = b;
    }

	/**
	 * Set the name of the class implementing the web service.
	 * This is especially useful when exporting a java class
	 * as a web service using Java2WSDL followed by WSDL2Java.
	 * 
	 * @param implementationClassName
	 */
	public void setImplementationClassName(String implementationClassName) {
		this.implementationClassName = implementationClassName;
	}


    /**
     * set the classpath
     * @return
     */
    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }

    /** Adds an additional namespace to the list to be included
     * in source code generation.
     */
    public NamespaceSelector createNsInclude() {
        NamespaceSelector selector = new NamespaceSelector();
        nsIncludes.add(selector);
        return selector;
    }

    /** Adds an additional namespace to the list to be excluded
     * from source code generation.
     */
    public NamespaceSelector createNsExclude() {
        NamespaceSelector selector = new NamespaceSelector();
        nsExcludes.add(selector);
        return selector;
    }

    /** Adds a property name/value pair for specialized
     * JavaGeneratorFactories.
     */
    public FactoryProperty createProperty() {
        FactoryProperty property = new FactoryProperty();
        properties.add(property);
        return property;
    }

    /** This factory method makes it easier to extend this Ant task
     * with a custom Emitter, if necessary.
     */
    protected Emitter createEmitter() {
        return new Emitter();
    }

    protected NamespaceSelector createSelector() {
        return new NamespaceSelector();
    }

    private void traceSystemSetting(String setting, int logLevel) {
        String value = System.getProperty(setting);
        log("\t" + setting + "=" + value, logLevel);
    }

    private void traceNetworkSettings(int logLevel) {
        traceSystemSetting("http.proxyHost", logLevel);
        traceSystemSetting("http.proxyPort", logLevel);
        traceSystemSetting("http.proxyUser", logLevel);
        traceSystemSetting("http.proxyPassword", logLevel);
        traceSystemSetting("socks.proxyHost", logLevel);
        traceSystemSetting("socks.proxyPort", logLevel);
    }
}

