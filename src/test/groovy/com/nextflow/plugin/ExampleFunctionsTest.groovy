package com.nextflow.plugin

import nextflow.Session
import spock.lang.Specification

class ExampleFunctionsTest extends Specification {

    def 'should return random string' () {
        given:
        def example = new ExampleFunctions()
        example.init(new Session([:]))

        when:
        def result = example.randomString(9)

        then:
        result.size()==9
    }

    def 'should return a workflow table YAML' () {
        given:
        def example = new ExampleFunctions()
        example.init(new Session([:]))
        def params = ['foo': 1, 'bar': 2, "buzzzz": 5]
        def bad_params = ["buzzzz"]
        def manifestMap = [
                "homePage": "www.internets.com",
                "author": "Stephen Kelly"
                ]
        def workflowMap = [
            "projectDir": "/path/to/here",
            "profile": "standard",
            "scriptName": "main.nf"
        ]

        def expected = """
id: 'workflow-summary'
description: " - this information is collected when the pipeline is started."
section_name: 'Workflow Summary'
section_href: 'www.internets.com'
plot_type: 'html'
data: |
    <b>Pipeline Parameters</b><br>
    <dl class="dl-horizontal">
        <dt>foo</dt><dd><samp>1</samp></dd>
        <dt>bar</dt><dd><samp>2</samp></dd>
    </dl>
    <br>
    <b>Workflow Parameters</b><br>
    <dl class="dl-horizontal">
        <dt>projectDir</dt><dd><samp>/path/to/here</samp></dd>
        <dt>profile</dt><dd><samp>standard</samp></dd>
        <dt>scriptName</dt><dd><samp>main.nf</samp></dd>
    </dl>
    <br>
    <b>Workflow Manifest</b><br>
    <dl class="dl-horizontal">
        <dt>homePage</dt><dd><samp>www.internets.com</samp></dd>
        <dt>author</dt><dd><samp>Stephen Kelly</samp></dd>
    </dl>
"""
        when:
        def result = example.workflowSummaryTableStr(params, workflowMap, manifestMap, bad_params)
        // println result

        then:
        result == expected
    }

    def 'should create a list of version objects' () {
        given:
            def example = new ExampleFunctions()
            example.init(new Session([:]))
            // NOTE: expected to be in alpha order!
            def expected = [
                example.createProcessVersion(
                    id: "WORKFLOW1:DO_BAR",
                    process: "DO_BAR",
                    software: [example.createSoftwareVersion(name: "bar_program", version: "v1.2.3")]),
                example.createProcessVersion(
                    id: "WORKFLOW1:DO_FOO",
                    process: "DO_FOO",
                    software: [
                        example.createSoftwareVersion(name: "foo1_program", version: "v1.2.3"),
                        example.createSoftwareVersion(name: "foo2_program", version: "2.3")
                    ])
                ]

            def versions = [
"""
"WORKFLOW1:DO_FOO":
    foo1_program: v1.2.3
    foo2_program: 2.3
""".stripIndent(),
"""
"WORKFLOW1:DO_BAR":
    bar_program: v1.2.3
""".stripIndent(),
// duplicate entry
"""
"WORKFLOW1:DO_BAR":
    bar_program: v1.2.3
""".stripIndent()
                ]


        when:
            def result = example.createProcessVersionFromYAMLstrList(versions)

        then:
            result == expected
    }



    def 'should create a YAML with HTML table' () {
        given:
            def example = new ExampleFunctions()
            example.init(new Session([:]))
            // NOTE: expected to be in alpha order!
            def expected = """section_name: My-Pipeline Software Versions
section_href: www.internets.com
plot_type: html
description: Versions collected at run time from the software output.
data: |2-

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

  <tbody>

  <tr>
  <td><samp>DO_BAR</samp></td>
  <td><samp>bar_program</samp></td>
  <td><samp>v1.2.3</samp></td>
  <td><samp></samp></td>
  <td><samp>WORKFLOW1:DO_BAR</samp></td>
  </tr>

  </tbody>
  <tbody>

  <tr>
  <td><samp>DO_FOO</samp></td>
  <td><samp>foo1_program</samp></td>
  <td><samp>v1.2.3</samp></td>
  <td><samp></samp></td>
  <td><samp>WORKFLOW1:DO_FOO</samp></td>
  </tr>


  <tr>
  <td><samp></samp></td>
  <td><samp>foo2_program</samp></td>
  <td><samp>2.3</samp></td>
  <td><samp></samp></td>
  <td><samp></samp></td>
  </tr>

  </tbody>
  <tbody>

  <tr>
  <td><samp>Workflow</samp></td>
  <td><samp>Nextflow</samp></td>
  <td><samp>23.10.1</samp></td>
  <td><samp></samp></td>
  <td><samp>Workflow</samp></td>
  </tr>


  <tr>
  <td><samp></samp></td>
  <td><samp>My-Pipeline</samp></td>
  <td><samp>4.2</samp></td>
  <td><samp></samp></td>
  <td><samp></samp></td>
  </tr>

  </tbody>
  </table>
"""

            def versions = [
"""
"WORKFLOW1:DO_FOO":
    foo1_program: v1.2.3
    foo2_program: 2.3
""".stripIndent(),
"""
"WORKFLOW1:DO_BAR":
    bar_program: v1.2.3
""".stripIndent(),
// duplicate entry
"""
"WORKFLOW1:DO_BAR":
    bar_program: v1.2.3
""".stripIndent()
                ]


        when:
            def versionsList = example.createProcessVersionFromYAMLstrList(versions)
            def result = example.makeVersionsYAML(
                    versionsList,
                    "My-Pipeline",
                    "www.internets.com",
                    "4.2",
                    "23.10.1"
                    )
            // println "expected:\n${expected}"
            // println "result:\n${result}"

        then:
            result == expected
    }
}