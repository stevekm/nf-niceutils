package com.nextflow.plugin

import groovy.transform.CompileStatic
import nextflow.plugin.extension.Function
import nextflow.plugin.extension.Operator
import nextflow.plugin.extension.PluginExtensionPoint
import nextflow.Session

import groovyx.gpars.dataflow.DataflowReadChannel
import groovyx.gpars.dataflow.DataflowWriteChannel
import nextflow.extension.CH
import nextflow.extension.DataflowHelper
import nextflow.Channel
import nextflow.script.WorkflowMetadata

import org.yaml.snakeyaml.Yaml
// https://www.baeldung.com/java-snake-yaml



@CompileStatic
class ExampleFunctions extends PluginExtensionPoint{

    private Session session
    private ExampleConfiguration configuration

    @Override
    protected void init(Session session) {
        this.session = session
        this.configuration = parseConfig(session.config.navigate('niceutils') as Map)
    }

    protected ExampleConfiguration parseConfig(Map map){
        new ExampleConfiguration(map)
    }

    /*
     *  Nextflow Function examples
     *
     * This contains some demo example Functions that we can use with the plugin
     * They dont actually do anything useful except show syntaxes
     *
     * Generate a random string
     *
     * Using @Function annotation we allow this function can be imported from the pipeline script
     */
    @Function
    String randomString(int length=9){

        length = Math.min(length, configuration.maxRandomSizeString)

        new Random().with {(1..length).collect {(('a'..'z')).join(null)[ nextInt((('a'..'z')).join(null).length())]}.join(null)}
    }

    @Function
    void printConfig() {
        println ">>> configuration: ${configuration} ; do_foo: ${configuration.do_foo} "
    }

    @Function
    String reverseString(String origin) {
        origin.reverse()
    }

    @Function
    void printMap(Map m, Map n, Map q) {
        println "m: ${m.getClass()}"
        println "n: ${n}"
        println "q: ${q}"
    }











    /*
    *
    * Workflow Summary Table
    *
    * create a YAML workflow summary table for inclusion with MultiQC
    *
    * USAGE:
    * workflowSummaryTableStr(params, workflow.properties, workflow.manifest.properties, bad_params = ['foo_key'])
    *
    */
    @Function
    String workflowSummaryTableBuilder(Map params, Map workflowMap, Map manifestMap, List bad_params){
         // remove some params that we dont want included in the report
        def table_params = params.findAll { key, _ -> !bad_params.contains(key) }
        def yaml = """
    id: 'workflow-summary'
    description: " - this information is collected when the pipeline is started."
    section_name: 'Workflow Summary'
    section_href: '${manifestMap["homePage"]}'
    plot_type: 'html'
    data: |
        <b>Pipeline Parameters</b><br>
        <dl class=\"dl-horizontal\">
${table_params.collect { k,v -> "            <dt>$k</dt><dd><samp>${v ?: '<span style=\"color:#999999;\">N/A</a>'}</samp></dd>" }.join("\n")}
        </dl>
        <br>
        <b>Workflow Parameters</b><br>
        <dl class=\"dl-horizontal\">
${workflowMap.collect { k,v -> "            <dt>$k</dt><dd><samp>${v ?: '<span style=\"color:#999999;\">N/A</a>'}</samp></dd>" }.join("\n")}
        </dl>
        <br>
        <b>Workflow Manifest</b><br>
        <dl class=\"dl-horizontal\">
${manifestMap.collect { k,v -> "            <dt>$k</dt><dd><samp>${v ?: '<span style=\"color:#999999;\">N/A</a>'}</samp></dd>" }.join("\n")}
        </dl>
        """.stripIndent()

        return yaml
    }

    // Need to have two different overloaded function methods
    // one if we want to pass a custom set of maps,
    // another if we want to pass the Nextflow `workflow` object directly
    @Function
    String workflowSummaryTableStr(Map params, Map workflowMap, Map manifestMap, List bad_params){
        return workflowSummaryTableBuilder(params, workflowMap, manifestMap, bad_params)
    }

    @Function
    String workflowSummaryTableStr(Map params, nextflow.script.WorkflowMetadata workflow, List bad_params){
        // convert the Nextflow object into more standard map for the next step
        def workflowMap = [:] + workflow.properties
        def manifestMap = [:] + workflow.manifest.properties
        manifestMap["homePage"] = workflow.manifest.properties.homePage
        return workflowSummaryTableBuilder(params, workflowMap, manifestMap, bad_params)
    }









    /*
    *
    * Software Versions Table
    *
    * create a YAML output with embedded HTML table with the Software Versions
    * used in the pipeline, for MultiQC
    *
    *
    *
    */
    // Methods to collect all the Versions YAML strings and
    // build them into a new YAML with HTML table for use with MultiQC
    // Use it in your Nextflow like this;
    // versions_ch.collect().map { versionList ->
    //     collectVersions(versionList)
    // }
    // replacement for https://github.com/stevekm/dumpSoftwareVersions
    class ProcessVersion {
        String id // the fully qualified workflow process id
        String process // just the process name without any preceeding parts
        String container
        List<SoftwareVersion> software

