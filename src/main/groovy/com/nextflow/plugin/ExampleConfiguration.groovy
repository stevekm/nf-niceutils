package com.nextflow.plugin

import groovy.transform.PackageScope

@PackageScope
class ExampleConfiguration {

    final private int maxSize
    final private boolean do_foo

    ExampleConfiguration(Map map){
        def config = map ?: Collections.emptyMap()
        maxSize = (config.maxSize ?: 1000) as int
        do_foo = (config.do_foo ?: false) as boolean
    }

    int getMaxRandomSizeString(){
        maxSize
    }
}
