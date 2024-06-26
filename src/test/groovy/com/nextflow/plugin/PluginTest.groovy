package com.nextflow.plugin

import nextflow.Session
import nextflow.Channel
import nextflow.plugin.Plugins
import nextflow.plugin.TestPluginDescriptorFinder
import nextflow.plugin.TestPluginManager
import nextflow.plugin.extension.PluginExtensionProvider
import org.pf4j.PluginDescriptorFinder
import spock.lang.Shared
import spock.lang.Specification
import test.Dsl2Spec
import test.MockScriptRunner

import java.nio.file.Path

class PluginTest extends Dsl2Spec{

    @Shared String pluginsMode

    def setup() {
        // reset previous instances
        PluginExtensionProvider.reset()
        // this need to be set *before* the plugin manager class is created
        pluginsMode = System.getProperty('pf4j.mode')
        System.setProperty('pf4j.mode', 'dev')
        // the plugin root should
        def root = Path.of('.').toAbsolutePath().normalize()
        def manager = new TestPluginManager(root){
            @Override
            protected PluginDescriptorFinder createPluginDescriptorFinder() {
                return new TestPluginDescriptorFinder(){
                    @Override
                    protected Path getManifestPath(Path pluginPath) {
                        return pluginPath.resolve('build/tmp/jar/MANIFEST.MF')
                    }
                }
            }
        }
        Plugins.init(root, 'dev', manager)
    }

    def cleanup() {
        Plugins.stop()
        PluginExtensionProvider.reset()
        pluginsMode ? System.setProperty('pf4j.mode',pluginsMode) : System.clearProperty('pf4j.mode')
    }

    def 'should starts' () {
        when:
        def SCRIPT = '''
            channel.of('hi!')
            '''
        and:
        def result = new MockScriptRunner([:]).setScript(SCRIPT).execute()
        then:
        result.val == 'hi!'
        result.val == Channel.STOP
    }

    def 'should execute a function' () {
        when:
        def SCRIPT = '''
            include {randomString} from 'plugin/nf-niceutils'
            channel
                .of( randomString(20) )
            '''
        and:
        def result = new MockScriptRunner([:]).setScript(SCRIPT).execute()
        then:
        result.val.size() == 20
        result.val == Channel.STOP
    }

    def 'should use a configuration' () {
        when:
        def SCRIPT = '''
            include {randomString} from 'plugin/nf-niceutils'
            channel
                .of( randomString(20) )
            '''
        and:
        def result = new MockScriptRunner([
                niceutils:[
                        maxSize : 5
                ]]).setScript(SCRIPT).execute()
        then:
        result.val.size() == 5
        result.val == Channel.STOP
    }

    //
    // Had to disable this test case because a real Nextflow output table here
    // includes a lot of parts that are non-deterministic
    // but keep this as an example of how to do these plugin test cases
    //
//     def 'should return a workflow summary table' () {
//         when:
//         def SCRIPT = '''
//             include {workflowSummaryTableStr} from 'plugin/nf-niceutils'
//             params.foo = 1
//             params.bar = 2
//             params.buzzzz = 5
//             workflowSummaryTableStr(
//                 params,
//                 workflow.properties,
//                 workflow.manifest.properties,
//                 bad_params = ['buzzzz']
//                 )
//             '''
//         def expected = """
// id: 'workflow-summary'
// description: " - this information is collected when the pipeline is started."
// section_name: 'Workflow Summary'
// section_href: 'null'
// plot_type: 'html'
// data: |
//     <b>Pipeline Parameters</b><br>
//     <dl class="dl-horizontal">
//         <dt>foo</dt><dd><samp>1</samp></dd>
//         <dt>bar</dt><dd><samp>2</samp></dd>
//     </dl>
//     <br>
//     <b>Workflow Parameters</b><br>
//     <dl class="dl-horizontal">
//         <dt>class</dt><dd><samp>class java.util.LinkedHashMap</samp></dd>
//         <dt>empty</dt><dd><samp><span style="color:#999999;">N/A</a></samp></dd>
//     </dl>
//     <br>
//     <b>Workflow Manifest</b><br>
//     <dl class="dl-horizontal">
//         <dt>class</dt><dd><samp>class java.util.LinkedHashMap</samp></dd>
//         <dt>empty</dt><dd><samp><span style="color:#999999;">N/A</a></samp></dd>
//     </dl>
// """
//         and:
//         def result = new MockScriptRunner([:]).setScript(SCRIPT).execute()
//         then:
//         result == expected
//     }

}