        Map toMap(){
            return [
                "id": this.id,
                "process": this.process,
                "container": this.container,
                "software": this.software.collect{ it.toMap() }
            ]
        }

        String toString(){
            return this.toMap().toString()
        }

        @Override
        boolean equals(Object obj) {
            if (!(obj instanceof ProcessVersion)) return false
            ProcessVersion other = (ProcessVersion)obj
            return this.toMap() == other.toMap()
        }

        // for equality testing
        @Override
        int hashCode() {
            // Combine hash codes of all attributes
            return Objects.hash(this.id, this.process, this.container, this.software)
        }

        // for sorting
        int compareTo(ProcessVersion other) {
            return this.id <=> other.id
        }
    }

    class SoftwareVersion {
        String name
        String version

        Map toMap(){
            return [
                "name": this.name,
                "version": this.version
            ]
        }

        String toString(){
            return this.toMap().toString()
        }

        // for equality testing
        @Override
        int hashCode() {
            // Combine hash codes of attributes
            return Objects.hash(this.name, this.version)
        }
    }

    // use builder functions so that we can create class instance
    // outside the scope of this class, more easily
    @Function
    ProcessVersion createProcessVersion(Map args = [:]) {
        // I wanted to use this
        // String id = "", String process = "", String container = "", List<SoftwareVersion> software = []
        // but was forced to do this instead... ??
        String id = args.containsKey('id') ? args.id : ''
        String process = args.containsKey('process') ? args.process : ''
        String container = args.containsKey('container') ? args.container : ''
        List<SoftwareVersion> software = args.containsKey('software') ? (List<SoftwareVersion>) args.software : []

        return new ProcessVersion(
            id: id,
            process: process,
            container: container,
            software: software
        )
    }

    @Function
    SoftwareVersion createSoftwareVersion(Map args = [:]) {
        String name = args.containsKey('name') ? args.name : ''
        String version = args.containsKey('version') ? args.version : ''
        return new SoftwareVersion(
            name: name,
            version: version
        )
    }

    // convert this into a ProcessVersion object
    // "WORKFLOW1:DO_FOO":
    //     foo1_program: v1.2.3
    //     foo2_program: 2.3
    //
    // NOTE: includes methods to clean up the output map, this is NOT a 1:1 conversion
    //
    @Function
    ProcessVersion createProcessVersionFromYAMLstr(String version) {
        def yaml = new Yaml()
        def parsedMap = yaml.load(version) as Map
        // [ "WORKFLOW1:DO_FOO": [ foo_program: v1.2.3, container: "foo_program:1.2.3" ] ]

        // get the first key in the map; there is supposed to only be one key...
        def firstKey = parsedMap.keySet()[0]
        // add the softwares to this list as we go
        def softwareList = [] //as List<SoftwareVersion>

        // NOTE: this does NOT work unless you specity the Map.Entry type... for some reason?
        parsedMap[firstKey].each { Map.Entry entry ->
            def key = entry.key
            def value = entry.value
            softwareList.add(createSoftwareVersion(name: key, version: value))
        }

        // clean the process label if needed
        def processID = firstKey.toString()
        def workflowID = firstKey.toString()
        if (processID.contains(':')) {
            processID = processID.split(':')[-1]
        }

        // return processVersion
        return createProcessVersion(
            id: workflowID,
            process: processID,
            // container: container, // NOTE: not handling containers yet, do this later
            software: softwareList
        )
    }

    @Function
    List<ProcessVersion> createProcessVersionFromYAMLstrList(List<String> versions) {
        def processVersions = [] as List<ProcessVersion>
        for (version in versions){
            processVersions << createProcessVersionFromYAMLstr(version)
        }
        // remove duplicates
        processVersions.unique()

        // TODO: sort the list!
        processVersions.sort()

        return processVersions
    }

