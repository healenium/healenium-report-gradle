# healenium-report-gradle
[ ![Download](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/epam/healenium/hlm-report/com.epam.healenium.hlm-report.gradle.plugin/maven-metadata.xml.svg?label=gradlePluginPortal) ](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/epam/healenium/hlm-report/com.epam.healenium.hlm-report.gradle.plugin/maven-metadata.xml.svg?label=gradlePluginPortal)

Healenium gradle plugin to listen test actions and generate report
https://plugins.gradle.org/plugin/com.epam.healenium.hlm-report
```
plugins {
  id "com.epam.healenium.hlm-report" version "1.1.2"
}
```
initReport task will generate sessionKey before test run

buildReport task will generate link to the healing report after test run automatically
