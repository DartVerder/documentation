import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 *
 * @author Konstantin Krivopustov
 */
class CreateMultiPageDoc extends DefaultTask {

    String docName
    String docLang
    String docTitle

    @InputDirectory
    File getSrcDir() {
        return new File("${project.buildDir}/$docName/$docLang/html-single")
    }

    @OutputDirectory
    File getDstDir() {
        return new File("${project.buildDir}/$docName/$docLang/html")
    }

    @TaskAction
    def createMultiPageDoc() {
        project.configurations.chopper.files.each { dep ->
            project.copy {
                from project.zipTree(dep.absolutePath)
                into "${project.buildDir}/tmp"
            }
        }

        def chopperRoot = new File("${project.buildDir}/tmp").listFiles().find {
            it.name.startsWith('asciidoc-html-chopper')
        }

        project.exec {
            workingDir new File(chopperRoot, 'bin').toString()

            if (docTitle) {
                environment('ASCIIDOC_HTML_CHOPPER_OPTS', "-Dchopper.title=\"$docTitle\"")
            }

            def argsLine = "asciidoc-html-chopper -inputFile ${srcDir}/${docName}.html " +
                    "-outputDir ${dstDir} " +
                    "${docLang == 'en' ? '' : '-loc ' + docLang} " +
                    "-vars ${project.rootDir}/tools/chopper-vars.properties"

            if (System.getProperty('os.name').toLowerCase().contains('windows')) {
                commandLine 'cmd', '/c'
                args argsLine
            } else {
                commandLine 'sh'
                args '-c', './' + argsLine
            }
        }

    }
}
