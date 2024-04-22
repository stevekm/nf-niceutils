nextflow.enable.dsl=2

// $ ./nextflow run examples/test.nf
// $ ./nextflow run examples/test.nf -profile multiqc

include { randomString } from 'plugin/nf-niceutils'
include {
    workflowSummaryTableStr;
    printConfig
    } from 'plugin/nf-niceutils'
include {
    createProcessVersionFromYAMLstrList;
    makeVersionsYAML
    } from 'plugin/nf-niceutils'


process MULTIQC {
    publishDir "${params.outdir}", mode: "copy"
    container "quay.io/biocontainers/multiqc:1.14--pyhdfd78af_0"

    input:
    path(files)

    output:
    path(output_file)

    script:
    output_file = "multiqc_report.html"
    """
    multiqc --force --filename "${output_file}" .
    """
}


process DO_FOO {
    input:
    val(x)

    output:
    path(output_file)
    path("versions.yml"), emit: versions

    script:
    output_file = "${x}.foo.txt"
    """
    echo '${x}' > "${output_file}"

    cat <<-END_VERSIONS > versions.yml
    "${task.process}":
        foo_program: v1.2.3
    END_VERSIONS
    """
}

process DO_BAR {
    input:
    val(x)

    output:
    path(output_file)

    exec:
    output_file = new File("${task.workDir}/${x}.bar.txt")
    output_file.write(x)
}

process DO_BAZ {
    input:
    val(x)

    output:
    path(output_file)
    path("versions.yml"), emit: versions

    script:
    output_file = "${x}.baz.txt"
    """
    echo '${x}' > "${output_file}"

    cat <<-END_VERSIONS > versions.yml
    "${task.process}":
        baz1_program: 2.4
        baz2_program: 3.5
    END_VERSIONS
    """
}

process DO_BUZZ {
    input:
    val(x)

    output:
    path(output_file)
    path("versions.yml"), emit: versions

    script:
    output_file = "${x}.baz.txt"
    """
    echo '${x}' > "${output_file}"

    cat <<-END_VERSIONS > versions.yml
    "${task.process}":
        buzz1_program: 5.6.7
        buzz2_program: v6.8
    END_VERSIONS
    """
}

workflow WORKFLOW1 {
    take:
        input_ch

    main:
        versions_ch = Channel.empty()
        DO_FOO(input_ch)
        DO_BAR(input_ch)

        versions_ch = versions_ch.mix(DO_FOO.out.versions)

    emit:
        versions = versions_ch
}

workflow {
    // some demo functions
    println "this is a random string: ${randomString()}"
    printConfig()

    input_ch = Channel.from("foo", "bar", "baz", "buzz")
    versions_ch = Channel.empty()

    WORKFLOW1(input_ch)
    DO_BAZ(input_ch)
    DO_BUZZ(input_ch)

    versions_ch = versions_ch.mix(
        WORKFLOW1.out.versions,
        DO_BAZ.out.versions,
        DO_BUZZ.out.versions)

    // create a MultiQC Software Versions Table YAML + HTML
    mqc_versions_ch = versions_ch.map{ it.text }
        .collect()
        .map{ versions ->
            def processList = createProcessVersionFromYAMLstrList(versions)
            makeVersionsYAML(
                processList,
                workflow.manifest.name,
                workflow.manifest.homePage,
                workflow.manifest.version,
                workflow.nextflow.version.toString()
                )
        }.collectFile(storeDir: "${params.outdir}", name: "software_versions_mqc.yaml")

    // create a workflow summary table
    workflow_summary_ch = Channel.from(
        workflowSummaryTableStr(
            params,
            workflow.properties,
            workflow.manifest.properties,
            ['foo_key'] // some 'bad params' to exclude from the output
            )
    ).collectFile(storeDir: "${params.outdir}", name: "workflow_summary_mqc.yaml")

    if ( params.useMultiQC ) {
        mqc_files = workflow_summary_ch.mix(mqc_versions_ch).collect()
        MULTIQC(mqc_files)
    }

}