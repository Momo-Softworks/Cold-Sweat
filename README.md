# Cold-Sweat
Cold Sweat is a mod for Minecraft that adds a comprehensive temperature system to the game. This is the public repository for the project on CurseForge:  
https://www.curseforge.com/minecraft/mc-mods/cold-sweat  
  
**PLEASE READ THE LICENSE** as there are important amendments and clarifications regarding derivative works and mods which use the Cold Sweat API.

---

## Documentation
Documentation for 3rd-party integration with Cold Sweat can be found here (WIP, but most important things are documented):  
https://mikul.gitbook.io/cold-sweat/  
The current documentation is designed for the latest beta on Minecraft 1.20, but it should be useable for other Minecraft versions with some adaptation.

---

## Developing with Cold Sweat
1. Get [Cursemaven](https://www.cursemaven.com/)
2. Go to the latest version on [CurseForge](https://legacy.curseforge.com/minecraft/mc-mods/cold-sweat/files) and add the ColdSweat-sources.jar file as a dependency:
```
dependencies {
  // This adds Cold Sweat 2.4-b05a and includes sources
  implementation fg.deobf("curse.maven:cold-sweat-506194:6991388-sources-6991389")
}
```
