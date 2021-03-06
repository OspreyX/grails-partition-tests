includeTargets << grailsScript("_GrailsEvents")

eventTestCompileStart = {testType ->
    if (!binding.hasVariable('totalSplits') || !binding.hasVariable('split')) {
        return
    }

    grailsConsole.addStatus("Adding split support to grails test type: ${testType.class}")
    Integer totalSplits = Integer.valueOf(binding.getVariable('totalSplits'))
    Integer split = Integer.valueOf(binding.getVariable('split'))

    try {
        def isSmartSplit = binding.hasVariable('testReportsUrl')
        def splitClass = isSmartSplit ?
                classLoader.loadClass('grails.plugin.partitiontests.VoodooTestSplitter') :
                classLoader.loadClass('grails.plugin.partitiontests.GrailsTestSplitter')

        def splitter = isSmartSplit ? splitClass.newInstance(split, totalSplits, binding.getVariable('testReportsUrl')) : splitClass.newInstance(split, totalSplits)

        grailsConsole.addStatus("Splitting tests with $splitter")

        testType.metaClass.eachSourceFile = splitter.eachSourceFileHotReplace
        testType.metaClass.testSplitter  = splitter

    } catch (Throwable t) {
        grailsConsole.error("Could not add split support", t)
    }
    grailsConsole.addStatus("Ready to complie '${testType.name}' tests for split run")
}

eventTestCompileEnd = {testType ->
    event("SplitTestTestCompileEnd", [testType])
}
