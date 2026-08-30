rootProject.name = "PlotNova"

include("shared", "paper")

project(":shared").name = "plotnova-core"
project(":paper").name = "plotnova-paper"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
