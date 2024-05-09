package com.nextflow.plugin

// https://www.nextflow.io/docs/latest/plugins.html#functions
import groovy.transform.CompileStatic
import nextflow.Session
import nextflow.plugin.extension.Function
import nextflow.plugin.extension.PluginExtensionPoint

// FOR SOME REASON THIS FUNCTION WONT BE AVAILABLE IN NEXTFLOW ??


// @CompileStatic
// class MyExtension extends PluginExtensionPoint {

//     @Override
//     void init(Session session) {}

//     @Function
//     String reverseString(String origin) {
//         origin.reverse()
//     }

// }