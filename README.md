# `nf-niceutils`

A set of nice utility methods for use with Nextflow pipelines

## Under Construction

This plugin is currently under construction. Please see the details in `NOTES.md` for development and usage. And check out `examples/test.nf` to see how the plugin is used in a Nextflow pipeline.

## Usage

An example workflow is included so you can see how the plugin can be used.

Run the basic workflow with

```bash
nextflow run examples/test.nf
```

You will get the following files

- `output/software_versions_mqc.yaml` : a software versions table for use with MultiQC
- `output/workflow_summary_mqc.yaml` : a workflow summary table for use with MultiQC

If you have Docker running you can run the full example

```bash
nextflow run examples/test.nf -profile multiqc
```

This will also give you the following file

- `output/multiqc_report.html` : a MultiQC report that utilizes the created tables

Some examples of the output are included in the repo here at `examples/example_output`.

### Usage with your own pipelines

To use it with your own pipeline, currently you will need to make sure to include it in your `nextflow.config`;

```groovy
plugins {
    id 'nf-niceutils'
}
```

and run it like this

```bash
NXF_PLUGINS_TEST_REPOSITORY=https://raw.githubusercontent.com/stevekm/nextflow-plugins-registry/main/plugins.json nextflow run main.nf
```

Some time soon hopefully I can get it pushed into the official Nextflow registry :)

## Methods

(these are subject to change)

The following methods are the most useful ones offered by the plugin;

- `createProcessVersionFromYAMLstrList` : creates a MultiQC Software Versions Table YAML with embedded HTML table
- `workflowSummaryTableStr`: creates a workflow summary table YAML for use with MultiQC

You can see examples of how to use each function inside the `examples/test.nf` script.

### `createProcessVersionFromYAMLstrList`

```groovy
    // create a MultiQC Software Versions Table YAML + HTML
    // versions_ch contains versions.yml files output by Nextflow processes
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
```

### `workflowSummaryTableStr`

```groovy
    // create a workflow summary table
    workflow_summary_ch = Channel.from(
        workflowSummaryTableStr(
            params,
            workflow.properties,
            workflow.manifest.properties,
            ['foo_key'] // some 'bad params' to exclude from the output
            )
    ).collectFile(storeDir: "${params.outdir}", name: "workflow_summary_mqc.yaml")
```