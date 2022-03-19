import net.minecraftforge.gradle.user.ReobfMappingType

plugins {
    java
    kotlin("jvm") version "1.6.10"
    id("net.minecraftforge.gradle.forge") version "6f5327738df"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.spongepowered.mixin") version "d75e32e"
}

group = "moe.nea89.sbdatacollection"
version = "2.1"

// Toolchains:

java {
    // Forge Gradle currently prevents using the toolchain: toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

minecraft {
    version = "1.8.9-11.15.1.2318-1.8.9"
    runDir = "run"
    mappings = "stable_22"
    clientJvmArgs.addAll(
        listOf(
            "-Dmixin.debug=true",
            "-Dasmhelper.verbose=true"
        )
    )
    clientRunArgs.addAll(
        listOf(
            "--tweakClass org.spongepowered.asm.launch.MixinTweaker",
            "--mixin mixins.sbdata.json"
        )
    )
}

mixin {
    add(sourceSets.main.get(), "mixins.sbdata.refmap.json")
}

sourceSets {
    main {
        output.setResourcesDir(file("$buildDir/classes/kotlin/main"))
    }
}

// Dependencies:

repositories {
    mavenCentral()
    flatDir { dirs("deps/") }
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT")
    annotationProcessor("org.spongepowered:mixin:0.7.11-SNAPSHOT")
}


// Tasks:

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(Jar::class) {
    archiveBaseName.set("sbdata")
    manifest.attributes.run {
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.sbdata.json"
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["FMLAT"] = "sbdata_at.cfg"
    }
}

tasks.shadowJar {
    archiveClassifier.set("uberfatshadowfullalldeps")
    fun relocate(name: String) = relocate(name, "moe.nea89.sbdata.deps.$name")
}

tasks.build.get().dependsOn(tasks.shadowJar)

reobf {
    create("shadowJar") {
        mappingType = ReobfMappingType.SEARGE
    }
}

tasks.processResources {
    from(sourceSets.main.get().resources.srcDirs)
    filesMatching("mcmod.info") {
        expand(
            "modversion" to project.version,
            "mcversion" to minecraft.version
        )
    }
    rename("(.+_at.cfg)".toPattern(), "META-INF/$1")
}

