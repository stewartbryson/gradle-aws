package com.redpillanalytics.plugin

import groovy.util.logging.Slf4j
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Slf4j
@Title("Execute :tasks task")
class TasksTest extends Specification {
   @Shared
   File projectDir

   @Shared
   String projectName = 'tasks-test'

   @Shared
   FileTreeBuilder projectTree

   @Shared
   BuildResult result

   def setupSpec() {

      projectTree = new FileTreeBuilder(new File(System.getProperty("projectBase")))
      projectDir = projectTree.dir(projectName)
      projectDir.deleteDir()

      projectTree.dir(projectName) {
         file('build.gradle', """
            |plugins {
            |   id 'com.redpillanalytics.gradle-aws'
            |}
            |
            |""".stripMargin())
         file('settings.gradle', """rootProject.name = '$projectName'""")
      }
   }

   //helper task
   def executeSingleTask(String taskName, List otherArgs = []) {

      otherArgs.add(0, taskName)

      log.warn "runner arguments: ${otherArgs.toString()}"

      // execute the Gradle test build
      result = GradleRunner.create()
              .withProjectDir(projectDir)
              .withArguments(otherArgs)
              .withPluginClasspath()
              .forwardOutput()
              .build()
   }

   @Unroll
   def ":tasks contains #task"() {

      given:
      executeSingleTask('tasks', ['-S'])

      expect:
      result.output.contains("$task")

      where:
      task << ['s3Upload', 's3Download']
   }

   @Unroll
   def "task help --task s3Upload contains #parameter"() {

      given:
      executeSingleTask('help', ['--task', 's3Upload', '-S'])

      expect:
      result.output.contains("--${parameter}")

      where:
      parameter << ['bucket-name','file-path','key-name']
   }
}