    @Function
    String makeVersionsYAML(List<ProcessVersion> versions, String manifestName, String manifestHomepage, String manifestVersion, String nxfVersion){
        // String manifestName // workflow.manifest.name
        // String manifestHomepage // workflow.manifest.homePage
        // String manifestVersion // workflow.manifest.version
        // String nxfVersion // workflow.nextflow.version

        versions.add(
            createProcessVersion(
                id: "Workflow",
                process: "Workflow",
                software: [
                    createSoftwareVersion(name: "Nextflow", version: nxfVersion),
                    createSoftwareVersion(name: manifestName, version: manifestVersion),
                ]
            )
        )

        // hold all the discrete HTML elements in a list
        def html = []
        html.add("""
<style>
#nf-core-versions tbody:nth-child(even) {
background-color: #f2f2f2;
}
</style>
<table class="table" style="width:100%" id="nf-core-versions">
<thead>
<tr>
<th> Process  </th>
<th> Software </th>
<th> Version  </th>
<th> Container </th>
<th> ID </th>
</tr>
</thead>
""")
        // iterate over the version maps in the list
        for (version in versions){
            // start a table body section
            html.add("<tbody>")
            // get the parts from the version instance
            def process = version.process
            def container = version.container
            def id = version.id
            def i = 0
            for(entry in version.software){
                // add a full line for the first item in the entry
                // add a truncated line for all subsequent items
                if (i == 0){
                    def toolString = """
<tr>
<td><samp>${process}</samp></td>
<td><samp>${entry.name}</samp></td>
<td><samp>${entry.version}</samp></td>
<td><samp>${container}</samp></td>
<td><samp>${id}</samp></td>
</tr>
"""
                html.add(toolString)
                } else {
                    def toolString = """
<tr>
<td><samp></samp></td>
<td><samp>${entry.name}</samp></td>
<td><samp>${entry.version}</samp></td>
<td><samp></samp></td>
<td><samp></samp></td>
</tr>
"""
                html.add(toolString)
                }
                i = i + 1

            }
            html.add("</tbody>")
        }
        html.add("</table>")
        // join into a single HTML string
        def htmlString = html.join("\n")

        // start creating a map to use for the final YAML output
        def yamlMap = [
            "section_name": String.format("%s Software Versions", manifestName),
            "section_href": String.format("%s", manifestHomepage),
            "plot_type":    "html",
            "description":  "Versions collected at run time from the software output.",
            "data": htmlString
        ]
        Yaml yaml = new Yaml();
        String output = yaml.dump(yamlMap);
        return output
    }














    /*

    THIS IS A SECTION WITH SOME EXAMPLES OF USING CUSTOM OPERATORS
    I COULD NOT GET THEM WORKING CORRECTLY
    BUT LEAVING IT HERE FOR NOTES
    WILL REMOVE THIS LATER


    */
    /*
    https://www.nextflow.io/docs/latest/plugins.html#operators
    https://github.com/nextflow-io/nf-sqldb/blob/e9f3da63888046df57270a2a5b35e71d2a7815ac/plugins/nf-sqldb/src/main/nextflow/sql/ChannelSqlExtension.groovy#L77
    */
    // @Operator
    // DataflowWriteChannel collectVersions(List<String> versions) {
    //     // keep only the text from each versions.yml and keep only the unique entries
    //     // versions_ch = versions_ch.map {
    //     // return it.text
    //     // }.unique().toSortedList().flatten()
    //     // .collectFile(name: 'collated_versions.yml', cache: false, storeDir: "${params.outdir}/versions")

    //     // return versions[0]

    //     // return
    // }

    /*
    * {@code goodbye} is a *consumer* method as it receives values from a channel to perform some logic.
    *
    * Consumer methods are introspected by nextflow-core and include into the DSL if the method:
    *
    * - it's public
    * - it returns a DataflowWriteChannel
    * - it has only one arguments of DataflowReadChannel class
    * - it's marked with the @Operator annotation
    *
    * a consumer method needs to proportionate 2 closures:
    * - a closure to consume items (one by one)
    * - a finalizer closure
    *
    * in this case `goodbye` will consume a message and will store it as an upper case
    */
    // // input_ch.goodbye().view()
    // versions_ch.collect()//.view()
    // versions_ch.collect().goodbye()
    // https://github.com/nextflow-io/nf-hello/blob/f686fbcf2346f3fc02b2288f567c016ab6bf2e50/plugins/nf-hello/src/main/nextflow/hello/HelloExtension.groovy#L79-L102
    // @Operator
    // DataflowWriteChannel goodbye(DataflowReadChannel source) {
    //     final target = CH.createBy(source)
    //     final next = { target.bind("Goodbye $it".toString()) }
    //     final done = { target.bind(Channel.STOP) }
    //     DataflowHelper.subscribeImpl(source, [onNext: next, onComplete: done])
    //     return target
    // }
    //
    // Got a lot of errors like these;
    //
    // java.lang.IllegalStateException: A DataflowVariable can only be assigned once. Only re-assignments to an equal value are allowed.
	// at groovyx.gpars.dataflow.expression.DataflowExpression.bind(DataflowExpression.java:368)
	// at com.nextflow.plugin.ExampleFunctions$_goodbye_closure7.doCall(ExampleFunctions.groovy:119)

//    @Operator
//    DataflowWriteChannel collectVersions(List<String> versions) {
//        // keep only the text from each versions.yml and keep only the unique entries
//        // versions_ch = versions_ch.map {
//        // return it.text
//        // }.unique().toSortedList().flatten()
//        // .collectFile(name: 'collated_versions.yml', cache: false, storeDir: "${params.outdir}/versions")
//    }

}
